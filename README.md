Unnamed Game
----

You need
- javafx - make sure if you're running openjdk to install, as only the oracle java has it pre installed
- kyronet - networking library included
- fxgl - java game library included


State
----
All in a prototype state, only tested with up to 8 players. More testing is needed.

Network
- Can handle logins(no db yet) and disconnects
- Syncs player movement with client interpolation
- Syncs npc movement with client interpolation
- Syncs game objects
- In-game chat
- Player inventory
- Basic queueing system that can store messages until the client is ready to accept them
- Basic projectile system with client side prediction
- Server designed to read map file and spawn npcs in the "spawn" layer


TODO
----
- dialogue system
- npc spawning system
- once in a while client sends entire game world. Server checks and corrects anything out of sync
- resyncing after lost or delayed packets
- use udp port for real time packets, e.g. movement
- authenticating packets based on timestamps, etc.
- split server into LoginServer and GameServer
- split client into launcher and game
- replace the NetworkedComponent class with IDComponent
- write more gc friendly code
- Rewrite packet code oop with a base of x,y,id

CharacterPckt extends Entity
GameObject extends Entity
NPCPacket extends Entity
for entity : entities
    if entity instanceof Gameobject...


Optimizations
----
- only update and broadcast npc if its within a certain distance to any player
- only send npc/player/projectile data to players near it


Bugs
----
- sometimes the client side collision detection will make the character get stuck
- other npcs don't show the idle animation
- npcs "twitch" a little when they choose to remain idle (RoamingBehavior)


Mobile
----
There is a thought that I might want to port this to android. If so, the game will have to be built on the pc first then ported to android.
