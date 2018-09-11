package org.folio.rest.impl;

import static io.vertx.core.Future.succeededFuture;
import static org.folio.rest.impl.Headers.TENANT_HEADER;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.function.Function;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.folio.rest.annotations.Validate;
import org.folio.rest.jaxrs.model.Error;
import org.folio.rest.jaxrs.model.Errors;
import org.folio.rest.jaxrs.model.Loan;
import org.folio.rest.jaxrs.model.Loans;
import org.folio.rest.jaxrs.model.Status;
import org.folio.rest.jaxrs.resource.LoanStorageResource;
import org.folio.rest.persist.Criteria.Criteria;
import org.folio.rest.persist.Criteria.Criterion;
import org.folio.rest.persist.Criteria.Limit;
import org.folio.rest.persist.Criteria.Offset;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.persist.cql.CQLWrapper;
import org.folio.rest.persist.interfaces.Results;
import org.folio.rest.tools.utils.OutStream;
import org.folio.rest.tools.utils.TenantTool;
import org.folio.rest.tools.utils.ValidationHelper;
import org.folio.support.Responder;
import org.folio.support.ResultHandlerFactory;
import org.folio.support.ServerErrorResponder;
import org.folio.support.UUIDValidation;
import org.folio.support.VertxContextRunner;
import org.joda.time.DateTime;
import org.z3950.zing.cql.cql2pgjson.CQL2PgJSON;

import com.github.mauricio.async.db.postgresql.exceptions.GenericDatabaseException;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.UpdateResult;

public class LoansAPI implements LoanStorageResource {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String MODULE_NAME = "mod_circulation_storage";
  private static final String LOAN_TABLE = "loan";
  //TODO: Change loan history table name when can be configured, used to be "loan_history_table"
  private static final String LOAN_HISTORY_TABLE = "audit_loan";

  private static final Class<Loan> LOAN_CLASS = Loan.class;
  private static final String OPEN_LOAN_STATUS = "Open";

  public LoansAPI(Vertx vertx, String tenantId) {
    PostgresClient.getInstance(vertx, tenantId).setIdField("_id");
  }

  @Override
  public void deleteLoanStorageLoans(
    String lang,
    Map<String, String> okapiHeaders,
    Handler<AsyncResult<Response>> responseHandler,
    Context vertxContext) {

    String tenantId = okapiHeaders.get(TENANT_HEADER);

    final ServerErrorResponder serverErrorResponder =
      new ServerErrorResponder(DeleteLoanStorageLoansResponse
        ::withPlainInternalServerError, responseHandler, log);

    final VertxContextRunner runner = new VertxContextRunner(
      vertxContext, serverErrorResponder::withError);

    runner.runOnContext(() -> {
      PostgresClient postgresClient = PostgresClient.getInstance(
        vertxContext.owner(), TenantTool.calculateTenantId(tenantId));

    final Responder noContentResponder = new Responder(
      responseHandler, DeleteLoanStorageLoansResponse::withNoContent);

      postgresClient.mutate(String.format("TRUNCATE TABLE %s_%s.loan",
        tenantId, MODULE_NAME),
        new ResultHandlerFactory<String>().when(
          s -> noContentResponder.respond(),
          serverErrorResponder::withError));
    });
  }

  @Validate
  @Override
  public void getLoanStorageLoans(
    int offset,
    int limit,
    String query,
    String lang,
    Map<String, String> okapiHeaders,
    Handler<AsyncResult<Response>> responseHandler,
    Context vertxContext) {

    String tenantId = okapiHeaders.get(TENANT_HEADER);

    final ServerErrorResponder serverErrorResponder =
      new ServerErrorResponder(GetLoanStorageLoansResponse
        ::withPlainInternalServerError, responseHandler, log);

    final VertxContextRunner runner = new VertxContextRunner(
      vertxContext, serverErrorResponder::withError);

    runner.runOnContext(() -> {
      PostgresClient postgresClient = PostgresClient.getInstance(
        vertxContext.owner(), TenantTool.calculateTenantId(tenantId));

      log.info("CQL Query: " + query);

      String[] fieldList = {"*"};

      CQL2PgJSON cql2pgJson = new CQL2PgJSON("loan.jsonb");
      CQLWrapper cql = new CQLWrapper(cql2pgJson, query)
        .setLimit(new Limit(limit))
        .setOffset(new Offset(offset));

      postgresClient.get(LOAN_TABLE, LOAN_CLASS, fieldList, cql,
        true, false, new ResultHandlerFactory<Results>().when(
          results -> {
            @SuppressWarnings("unchecked")
            List<Loan> loans = (List<Loan>) results.getResults();

            Loans pagedLoans = new Loans();
            pagedLoans.setLoans(loans);
            pagedLoans.setTotalRecords(results.getResultInfo().getTotalRecords());

            responseHandler.handle(succeededFuture(
              LoanStorageResource.GetLoanStorageLoansResponse.
                withJsonOK(pagedLoans)));
          },
          serverErrorResponder::withError));
    });
  }

