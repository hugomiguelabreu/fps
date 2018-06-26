-module(zk).
-export([init/2,register/3, login/2, create_group/2, join_group/2, set_online/2, set_offline/1, get_group_location/1, get_tracker_list/0, get_frontsv/1, new_torrent/4, unreceived_torrent/3, received_torrent/3, get_tracker/1, get_new_content/1, get_groups/1, users_online_group/1, register_current/2]).


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
			V = create_group(Pid, N, U),
			From ! {?MODULE, V},
    		loop(Pid);
		{{join_group, N, U}, From} ->
			V = join_group(N, U, Pid),
			From ! {?MODULE, V},
    		loop(Pid);
		{{set_online, U, SID}, From} ->
			V = set_online(U, SID, Pid),
			From ! {?MODULE, V},
    		loop(Pid);
    	{{set_offline, U}, From} ->
			V = set_offline(U, Pid),
			From ! {?MODULE, V},
    		loop(Pid);
    	{{group_users, N}, From} ->
			V = get_group_location(N, Pid),
			From ! {?MODULE, V},
    		loop(Pid);
    	{{tracker_list}, From} ->
			V = get_tracker_list(Pid),
			From ! {?MODULE, V},
    		loop(Pid);
    	{{front_sv, ID}, From} ->
			V = get_frontsv(ID, Pid),
			From ! {?MODULE, V},
    		loop(Pid);
    	{{tracker, ID}, From} ->
			V = get_tracker(ID, Pid),
			From ! {?MODULE, V},
    		loop(Pid);	
    	{{new_torrent, TID, U, G, L}, From} ->
    		V = new_torrent(TID, U, G, L, Pid),
			From ! {?MODULE, V},
    		loop(Pid);
    	{{new_content, U}, From} ->
    		V = get_new_content(U, Pid),
			From ! {?MODULE, V},
    		loop(Pid);	
    	{{unreceived_torrent, TID, U, G}, From} ->
    		V = unreceived_torrent(TID, U, G, Pid),
			From ! {?MODULE, V},
    		loop(Pid);
    	{{received_torrent, TID, U, G}, From} ->
    		V = received_torrent(TID, U, G, Pid),
			From ! {?MODULE, V},
    		loop(Pid);
		{{group_online, G}, From} ->
    		V = users_online_group(G, Pid),
			From ! {?MODULE, V},
    		loop(Pid);
		{{groups_user, U}, From} ->
    		V = get_groups(U, Pid),
			From ! {?MODULE, V},
    		loop(Pid);
		{{register_current, ID, IP}, From} ->
    		V = register_current(ID, IP, Pid),
			From ! {?MODULE, V},
    		loop(Pid)

	end.

rpc(Request) ->
	?MODULE ! {Request, self()},
	receive {?MODULE, Res} -> Res end.

% ---------------------------------------------------
% client requests
% ---------------------------------------------------


register(U,P,N) -> rpc({register,U,P,N}).
register(Pid, Username, Password, Name) ->
	UserPath = "/users/" ++ Username,
	SvPath   = "/users/" ++ Username ++ "/sv",
	NamePath = "/users/" ++ Username ++ "/name",
	OnPath   = "/users/" ++ Username ++ "/online",
	Missing  = "/users/" ++ Username ++ "/missing",
	Groups  = "/users/" ++ Username ++ "/groups",

	case erlzk:create(Pid, UserPath, list_to_binary(Password)) of
		{error, _} -> error;
		{ok, _} ->
			erlzk:create(Pid, NamePath, list_to_binary(Name)),
			erlzk:create(Pid, SvPath, list_to_binary("")),
			erlzk:create(Pid, OnPath, list_to_binary("false")),
			erlzk:create(Pid, Missing, list_to_binary("")),
			erlzk:create(Pid, Groups, list_to_binary("")),
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

create_group(N,U) -> rpc({create_group,N,U}).
create_group(PID,Name,User) -> 
	GroupPath = "/groups/" ++ Name,
	case erlzk:exists(PID,GroupPath) of
		{error, no_node} ->
			erlzk:create(PID, GroupPath, list_to_binary("")),
			erlzk:create(PID, GroupPath ++ "/log", list_to_binary("")),
			erlzk:create(PID, GroupPath ++ "/meta", list_to_binary("")),
			erlzk:create(PID, GroupPath ++ "/torrents", list_to_binary("")),
			erlzk:create(PID, GroupPath ++ "/users", list_to_binary("")),
			erlzk:create(PID, GroupPath ++ "/users/" ++ User, list_to_binary("admin")),
			erlzk:create(PID, "/users/" ++ User ++ "/groups/" ++ Name, list_to_binary("")),
			ok;
		{ok, _} ->
			group_exists;
		_ ->
			error
	end.

join_group(N,U) -> rpc({join_group,N,U}).
join_group(Name, Username, PID) ->
	GroupPath = "/groups/" ++ Name,
	case erlzk:exists(PID,GroupPath) of
		{error, no_node} ->
			no_group;
		{ok, _} ->
			erlzk:create(PID, GroupPath ++ "/users/" ++ Username, list_to_binary("user")),
			erlzk:create(PID, "/users/" ++ Username ++ "/groups/" ++ Name, list_to_binary("")),
			ok;
		_ ->
			error
	end.

set_online(U,SID) -> rpc({set_online, U, SID}).
set_online(Username,SID,PID) ->
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

