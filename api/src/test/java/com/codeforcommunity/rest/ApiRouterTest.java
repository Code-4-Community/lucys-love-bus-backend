package com.codeforcommunity.rest;

import com.codeforcommunity.api.IAnnouncementsProcessor;
import com.codeforcommunity.rest.subrouter.CommonRouter;
import com.codeforcommunity.rest.subrouter.AuthRouter;
import com.codeforcommunity.rest.subrouter.PfRequestRouter;
import com.codeforcommunity.rest.subrouter.EventsRouter;
import com.codeforcommunity.rest.subrouter.AnnouncementsRouter;
import com.codeforcommunity.rest.subrouter.WebhooksRouter;
import com.codeforcommunity.rest.subrouter.CheckoutRouter;
import io.vertx.core.Vertx;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeforcommunity.api.IAuthProcessor;
import com.codeforcommunity.api.IEventsProcessor;
import com.codeforcommunity.api.IRequestsProcessor;
import com.codeforcommunity.auth.JWTAuthorizer;

import io.vertx.ext.web.Router;
import org.junit.Before;
import org.junit.Test;

// Contains tests for ApiRouter.java in rest package
public class ApiRouterTest {
    IAuthProcessor myIAuthProcessor;
    IRequestsProcessor myIRequestsProcessor;
    IEventsProcessor myIEventsProcessor;
    IAnnouncementsProcessor myIAnnouncementsProcessor;
    JWTAuthorizer myJWTAuthorizer;
    ApiRouter myAPIRouter;

    CommonRouter myCommonRouter;
    AuthRouter myAuthRouter;
    PfRequestRouter myRequestRouter;
    EventsRouter myEventsRouter;
    AnnouncementsRouter myAnnouncementsRouter;

    Vertx myVertx;
    ApiRouter.Externals myExterns;
    Router myRouter;

    @Before
    public void setup() {
        this.myIAuthProcessor = mock(IAuthProcessor.class);
        this.myIRequestsProcessor = mock(IRequestsProcessor.class);
        this.myIEventsProcessor = mock(IEventsProcessor.class);
        this.myIAnnouncementsProcessor = mock(IAnnouncementsProcessor.class);
        this.myJWTAuthorizer = mock(JWTAuthorizer.class);

        this.myCommonRouter = mock(CommonRouter.class);
        this.myAuthRouter = mock(AuthRouter.class);
        this.myRequestRouter = mock(PfRequestRouter.class);
        this.myEventsRouter = mock(EventsRouter.class);
        this.myAnnouncementsRouter = mock(AnnouncementsRouter.class);

        this.myVertx = mock(Vertx.class);
        this.myExterns = mock(ApiRouter.Externals.class);
        this.myRouter = mock(Router.class);
    }

    @Test
    // example unit test for the main api router
    public void testApiRouter1() {
        when(myExterns.getCommonRouter()).thenReturn(myCommonRouter);
        when(myExterns.getAuthRouter()).thenReturn(myAuthRouter);
        when(myExterns.getRequestRouter()).thenReturn(myRequestRouter);
        when(myExterns.getEventsRouter()).thenReturn(myEventsRouter);
        when(myExterns.getAnnouncementsRouter()).thenReturn(myAnnouncementsRouter);

        when(myCommonRouter.initializeRouter(any(Vertx.class))).thenReturn(myRouter);
        when(myExterns.getRouter(any(Vertx.class))).thenReturn(myRouter);

        myAPIRouter = new ApiRouter(this.myExterns);
        myAPIRouter.initializeRouter(myVertx);

        verify(myCommonRouter).initializeRouter(myVertx);
        verify(myAuthRouter).initializeRouter(myVertx);
        verify(myRequestRouter).initializeRouter(myVertx);
        verify(myEventsRouter).initializeRouter(myVertx);
        verify(myAnnouncementsRouter).initializeRouter(myVertx);
        verify(myRouter, times(5)).mountSubRouter(anyString(), any());
    }

    /*
     * @Before // Do some initialization "before" we start running tests public void
     * before(TestContext context) { vertx = Vertx.vertx(); router =
     * Router.router(vertx); }
     * 
     * @After // Tear down resources used by vertx testing public void
     * after(TestContext context) { // p.destroy();
     * vertx.close(context.asyncAssertSuccess()); }
     * 
     * @Test public void testDefaultIndex() { HttpServerResponse res =
     * mock(HttpServerResponse.class); // Gives a NullPointerException // Even
     * though res isn't null...
     * 
     * ApiRouter.end(res, 200, null);
     * 
     * HttpServerRequest myHttpServerRequest = routingContext.request();
     * HttpServerResponse myHttpServerResponse = myHttpServerRequest.response();
     * 
     * ApiRouter.end(myHttpServerRequest.response(), 200);
     * System.out.println(myHttpServerRequest.response().getStatusCode());
     * 
     * RoutingContext routingContext = mock(RoutingContext.class);
     * 
     * when(routingContext.normalisedPath()).thenReturn("/"); Route currentRoute =
     * mock(Route.class); when(currentRoute.getPath()).thenReturn("/");
     * when(routingContext.currentRoute()).thenReturn(currentRoute); }
     */

    // INTEGRATION TESTS FOLLOW
    // PLEASE UNCOMMENT TO USE, BUT MAKE SURE YOU HAVE THE LIVE API RUNNING

    /*
     * @Test // Are all the keys we want are returned in the JSON response? public
     * void emptyTestParameters(TestContext context) { HttpClient client =
     * vertx.createHttpClient(); Async async = context.async();
     * 
     * client.getNow(8081, "localhost", "/api/v1/notes", resp -> {
     * resp.bodyHandler(body -> { JsonObject b = new JsonObject(body);
     * context.assertEquals(resp.statusCode(), 200);
     * 
     * context.assertEquals(b.containsKey("status"), true);
     * context.assertEquals(b.containsKey("notes"), true);
     * context.assertEquals(b.containsKey("not-here"), false);
     * 
     * client.close(); async.complete(); }); }); }
     * 
     * @Test // Are all the definitions we want are returned in the JSON response?
     * public void emptyTest(TestContext context) { HttpClient client =
     * vertx.createHttpClient(); Async async = context.async();
     * 
     * client.getNow(8081, "localhost", "/api/v1/notes", resp -> {
     * resp.bodyHandler(body -> { JsonObject b = new JsonObject(body);
     * 
     * context.assertEquals(b.getString("status"), "OK");
     * context.assertEquals(b.getJsonArray("notes").size(), 0);
     * 
     * client.close(); async.complete(); }); }); }
     */
}