  @Override
  public void postLoanStorageLoans(
    String lang,
    Loan loan,
    Map<String, String> okapiHeaders,
    Handler<AsyncResult<Response>> responseHandler,
    Context vertxContext) {

    String tenantId = okapiHeaders.get(TENANT_HEADER);

    if(loan.getStatus() == null) {
      loan.setStatus(new Status().withName(OPEN_LOAN_STATUS));
    }

    if(isOpenAndHasNoUserId(loan)) {
      respondWithError(responseHandler,
        PostLoanStorageLoansResponse::withJsonUnprocessableEntity,
        "Open loan must have a user ID");
      return;
    }

    //TODO: Convert this to use validation responses (422 and error of errors)
    ImmutablePair<Boolean, String> validationResult = validateLoan(loan);

    if(!validationResult.getLeft()) {
      responseHandler.handle(
        succeededFuture(
          LoanStorageResource.PostLoanStorageLoansResponse
            .withPlainBadRequest(
              validationResult.getRight())));

      return;
    }

    final ServerErrorResponder serverErrorResponder =
      new ServerErrorResponder(PostLoanStorageLoansResponse
        ::withPlainInternalServerError, responseHandler, log);

    final VertxContextRunner runner = new VertxContextRunner(
      vertxContext, serverErrorResponder::withError);

    runner.runOnContext(() -> {
      PostgresClient postgresClient = PostgresClient.getInstance(
        vertxContext.owner(), TenantTool.calculateTenantId(tenantId));

      if(loan.getId() == null) {
        loan.setId(UUID.randomUUID().toString());
      }

      postgresClient.save(LOAN_TABLE, loan.getId(), loan,
        new ResultHandlerFactory<String>().when(
          result -> {
            OutStream stream = new OutStream();
            stream.setData(loan);

            responseHandler.handle(succeededFuture(
              LoanStorageResource.PostLoanStorageLoansResponse
                .withJsonCreated(result, stream)));
          },
          e -> {
            if(isMultipleOpenLoanError(e)) {
              responseHandler.handle(succeededFuture(
                LoanStorageResource.PostLoanStorageLoansResponse
                  .withJsonUnprocessableEntity(moreThanOneOpenLoanError(loan))));
            }
            else {
              serverErrorResponder.withError(e);
            }
          }));
    });
  }

  @Override
  public void postLoanStorageLoansAnonymizeByUserId(
    @NotNull String userId,
    Map<String, String> okapiHeaders,
    Handler<AsyncResult<Response>> responseHandler,
    Context vertxContext) {

    final ServerErrorResponder serverErrorResponder =
      new ServerErrorResponder(PostLoanStorageLoansAnonymizeByUserIdResponse
        ::withPlainInternalServerError, responseHandler, log);

    final VertxContextRunner runner = new VertxContextRunner(
      vertxContext, serverErrorResponder::withError);

    runner.runOnContext(() -> {
      if(!UUIDValidation.isValidUUID(userId)) {
        final Errors errors = ValidationHelper.createValidationErrorMessage(
          "userId", userId, "Invalid user ID, should be a UUID");

        responseHandler.handle(succeededFuture(
          PostLoanStorageLoansAnonymizeByUserIdResponse
            .withJsonUnprocessableEntity(errors)));
        return;
      }

      final String tenantId = TenantTool.tenantId(okapiHeaders);

      final PostgresClient postgresClient = PostgresClient.getInstance(
          vertxContext.owner(), tenantId);

      final String combinedAnonymizationSql = createAnonymizationSQL(userId,
        tenantId);

      log.info(String.format("Anonymization SQL: %s", combinedAnonymizationSql));

      postgresClient.mutate(combinedAnonymizationSql,
        new ResultHandlerFactory<String>().when(
          s -> responseHandler.handle(succeededFuture(
            PostLoanStorageLoansAnonymizeByUserIdResponse.withNoContent())),
          serverErrorResponder::withError));
    });
  }

