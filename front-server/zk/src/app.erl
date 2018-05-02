-module(app).
-export([start/1]).

%falta tirar hard-code do ip ZK
start(ID) ->
	zk:init("localhost",2184),
	data:init(),
	data:startCounter(),
	data:getCurrentCounter(),
	auth:init(ID).