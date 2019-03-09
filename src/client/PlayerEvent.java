package client;

import javafx.event.Event;
import javafx.event.EventType;
import shared.Network;

public class PlayerEvent extends Event {
    public static final EventType<PlayerEvent> DEATH
            = new EventType<>(Event.ANY, "DEATH");

    private Network.UpdateCharacter data;

    public PlayerEvent(EventType<? extends Event> eventType) {
        super(eventType);
    }

}