  @Validate
  @Override
  public void getLoanStorageLoansByLoanId(
    String loanId,
    String lang,
    Map<String, String> okapiHeaders,
    Handler<AsyncResult<Response>> responseHandler,
    Context vertxContext) {

    String tenantId = okapiHeaders.get(TENANT_HEADER);

    final ServerErrorResponder serverErrorResponder =
      new ServerErrorResponder(GetLoanStorageLoansByLoanIdResponse
        ::withPlainInternalServerError, responseHandler, log);

    final VertxContextRunner runner = new VertxContextRunner(
      vertxContext, serverErrorResponder::withError);

    runner.runOnContext(() -> {
      PostgresClient postgresClient = PostgresClient.getInstance(
        vertxContext.owner(), TenantTool.calculateTenantId(tenantId));

      Criteria a = new Criteria();

      a.addField("'id'");
      a.setOperation("=");
      a.setValue(loanId);

      Criterion criterion = new Criterion(a);

      postgresClient.get(LOAN_TABLE, LOAN_CLASS, criterion, true, false,
        new ResultHandlerFactory<Results>().when(
          results -> {
            @SuppressWarnings("unchecked")
            List<Loan> loans = (List<Loan>) results.getResults();

            if (loans.size() == 1) {
              Loan loan = loans.get(0);

              responseHandler.handle(
                succeededFuture(
                  LoanStorageResource.GetLoanStorageLoansByLoanIdResponse.
                    withJsonOK(loan)));
            }
            else {
              responseHandler.handle(
                succeededFuture(
                  LoanStorageResource.GetLoanStorageLoansByLoanIdResponse.
                    withPlainNotFound("Not Found")));
            }
          },
          serverErrorResponder::withError
        ));
    });
  }

  @Override
  public void deleteLoanStorageLoansByLoanId(
    String loanId,
    String lang,
    Map<String, String> okapiHeaders,
    Handler<AsyncResult<Response>> responseHandler,
    Context vertxContext) {

    String tenantId = okapiHeaders.get(TENANT_HEADER);

    final ServerErrorResponder serverErrorResponder =
      new ServerErrorResponder(DeleteLoanStorageLoansByLoanIdResponse
        ::withPlainInternalServerError, responseHandler, log);

    final VertxContextRunner runner = new VertxContextRunner(
      vertxContext, serverErrorResponder::withError);

    final Responder noContentResponder = new Responder(
      responseHandler, DeleteLoanStorageLoansByLoanIdResponse::withNoContent);

    runner.runOnContext(() -> {
      PostgresClient postgresClient = PostgresClient.getInstance(
          vertxContext.owner(), TenantTool.calculateTenantId(tenantId));

      Criteria a = new Criteria();

      a.addField("'id'");
      a.setOperation("=");
      a.setValue(loanId);

      Criterion criterion = new Criterion(a);

      postgresClient.delete(LOAN_TABLE, criterion,
        new ResultHandlerFactory<UpdateResult>().when(
          r -> noContentResponder.respond(),
          serverErrorResponder::withError));
    });
  }

