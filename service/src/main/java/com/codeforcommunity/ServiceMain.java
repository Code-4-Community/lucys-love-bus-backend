package com.codeforcommunity;

import com.codeforcommunity.api.IAnnouncementsProcessor;
import com.codeforcommunity.api.IAuthProcessor;
import com.codeforcommunity.api.ICheckoutProcessor;
import com.codeforcommunity.api.IEventsProcessor;
import com.codeforcommunity.api.IProtectedUserProcessor;
import com.codeforcommunity.api.IPublicAnnouncementsProcessor;
import com.codeforcommunity.api.IPublicEventsProcessor;
import com.codeforcommunity.api.IRequestsProcessor;
import com.codeforcommunity.auth.JWTAuthorizer;
import com.codeforcommunity.auth.JWTCreator;
import com.codeforcommunity.auth.JWTHandler;
import com.codeforcommunity.logger.SLogger;
import com.codeforcommunity.processor.AnnouncementsProcessorImpl;
import com.codeforcommunity.processor.AuthProcessorImpl;
import com.codeforcommunity.processor.CheckoutProcessorImpl;
import com.codeforcommunity.processor.EventsProcessorImpl;
import com.codeforcommunity.processor.ProtectedUserProcessorImpl;
import com.codeforcommunity.processor.PublicAnnouncementsProcessorImpl;
import com.codeforcommunity.processor.PublicEventsProcessorImpl;
import com.codeforcommunity.processor.RequestsProcessorImpl;
import com.codeforcommunity.propertiesLoader.PropertiesLoader;
import com.codeforcommunity.requester.Emailer;
import com.codeforcommunity.rest.ApiRouter;
import io.vertx.core.Vertx;
import java.util.Properties;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

public class ServiceMain {
  private DSLContext db;
  private final Properties dbProperties = PropertiesLoader.getDbProperties();

  public static void main(String[] args) {
    try {
      ServiceMain serviceMain = new ServiceMain();
      serviceMain.initialize();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /** Start the server, get everything going. */
  public void initialize() {
    setUpSystemProperties();
    connectDb();
    initializeServer();
  }

  /** Adds any necessary system properties. */
  private void setUpSystemProperties() {
    Properties systemProperties = System.getProperties();
    systemProperties.setProperty(
        "vertx.logger-delegate-factory-class-name",
        "io.vertx.core.logging.SLF4JLogDelegateFactory");
    System.setProperties(systemProperties);
  }

  /** Connect to the database and create a DSLContext so jOOQ can interact with it. */
  private void connectDb() {
    // This block ensures that the driver is loaded in the classpath
    try {
      Class.forName(dbProperties.getProperty("database.driver"));
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

    this.db =
        DSL.using(
            dbProperties.getProperty("database.url"),
            dbProperties.getProperty("database.username"),
            dbProperties.getProperty("database.password"));
  }

  /** Initialize the server and get all the supporting classes going. */
  private void initializeServer() {
    JWTHandler jwtHandler =
        new JWTHandler(PropertiesLoader.getJwtProperties().getProperty("secret_key"));
    JWTAuthorizer jwtAuthorizer = new JWTAuthorizer(jwtHandler);
    JWTCreator jwtCreator = new JWTCreator(jwtHandler);

    String productName = "LLB";
    Vertx vertx = Vertx.vertx();
    SLogger.initializeLogger(vertx, productName);

    // Log uncaught exceptions to Slack
    vertx.exceptionHandler(SLogger::logApplicationError);

    Emailer emailer = new Emailer(this.db);

    IAuthProcessor authProcessor = new AuthProcessorImpl(this.db, jwtCreator, emailer);
    IProtectedUserProcessor protectedUserProcessor =
        new ProtectedUserProcessorImpl(this.db, emailer);
    IRequestsProcessor requestsProcessor = new RequestsProcessorImpl(this.db, emailer);
    IEventsProcessor eventsProcessor = new EventsProcessorImpl(this.db);
    IPublicEventsProcessor publicEventsProcessor = new PublicEventsProcessorImpl(this.db);
    IAnnouncementsProcessor announcementProcessor =
        new AnnouncementsProcessorImpl(this.db, emailer);
    IPublicAnnouncementsProcessor publicAnnouncementsProcessor =
        new PublicAnnouncementsProcessorImpl(this.db, emailer);
    ICheckoutProcessor checkoutProcessor = new CheckoutProcessorImpl(this.db, emailer);

    ApiRouter router =
        new ApiRouter(
            authProcessor,
            protectedUserProcessor,
            requestsProcessor,
            eventsProcessor,
            publicEventsProcessor,
            announcementProcessor,
            publicAnnouncementsProcessor,
            checkoutProcessor,
            jwtAuthorizer);

    startApiServer(router);
  }

  /** Start up the actual API server that will listen for requests. */
  private void startApiServer(ApiRouter router) {
    ApiMain apiMain = new ApiMain(router);
    apiMain.startApi();
  }
}
