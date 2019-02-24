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
- Syncs player movement
- Syncs npc movement
- Syncs game objects
- In-game chat
- Player inventory
- Basic queueing system that can store messages until the client is ready to accept them


TODO
----
- once in a while client sends entire game world. Server checks and corrects anything out of sync
- lag compensation / client side prediction / resyncing after lost or delayed packets
- use udp port for real time packets, e.g. movement


Mobile
----
There is a thought that I might want to port this to android. If so, the game will have to be built on the pc first then ported to android.