  @Override
  public void putLoanStorageLoansByLoanId(
    String loanId,
    String lang,
    Loan loan,
    Map<String, String> okapiHeaders,
    Handler<AsyncResult<Response>> responseHandler,
    Context vertxContext) {

    String tenantId = okapiHeaders.get(TENANT_HEADER);

    if(loan.getStatus() == null) {
      loan.setStatus(new Status().withName(OPEN_LOAN_STATUS));
    }

    ImmutablePair<Boolean, String> validationResult = validateLoan(loan);

    if(!validationResult.getLeft()) {
      responseHandler.handle(succeededFuture(
        PutLoanStorageLoansByLoanIdResponse
          .withPlainBadRequest(validationResult.getRight())));
      return;
    }

    if(isOpenAndHasNoUserId(loan)) {
      respondWithError(responseHandler,
        PutLoanStorageLoansByLoanIdResponse::withJsonUnprocessableEntity,
        "Open loan must have a user ID");
      return;
    }

    final ServerErrorResponder serverErrorResponder =
      new ServerErrorResponder(PutLoanStorageLoansByLoanIdResponse
        ::withPlainInternalServerError, responseHandler, log);

    final VertxContextRunner runner = new VertxContextRunner(
      vertxContext, serverErrorResponder::withError);

    runner.runOnContext(() -> {
      PostgresClient postgresClient = PostgresClient.getInstance(
          vertxContext.owner(), TenantTool.calculateTenantId(tenantId));

      Criteria a = new Criteria();

      a.addField("'id'");
      a.setOperation("=");
      a.setValue(loanId);

      Criterion criterion = new Criterion(a);

      postgresClient.get(LOAN_TABLE, LOAN_CLASS, criterion, true, false,
        new ResultHandlerFactory<Results>().when(
          results -> {
            @SuppressWarnings("unchecked")
            List<Loan> loanList = (List<Loan>) results.getResults();

            if (loanList.size() == 1) {
              postgresClient.update(LOAN_TABLE, loan, criterion, true,
                new ResultHandlerFactory<UpdateResult>().when(
                  r -> {
                    OutStream stream = new OutStream();
                    stream.setData(loan);

                    responseHandler.handle(succeededFuture(
                      PutLoanStorageLoansByLoanIdResponse
                        .withNoContent()));
                  },
                  e -> {
                    if(isMultipleOpenLoanError(e)) {
                      responseHandler.handle(succeededFuture(
                        PutLoanStorageLoansByLoanIdResponse
                          .withJsonUnprocessableEntity(
                            moreThanOneOpenLoanError(loan))));
                    }
                    else {
                      serverErrorResponder.withError(e);
                    }
                  }));
            }
            else {
              postgresClient.save(LOAN_TABLE, loan.getId(), loan,
                new ResultHandlerFactory<String>().when(
                  r -> {
                    OutStream stream = new OutStream();
                    stream.setData(loan);

                    //TODO: Replace with 201 Created response?
                    responseHandler.handle(succeededFuture(
                      PutLoanStorageLoansByLoanIdResponse
                        .withNoContent()));
                  },
                  e -> {
                    if(isMultipleOpenLoanError(e)) {
                      responseHandler.handle(succeededFuture(
                        PutLoanStorageLoansByLoanIdResponse
                          .withJsonUnprocessableEntity(
                            moreThanOneOpenLoanError(loan))));
                    }
                    else {
                      serverErrorResponder.withError(e);
                    }
                  }));
            }
          },
        serverErrorResponder::withError));
    });
  }

  @Validate
  @Override
  public void getLoanStorageLoanHistory(
    int offset,
    int limit,
    String query,
    String lang,
    Map<String, String> okapiHeaders,
    Handler<AsyncResult<Response>> responseHandler,
    Context vertxContext) {

    String tenantId = okapiHeaders.get(TENANT_HEADER);

    final ServerErrorResponder serverErrorResponder =
      new ServerErrorResponder(GetLoanStorageLoanHistoryResponse
        ::withPlainInternalServerError, responseHandler, log);

    final VertxContextRunner runner = new VertxContextRunner(
      vertxContext, serverErrorResponder::withError);

    runner.runOnContext(() -> {
      PostgresClient postgresClient = PostgresClient.getInstance(
        vertxContext.owner(), TenantTool.calculateTenantId(tenantId));

      String[] fieldList = {"*"};
      CQLWrapper cql = null;
      String adjustedQuery = null;

      CQL2PgJSON cql2pgJson = new CQL2PgJSON(LOAN_HISTORY_TABLE+".jsonb");

      if(query != null){
        //a bit of a hack, assume that <space>sortBy<space>
        //is a sort request that is received as part of the cql , and hence pass
        //the cql as is. If no sorting is requested, sort by created_date column
        //in the loan history table which represents the date the entry was created
        //aka the date an action was made on the loan
        if(!query.contains(" sortBy ")) {
          cql = new CQLWrapper(cql2pgJson, query);

          adjustedQuery = cql.toString() + " order by created_date desc ";

          adjustedQuery = adjustedQuery
            + new Limit(limit).toString() + " "
            + new Offset(offset).toString();
        }
        else {
          cql = new CQLWrapper(cql2pgJson, query)
              .setLimit(new Limit(limit))
              .setOffset(new Offset(offset));

          adjustedQuery = cql.toString();
        }

        log.debug("CQL Query: " + cql.toString());

      } else {
        cql = new CQLWrapper(cql2pgJson, query)
              .setLimit(new Limit(limit))
              .setOffset(new Offset(offset));

        adjustedQuery = cql.toString();
      }

      postgresClient.get(LOAN_HISTORY_TABLE, LOAN_CLASS, fieldList, adjustedQuery,
        true, false, new ResultHandlerFactory<Results>().when(
          results -> {
            @SuppressWarnings("unchecked")
            List<Loan> loans = (List<Loan>) results.getResults();

            Loans pagedLoans = new Loans();

            pagedLoans.setLoans(loans);
            pagedLoans.setTotalRecords(results.getResultInfo().getTotalRecords());

            responseHandler.handle(succeededFuture(
              GetLoanStorageLoanHistoryResponse.withJsonOK(pagedLoans)));
          },
        serverErrorResponder::withError));
    });
  }

