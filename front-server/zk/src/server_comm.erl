-module(server_comm).
-export([init/2, sendFrontServer/5]).
-include("server_wrapper.hrl").

init(Port, ID) ->
	{ok, LSock} = gen_tcp:listen(Port, [binary, {reuseaddr, true}, {packet, 1}]),
	io:format("> Listening for server communication\n"),
	acceptor(LSock, ID),
	receive 
		keep_me_alive->
		 io:format("keep_me_alive")
	end.		

acceptor(LSock, ID) ->
	{ok, Socket} = gen_tcp:accept(LSock),
	spawn(fun() -> acceptor(LSock, ID) end),
	receiveMsg(Socket, ID).

receiveMsg(Socket, ID) ->
	receive 
		{tcp, Socket, Data} ->
			msgDecriptor(Data, ID);
		{error, _} ->
			io:format("closed\n");
		_ ->
		 	io:format("error\n")
	end.

msgDecriptor(Data, ID) ->
	{_, {T, D}} =  server_wrapper:decode_msg(Data, 'ServerMessage'),
	case T of
		frontEndTorrent ->
			{'FrontEndTorrent', TID, UserList, Group, Content} = D,
			sendTracker(integer_to_list(ID), Content),
			lists:foreach( fun(User) ->
				case data:get_pid(User) of
					{ok, Pid} ->
						Pid ! {User, unpacked_torrent, binary_to_list(Group), Content};
					_ ->
						zk:setUnreceivedTorrent(binary_to_list(TID), User, binary_to_list(Group))
				end
			end, string:tokens(binary_to_list(UserList),";"))
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


sendTracker(ID, Data) ->
	TrackerLOC = zk:getTracker(ID),

	case TrackerLOC of 
		error ->
			io:format(">>> error: getting tracker\n");
		_ ->
			[T_IP,T_PORT] = string:split(TrackerLOC,":"),

			Msg = server_wrapper:encode_msg(#'ServerMessage'{msg = {trackerTorrent, #'TrackerTorrent'{content=Data}}}),
			case gen_tcp:connect(T_IP, list_to_integer(T_PORT), [binary, {reuseaddr, true}, {packet, 1}]) of
				{ok, TrackerSocket} ->
		    		gen_tcp:send(TrackerSocket, Msg),
		    		gen_tcp:close(TrackerSocket),
		    		io:format("> Sent to Tracker\n");
		    	{error, Reason} ->
		    		Reason
	    	end
	end.