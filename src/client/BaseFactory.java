package client;

import com.almasb.fxgl.entity.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import shared.CharacterPacket;

public class BaseFactory implements EntityFactory {


    // We need this for networked comps
    private ClientHandler handler;

    public BaseFactory(ClientHandler handler) {
        this.handler = handler;
    }

    @Spawns("player")
    public Entity spawnPlayer(SpawnData data) {
        Entity player = Entities.builder()
                .at(data.getX(), data.getY())
                .viewFromNode(new Rectangle(25, 25, Color.BLUE))
                .build();
        player.setType(EntityType.PLAYER);

        // We need to set the temp id of the player so we are in sync with the client. This data is passed from the server.
        player.addComponent(new NetworkedComponent(data.get("ID"), handler));
        player.addComponent(new MovementComponent());
        return player;
    }
}
