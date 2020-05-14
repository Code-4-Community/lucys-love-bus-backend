package com.codeforcommunity.rest.subrouter;

import com.codeforcommunity.api.IAuthProcessor;
import com.codeforcommunity.api.IRequestsProcessor;
import com.codeforcommunity.auth.JWTData;
import com.codeforcommunity.dto.auth.NewUserAsPFRequest;
import com.codeforcommunity.dto.auth.SessionResponse;
import com.codeforcommunity.rest.IRouter;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import static com.codeforcommunity.rest.RestFunctions.getJsonBodyAsClass;
import static com.codeforcommunity.rest.ApiRouter.end;

public class PfOperationsRouter implements IRouter {

	private final IAuthProcessor authProcessor;
	private final IRequestsProcessor requestsProcessor;

	public PfOperationsRouter(IAuthProcessor authProcessor, IRequestsProcessor requestsProcessor) {
		this.authProcessor = authProcessor;
		this.requestsProcessor = requestsProcessor;
	}

	@Override
	public Router initializeRouter(Vertx vertx) {

		Router router = Router.router(vertx);

		registerSignUpPF(router);

		return router;

	}

	private void registerSignUpPF(Router router) {
		Route signUpPfRoute = router.post("/signup");
		signUpPfRoute.handler(this::handleSignUpPF);
	}

	private void handleSignUpPF(RoutingContext ctx) {
		NewUserAsPFRequest newUserPFRequest = getJsonBodyAsClass(ctx, NewUserAsPFRequest.class);
		SessionResponse sessionResponse = authProcessor.signUp(newUserPFRequest.getNewUserRequest());

		end(ctx.response(), 201, JsonObject.mapFrom(sessionResponse).toString());
	}
}
