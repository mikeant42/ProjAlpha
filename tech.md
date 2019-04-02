TODO
----
- npc drop table
- more spells and shields
- finish player death
- dialogue system
- npc spawning system
- once in a while client sends entire game world. Server checks and corrects anything out of sync
- resyncing after lost or delayed packets
- use udp port for real time packets, e.g. movement
- authenticating packets based on timestamps, etc.
- split server into LoginServer and GameServer
- split client into launcher and game
- write more gc friendly code


Optimizations
----
- only update and broadcast npc if its within a certain distance to any player
- only send npc/player/projectile data to players near it


Bugs
----
- client side collision resolution needs to be fixed, it's not great with corners
- player death function is not finished