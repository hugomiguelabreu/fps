
-module(auth).

-include("wrapper.hrl").

-export([init/1]).

% registo -> true
% login -> false

%%====================================================================
%% API
%%====================================================================

init(ID) ->
	{ok, LSock} = gen_tcp:listen(2000, [binary, {reuseaddr, true}, {packet, 1}]),
	io:format("> autentication started\n"),
	acceptor(LSock,ID).

%%====================================================================
%% Initialization
%%====================================================================

acceptor(LSock,ID) ->
	{ok, Socket} = gen_tcp:accept(LSock),
	spawn(fun() -> acceptor(LSock,ID) end),
	auth(Socket,ID).

auth(Socket, ID) ->
	receive 
		{tcp, Socket, Data} ->
			msgDecriptor(Data, "" ,Socket, ID);
		{tcp_closed, _} ->
			io:format("closed\n");
		_ ->
		 	io:format("error\n")

	end.

%%====================================================================
%% Available features
%%====================================================================

register(Username, Password, Name, Socket) -> 
	case (zk:register(Username,Password,Name)) of
		no_user ->
			MsgContainer = wrapper:encode_msg(#'ClientMessage'{msg = {response,#'Response'{rep=false}}}),
			gen_tcp:send(Socket, MsgContainer);
		error ->
			register(Username,Password,Name, Socket);
		ok ->
			MsgContainer = wrapper:encode_msg(#'ClientMessage'{msg = {response,#'Response'{rep=true}}}),
			gen_tcp:send(Socket, MsgContainer)
	end.	
		

login(Username, Password, ID, Socket) ->
	case (zk:login(Username,Password)) of
		no_user ->
			MsgContainer = wrapper:encode_msg(#'ClientMessage'{msg = {response,#'Response'{rep=false}}}),
			gen_tcp:send(Socket, MsgContainer);
		error ->
			login(Username,Password, ID, Socket);
		true ->
			zk:setOnline(Username, ID),
			data:register_pid(Username,self()),
			MsgContainer = wrapper:encode_msg(#'ClientMessage'{msg = {response,#'Response'{rep=true}}}),
			gen_tcp:send(Socket, MsgContainer),
			io:format("> Client " ++ Username ++ " logged in.\n"),
			loggedLoop(Socket, Username, ID);
		false ->
			MsgContainer = wrapper:encode_msg(#'ClientMessage'{msg = {response,#'Response'{rep=false}}}),
			gen_tcp:send(Socket, MsgContainer)
	end.

loggedLoop(Socket, Username, ID) ->
	receive
		{tcp, Socket, Data} ->
			msgDecriptor(Data, Username, Socket, ID),
			loggedLoop(Socket, Username, ID);
		
		{tcp_closed, Socket} ->
		 	zk:setOffline(Username),
		 	data:delete_pid(Username),
		 	io:format("> client " ++ Username ++ " closed connection\n");

		{tcp_error, Socket, Reason} ->
		 	zk:setOffline(Username),
		 	data:delete_pid(Username),
		 	io:format("> client " ++ Username ++ " timed out: " ++ Reason ++ "\n");

		{Username, torrent, Data} ->
			io:format("received and redirected\n"),
			T = wrapper:encode_msg(#'ClientMessage'{msg = {torrentWrapper, Data}}),
			gen_tcp:send(Socket, T),
			loggedLoop(Socket, Username, ID)
	end.

createGroup(User, GroupName, Socket) ->
	case zk:createGroup(GroupName,User) of
		ok ->
			MsgContainer = wrapper:encode_msg(#'ClientMessage'{msg = {response,#'Response'{rep=true}}}),
			gen_tcp:send(Socket, MsgContainer);
		group_exists ->
			MsgContainer = wrapper:encode_msg(#'ClientMessage'{msg = {response,#'Response'{rep=false}}}),
			gen_tcp:send(Socket, MsgContainer);
		error -> 
			createGroup(User,GroupName,Socket)
	end.

joinGroup(User, GroupName, Socket) ->
	case zk:createGroup(GroupName,User) of
		ok ->
			MsgContainer = wrapper:encode_msg(#'ClientMessage'{msg = {response,#'Response'{rep=true}}}),
			gen_tcp:send(Socket, MsgContainer);
		no_group ->
			MsgContainer = wrapper:encode_msg(#'ClientMessage'{msg = {response,#'Response'{rep=false}}}),
			gen_tcp:send(Socket, MsgContainer);
		error -> 
			createGroup(User,GroupName,Socket)
	end.


msgDecriptor(Data, User, Socket, ID) ->
	{_, {T, D}} =  wrapper:decode_msg(Data, 'ClientMessage'),
	io:format(T),
	case T of
		login ->
			{'Login',U,P}=D,
			login(binary_to_list(U),binary_to_list(P),ID, Socket);
		register ->
			{'Register',U,P,N}=D,
			register(binary_to_list(U),binary_to_list(P),binary_to_list(N),Socket);
		createGroup ->
			{G} = D,
			createGroup(User, G, Socket);
		joinGroup ->
			{G} = D,
			joinGroup(User, G, Socket);
		torrentWrapper ->
			redirect(D)
	end.

redirect(ProtoTorrent) ->
	io:format("> starting to redirect to group " ++ binary_to_list(ProtoTorrent#'TorrentWrapper'.group) ++ "\n"),
	case zk:getGroupUsers(binary_to_list(ProtoTorrent#'TorrentWrapper'.group)) of 
		{ok, L} ->
			lists:foreach(fun(USR) ->
							case data:get_pid(USR) of
								{ok, Pid} ->
									Pid ! {USR, torrent, ProtoTorrent};
								{error, Reason} ->
									Reason
								end
						  end, L);
		no_group ->
			io:format("error: group doesn't exist\n");

		_ ->
			io:format("list error\n")
	end.






