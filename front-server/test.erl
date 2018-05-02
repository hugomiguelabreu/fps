
-module(test).

-include("wrapper.hrl").

-export([init/0]).

% registo -> true
% login -> false

%%====================================================================
%% API
%%====================================================================

init() ->
	io:format("> autentication staterd\n"),
	{ok, LSock} = gen_tcp:listen(2000, [binary, {reuseaddr, true}, {packet, 1}]),
	acceptor(LSock).

%%====================================================================
%% Internal functions
%%====================================================================

acceptor(LSock) ->
	{ok, Socket} = gen_tcp:accept(LSock),
	spawn(fun() -> acceptor(LSock) end),
	auth(Socket).

auth(Socket) ->
	receive 
		{tcp, Socket, Data} ->
			{_, {T, D}} =  wrapper:decode_msg(Data, 'ClientMessage'),
			case T == account of
				true ->
					io:format("account" ++ "\n");
				_ ->
					io:format("not" ++ "\n")
			end;

		{tcp_closed, _} ->
			io:format("closed\n");
		_ ->
		 	io:format("error\n")

	end.