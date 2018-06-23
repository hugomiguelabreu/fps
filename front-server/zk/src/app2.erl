-module(app2).
-export([start/0]).

%falta tirar hard-code do ip ZK
start() ->
	spawn(zk, init, ["localhost", 2184]),
	spawn(data, init, []),
	spawn(server_comm, init, [3001, 2]),
	spawn(auth, init, [2, 2001]).