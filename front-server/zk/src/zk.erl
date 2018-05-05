-module(zk).
-export([init/2,register/3, login/2, createGroup/2, joinGroup/2, setOnline/2, setOffline/1, getGroupUsers/1, getTrackerList/0, getFrontSv/1, newTorrent/3, setUnreceivedTorrent/3]).


init(Host, Port) ->
	application:start(crypto),
	application:start(erlzk),
	{ok, Pid} = erlzk:connect([{Host, Port}], 30000),
	io:format("> ZooKeeper connected.\n"),
	register(?MODULE,spawn(fun() -> loop(Pid) end)).

loop(Pid) -> 
	receive 
		{{register, U, P, N}, From} ->
			V = register(Pid, U, P, N),
			From ! {?MODULE, V},
    		loop(Pid);
    	{{login, U, P}, From} ->
			V = login(Pid, U, P),
			From ! {?MODULE, V},
    		loop(Pid);
    	{{create_group, N, U}, From} ->
			V = createGroup(Pid, N, U),
			From ! {?MODULE, V},
    		loop(Pid);
		{{join_group, N, U}, From} ->
			V = joinGroup(N, U, Pid),
			From ! {?MODULE, V},
    		loop(Pid);
		{{set_online, U, SID}, From} ->
			V = setOnline(U, SID, Pid),
			From ! {?MODULE, V},
    		loop(Pid);
    	{{set_offline, U}, From} ->
			V = setOffline(U, Pid),
			From ! {?MODULE, V},
    		loop(Pid);
    	{{group_users, N}, From} ->
			V = getGroupUsers(N, Pid),
			From ! {?MODULE, V},
    		loop(Pid);
    	{{tracker_list}, From} ->
			V = getTrackerList(Pid),
			From ! {?MODULE, V},
    		loop(Pid);
    	{{front_sv, ID}, From} ->
			V = getFrontSv(ID, Pid),
			From ! {?MODULE, V},
    		loop(Pid);
    	{{new_torrent, TID, U, G}, From} ->
    		V = newTorrent(TID, U, G, Pid),
			From ! {?MODULE, V},
    		loop(Pid);
    	{{unreceived_torrent, TID, U, G}, From} ->
    		V = setUnreceivedTorrent(TID, U, G, Pid),
			From ! {?MODULE, V},
    		loop(Pid)
	end.

rpc(Request) ->
	?MODULE ! {Request, self()},
	receive {?MODULE, Res} -> Res end.

% ---------------------------------------------------
% client 
% ---------------------------------------------------


register(U,P,N) -> rpc({register,U,P,N}).
register(Pid, Username, Password, Name) ->
	UserPath = "/users/" ++ Username,
	SvPath   = "/users/" ++ Username ++ "/sv",
	NamePath = "/users/" ++ Username ++ "/name",
	OnPath   = "/users/" ++ Username ++ "/online",

	case erlzk:create(Pid, UserPath, list_to_binary(Password)) of
		{error, _} -> error;
		{ok, _} ->
			erlzk:create(Pid, NamePath, list_to_binary(Name)),
			erlzk:create(Pid, SvPath, list_to_binary("")),
			erlzk:create(Pid, OnPath, list_to_binary("false")),
			ok
	end.

login(U,P) -> rpc({login,U,P}).
login(PID,Username,Password) ->
	UserPath = "/users/" ++ Username,
	case erlzk:exists(PID,UserPath) of
		{error, no_node} ->
			no_user;
		{error, _} ->
			error;
		_ -> 
			case erlzk:get_data(PID, UserPath) of
				 {error, _} ->
				 	error;
				 {ok,{RP,_}} ->
				 	string:equal(RP,Password)
			end
	end.

createGroup(N,U) -> rpc({create_group,N,U}).
createGroup(PID,Name,User) -> 
	GroupPath = "/groups/" ++ Name,
	case erlzk:exists(PID,GroupPath) of
		{error, no_node} ->
			erlzk:create(PID,GroupPath, list_to_binary("")),
			erlzk:create(PID,GroupPath ++ "/log", list_to_binary("")),
			erlzk:create(PID,GroupPath ++ "/meta", list_to_binary("")),
			erlzk:create(PID,GroupPath ++ "/torrents", list_to_binary("")),
			erlzk:create(PID,GroupPath ++ "/users", list_to_binary("")),
			erlzk:create(PID,GroupPath ++ "/users/" ++ User, list_to_binary("admin")),
			ok;
		{ok, _} ->
			group_exists;
		_ ->
			error
	end.

