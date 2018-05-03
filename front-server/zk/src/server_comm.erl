-module(server_comm).
-export([init/0, send_torrent/5]).
-include("server_wrapper.hrl").

init() ->
	{ok, LSock} = gen_tcp:listen(2001, [binary, {reuseaddr, true}, {packet, 1}]),
	io:format("> Listening for server communication\n"),
	acceptor(LSock).

acceptor(LSock) ->
	{ok, Socket} = gen_tcp:accept(LSock),
	spawn(fun() -> acceptor(LSock) end),
	receiveMsg(Socket).

receiveMsg(Socket) ->
	receive 
		{tcp, Socket, Data} ->
			msgDecriptor(Data);
		{tcp_closed, _} ->
			io:format("closed\n");
		_ ->
		 	io:format("error\n")
	end.

msgDecriptor(Data) ->
	{_, {T, D}} =  server_wrapper:decode_msg(Data, 'ServerMessage'),
	io:format(T),
	case T of
		torrentWrapper ->
			{'TorrentWrapper', ID, User, Group, Content} = D,
			case data:get_pid(User) of
				{ok, Pid} ->
					Pid ! {User, unpacked_torrent, Group, Content};
				_ ->
					zk:setUnreceivedTorrent(ID, User, Group)
			end	
	end.

send_torrent(Loc, User, Group, TID, Data) ->
	IP_PORT = zk:getFrontSv(Loc),
	case IP_PORT of
		error ->
			error_sending;
		_ ->
			[IP,PORT] = string:split(IP_PORT,":"),
			Msg = server_wrapper:encode_msg(#'ServerMessage'{msg={torrentWrapper, #'TorrentWrapper'{id=TID, user=User, group=Group, content= Data}}}),
			{ok, Socket} = gen_tcp:connect(IP, PORT, [binary, {packet, 0}]),
		    ok = gen_tcp:send(Socket, Msg),
		    ok = gen_tcp:close(Socket)
	end.

