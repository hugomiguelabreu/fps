%%%-------------------------------------------------------------------
%% @doc frontServer public API
%% @end
%%%-------------------------------------------------------------------

-module(frontServer).

% -behaviour(gen_server).

-export ([start/0, stop/1]).

%%====================================================================
%% API
%%====================================================================

start() -> 
	{ok, LSock} = gen_tcp:listen(2000, [binary, {reuseaddr, true}, {packet, 1}]),
	acceptor(LSock).

%%--------------------------------------------------------------------

stop(_State) ->
    ok.

%%====================================================================
%% Internal functions
%%====================================================================

acceptor(LSock) ->
	{ok, Sock} = gen_tcp:accept(LSock),
	spawn(fun() -> acceptor(LSock) end),
	worker(Sock).

worker(Sock) ->
	receive
		{tcp, Sock, Data} ->
			io:format("asd");
		{tcp_closed, _} ->
			io:format("kappa");
		_ ->
			io:format("jnwjw")
	end.