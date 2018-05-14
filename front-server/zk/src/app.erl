-module(app).
-export([start/1]).

%falta tirar hard-code do ip ZK
start(ID) ->
	spawn(zk, init, ["localhost", 2184]),
	spawn(data, init, []),
	spawn(server_comm, init, [3000, ID]),
	spawn(auth, init, [ID, 2000]).