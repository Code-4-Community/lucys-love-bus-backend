package com.codeforcommunity.rest.subrouter;

import com.codeforcommunity.api.IPfOperationsProcessor;
import com.codeforcommunity.dto.auth.NewUserAsPFRequest;
import com.codeforcommunity.dto.auth.SessionResponse;

import static com.codeforcommunity.rest.RestFunctions.getJsonBodyAsClass;
import static com.codeforcommunity.rest.ApiRouter.end;

public class PfOperationsRouter implements IRouter {

	private final IPfOperationsProcessor pfOperationsProcessor;

	public PfOperationsRouter(IPfOperationsProcessor pfOperationsProcessor) {

		this.pfOperationsProcessor = pfOperationsProcessor;

	}

	@Override
	Router initializeRouter(Vertx vertx) {

		Router router = Router.router(vertx);

		registerSignUpPF(router);

		return router;

	}

	private void registerSignUpPF(Router router) {
		Route signUpPfRoute = router.post("/") //todo what subdomain
		signUpPfRoute.handler(this::handleSignUpPF)
	}


	private void handleSignUpPF(RoutingContext ctx) {
		NewUserAsPFRequest newUserAsPFRequest = getJsonBodyAsClass(ctx, NewUserAsPFRequest.class);
		SessionResponse sessionResponse = pfOperationsProcessor.signUpPF(newUserAsPFRequest);
		end(ctx.response(), 201, JsonObject.mapFrom(sessionResponse).toString);
	}

}