
-module(auth).

-include("account.hrl").
-include("response.hrl").

-export([init/0]).

% registo -> true
% login -> false

%%====================================================================
%% API
%%====================================================================

init() ->
	io:format("> autentication staterd\n"),
	zk:init("localhost",2184),
	{ok, LSock} = gen_tcp:listen(2000, [binary, {reuseaddr, true}, {packet, 1}]),
	acceptor(LSock),
	receive
		kek ->
			io:format("Kek\n")
	end.

%%====================================================================
%% Internal functions
%%====================================================================

acceptor(LSock) ->
	{ok, Socket} = gen_tcp:accept(LSock),
	spawn(fun() -> acceptor(LSock) end),
	auth(Socket).

auth(Socket) ->
	io:format("recebi merdas"),
	case gen_tcp:recv(Socket, 0) of
		{ok, Data} ->
			io:format("oix"),
			ProtAcc =  account:decode_msg(Data, 'Account'),
			Username = ProtAcc#'Account'.username,
			Password = ProtAcc#'Account'.password,
			Type = ProtAcc#'Account'.type,

			case Type of 
				true ->
					Name = ProtAcc#'Account'.name,
					register(Username, Password, Name, Socket);
				false -> 
					login(Username, Password, Socket)
			end;

		%{tcp_closed, _} ->
		%	io:format("closed\n");
		_ ->
		 	io:format("error\n")

	end.


register(Username, Password, Name, Socket) -> 
	case (zk:register(Username,Password,Name)) of
		no_user ->
			Msg = response:encode_msg(#'Response'{rep=false}),
			gen_tcp:send(Socket, Msg);
		error ->
			register(Username,Password,Name, Socket);
		ok ->
			Msg = response:encode_msg(#'Response'{rep=true}),
			gen_tcp:send(Socket, Msg)	
	end.	
		

login(Username, Password, Socket) ->
	case (zk:login(Username,Password)) of
		no_user ->
			Msg = response:encode_msg(#'Response'{rep=false}),
			gen_tcp:send(Socket, Msg);
		error ->
			login(Username,Password,Socket);
		ok ->
			Msg = response:encode_msg(#'Response'{rep=true}),
			zk:setOnline(Username),
			gen_tcp:send(Socket, Msg),
			spawn(fun() -> loggedLoop(Socket, Username) end)
	end.

loggedLoop(Socket, Username) ->
	receive
		{tcp, Socket, Data} ->
			io:format(Data),
			loggedLoop(Socket, Username);
		_ ->
		 zk:setOffline(Username),
		 exit("client " ++ Username ++ " timed out.")
	end.