set_offline(U) -> rpc({set_offline, U}).
set_offline(Username,PID) ->
	UserPath = "/users/" ++ Username ++ "/online",
	case erlzk:set_data(PID,UserPath,list_to_binary("false")) of
		{error, _} ->
			error;
		_ ->
			ok
	end.


new_torrent(TID, U, G, L) -> rpc({new_torrent, TID, U, G, L}). 
new_torrent(TID, User, Group, Length, PID) ->
	GroupPath = "/groups/" ++ Group ++ "/torrents/" ++ TID,
	erlzk:create(PID, GroupPath, list_to_binary(User)),
	erlzk:create(PID, GroupPath ++ "/file", list_to_binary(integer_to_list(Length))),
	erlzk:create(PID, GroupPath ++ "/torrent", list_to_binary(integer_to_list(Length))),
	ok.	

unreceived_torrent(TID, U, G) -> rpc({unreceived_torrent, TID, U, G}). 
unreceived_torrent(TID, User, Group, PID) -> 
	Path = "/users/" ++ User ++ "/missing/" ++ TID,
	erlzk:create(PID, Path, list_to_binary(Group)),
	ok.	

received_torrent(TID, U, G) -> rpc({received_torrent, TID, U ,G}).
received_torrent(TID, User, Group, PID) ->
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
					case Total - 1 == length(L) of 
						true -> {ok, remove};
						false -> {ok, success}
					end;
				{error, _} -> 
					error
			end
 	end.


get_new_content(U) -> rpc({new_content, U}).
get_new_content(User, PID) ->
	Path = "/users/" ++ User ++ "/missing",
	R = erlzk:get_children(PID, Path),
	case R of
		{ok, L} ->
			{ok, lists:map(fun(X) -> {X, get_torrent_group(User, X, PID)} end, L)};
		{error, no_node} ->
			no_group;
		_ ->
			error
	end.
get_torrent_group(User,T, PID) ->
	{ok,{R,_}} = erlzk:get_data(PID, "/users/" ++ User ++ "/missing/" ++ T),
	binary_to_list(R).


users_online_group(G) -> rpc({group_online, G}).
users_online_group(Group, PID) ->
	case erlzk:get_children(PID,"/groups/" ++ Group  ++ "/users") of 
		{ok, L} ->
			lists:filter(fun(X) -> is_online_user(X,PID)== true end, L);
		_ ->
			[]
	end.
is_online_user(User, PID) ->
	case erlzk:get_data(PID, "/users/" ++ User ++ "/online") of
		{error, _} ->
			false;
		{ok,{R,_}} ->
			string:equal(binary_to_list(R), "true")
	end.

get_groups(U) -> rpc({groups_user, U}).
get_groups(User, PID) ->
	GroupPath = "/users/" ++ User ++ "/groups",
	case erlzk:get_children(PID,GroupPath) of 
		{ok, L} ->
			L;
		_ ->
			[]
	end.

% ---------------------------------------------------
% server
% ---------------------------------------------------


get_group_location(N)-> rpc({group_users, N}).
get_group_location(Name, PID) ->
	GroupPath = "/groups/" ++ Name ++ "/users",
	R = erlzk:get_children(PID,GroupPath),
	case R of
		{ok, L} ->
			{ok, get_user_location(L, #{}, PID), length(L)};
		{error, no_node} ->
			no_group;
		_ ->
			error
	end.

get_user_location([], Map, _) -> Map;
get_user_location(Users, Map, PID) ->	
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
									get_user_location(Tail,maps:put(binary_to_list(LOC),[User],Map),PID);
								true ->
									L = lists:merge(maps:get(binary_to_list(LOC), Map), [User]),
									get_user_location(Tail,maps:put(binary_to_list(LOC),L,Map),PID)
							end
					end;
				"false" ->
					case maps:is_key(offline, Map) of
						false ->
							get_user_location(Tail,maps:put(offline,[User],Map),PID);
						true ->
							L = lists:merge(maps:get(offline, Map), [User]),
							get_user_location(Tail,maps:put(offline,L,Map),PID)
					end
			end

 	end.

get_tracker_list() -> rpc({tracker_list}).
get_tracker_list(PID) ->
	TrackersPath = "/trackers",
	case erlzk:get_children(PID,TrackersPath) of
		{error, no_node} ->
			io:format("> tracker list empty\n");
		{ok, L} ->
			{ok,L};
		_ ->
			error
	end.

get_frontsv(ID) -> rpc({front_sv, ID}).
get_frontsv(ID, PID) ->
	FEpath = "/front-servers/" ++ ID, 
	case erlzk:get_data(PID, FEpath) of
		{error, _} ->
		 	error;
		{ok,{RP,_}} ->
		 	RP
	 	end.


get_tracker(ID) -> rpc({tracker, ID}).
get_tracker(ID, PID) ->
	case erlzk:get_data(PID, "/trackers/" ++ ID) of
		{error, _} ->
		 	error;
		{ok,{RP,_}} ->
		 	binary_to_list(RP)
	 	end.



register_current(ID, IP) -> rpc({register_current, ID, IP}).
register_current(ID, IP, PID) ->
	case erlzk:create(PID, "/front-servers/" ++ ID, list_to_binary(IP), ephemeral) of
		{ok, _} -> ok;
		{error, _} -> error
	end.





