package com.goticks;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static akka.pattern.PatternsCS.ask;
import static akka.pattern.PatternsCS.pipe;

public class BoxOffice extends AbstractActor implements IBoxOffice {
  private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
  private final String msg = "    📩 {}";
  private final Duration timeout;

  // コンストラクタ
  private BoxOffice(Duration timeout) {
    this.timeout = timeout;
  }

  // propsの定義
  public static Props props(Duration timeout) {
    return Props.create(BoxOffice.class, () -> new BoxOffice(timeout));
  }

  // TODO: 1.2. TicketSellerアクターを生成する
  private ActorRef createTicketSeller(String name) {
    //return getContext().actorOf(TicketSeller.props(name), name);
    throw new UnsupportedOperationException("TODO: 1.2. が未実装です。");
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(CreateEvent.class, this::createEvent)
        .match(GetTickets.class, this::getTickets)
        .match(GetEvent.class, this::getEvent)
        .match(GetEvents.class, this::getEvents)
        .match(CancelEvent.class, this::cancelEvent)
        .build();
  }

  private void createEvent(CreateEvent createEvent) {
    log.debug(msg, createEvent);

    Optional<ActorRef> child = getContext().findChild(createEvent.getName());
    if (child.isPresent()) {
      getContext().sender().tell(new EventExists(), self());
    } else {
      ActorRef eventTickets = createTicketSeller(createEvent.getName());
      List<TicketSeller.Ticket> newTickets =
          IntStream.rangeClosed(1, createEvent.getTickets())
              .mapToObj(ticketId -> (new TicketSeller.Ticket(ticketId)))
              .collect(Collectors.toList());

      // TODO: 1.5. TicketSellerアクターにAddメッセージを送信する
//      eventTickets.tell(new TicketSeller.Add(newTickets), getSelf());
      // TODO: 1.6. 送信元アクターにEventCreatedメッセージを送信する
//      getContext().sender().tell(new EventCreated(new Event(createEvent.getName(), createEvent.getTickets())), getSelf());
    }
  }

  private void getTickets(GetTickets getTickets) {
    log.debug(msg, getTickets);

    Optional<ActorRef> child = getContext().findChild(getTickets.getEvent());
    // TODO: 2.3. TicketSellerアクターにBuyメッセージを送信する(返信先はRestApi)
//    if (child.isPresent())
//      child.get().forward(new TicketSeller.Buy(getTickets.getTickets()), getContext());
    // TODO: 2.4. 送信元アクターに空メッセージを送信する
//    else
//      getContext().sender().tell(new TicketSeller.Tickets(getTickets.getEvent()), getSelf());
  }

  private void getEvent(GetEvent getEvent) {
    log.debug(msg, getEvent);

    Optional<ActorRef> child = getContext().findChild(getEvent.getName());
    // TODO: 3.4. TicketSellerアクターにGetEventメッセージを送信する(返信先はRestApi)
//    if (child.isPresent())
//      child.get().forward(new TicketSeller.GetEvent(), getContext());
    // TODO: 3.5. 送信元アクターに空メッセージを送信する
//    else
//      getContext().sender().tell(Optional.empty(), getSelf());
  }

  @SuppressWarnings("unchecked")
  private void getEvents(GetEvents getEvents) {
    log.debug(msg, getEvents);

    // 子アクター（TicketSeller）に ask した結果のリストを作成
    List<CompletableFuture<Optional<Event>>> children = new ArrayList<>();
    // TODO: 3.3. 自アクターにGetEventメッセージを送信する(応答あり)
//    getContext().getChildren().forEach(child ->
//        children.add(ask(getSelf(), new GetEvent(child.path().name()), timeout)
//            .thenApply(event -> (Optional<Event>) event)
//            .toCompletableFuture()));

    // List<CompletableFuture<Optional<Event>>> の children を CompletionStage<Events> に変換
    // Events は List<Event> を持つ
    CompletionStage<Events> futureEvents = CompletableFuture
        .allOf(children.toArray(new CompletableFuture[0]))
        .thenApply(ignored -> {
          List<Event> events = children.stream()
              .map(CompletableFuture::join)
              .filter(Optional::isPresent)
              .map(Optional::get)
              .collect(Collectors.toList());
          return new Events(events);
        });

    // TODO: 3.6. 送信元アクターに取得したEventsメッセージを返す
//    pipe(futureEvents, getContext().dispatcher()).to(sender());
  }

  private void cancelEvent(CancelEvent cancelEvent) {
    log.debug(msg, cancelEvent);

    Optional<ActorRef> child = getContext().findChild(cancelEvent.getName());
    // TODO: 4.3. TicketSellerアクターにCancelメッセージを送信する(返信先はRestApi)
//    if (child.isPresent())
//      child.get().forward(new TicketSeller.Cancel(), getContext());
    // TODO: 4.4. 送信元アクターに空メッセージを送信する
//    else
//      getContext().sender().tell(Optional.empty(), getSelf());
  }
}
