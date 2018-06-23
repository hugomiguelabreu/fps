
-module(auth).
-include("server_wrapper.hrl").
-include("client_wrapper.hrl").
-export([init/2]).

% registo -> true
% login -> false

%%====================================================================
%% API
%%====================================================================

init(ID,Port) ->
	{ok, LSock} = gen_tcp:listen(Port, [binary, {reuseaddr, true}, {packet, 4}]),
	io:format("> autentication started\n"),
	acceptor(LSock, ID),
	receive 
		keep_me_alive->
		 io:format("keep_me_alive")
	end.		

%%====================================================================
%% Initialization
%%====================================================================

acceptor(LSock,ID) ->
	case gen_tcp:accept(LSock) of
		{ok, Socket} ->
			spawn(fun() -> acceptor(LSock,ID) end),
			auth(Socket,ID);
		{error, Reason} ->
			io:format(Reason),
			Reason
		end.

auth(Socket, ID) ->
	receive 
		{tcp, Socket, Data} ->
			msg_decrypt(Data, "" ,Socket, ID);
		{tcp_closed, Socket} ->
			io:format("closed\n"),
			gen_tcp:close(Socket);
		_ ->
		 	io:format("error\n")

	end.


logged_loop(Socket, Username, ID) ->
	receive
		{tcp, Socket, Data} ->
			msg_decrypt(Data, Username, Socket, ID),
			logged_loop(Socket, Username, ID);
		
		{tcp_closed, Socket} ->
		 	zk:setOffline(Username),
		 	data:delete_pid(Username),
		 	io:format("> client " ++ Username ++ " closed connection\n"),
		 	gen_tcp:close(Socket);

		{tcp_error, Socket, Reason} ->
		 	zk:setOffline(Username),
		 	data:delete_pid(Username),
		 	io:format("> client " ++ Username ++ " timed out: " ++ Reason ++ "\n"),
		 	gen_tcp:close(Socket);

		{Username, packed_torrent, Data} ->
			io:format("> received and redirected\n"),
			T = client_wrapper:encode_msg(#'ClientMessage'{msg = {torrentWrapper, Data}}),
			gen_tcp:send(Socket, T),
			{'TorrentWrapper', Group, _ , TID} = Data,
			
			case zk:setReceivedTorrent(binary_to_list(TID), Username, binary_to_list(Group)) of
				{ok, remove} ->
					file:delete("./torrents/" ++ binary_to_list(TID)),
					logged_loop(Socket, Username, ID);
				_->
					logged_loop(Socket, Username, ID)
			end;

		{Username, unpacked_torrent, Group, Data, TID} ->
			io:format("> received from another server and redirected\n"),
			Wrapped = client_wrapper:encode_msg(#'ClientMessage'{msg = {torrentWrapper,#'TorrentWrapper'{group=Group, content=Data, id=TID}}}),
			gen_tcp:send(Socket, Wrapped),
			logged_loop(Socket, Username, ID)
	end.

msg_decrypt(Data, User, Socket, ID) ->
	{_, {T, D}} =  client_wrapper:decode_msg(Data, 'ClientMessage'),
	case T of
		login ->
			{'Login',U,P}=D,
			login(binary_to_list(U),binary_to_list(P),ID, Socket);
		register ->
			{'Register',U,P,N}=D,
			register(binary_to_list(U),binary_to_list(P),binary_to_list(N),Socket);
		createGroup ->
			{'CreateGroup', G} = D,
			create_group(User, binary_to_list(G), Socket);
		joinGroup ->
			{'JoinGroup', G} = D,
			join_group(User, binary_to_list(G), Socket);
		torrentWrapper ->
			redirect(D, integer_to_list(ID), User);
		onlineUsers ->
			{'OnlineUsers', G} = D,
			get_online_users(binary_to_list(G),Socket);
        groupUsers ->
			{'GroupUsers', U} = D,
			get_user_groups(binary_to_list(U), Socket)
	end.


%%====================================================================
%% Available features
%%====================================================================

register(Username, Password, Name, Socket) -> 
	case (zk:register(Username,Password,Name)) of
		no_user ->
			MsgContainer = client_wrapper:encode_msg(#'ClientMessage'{msg = {response,#'Response'{rep=false}}}),
			gen_tcp:send(Socket, MsgContainer);
		error ->
			register(Username,Password,Name, Socket);
		ok ->
			MsgContainer = client_wrapper:encode_msg(#'ClientMessage'{msg = {response,#'Response'{rep=true}}}),
			gen_tcp:send(Socket, MsgContainer)
	end.	
		

login(Username, Password, ID, Socket) ->
	case (zk:login(Username,Password)) of
		no_user ->
			MsgContainer = client_wrapper:encode_msg(#'ClientMessage'{msg = {response,#'Response'{rep=false}}}),
			gen_tcp:send(Socket, MsgContainer);
		error ->
			login(Username,Password, ID, Socket);
		true ->
			zk:setOnline(Username, ID),
			data:register_pid(Username,self()),
			MsgContainer = client_wrapper:encode_msg(#'ClientMessage'{msg = {response,#'Response'{rep=true}}}),
			gen_tcp:send(Socket, MsgContainer),
			io:format("> Client " ++ Username ++ " logged in.\n"),
			check_new_content(Username),
			logged_loop(Socket, Username, ID);
		false ->
			MsgContainer = client_wrapper:encode_msg(#'ClientMessage'{msg = {response,#'Response'{rep=false}}}),
			gen_tcp:send(Socket, MsgContainer)
	end.


create_group(User, GroupName, Socket) ->
	case zk:createGroup(GroupName,User) of
		ok ->
			MsgContainer = client_wrapper:encode_msg(#'ClientMessage'{msg = {response,#'Response'{rep=true}}}),
			gen_tcp:send(Socket, MsgContainer);
		group_exists ->
			MsgContainer = client_wrapper:encode_msg(#'ClientMessage'{msg = {response,#'Response'{rep=false}}}),
			Result = gen_tcp:send(Socket, MsgContainer),
			io:format(Result);
		error -> 
			create_group(User,GroupName,Socket)
	end.

join_group(User, GroupName, Socket) ->
	case zk:joinGroup(GroupName,User) of
		ok ->
			MsgContainer = client_wrapper:encode_msg(#'ClientMessage'{msg = {response,#'Response'{rep=true}}}),
			gen_tcp:send(Socket, MsgContainer);
		no_group ->
			MsgContainer = client_wrapper:encode_msg(#'ClientMessage'{msg = {response,#'Response'{rep=false}}}),
			gen_tcp:send(Socket, MsgContainer);
		error -> 
			join_group(User,GroupName,Socket)
	end.

get_online_users(Group, Socket) ->
	List = lists:concat(lists:join(";",zk:getGroupOnline(Group))), 
	MsgContainer = client_wrapper:encode_msg(#'ClientMessage'{msg = {onlineUsers,#'OnlineUsers'{onlineUsers = List}}}),
	gen_tcp:send(Socket, MsgContainer).
			
get_user_groups(User, Socket) ->
	List = lists:concat(lists:join(";",zk:getGroups(User))), 
	MsgContainer = client_wrapper:encode_msg(#'ClientMessage'{msg = {groupUsers,#'GroupUsers'{groupUsers = List }}}),
	gen_tcp:send(Socket, MsgContainer).		

%%====================================================================
%% Private functions
%%====================================================================

redirect(ProtoTorrent, ID, CurrentUser) ->
	io:format("> starting to redirect to group " ++ binary_to_list(ProtoTorrent#'TorrentWrapper'.group) ++ "\n"),
	%send_tracker(ID,ProtoTorrent#'TorrentWrapper'.content),
	
	case zk:getGroupUsers(binary_to_list(ProtoTorrent#'TorrentWrapper'.group)) of 
		{ok, UsersMap, Length} ->
			{'TorrentWrapper', _, Content , TID} = ProtoTorrent,
			zk:newTorrent(binary_to_list(TID), CurrentUser, binary_to_list(ProtoTorrent#'TorrentWrapper'.group), Length),
			file:write_file("./torrents/" ++ TID, ProtoTorrent),
			lists:foreach(fun(ServerID) ->
							case ServerID of
								offline ->
									UsersOffline = maps:get(offline,UsersMap),
									lists:foreach(fun(Usr) -> 
									zk:setUnreceivedTorrent(TID, Usr, binary_to_list(ProtoTorrent#'TorrentWrapper'.group)) end, UsersOffline);
								ID ->
									lists:foreach(fun(Usr) -> 
										UsrPid = data:get_pid(Usr),
										case UsrPid of
											{ok, Pid} ->
												Pid ! {Usr, packed_torrent, ProtoTorrent};
											error ->
												zk:setUnreceivedTorrent(TID, Usr, binary_to_list(ProtoTorrent#'TorrentWrapper'.group))
										end
									 end, maps:get(ID, UsersMap));
								_ ->
									UsersSv = lists:concat(lists:join(";",maps:get(ServerID,UsersMap))),
									server_comm:sendFrontServer(ServerID, UsersSv, binary_to_list(ProtoTorrent#'TorrentWrapper'.group), TID, Content)
							end
						  end, maps:keys(UsersMap));
		no_group ->
			io:format("error: group doesn't exist\n");

		_ ->
			io:format("list error\n")
	end.


send_tracker(ID, Data) ->
	TrackerLOC = zk:getTracker(ID),
	io:format(TrackerLOC),
	case TrackerLOC of 
		error ->
			io:format(">>> error: getting tracker\n");
		_ ->
			[T_IP,T_PORT] = string:split(TrackerLOC,":"),

			Msg = server_wrapper:encode_msg(#'ServerMessage'{msg = {trackerTorrent, #'TrackerTorrent'{content=Data}}}),
			case gen_tcp:connect(T_IP, list_to_integer(T_PORT), [binary, {reuseaddr, true}, {packet, 4}]) of
				{ok, TrackerSocket} ->
		    		gen_tcp:send(TrackerSocket, Msg),
		    		gen_tcp:close(TrackerSocket),
		    		io:format("> Sent to Tracker\n");
		    	{error, Reason} ->
		    		Reason
	    	end
	end.


check_new_content(User) ->
	case zk:getNewContent(User) of
		{ok, L} ->
			lists:foreach(fun(Filename) ->
					ProtoTorrent = file:read_file("./torrents/" ++ Filename),
					self() ! {User, packed_torrent, ProtoTorrent}
				end, L);
		_ ->
			error_new_Content
	end.
