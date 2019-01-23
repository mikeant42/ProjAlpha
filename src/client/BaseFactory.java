package client;

import com.almasb.fxgl.entity.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import shared.CharacterPacket;

public class BaseFactory implements EntityFactory {

    @Spawns("player")
    public Entity spawnPlayer(SpawnData data) {
        Entity player = Entities.builder()
                .at(data.getX(), data.getY())
                .viewFromNode(new Rectangle(25, 25, Color.BLUE))
                .build();
        player.setType(EntityType.PLAYER);
        player.addComponent(new NetworkedComponent(data.get("ID")));
        player.addComponent(new MovementComponent());
        return player;
    }
}
