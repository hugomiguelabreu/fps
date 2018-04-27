
-module(auth).

-include("account.hrl").
-include("response.hrl").
-include("torrentWrapper.hrl").

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
	receive 
		{tcp, Socket, Data} ->
			ProtAcc =  account:decode_msg(Data, 'Account'),
			Username = binary_to_list(ProtAcc#'Account'.username),
			Password = binary_to_list(ProtAcc#'Account'.password),
			Type = ProtAcc#'Account'.type,

			case Type of 
				true ->
					Name = binary_to_list(ProtAcc#'Account'.name),
					register(Username, Password, Name, Socket);
				false -> 
					login(Username, Password, Socket)
			end;

		{tcp_closed, _} ->
			io:format("closed\n");
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
		true ->
			Msg = response:encode_msg(#'Response'{rep=true}),
			zk:setOnline(Username),
			gen_tcp:send(Socket, Msg),
			io:format("> Client " ++ Username ++ " logged in.\n"),
			loggedLoop(Socket, Username);
		false ->
			Msg = response:encode_msg(#'Response'{rep=false}),
			gen_tcp:send(Socket, Msg)
	end.

loggedLoop(Socket, Username) ->
	receive
		{tcp, Socket, Data} ->
			ProtoTorrent =  torrentWrapper:decode_msg(Data,'TorrentWrapper'),
			redirect(ProtoTorrent),
			loggedLoop(Socket, Username);
		
		{tcp_closed, Socket} ->
		 	zk:setOffline(Username),
		 	io:format("> client " ++ Username ++ " closed connection\n");

		{tcp_error, Socket, Reason} ->
		 	zk:setOffline(Username),
		 	io:format("> client " ++ Username ++ " timed out: " ++ Reason ++ "\n");

		{Username, torrent, Data} ->
			io:format("received and redirected\n"),
			torrentWrapper:encode_msg(Data),
			gen_tcp:send(Socket,Data) 
	end.

redirect(ProtoTorrent) ->
	io:format("> starting to redirect to group " ++ binary_to_list(ProtoTorrent#'TorrentWrapper'.group) ++ "\n"),
	case zk:getGroupUsers(binary_to_list(ProtoTorrent#'TorrentWrapper'.group)) of 
		{ok, L} ->
			lists:foreach(fun(USR) ->
							?MODULE ! {USR, torrent, ProtoTorrent}
						 end, L);
		no_group ->
			io:format("error: group doesn't exist\n");

		_ ->
			io:format("error NA LISTA\n")
	end.






