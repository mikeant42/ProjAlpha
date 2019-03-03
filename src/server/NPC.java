package server;

import shared.CharacterPacket;
import shared.CombatObject;
import shared.EntityType;
import shared.Network;

enum BehaviorType {
    ROAMING, STANDING
}

public class NPC {
    private NPCBehavior behavior;

    private Network.NPCPacket packet;
    private Network.UpdateNPC updateNPC;

    private String name;

    private float spawnX, spawnY;


    public NPC(String name, float spawnX, float spawnY) {
        this.name = name;
        packet = new Network.NPCPacket();
        updateNPC = new Network.UpdateNPC();

        packet.type = EntityType.NPC;
        packet.x = spawnX;
        packet.y = spawnY;

        packet.combat = new CombatObject();
        packet.combat.setHealth(100);
        packet.combat.setMana(100); // the npc shoulnt need mana

        this.spawnX = spawnX;
        this.spawnY = spawnY;
    }

    public boolean isInteractable() {
        return packet.interactable;
    }

    public void setInteractable(boolean interactable) {
        packet.interactable = interactable;
    }

    public boolean isTrader() {
        return packet.trader;
    }

    public void setTrader(boolean trader) {
        packet.trader = trader;
    }

    public void setBehavior(BehaviorType behavior) {
        switch (behavior) {
            case ROAMING:
                this.behavior = new RoamingBehavior(spawnX, spawnY);
        }
    }

    public void update() {
        behavior.update();
    }

    public Network.NPCPacket getPacket() {
        return packet;
    }

    // This would only be implemented for dynamic npcs. Static npcs should have all interactive code on the client
    public void onCollisionWithPlayer(CharacterPacket packet) {
    }


    public Network.UpdateNPC formUpdate() {
        updateNPC.x =  behavior.getX();
        updateNPC.y =  behavior.getY();
        updateNPC.uid = packet.uid;
        updateNPC.moveState = behavior.getState();

        return updateNPC;
    }

    public float getX() {
        return behavior.getX();
    }

    public float getY() {
        return behavior.getY();
    }


}
