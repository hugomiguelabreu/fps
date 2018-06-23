-module(app).
-export([start/0]).

%falta tirar hard-code do ip ZK
start() ->
	spawn(zk, init, ["localhost", 2184]),
	spawn(data, init, []),
	spawn(server_comm, init, [3000, 1]),
	spawn(auth, init, [1, 2000]).