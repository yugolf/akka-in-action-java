package com.goticks;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class EventMarshalling {

  public static class EventDescription {
    private final int tickets;

    @JsonCreator
    EventDescription(@JsonProperty("tickets") int tickets) {
      this.tickets = tickets;
    }

    public int getTickets() {
      return tickets;
    }
  }

  public static class TicketRequest {
    private final int tickets;

    @JsonCreator
    TicketRequest(@JsonProperty("tickets") int tickets) {
      this.tickets = tickets;
    }

    public int getTickets() {
      return tickets;
    }
  }

  public static class EventError {
    private final String message;

    @JsonCreator
    EventError(@JsonProperty("message") String message) {
      this.message = message;
    }

    public String getMessage() {
      return message;
    }
  }
}
