package com.codeforcommunity.rest.subrouter;

import static com.codeforcommunity.rest.ApiRouter.end;

import com.codeforcommunity.api.IRequestsProcessor;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.pfrequests.CreateRequest;
import com.codeforcommunity.dto.pfrequests.GetRequestsResponse;
import com.codeforcommunity.dto.pfrequests.RequestData;
import com.codeforcommunity.dto.pfrequests.RequestStatusResponse;
import com.codeforcommunity.enums.RequestStatus;
import com.codeforcommunity.rest.IRouter;
import com.codeforcommunity.rest.RestFunctions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import java.util.List;

public class PfRequestRouter implements IRouter {

  private IRequestsProcessor requestsProcessor;

  public PfRequestRouter(IRequestsProcessor requestsProcessor) {
    this.requestsProcessor = requestsProcessor;
  }

  @Override
  public Router initializeRouter(Vertx vertx) {
    Router router = Router.router(vertx);

    registerCreateRequest(router);
    registerGetRequests(router);
    registerApproveRequest(router);
    registerRejectRequest(router);
    registerGetRequestStatus(router);

    return router;
  }

  private void registerCreateRequest(Router router) {
    Route createRequestRoute = router.post("/");
    createRequestRoute.handler(this::handleCreateRequestRoute);
  }

  private void registerGetRequests(Router router) {
    Route getRequestsRoute = router.get("/");
    getRequestsRoute.handler(this::handleGetRequestsRoute);
  }

  private void registerApproveRequest(Router router) {
    Route approveRequestRoute = router.post("/:request_id/approve");
    approveRequestRoute.handler(this::handleApproveRequestRoute);
  }

  private void registerRejectRequest(Router router) {
    Route rejectRequestRoute = router.post("/:request_id/reject");
    rejectRequestRoute.handler(this::handleRejectRequestRoute);
  }

  private void registerGetRequestStatus(Router router) {
    Route getRequestStatusRoute = router.get("/:request_id");
    getRequestStatusRoute.handler(this::handleGetRequestStatusRoute);
  }

  private void handleCreateRequestRoute(RoutingContext ctx) {
    CreateRequest requestData = RestFunctions.getJsonBodyAsClass(ctx, CreateRequest.class);
    JWTData userData = ctx.get("jwt_data"); // TODO: Make sure this isn't throwing NP exceptions

    requestsProcessor.createRequest(requestData, userData);

    end(ctx.response(), 200);
  }

  private void handleGetRequestsRoute(RoutingContext ctx) {
    JWTData userData = ctx.get("jwt_data");

    List<RequestData> requests = requestsProcessor.getRequests(userData);
    GetRequestsResponse response = new GetRequestsResponse(requests);

    end(ctx.response(), 200, JsonObject.mapFrom(response).encode());
  }

  private void handleApproveRequestRoute(RoutingContext ctx) {
    int requestId = RestFunctions.getRequestParameterAsInt(ctx.request(), "request_id");
    JWTData userData = ctx.get("jwt_data");

    requestsProcessor.approveRequest(requestId, userData);

    end(ctx.response(), 200);
  }

  private void handleRejectRequestRoute(RoutingContext ctx) {
    int requestId = RestFunctions.getRequestParameterAsInt(ctx.request(), "request_id");
    JWTData userData = ctx.get("jwt_data");

    requestsProcessor.rejectRequest(requestId, userData);

    end(ctx.response(), 200);
  }

  private void handleGetRequestStatusRoute(RoutingContext ctx) {
    int requestId = RestFunctions.getRequestParameterAsInt(ctx.request(), "request_id");
    JWTData userData = ctx.get("jwt_data");

    RequestStatus requestStatus = requestsProcessor.getRequestStatus(requestId, userData);
    RequestStatusResponse response = new RequestStatusResponse(requestStatus);

    end(ctx.response(), 200, JsonObject.mapFrom(response).encode());
  }
}
