package com.goticks;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

import static akka.pattern.PatternsCS.ask;
import static akka.pattern.PatternsCS.pipe;

public class BoxOffice extends AbstractActor {
  private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
  private final String msg = "    📩 {}";

  // propsの定義
  public static Props props(Duration timeout) {
    return Props.create(BoxOffice.class, () -> new BoxOffice(timeout));
  }

  private final Duration timeout;

  // コンストラクタ
  private BoxOffice(Duration timeout) {
    this.timeout = timeout;
  }

  // メッセージプロトコルの定義
  // ------------------------------------------>
  public static class CreateEvent extends AbstractMessage {
    private final String name;
    private final int tickets;

    public CreateEvent(String name, int tickets) {
      this.name = name;
      this.tickets = tickets;
    }

    public String getName() {
      return name;
    }

    public int getTickets() {
      return tickets;
    }
  }

  public static class GetEvent extends AbstractMessage {
    private final String name;

    public GetEvent(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

  public static class GetEvents extends AbstractMessage {
  }

  public static class GetTickets extends AbstractMessage {
    private final String event;
    private final int tickets;

    public GetTickets(String event, int tickets) {
      this.event = event;
      this.tickets = tickets;
    }

    public String getEvent() {
      return event;
    }

    public int getTickets() {
      return tickets;
    }

  }

  public static class CancelEvent extends AbstractMessage {
    private final String name;

    public CancelEvent(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

  public static class Event extends AbstractMessage {
    private final String name;
    private final int tickets;

    public Event(String name, int tickets) {
      this.name = name;
      this.tickets = tickets;
    }

    public String getName() {
      return name;
    }

    public int getTickets() {
      return tickets;
    }
  }

  public static class Events extends AbstractMessage {
    private final List<Event> events;

    public Events(List<Event> events) {
      this.events = Collections.unmodifiableList(new ArrayList<>(events));
    }

    public List<Event> getEvents() {
      return events;
    }
  }

  public abstract static class EventResponse extends AbstractMessage {
  }

  public static class EventCreated extends EventResponse {
    private final Event event;

    public EventCreated(Event event) {
      this.event = event;
    }

    public Event getEvent() {
      return event;
    }
  }

  public static class EventExists extends EventResponse {
  }
  // <------------------------------------------

  private ActorRef createTicketSeller(String name) {
    return getContext().actorOf(TicketSeller.props(name), name);
  }

  private void create(String name, int tickets) {
    ActorRef eventTickets = createTicketSeller(name);
    List<TicketSeller.Ticket> newTickets = IntStream.rangeClosed(1, tickets)
        .mapToObj(ticketId -> (new TicketSeller.Ticket(ticketId))).collect(Collectors.toList());

    eventTickets.tell(new TicketSeller.Add(newTickets), getSelf());
    getContext().sender().tell(new EventCreated(new Event(name, tickets)), getSelf());
  }

  @SuppressWarnings("unchecked")
  private CompletionStage<Events> getEvents() {
    // 子アクター（TicketSeller）に ask した結果のリストを作成
    List<CompletableFuture<Optional<Event>>> children = new ArrayList<>();
    getContext().getChildren().forEach (child ->
      children.add(ask(getSelf(), new GetEvent(child.path().name()), timeout)
          .thenApply(event -> (Optional<Event>) event).toCompletableFuture()));

    // List<CompletableFuture<Optional<Event>>> の children を CompletionStage<Events> に変換
    // Events は List<Event> を持つ
    return CompletableFuture
        .allOf(children.toArray(new CompletableFuture[0]))
        .thenApply(ignored -> {
          List<Event> events = children.stream()
              .map(CompletableFuture::join)
              .map(Optional::get)
              .collect(Collectors.toList());
          return new Events(events);
        });
  }

  @Override
  public Receive createReceive() {

    return receiveBuilder()
        .match(CreateEvent.class, createEvent -> {
          log.debug(msg, createEvent);

          Optional<ActorRef> child = getContext().findChild(createEvent.name);
          if (child.isPresent())
            getContext().sender().tell(new EventExists(), self());
          else
            create(createEvent.name, createEvent.tickets);
        })
        .match(GetTickets.class, getTickets -> {
          log.debug(msg, getTickets);

          Optional<ActorRef> child = getContext().findChild(getTickets.event);
          if (child.isPresent())
            child.get().forward(new TicketSeller.Buy(getTickets.tickets), getContext());
          else
            getContext().sender().tell(new TicketSeller.Tickets(getTickets.event), getSelf());
        })
        .match(GetEvent.class, getEvent -> {
          log.debug(msg, getEvent);

          Optional<ActorRef> child = getContext().findChild(getEvent.name);
          if (child.isPresent())
            child.get().forward(new TicketSeller.GetEvent(), getContext());
          else
            getContext().sender().tell(Optional.empty(), getSelf());
        })
        .match(GetEvents.class, getEvents -> {
          log.debug(msg, getEvents);

          pipe(getEvents(), getContext().dispatcher()).to(sender());
        })
        .match(CancelEvent.class, cancelEvent -> {
          log.debug(msg, cancelEvent);

          Optional<ActorRef> child = getContext().findChild(cancelEvent.name);
          if (child.isPresent())
            child.get().forward(new TicketSeller.Cancel(), getContext());
          else
            getContext().sender().tell(Optional.empty(), getSelf());
        })
        .build();
  }
}
