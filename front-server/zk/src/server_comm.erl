-module(server_comm).
-export([init/2, send_front_server/5, send_tracker/2]).
-include("server_wrapper.hrl").


% ----------------------------------------
% start up and socket listening
% ----------------------------------------

init(Port, ID) ->
	{ok, LSock} = gen_tcp:listen(Port + ID, [binary, {reuseaddr, true}, {packet, 4}]),
	io:format("> Listening for server communication\n"),
	acceptor(LSock, ID),
	receive 
		keep_me_alive->
		 io:format("keep_me_alive")
	end.		

acceptor(LSock, ID) ->
	{ok, Socket} = gen_tcp:accept(LSock),
	spawn(fun() -> acceptor(LSock, ID) end),
	msg_listener(Socket, ID).

msg_listener(Socket, ID) ->
	receive 
		{tcp, Socket, Data} ->
			msg_decrypt(Data);
		{error, _} ->
			io:format("closed\n");
		_ ->
		 	io:format("error\n")
	end.

msg_decrypt(Data) ->
	{_, {T, D}} =  server_wrapper:decode_msg(Data, 'ServerMessage'),
	case T of
		frontEndTorrent ->
			{'FrontEndTorrent', TID, UserList, Group, Content} = D,
			%sendTracker(integer_to_list(ID), Content),
			lists:foreach( fun(User) ->
				case data:get_pid(User) of
					{ok, Pid} ->
						Pid ! {User, unpacked_torrent, binary_to_list(Group), Content, TID};
					_ ->
						zk:setUnreceivedTorrent(binary_to_list(TID), User, binary_to_list(Group))
				end
			end, string:tokens(binary_to_list(UserList),";"));
		trackerTorrent ->
			{'RequestTorrent', _, C} = D,
			C
	end.


% ----------------------------------------
% public functions
% ----------------------------------------

send_front_server(Loc, User, Group, TID, Data) ->
	IP_PORT = binary_to_list(zk:getFrontSv(Loc)),
	case IP_PORT of
		error ->
			error_sending;
		_ ->
			[IP,PORT] = string:split(IP_PORT,":"),
			Msg = server_wrapper:encode_msg(#'ServerMessage'{msg={frontEndTorrent, #'FrontEndTorrent'{id=TID, user=User, group=Group, content=Data}}}),
			case gen_tcp:connect(IP, list_to_integer(PORT), [binary, {reuseaddr, true}, {packet, 4}]) of
				{ok, Socket} ->
		    		gen_tcp:send(Socket, Msg),
		    		gen_tcp:close(Socket);
		    	{error, Reason} ->
		    		Reason
	    	end
	end.


send_tracker(ID, Data, Group) ->
	TrackerLOC = zk:getTracker(ID),

	case TrackerLOC of 
		error ->
			io:format(">>> error: getting tracker\n");
		_ ->
			[T_IP,T_PORT] = string:split(TrackerLOC,":"),

			Msg = server_wrapper:encode_msg(#'ServerMessage'{msg = {trackerTorrent, #'TrackerTorrent'{content=Data, group = Group}}}),
			case gen_tcp:connect(T_IP, list_to_integer(T_PORT), [binary, {reuseaddr, true}, {packet, 4}]) of
				{ok, TrackerSocket} ->
		    		gen_tcp:send(TrackerSocket, Msg),
		    		gen_tcp:close(TrackerSocket),
		    		io:format("> Sent to Tracker\n");
		    	{error, Reason} ->
		    		Reason
	    	end
	end.

req_file(ID, File) ->
	TrackerLOC = zk:getTracker(ID),

	case TrackerLOC of 
		error ->
			io:format(">>> error: getting tracker\n");
		_ ->
			[T_IP,T_PORT] = string:split(TrackerLOC,":"),
			Msg = server_wrapper:encode_msg(#'ServerMessage'{msg = {requestTorrent, #'RequestTorrent'{id=File}}}),
			
			case gen_tcp:connect(T_IP, list_to_integer(T_PORT), [binary, {reuseaddr, true}, {packet, 4}]) of
				{ok, TrackerSocket} ->
		    		gen_tcp:send(TrackerSocket, Msg),
		    		io:format("> Requested torrent to Tracker\n"),
					receive 
						{tcp, TrackerSocket, Data} ->
							gen_tcp:close(TrackerSocket),
							msg_decrypt(Data);
						_ ->
							req_file(ID, File)
					end;
		    	{error, _} ->
		    		error
	    	end
	end.



