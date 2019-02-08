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
