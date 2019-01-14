Client
----

- The client only needs a list of characters that are positioned close to him. It does not need everyone
on the server.


Login Process
--
- After logging in
1. Character is added into the server and launched into the world
2. client is given the grid in which his position corresponds
3. every other client on that grid is added to the client - vice versa


Server
----

- needs to handle logins, and keeping the game world synced
- eventually logins need to happen on a different server than the gameworld
    - perhaps a python web server connected to a db