joinGroup(N,U) -> rpc({join_group,N,U}).
joinGroup(Name, Username, PID) ->
	GroupPath = "/groups/" ++ Name,
	case erlzk:exists(PID,GroupPath) of
		{error, no_node} ->
			no_group;
		{ok, _} ->
			erlzk:create(PID,GroupPath ++ "/users/" ++ Username, list_to_binary("user")),
			ok;
		_ ->
			error
	end.

setOnline(U,SID) -> rpc({set_online, U, SID}).
setOnline(Username,SID,PID) ->
	UserPath = "/users/" ++ Username ++ "/online",
	case erlzk:set_data(PID,UserPath,list_to_binary("true")) of
		{error, _} ->
			error;
		_ ->
			SvPath = "/users/" ++ Username ++ "/sv",
			case erlzk:set_data(PID,SvPath,integer_to_binary(SID)) of
				{error, _} ->
					error;
				_ ->
					ok
			end
	end.

setOffline(U) -> rpc({set_offline, U}).
setOffline(Username,PID) ->
	UserPath = "/users/" ++ Username ++ "/online",
	case erlzk:set_data(PID,UserPath,list_to_binary("false")) of
		{error, _} ->
			error;
		_ ->
			ok
	end.

getGroupUsers(N)-> rpc({group_users, N}).
getGroupUsers(Name, PID) ->
	GroupPath = "/groups/" ++ Name ++ "/users",
	R = erlzk:get_children(PID,GroupPath),
	case R of
		{ok, L} ->
			{ok, lists:map(fun(X) -> getLoc(PID, X) end, L)};
		{error, no_node} ->
			no_group;
		_ ->
			error
	end.


getLoc(PID, User) ->
	case erlzk:get_data(PID, "/users/" ++ User ++ "/online") of
		{error, _} ->
		 	error;
		{ok,{RP,_}} ->
		 	case binary_to_list(RP) of
		 		"true" -> 
		 			case erlzk:get_data(PID, "/users/" ++ User ++ "/sv") of 
		 				{error, _} ->
		 					error;
						{ok,{LOC,_}} ->
							{binary_to_list(LOC),User}
					end;
				"false" ->
					{'offline', User}
			end

 	end.


% ---------------------------------------------------
% server
% ---------------------------------------------------

getTrackerList() -> rpc({tracker_list}).
getTrackerList(PID) ->
	TrackersPath = "/trackers",
	case erlzk:get_children(PID,TrackersPath) of
		{error, no_node} ->
			io:format("> tracker list empty\n");
		{ok, L} ->
			{ok,L};
		_ ->
			error
	end.

getFrontSv(ID) -> rpc({front_sv, ID}).
getFrontSv(ID, PID) ->
	FEpath = "/front-servers/" ++ ID, 
	case erlzk:get_data(PID, FEpath) of
		{error, _} ->
		 	error;
		{ok,{RP,_}} ->
		 	RP
	 	end.

newTorrent(TID, U, G) -> rpc({new_torrent, TID, U, G}). 
newTorrent(TID, User, Group, PID) ->
	GroupPath = "/groups/" ++ Group ++ "/torrents/" ++ TID,
	erlzk:create(PID, GroupPath, list_to_binary(User)),
	erlzk:create(PID, GroupPath ++ "/torrent/", list_to_binary("")),
	erlzk:create(PID, GroupPath ++ "/file/", list_to_binary("")),
	ok.	

setUnreceivedTorrent(TID, U, G) -> rpc({unreceived_torrent, TID, U, G}). 
setUnreceivedTorrent(TID, User, Group, PID) -> 
	GroupPath = "/groups/" ++ Group ++ "/torrents/" ++ TID ++ "/torrent/" ++ User,
	erlzk:create(PID, GroupPath ++ User, list_to_binary("")),
	ok.	

		







	


 






