package com.goticks;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import com.goticks.BoxOffice.*;
import com.goticks.EventMarshalling.*;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static akka.http.javadsl.server.PathMatchers.segment;
import static akka.pattern.PatternsCS.ask;


class RestApi extends AllDirectives {
  private final Duration timeout;
  private final LoggingAdapter log;
  private final ActorRef boxOfficeActor;
  private final String msg = "      📩 {}";

  // コンストラクタ
  RestApi(ActorSystem system, Duration timeout) {
    this.timeout = timeout;
    log = Logging.getLogger(system, this);
    boxOfficeActor = system.actorOf(BoxOffice.props(timeout), "boxOfficeActor");
  }

  private CompletionStage<Events> getEvents() {
    return ask(boxOfficeActor, new GetEvents(), timeout).thenApply((Events.class::cast));
  }

  @SuppressWarnings("unchecked")
  private CompletionStage<Optional<Event>> getEvent(String name) {
    return ask(boxOfficeActor, new GetEvent(name), timeout).thenApply(obj -> (Optional<Event>) obj);
  }

  @SuppressWarnings("unchecked")
  private CompletionStage<Optional<Event>> cancelEvent(String name) {
    return ask(boxOfficeActor, new CancelEvent(name), timeout).thenApply(obj -> (Optional<Event>) obj);
  }

  private CompletionStage<EventResponse> createEvent(String name, int nrOfTickets) {
    return ask(boxOfficeActor, new CreateEvent(name, nrOfTickets), timeout).thenApply(EventResponse.class::cast);
  }

  private CompletionStage<TicketSeller.Tickets> requestTickets(String event, int tickets) {
    return ask(boxOfficeActor, new GetTickets(event, tickets), timeout).thenApply(TicketSeller.Tickets.class::cast);
  }

  public Route createRoute() {
    return route(
        pathPrefix("events", () -> route(
            // [Get all events] GET /events/
            get(() -> pathEndOrSingleSlash(() -> {
              log.debug("---------- GET /events/ ----------");

              CompletionStage<Events> events = getEvents();
              return onSuccess(() -> events, maybeEvent -> {
                    log.debug(msg, maybeEvent);
                    return completeOK(maybeEvent, Jackson.marshaller());
                  }
              );
            })),

            // [Get an event] GET /events/:name/
            get(() -> pathPrefix(segment(), (String name) ->
                pathEndOrSingleSlash(() -> {
                  log.debug("---------- GET /events/{}/ ----------", name);

                  CompletionStage<Optional<Event>> futureEvent = getEvent(name);
                  return onSuccess(() -> futureEvent, maybeEvent -> {
                        log.debug(msg, maybeEvent);
                        if (maybeEvent.isPresent())
                          return completeOK(maybeEvent.get(), Jackson.marshaller());
                        else
                          return complete(StatusCodes.NOT_FOUND);
                      }
                  );
                }))),

            // [Create an event] POST /events/:name/ tickets:=:tickets
            post(() -> pathPrefix(segment(), (String name) ->
                pathEndOrSingleSlash(() ->
                    entity(Jackson.unmarshaller(EventDescription.class), event -> {
                      log.debug("---------- POST /events/{}/ {\"tickets\":{}} ----------", name, event.getTickets());

                      CompletionStage<EventResponse> futureEventResponse = createEvent(name, event.getTickets());
                      return onSuccess(() -> futureEventResponse, maybeEventResponse -> {
                            log.debug(msg, maybeEventResponse);

                            if (maybeEventResponse instanceof EventCreated) {
                              Event maybeEvent = ((EventCreated) maybeEventResponse).getEvent();
                              return complete(StatusCodes.CREATED, maybeEvent, Jackson.marshaller());
                            } else {
                              EventError err = new EventError(name + " exists already.");
                              return complete(StatusCodes.BAD_REQUEST, err, Jackson.marshaller());
                            }
                          }
                      );
                    })
                ))),

            // [Buy tickets] POST /events/:event/tickets/ tickets:=:request
            post(() -> pathPrefix(segment().slash(segment("tickets")), (String event) ->
                pathEndOrSingleSlash(() ->
                    entity(Jackson.unmarshaller(TicketRequest.class), request -> {
                      log.debug("---------- POST /events/{}/tickets/ {\"tickets\":{}} ----------", event, request.getTickets());

                      CompletionStage<TicketSeller.Tickets> futureTickets = requestTickets(event, request.getTickets());
                      return onSuccess(() -> futureTickets, maybeTickets -> {
                            log.debug(msg, maybeTickets);

                            if (maybeTickets.getEntries().isEmpty())
                              return complete(StatusCodes.NOT_FOUND);
                            else
                              return complete(StatusCodes.CREATED, maybeTickets, Jackson.marshaller());
                          }
                      );
                    })
                ))),

            // [Cancel an event] DELETE /events/:name/
            delete(() -> pathPrefix(segment(), (String name) ->
                pathEndOrSingleSlash(() -> {
                      log.debug("---------- DELETE /events/{}/ ----------", name);

                      CompletionStage<Optional<Event>> futureEvent = cancelEvent(name);
                      return onSuccess(() -> futureEvent, maybeEvent -> {
                            log.debug(msg, maybeEvent);

                            if (maybeEvent.isPresent())
                              return completeOK(maybeEvent.get(), Jackson.marshaller());
                            else
                              return complete(StatusCodes.NOT_FOUND);
                          }
                      );
                    }
                )))
        ))
    );

  }

}
