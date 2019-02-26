package shared.objects;

import shared.*;
import shared.Data.*;

public class Fish extends GameObject{
    private double healAmount = 0.25;

    public Fish() {
        super(IDs.Food.FISH);
        setName(Names.Food.FISH);
    }

    @Override
    public void use(CharacterPacket packet) {
        int effect = (int)(packet.combat.getHealth() + (Data.PlayerConstants.MAX_HEALTH * healAmount));
        if (effect < Data.PlayerConstants.MAX_HEALTH) {
            packet.combat.setHealth(effect); // updateHealth()
            System.out.println(effect);
        }
    }

}
