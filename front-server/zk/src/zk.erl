-module(zk).
-export([init/2,register/3, login/2, createGroup/2, joinGroup/2, setOnline/2, setOffline/1, getGroupUsers/1, getTrackerList/0, getFrontSv/1, newTorrent/4, setUnreceivedTorrent/3, setReceivedTorrent/3, getTracker/1, getNewContent/1, getGroups/1, getGroupOnline/1]).


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
    	{{tracker, ID}, From} ->
			V = getTracker(ID, Pid),
			From ! {?MODULE, V},
    		loop(Pid);	
    	{{new_torrent, TID, U, G, L}, From} ->
    		V = newTorrent(TID, U, G, L, Pid),
			From ! {?MODULE, V},
    		loop(Pid);
    	{{new_content, U}, From} ->
    		V = getNewContent(U, Pid),
			From ! {?MODULE, V},
    		loop(Pid);	
    	{{unreceived_torrent, TID, U, G}, From} ->
    		V = setUnreceivedTorrent(TID, U, G, Pid),
			From ! {?MODULE, V},
    		loop(Pid);
    	{{received_torrent, TID, U, G}, From} ->
    		V = setReceivedTorrent(TID, U, G, Pid),
			From ! {?MODULE, V},
    		loop(Pid);
		{{group_online, G}, From} ->
    		V = getGroupOnline(G, Pid),
			From ! {?MODULE, V},
    		loop(Pid);
		{{groups_user, U}, From} ->
    		V = getGroups(U, Pid),
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
	Missing  = "/users/" ++ Username ++ "/missing",

	case erlzk:create(Pid, UserPath, list_to_binary(Password)) of
		{error, _} -> error;
		{ok, _} ->
			erlzk:create(Pid, NamePath, list_to_binary(Name)),
			erlzk:create(Pid, SvPath, list_to_binary("")),
			erlzk:create(Pid, OnPath, list_to_binary("false")),
			erlzk:create(Pid, Missing, list_to_binary("")),
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
				 	string:equal(binary_to_list(RP),Password)
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
			erlzk:create(PID, "/users/" ++ User ++ "/groups/" ++ Name, ""),
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
			erlzk:create(PID, GroupPath ++ "/users/" ++ Username, list_to_binary("user")),
			erlzk:create(PID, "/users/" ++ Username ++ "/groups/" ++ Name, ""),
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
			{ok, getLoc(L, #{}, PID), length(L)};
		{error, no_node} ->
			no_group;
		_ ->
			error
	end.

getLoc([], Map, _) -> Map;
getLoc(Users, Map, PID) ->	
	[User | Tail] = Users,
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
							case maps:is_key(binary_to_list(LOC), Map) of
								false ->
									getLoc(Tail,maps:put(binary_to_list(LOC),[User],Map),PID);
								true ->
									L = lists:merge(maps:get(binary_to_list(LOC), Map), [User]),
									getLoc(Tail,maps:put(binary_to_list(LOC),L,Map),PID)
							end
					end;
				"false" ->
					case maps:is_key(offline, Map) of
						false ->
							getLoc(Tail,maps:put(offline,[User],Map),PID);
						true ->
							L = lists:merge(maps:get(offline, Map), [User]),
							getLoc(Tail,maps:put(offline,L,Map),PID)
					end
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


getTracker(ID) -> rpc({tracker, ID}).
getTracker(ID, PID) ->
	case erlzk:get_data(PID, "/trackers/" ++ ID) of
		{error, _} ->
		 	error;
		{ok,{RP,_}} ->
		 	binary_to_list(RP)
	 	end.

newTorrent(TID, U, G, L) -> rpc({new_torrent, TID, U, G, L}). 
newTorrent(TID, User, Group, Length, PID) ->
	GroupPath = "/groups/" ++ Group ++ "/torrents/" ++ TID,
	erlzk:create(PID, GroupPath, list_to_binary(User)),
	erlzk:create(PID, GroupPath ++ "/file", list_to_binary(integer_to_list(Length))),
	erlzk:create(PID, GroupPath ++ "/torrent", list_to_binary(integer_to_list(Length))),
	ok.	

setUnreceivedTorrent(TID, U, G) -> rpc({unreceived_torrent, TID, U, G}). 
setUnreceivedTorrent(TID, User, Group, PID) -> 
	Path = "/users/" ++ User ++ "/missing/" ++ TID,
	erlzk:create(PID, Path, list_to_binary(Group)),
	ok.	

setReceivedTorrent(TID, U, G) -> rpc({received_torrent, TID, U ,G}).
setReceivedTorrent(TID, User, Group, PID) ->
	erlzk:delete(PID, "/users/" ++ User ++ "/missing/" ++ TID),
	erlzk:create(PID, "/groups/" ++ Group ++ "/torrents/" ++ TID ++ "/torrent/" ++ User, list_to_binary("")),

	case erlzk:get_data(PID, "/groups/" ++ Group ++ "/torrents/" ++ TID ++ "/torrent") of
		{error, _} ->
		 	error;
		{ok,{RP,_}} ->
		 	Total = list_to_integer(binary_to_list(RP)),
		 	R = erlzk:get_children(PID, "/groups/" ++ Group ++ "/torrents/" ++ TID ++ "/torrent"),
			case R of
				{ok, L} ->
					case Total - 1 == R of 
						true -> {ok, remove};
						false -> {ok, success}
					end;
				{error, _} -> 
					error
			end
 	end.


getNewContent(U) -> rpc({new_content, U}).
getNewContent(User, PID) ->
	Path = "/users/" ++ User ++ "/missing",
	R = erlzk:get_children(PID, Path),
	case R of
		{ok, L} ->
			{ok, L};
		{error, no_node} ->
			no_group;
		_ ->
			error
	end.

getGroupOnline(G) -> rpc({group_online, G}).
getGroupOnline(Group, PID) ->
	case erlzk:get_children(PID,"/groups/" ++ Group  ++ "/users") of 
		{ok, L} ->
			lists:filter(fun(X) -> isUserOnline(X,PID)== true end, L);
		_ ->
			[]
	end.
	
isUserOnline(User, PID) ->
	case erlzk:get_data(PID, "/users/" ++ User ++ "/online") of
		{error, _} ->
			false;
		{ok,{R,_}} ->
			string:equal(binary_to_list(R), "true")
	end.

getGroups(U) -> rpc({groups_user, U}).
getGroups(User, PID) ->
	GroupPath = "/users/" ++ User ++ "/groups",
	case erlzk:get_children(PID,GroupPath) of 
		{ok, L} ->
			L;
		_ ->
			[]
	end.




