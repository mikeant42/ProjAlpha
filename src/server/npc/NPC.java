package server.npc;

import shared.*;

public class NPC {
    private NPCBehavior behavior;

    private Network.NPCPacket packet;
    private Network.UpdateNPC updateNPC;

    private String name;

    private double spawnX, spawnY;
    private boolean isEnemy = false;


    public NPC(String name, double spawnX, double spawnY) {
        this.name = name;
        packet = new Network.NPCPacket();
        updateNPC = new Network.UpdateNPC();

        packet.type = EntityType.NPC;
        packet.x = spawnX;
        packet.y = spawnY;



        packet.name = name;

        this.spawnX = spawnX;
        this.spawnY = spawnY;
    }

    public void setEnemy(boolean enemy) {
        isEnemy = enemy;
        if (enemy) {
            packet.combat = new CombatObject();
            packet.combat.setHealth(100);
            packet.combat.setMana(100); // the npc shoulnt need mana
            packet.type = EntityType.ENEMY;
        } else {
            packet.type = EntityType.NPC;
            packet.combat = null;
        }
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

    public boolean isEnemy() {
        return isEnemy;
    }

    public void setUniqueId(int uid) {
        packet.uid = uid;
    }

    public void setBehavior(BehaviorType behavior) {
        packet.behaviorType = behavior;
        switch (behavior) {
            case ROAMING:
                this.behavior = new RoamingBehavior(spawnX, spawnY);
                return;
            case STATIC:
                this.behavior = new NPCBehavior(spawnX, spawnY);
                this.behavior.setAllowedToMove(false);
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

        // also update our packet for newcomers
        packet.x = behavior.getX();
        packet.y = behavior.getY();

        return updateNPC;
    }

    public boolean shouldUpdate() {
        return !(behavior.getState() == Data.MovementState.STANDING);
    }

    public double getX() {
        return behavior.getX();
    }

    public double getY() {
        return behavior.getY();
    }


}
