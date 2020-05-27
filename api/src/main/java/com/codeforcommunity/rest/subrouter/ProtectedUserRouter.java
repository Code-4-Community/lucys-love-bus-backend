package com.codeforcommunity.rest.subrouter;

import static com.codeforcommunity.rest.ApiRouter.end;
import static com.codeforcommunity.rest.RestFunctions.getJsonBodyAsClass;

import com.codeforcommunity.api.IProtectedUserProcessor;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.protected_user.SetContactsAndChildrenRequest;
import com.codeforcommunity.dto.protected_user.UserInformation;
import com.codeforcommunity.dto.user.ChangePasswordRequest;
import com.codeforcommunity.rest.IRouter;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class ProtectedUserRouter implements IRouter {

  private final IProtectedUserProcessor processor;

  public ProtectedUserRouter(IProtectedUserProcessor processor) {
    this.processor = processor;
  }

  @Override
  public Router initializeRouter(Vertx vertx) {
    Router router = Router.router(vertx);

    registerDeleteUser(router);
    registerChangePassword(router);
    registerSetUserContactInfo(router);
    registerGetUserContactInfo(router);

    return router;
  }

  private void registerDeleteUser(Router router) {
    Route deleteUserRoute = router.delete("/");
    deleteUserRoute.handler(this::handleDeleteUser);
  }

  private void registerChangePassword(Router router) {
    Route changePasswordRoute = router.post("/change_password");
    changePasswordRoute.handler(this::handleChangePasswordRoute);
  }

  private void registerSetUserContactInfo(Router router) {
    Route setUserContactInfoRoute = router.post("/contact_info");
    setUserContactInfoRoute.handler(this::handleSetUserContactInfo);
  }

  private void registerGetUserContactInfo(Router router) {
    Route setUserContactInfoRoute = router.get("/contact_info");
    setUserContactInfoRoute.handler(this::handleGetUserContactInfo);
  }

  private void handleDeleteUser(RoutingContext ctx) {
    JWTData userData = ctx.get("jwt_data");

    processor.deleteUser(userData);

    end(ctx.response(), 200);
  }

  private void handleChangePasswordRoute(RoutingContext ctx) {
    JWTData userData = ctx.get("jwt_data");
    ChangePasswordRequest changePasswordRequest =
        getJsonBodyAsClass(ctx, ChangePasswordRequest.class);

    processor.changePassword(userData, changePasswordRequest);

    end(ctx.response(), 200);
  }

  private void handleSetUserContactInfo(RoutingContext ctx) {
    JWTData userData = ctx.get("jwt_data");
    SetContactsAndChildrenRequest setUserContactInfoRequest =
        getJsonBodyAsClass(ctx, SetContactsAndChildrenRequest.class);

    processor.setContactsAndChildren(userData, setUserContactInfoRequest);

    end(ctx.response(), 201);
  }

  private void handleGetUserContactInfo(RoutingContext ctx) {
    JWTData userData = ctx.get("jwt_data");

    UserInformation userInformation = processor.getPersonalUserInformation(userData);

    end(ctx.response(), 200, JsonObject.mapFrom(userInformation).encode());
  }
}
