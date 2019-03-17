package shared.objects;

import shared.*;
import shared.Data.*;

public class Fish extends GameObject{
    private double healAmount = 0.25;

    public Fish() {
        super(IDs.Food.FISH);
        setName(Names.Food.FISH);
    }

    // this needs to get out of this class
    @Override
    public void use(CharacterPacket packet) {
        int effect = (int)(packet.combat.getHealth() + (Data.PlayerConstants.MAX_HEALTH * healAmount));
        packet.combat.setHealth(AlphaUtil.ensureRange(effect, 0, PlayerConstants.MAX_HEALTH));

//        if (effect < Data.PlayerConstants.MAX_HEALTH) {
//            packet.combat.setHealth(effect); // updateHealth()
//        } else {
//            int buffer = (effect - PlayerConstants.MAX_HEALTH);
//            packet.combat.setHealth(packet.combat.getHealth()+buffer);
//            System.out.println("health: " + buffer);
//        }
    }

}
