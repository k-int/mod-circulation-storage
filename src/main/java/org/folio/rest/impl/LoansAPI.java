package org.folio.rest.impl;

import io.vertx.core.*;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.folio.rest.annotations.Validate;
import org.folio.rest.jaxrs.model.Loan;
import org.folio.rest.jaxrs.model.Loans;
import org.folio.rest.jaxrs.resource.LoanStorageResource;
import org.folio.rest.persist.Criteria.Criteria;
import org.folio.rest.persist.Criteria.Criterion;
import org.folio.rest.persist.Criteria.Limit;
import org.folio.rest.persist.Criteria.Offset;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.persist.cql.CQLWrapper;
import org.folio.rest.tools.utils.OutStream;
import org.folio.rest.tools.utils.TenantTool;
import org.joda.time.DateTime;
import org.z3950.zing.cql.cql2pgjson.CQL2PgJSON;

import javax.ws.rs.core.Response;
import java.util.*;

import static org.folio.rest.impl.Headers.TENANT_HEADER;

public class LoansAPI implements LoanStorageResource {

  private static final Logger log = LoggerFactory.getLogger(LoansAPI.class);

  private final String LOAN_TABLE = "loan";
  private final String LOAN_HISTORY_TABLE = "loan_history_table";

  private final Class<Loan> LOAN_CLASS = Loan.class;

  public LoansAPI(Vertx vertx, String tenantId) {
    PostgresClient.getInstance(vertx, tenantId).setIdField("_id");
  }

