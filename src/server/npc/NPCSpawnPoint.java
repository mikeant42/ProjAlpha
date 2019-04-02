package server.npc;

import shared.Network;

public abstract class NPCSpawnPoint {
   private String name;
   private double x,y;

   public NPCSpawnPoint() {

   }

   public abstract void onSpawn(Network.NPCPacket packet);
   public abstract void onDeath(Network.NPCPacket packet);
}

