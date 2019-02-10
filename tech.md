Client
----

- The client only needs a list of characters that are positioned close to him. It does not need everyone
on the server.

Server
----

- needs to handle logins, and keeping the game world synced
- eventually logins need to happen on a different server than the gameworld
    - perhaps a python web server connected to a db
- each map needs to have a corresponding server World class that has all of the networked objects set up correctly

Map architecture like this
- One "Area" comprises of two or three different maps. These maps can all be accessed between one another
 Area
 - GameMap
    NPCupdate
    Playerupdate


- If i have the time i will add scripting to the game, so i can set the npc locations in a script and not in the code
