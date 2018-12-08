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

  // TODO: 1.1. アクターのファクトリーメソッド(props)を定義
  // propsの定義
  public static Props props(String event) {
    //return Props.create(TicketSeller.class, () -> new TicketSeller(event));
    throw new UnsupportedOperationException("TODO: 1.1. が未実装です。");
  }

  private final List<Ticket> tickets = new ArrayList<>();

  // receiveメソッドの定義
  @Override
  public Receive createReceive() {
    return receiveBuilder()
        // TODO: 1.4.1. メッセージ(Add)受信時のふるまいを定義
//        .match(Add.class, this::add)
        // TODO: 2.2.1. メッセージ(Buy)受信時のふるまいを定義
//        .match(Buy.class, this::buy)
        // TODO: 3.2.1. メッセージ(GetEvent)受信時のふるまいを定義
//        .match(GetEvent.class, this::getEvent)
        // TODO: 4.2.1. メッセージ(Cancel)受信時のふるまいを定義
//        .match(Cancel.class, this::cancel)
        .build();
  }

  // TODO: 1.4.2. メッセージ(Add)受信時のふるまいを定義
//  private void add(Add add) {
//    log.debug(msg, add);
//
//    tickets.addAll(add.getTickets());
//  }

  // TODO: 2.2.2. メッセージ(Buy)受信時のふるまいを定義
//  private void buy(Buy buy){
//    log.debug(msg, buy);
//
//    if (tickets.size() >= buy.getTickets()) {
//      List<Ticket> entries = tickets.subList(0, buy.getTickets());
//      getContext().sender().tell(new Tickets(event, entries), getSelf());
//      entries.clear();
//    } else {
//      getContext().sender().tell(new Tickets(event), getSelf());
//    }
//  }

  // TODO: 3.2.2. メッセージ(GetEvent)受信時のふるまいを定義
//  private void getEvent(GetEvent getEvent) {
//    log.debug(msg, getEvent);
//
//    sender().tell(Optional.of(new BoxOffice.Event(event, tickets.size())), self());
//  }

  // TODO: 4.2.2. メッセージ(Cancel)受信時のふるまいを定義
//  private void cancel(Cancel cancel){
//    log.debug(msg, cancel);
//
//    sender().tell(Optional.of(new BoxOffice.Event(event, tickets.size())), self());
//    self().tell(PoisonPill.getInstance(), self());
//  }
}
