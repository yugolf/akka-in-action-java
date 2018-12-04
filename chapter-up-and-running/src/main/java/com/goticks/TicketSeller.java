package com.goticks;

import akka.actor.AbstractActor;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// アクタークラスの定義
public class TicketSeller extends AbstractActor implements ITicketSeller {
  private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
  private final String msg = " 📩 {}";
  private final String event;

  // コンストラクタ
  private TicketSeller(String event) {
    this.event = event;
  }

  // propsの定義
  public static Props props(String event) {
    return Props.create(TicketSeller.class, () -> new TicketSeller(event));
  }

  private final List<Ticket> tickets = new ArrayList<>();

  // receiveメソッドの定義
  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(Add.class, add -> {
          log.debug(msg, add);

          tickets.addAll(add.getTickets());
        })
        .match(Buy.class, buy -> {
          log.debug(msg, buy);

          if (tickets.size() >= buy.getTickets()) {
            List<Ticket> entries = tickets.subList(0, buy.getTickets());
            getContext().sender().tell(new Tickets(event, entries), getSelf());
            entries.clear();
          } else {
            getContext().sender().tell(new Tickets(event), getSelf());
          }
        })
        .match(GetEvent.class, getEvent -> {
          log.debug(msg, getEvent);

          sender().tell(Optional.of(new BoxOffice.Event(event, tickets.size())), self());
        })
        .match(Cancel.class, getCancel -> {
          log.debug(msg, getCancel);

          sender().tell(Optional.of(new BoxOffice.Event(event, tickets.size())), self());
          self().tell(PoisonPill.getInstance(), self());
        })
        .build();
  }
}
