package client;

import javafx.event.Event;
import javafx.event.EventType;
import shared.CharacterPacket;
import shared.Network;

public class MoveEvent extends Event {
    public static final EventType<MoveEvent> CHARACTER
            = new EventType<>(Event.ANY, "MOVE_EVENT");

    private Network.UpdateCharacter data;

    public MoveEvent(EventType<? extends Event> eventType, Network.UpdateCharacter data) {
        super(eventType);
        this.data = data;
    }

    public Network.UpdateCharacter getData() {
        return data;
    }
}
