Alpha Online
----
![Game Screenshot](pic1.png)

State
----
All in a prototype state, only tested with up to 8 players. More testing is needed. Alpha Online is a 2d multiplayer rpg-like fighting game. Art is mostly placeholders for now; there is not audio yet either.

AO is not cheat proof, but uses an authorative, dedicated  server. 

Network
- Can handle logins(no db) and disconnects
- Syncs player movement with client interpolation
- Syncs npc movement with client interpolation
- Syncs game objects
- In-game chat
- Player inventory
- Basic queueing system that can store messages until the client is ready to accept them
- Basic projectile system with client side prediction

Running
----
Make sure you have oracle java 8, and not openjdk. This is the best way to download the compatible javafx version. You will need to run AO using this version until alpha supports javafx 11.

If you are getting a problem with too-high framerates on ubuntu, it's caused by a bug in javafx. Run `export _JAVA_OPTIONS="-Dquantum.multithreaded=false"`, then try to run alpha again.

Dependencies
----
- javafx - this should be from the jdk, javafx 11 not yet supported
- kyronet - networking library included
- fxgl - java game library included

If you're on windows, you may need to allow it to pass through the firewall.