  private ImmutablePair<Boolean, String> validateLoan(Loan loan) {

    Boolean valid = true;
    StringJoiner messages = new StringJoiner("\n");

    //ISO8601 is less strict than RFC3339 so will not catch some issues
    try {
      DateTime.parse(loan.getLoanDate());
    }
    catch(Exception e) {
      valid = false;
      messages.add("loan date must be a date time (in RFC3339 format)");
    }

    if(loan.getReturnDate() != null) {
      //ISO8601 is less strict than RFC3339 so will not catch some issues
      try {
        DateTime.parse(loan.getReturnDate());
      }
      catch(Exception e) {
        valid = false;
        messages.add("return date must be a date time (in RFC3339 format)");
      }
    }

    return new ImmutablePair<>(valid, messages.toString());
  }

  private Errors moreThanOneOpenLoanError(Loan entity) {
    return ValidationHelper.createValidationErrorMessage(
      "itemId", entity.getItemId(),
      "Cannot have more than one open loan for the same item");
  }

  private boolean isMultipleOpenLoanError(Throwable cause) {
    return cause instanceof GenericDatabaseException &&
      ((GenericDatabaseException) cause).errorMessage().message()
        .contains("loan_itemid_idx_unique");
  }

  private boolean isOpenAndHasNoUserId(Loan loan) {
    return Objects.equals(loan.getStatus().getName(), OPEN_LOAN_STATUS)
      && loan.getUserId() == null;
  }

  private void respondWithError(
    Handler<AsyncResult<Response>> responseHandler,
    Function<Errors, Response> responseCreator,
    String message) {

    final ArrayList<Error> errorsList = new ArrayList<>();

    errorsList.add(new Error().withMessage(message));

    final Errors errors = new Errors()
      .withErrors(errorsList);

    responseHandler.handle(succeededFuture(
      responseCreator.apply(errors)));
  }

  private String createAnonymizationSQL(
    @NotNull String userId,
    String tenantId) {

    final String anonymizeLoansSql = String.format(
      "UPDATE %s_%s.loan SET jsonb = jsonb - 'userId'"
        + " WHERE jsonb->>'userId' = '" + userId + "'"
        + " AND jsonb->'status'->>'name' = 'Closed'",
      tenantId, MODULE_NAME);

    //Only anonymize the history for loans that are currently closed
    //meaning that we need to refer to loans in this query
    final String anonymizeLoansActionHistorySql = String.format(
      "UPDATE %s_%s.%s SET jsonb = jsonb - 'userId'"
        + " WHERE jsonb->>'userId' = '" + userId + "'"
        + " AND jsonb->>'id' IN (SELECT l.jsonb->>'id'" +
        " FROM %s_%s.loan l WHERE l.jsonb->>'userId' = '" + userId + "'"
        + " AND l.jsonb->'status'->>'name' = 'Closed')",
      tenantId, MODULE_NAME, LOAN_HISTORY_TABLE,
      tenantId, MODULE_NAME);

    //Loan action history needs to go first, as needs to be for specific loans
    return anonymizeLoansActionHistorySql + "; " + anonymizeLoansSql;
  }

}