  @Override
  public void deleteLoanStorageLoans(
    String lang,
    Map<String, String> okapiHeaders,
    Handler<AsyncResult<Response>> asyncResultHandler,
    Context vertxContext) throws Exception {

    String tenantId = okapiHeaders.get(TENANT_HEADER);

    vertxContext.runOnContext(v -> {
      try {
        PostgresClient postgresClient = PostgresClient.getInstance(
          vertxContext.owner(), TenantTool.calculateTenantId(tenantId));

        postgresClient.mutate(String.format("TRUNCATE TABLE %s_%s.loan",
          tenantId, "mod_circulation_storage"),
          reply -> {
            asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
              LoanStorageResource.DeleteLoanStorageLoansResponse
                .noContent().build()));
          });
      }
      catch(Exception e) {
        asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
          LoanStorageResource.DeleteLoanStorageLoansResponse
            .withPlainInternalServerError(e.getMessage())));
      }
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
    Handler<AsyncResult<Response>> asyncResultHandler,
    Context vertxContext) throws Exception {

    String tenantId = okapiHeaders.get(TENANT_HEADER);

    try {
      vertxContext.runOnContext(v -> {
        try {
          PostgresClient postgresClient = PostgresClient.getInstance(
            vertxContext.owner(), TenantTool.calculateTenantId(tenantId));

          System.out.println("CQL Query: " + query);

          String[] fieldList = {"*"};

          CQL2PgJSON cql2pgJson = new CQL2PgJSON("loan.jsonb");
          CQLWrapper cql = new CQLWrapper(cql2pgJson, query)
            .setLimit(new Limit(limit))
            .setOffset(new Offset(offset));

          postgresClient.get(LOAN_TABLE, LOAN_CLASS, fieldList, cql,
            true, false, reply -> {
              try {
                if(reply.succeeded()) {
                  List<Loan> loans = (List<Loan>) reply.result()[0];

                  Loans pagedLoans = new Loans();
                  pagedLoans.setLoans(loans);
                  pagedLoans.setTotalRecords((Integer)reply.result()[1]);

                  asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
                    LoanStorageResource.GetLoanStorageLoansResponse.
                      withJsonOK(pagedLoans)));
                }
                else {
                  asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
                    LoanStorageResource.GetLoanStorageLoansResponse.
                      withPlainInternalServerError(reply.cause().getMessage())));
                }
              } catch (Exception e) {
                e.printStackTrace();
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
                  LoanStorageResource.GetLoanStorageLoansResponse.
                    withPlainInternalServerError(e.getMessage())));
              }
            });
        } catch (Exception e) {
          e.printStackTrace();
          asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
            LoanStorageResource.GetLoanStorageLoansResponse.
              withPlainInternalServerError(e.getMessage())));
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
      asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
        LoanStorageResource.GetLoanStorageLoansResponse.
          withPlainInternalServerError(e.getMessage())));
    }
  }

  @Override
  public void postLoanStorageLoans(
    String lang,
    Loan entity,
    Map<String, String> okapiHeaders,
    Handler<AsyncResult<Response>> asyncResultHandler,
    Context vertxContext) throws Exception {

    String tenantId = okapiHeaders.get(TENANT_HEADER);

    ImmutablePair<Boolean, String> validationResult = validateLoan(entity);

    if(validationResult.getLeft() == false) {
      asyncResultHandler.handle(
        io.vertx.core.Future.succeededFuture(
          LoanStorageResource.PostLoanStorageLoansResponse
            .withPlainBadRequest(
              validationResult.getRight())));

      return;
    }

    try {
      PostgresClient postgresClient =
        PostgresClient.getInstance(
          vertxContext.owner(), TenantTool.calculateTenantId(tenantId));

      vertxContext.runOnContext(v -> {
        try {

          if(entity.getId() == null) {
            entity.setId(UUID.randomUUID().toString());
          }

          postgresClient.save(LOAN_TABLE, entity.getId(), entity,
            reply -> {
              try {
                if(reply.succeeded()) {
                  OutStream stream = new OutStream();
                  stream.setData(entity);

                  asyncResultHandler.handle(
                    io.vertx.core.Future.succeededFuture(
                      LoanStorageResource.PostLoanStorageLoansResponse
                        .withJsonCreated(reply.result(), stream)));
                }
                else {
                  asyncResultHandler.handle(
                    io.vertx.core.Future.succeededFuture(
                      LoanStorageResource.PostLoanStorageLoansResponse
                        .withPlainInternalServerError(reply.cause().toString())));
                }
              } catch (Exception e) {
                e.printStackTrace();
                asyncResultHandler.handle(
                  io.vertx.core.Future.succeededFuture(
                    LoanStorageResource.PostLoanStorageLoansResponse
                      .withPlainInternalServerError(e.getMessage())));
              }
            });
        } catch (Exception e) {
          e.printStackTrace();
          asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
            LoanStorageResource.PostLoanStorageLoansResponse
              .withPlainInternalServerError(e.getMessage())));
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
      asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
        LoanStorageResource.PostLoanStorageLoansResponse
          .withPlainInternalServerError(e.getMessage())));
    }
  }

  @Validate
  @Override
  public void getLoanStorageLoansByLoanId(
    String loanId,
    String lang,
    Map<String, String> okapiHeaders,
    Handler<AsyncResult<Response>> asyncResultHandler,
    Context vertxContext) throws Exception {

    String tenantId = okapiHeaders.get(TENANT_HEADER);

    try {
      PostgresClient postgresClient = PostgresClient.getInstance(
        vertxContext.owner(), TenantTool.calculateTenantId(tenantId));

      Criteria a = new Criteria();

      a.addField("'id'");
      a.setOperation("=");
      a.setValue(loanId);

      Criterion criterion = new Criterion(a);

      vertxContext.runOnContext(v -> {
        try {
          postgresClient.get(LOAN_TABLE, LOAN_CLASS, criterion, true, false,
            reply -> {
              try {
                if (reply.succeeded()) {
                  List<Loan> loans = (List<Loan>) reply.result()[0];

                  if (loans.size() == 1) {
                    Loan loan = loans.get(0);

                    asyncResultHandler.handle(
                      io.vertx.core.Future.succeededFuture(
                        LoanStorageResource.GetLoanStorageLoansByLoanIdResponse.
                          withJsonOK(loan)));
                  }
                  else {
                    asyncResultHandler.handle(
                      Future.succeededFuture(
                        LoanStorageResource.GetLoanStorageLoansByLoanIdResponse.
                          withPlainNotFound("Not Found")));
                  }
                } else {
                  asyncResultHandler.handle(
                    Future.succeededFuture(
                      LoanStorageResource.GetLoanStorageLoansByLoanIdResponse.
                        withPlainInternalServerError(reply.cause().getMessage())));

                }
              } catch (Exception e) {
                e.printStackTrace();
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
                  LoanStorageResource.GetLoanStorageLoansByLoanIdResponse.
                    withPlainInternalServerError(e.getMessage())));
              }
            });
        } catch (Exception e) {
          e.printStackTrace();
          asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
            LoanStorageResource.GetLoanStorageLoansByLoanIdResponse.
              withPlainInternalServerError(e.getMessage())));
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
      asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
        LoanStorageResource.GetLoanStorageLoansByLoanIdResponse.
          withPlainInternalServerError(e.getMessage())));
    }
  }

  @Override
  public void deleteLoanStorageLoansByLoanId(
    String loanId,
    String lang,
    Map<String, String> okapiHeaders,
    Handler<AsyncResult<Response>> asyncResultHandler,
    Context vertxContext) throws Exception {

    String tenantId = okapiHeaders.get(TENANT_HEADER);

    try {
      PostgresClient postgresClient =
        PostgresClient.getInstance(
          vertxContext.owner(), TenantTool.calculateTenantId(tenantId));

      Criteria a = new Criteria();

      a.addField("'id'");
      a.setOperation("=");
      a.setValue(loanId);

      Criterion criterion = new Criterion(a);

      vertxContext.runOnContext(v -> {
        try {
          postgresClient.delete(LOAN_TABLE, criterion,
            reply -> {
              if(reply.succeeded()) {
                asyncResultHandler.handle(
                  Future.succeededFuture(
                    DeleteLoanStorageLoansByLoanIdResponse
                      .withNoContent()));
              }
              else {
                asyncResultHandler.handle(Future.succeededFuture(
                  DeleteLoanStorageLoansByLoanIdResponse
                    .withPlainInternalServerError(reply.cause().getMessage())));
              }
            });
        } catch (Exception e) {
          asyncResultHandler.handle(Future.succeededFuture(
            DeleteLoanStorageLoansByLoanIdResponse
              .withPlainInternalServerError(e.getMessage())));
        }
      });
    } catch (Exception e) {
      asyncResultHandler.handle(Future.succeededFuture(
        DeleteLoanStorageLoansByLoanIdResponse
          .withPlainInternalServerError(e.getMessage())));
    }
  }

  @Override
  public void putLoanStorageLoansByLoanId(
    String loanId,
    String lang,
    Loan entity, Map<String, String> okapiHeaders,
    Handler<AsyncResult<Response>> asyncResultHandler,
    Context vertxContext) throws Exception {

    String tenantId = okapiHeaders.get(TENANT_HEADER);

    ImmutablePair<Boolean, String> validationResult = validateLoan(entity);

    if(validationResult.getLeft() == false) {
      asyncResultHandler.handle(
        io.vertx.core.Future.succeededFuture(
          LoanStorageResource.PostLoanStorageLoansResponse
            .withPlainBadRequest(
              validationResult.getRight())));

      return;
    }

    try {
      PostgresClient postgresClient =
        PostgresClient.getInstance(
          vertxContext.owner(), TenantTool.calculateTenantId(tenantId));

      Criteria a = new Criteria();

      a.addField("'id'");
      a.setOperation("=");
      a.setValue(loanId);

      Criterion criterion = new Criterion(a);

      vertxContext.runOnContext(v -> {
        try {
          postgresClient.get(LOAN_TABLE, LOAN_CLASS, criterion, true, false,
            reply -> {
              if(reply.succeeded()) {
                List<Loan> loanList = (List<Loan>) reply.result()[0];

                if (loanList.size() == 1) {
                  try {
                    postgresClient.update(LOAN_TABLE, entity, criterion,
                      true,
                      update -> {
                        try {
                          if(update.succeeded()) {
                            OutStream stream = new OutStream();
                            stream.setData(entity);

                            asyncResultHandler.handle(
                              Future.succeededFuture(
                                PutLoanStorageLoansByLoanIdResponse
                                  .withNoContent()));
                          }
                          else {
                            asyncResultHandler.handle(
                              Future.succeededFuture(
                                PutLoanStorageLoansByLoanIdResponse
                                  .withPlainInternalServerError(
                                    update.cause().getMessage())));
                          }
                        } catch (Exception e) {
                          asyncResultHandler.handle(
                            Future.succeededFuture(
                              PutLoanStorageLoansByLoanIdResponse
                                .withPlainInternalServerError(e.getMessage())));
                        }
                      });
                  } catch (Exception e) {
                    asyncResultHandler.handle(Future.succeededFuture(
                      PutLoanStorageLoansByLoanIdResponse
                        .withPlainInternalServerError(e.getMessage())));
                  }
                }
                else {
                  try {
                    postgresClient.save(LOAN_TABLE, entity.getId(), entity,
                      save -> {
                        try {
                          if(save.succeeded()) {
                            OutStream stream = new OutStream();
                            stream.setData(entity);

                            asyncResultHandler.handle(
                              Future.succeededFuture(
                                PutLoanStorageLoansByLoanIdResponse
                                  .withNoContent()));
                          }
                          else {
                            asyncResultHandler.handle(
                              Future.succeededFuture(
                                PutLoanStorageLoansByLoanIdResponse
                                  .withPlainInternalServerError(
                                    save.cause().getMessage())));
                          }
                        } catch (Exception e) {
                          asyncResultHandler.handle(
                            Future.succeededFuture(
                              PutLoanStorageLoansByLoanIdResponse
                                .withPlainInternalServerError(e.getMessage())));
                        }
                      });
                  } catch (Exception e) {
                    asyncResultHandler.handle(Future.succeededFuture(
                      PutLoanStorageLoansByLoanIdResponse
                        .withPlainInternalServerError(e.getMessage())));
                  }
                }
              } else {
                asyncResultHandler.handle(Future.succeededFuture(
                  PutLoanStorageLoansByLoanIdResponse
                    .withPlainInternalServerError(reply.cause().getMessage())));
              }
            });
        } catch (Exception e) {
          asyncResultHandler.handle(Future.succeededFuture(
            PutLoanStorageLoansByLoanIdResponse
              .withPlainInternalServerError(e.getMessage())));
        }
      });
    } catch (Exception e) {
      asyncResultHandler.handle(Future.succeededFuture(
        PutLoanStorageLoansByLoanIdResponse
          .withPlainInternalServerError(e.getMessage())));
    }
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

  @Validate
  @Override
  public void getLoanStorageLoanHistory(int offset, int limit, String query, String lang,
      Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler,
      Context vertxContext) throws Exception {

    String tenantId = okapiHeaders.get(TENANT_HEADER);

    try {
      vertxContext.runOnContext(v -> {
        try {
          PostgresClient postgresClient = PostgresClient.getInstance(
            vertxContext.owner(), TenantTool.calculateTenantId(tenantId));

          String[] fieldList = {"*"};
          CQLWrapper cql = null;
          String adjustedQuery = null;
          if(query != null){
            //a bit of a hack, assume that <space>sortBy<space>
            //is a sort request that is received as part of the cql , and hence pass
            //the cql as is. If no sorting is requested, sort by created_date column
            //in the loan history table which represents the date the entry was created
            //aka the date an action was made on the loan
            CQL2PgJSON cql2pgJson = new CQL2PgJSON(LOAN_HISTORY_TABLE+".jsonb");
            if(!query.contains(" sortBy ")){
              cql = new CQLWrapper(cql2pgJson, query);
              adjustedQuery = cql.toString() + " order by created_date desc ";
              adjustedQuery = adjustedQuery + new Limit(limit).toString() + " " +new Offset(offset).toString();
            } else{
              cql = new CQLWrapper(cql2pgJson, query)
                  .setLimit(new Limit(limit))
                  .setOffset(new Offset(offset));
              adjustedQuery = cql.toString();
            }
            System.out.println("CQL Query: " + cql.toString());
          }

          postgresClient.get(LOAN_HISTORY_TABLE, LOAN_CLASS, fieldList, adjustedQuery,
            true, false, reply -> {
              try {
                if(reply.succeeded()) {
                  List<Loan> loans = (List<Loan>) reply.result()[0];

                  Loans pagedLoans = new Loans();
                  pagedLoans.setLoans(loans);
                  pagedLoans.setTotalRecords((Integer)reply.result()[1]);

                  asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
                    GetLoanStorageLoanHistoryResponse.
                      withJsonOK(pagedLoans)));
                }
                else {
                  log.error(reply.cause().getMessage(), reply.cause());
                  asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
                    GetLoanStorageLoanHistoryResponse.
                      withPlainInternalServerError(reply.cause().getMessage())));
                }
              } catch (Exception e) {
                log.error(e.getMessage(), e);
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
                  GetLoanStorageLoanHistoryResponse.
                    withPlainInternalServerError(e.getMessage())));
              }
            });
        } catch (Exception e) {
          log.error(e.getMessage(), e);
          asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
            GetLoanStorageLoanHistoryResponse.
              withPlainInternalServerError(e.getMessage())));
        }
      });
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
        GetLoanStorageLoanHistoryResponse.
          withPlainInternalServerError(e.getMessage())));
    }

  }
}
