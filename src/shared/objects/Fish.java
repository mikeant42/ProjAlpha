package shared.objects;

import shared.CharacterPacket;
import shared.GameObject;
import shared.IDs;
import shared.Names;

public class Fish extends GameObject{
    private double healAmount = 0.25;

    public Fish() {
        super(IDs.Food.FISH);
        setName(Names.Food.FISH);
    }

    @Override
    public void use(CharacterPacket packet) {
        System.out.println(packet.x);
    }

}
