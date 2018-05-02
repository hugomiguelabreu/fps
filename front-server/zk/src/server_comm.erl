-module(server_comm).
-export([start/0]).

init() ->
	{ok, LSock} = gen_tcp:listen(2001, [binary, {reuseaddr, true}, {packet, 1}]),
	io:format("> Listening for server communication\n"),
	acceptor(LSock).