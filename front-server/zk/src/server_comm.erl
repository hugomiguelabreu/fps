-module(server_comm).
-export([init/1, sendFrontServer/5]).
-include("server_wrapper.hrl").

init(Port) ->
	{ok, LSock} = gen_tcp:listen(Port, [binary, {reuseaddr, true}, {packet, 1}]),
	io:format("> Listening for server communication\n"),
	acceptor(LSock),
	receive 
		keep_me_alive->
		 io:format("keep_me_alive")
	end.		

acceptor(LSock) ->
	{ok, Socket} = gen_tcp:accept(LSock),
	spawn(fun() -> acceptor(LSock) end),
	receiveMsg(Socket).

receiveMsg(Socket) ->
	receive 
		{tcp, Socket, Data} ->
			msgDecriptor(Data);
		{error, _} ->
			io:format("closed\n");
		_ ->
		 	io:format("error\n")
	end.

msgDecriptor(Data) ->
	{_, {T, D}} =  server_wrapper:decode_msg(Data, 'ServerMessage'),
	io:format(T),
	case T of
		frontEndTorrent ->
			{'FrontEndTorrent', ID, User, Group, Content} = D,
			case data:get_pid(binary_to_list(User)) of
				{ok, Pid} ->
					Pid ! {binary_to_list(User), unpacked_torrent, binary_to_list(Group), Content};
				_ ->
					zk:setUnreceivedTorrent(binary_to_list(ID), binary_to_list(User), binary_to_list(Group))
			end	
	end.

sendFrontServer(Loc, User, Group, TID, Data) ->
	IP_PORT = binary_to_list(zk:getFrontSv(Loc)),
	case IP_PORT of
		error ->
			error_sending;
		_ ->
			[IP,PORT] = string:split(IP_PORT,":"),
			Msg = server_wrapper:encode_msg(#'ServerMessage'{msg={frontEndTorrent, #'FrontEndTorrent'{id=TID, user=User, group=Group, content=Data}}}),
			case gen_tcp:connect(IP, list_to_integer(PORT), [binary, {reuseaddr, true}, {packet, 1}]) of
				{ok, Socket} ->
		    		gen_tcp:send(Socket, Msg),
		    		gen_tcp:close(Socket);
		    	{error, Reason} ->
		    		Reason
	    	end
	end.

