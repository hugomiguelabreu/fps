-module(zk).
-export([init/2,register/3, login/2, createGroup/2, joinGroup/2, setOnline/1, setOffline/1, getGroupUsers/1]).


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
		{{set_online, U}, From} ->
			V = setOnline(U, Pid),
			From ! {?MODULE, V},
    		loop(Pid);
    	{{set_offline, U}, From} ->
			V = setOffline(U, Pid),
			From ! {?MODULE, V},
    		loop(Pid);
    	{{group_users, N}, From} ->
			V = getGroupUsers(N, Pid),
			From ! {?MODULE, V},
    		loop(Pid)
	end.

rpc(Request) ->
	?MODULE ! {Request, self()},
	receive {?MODULE, Res} -> Res end.

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

setOnline(U) -> rpc({set_online, U}).
setOnline(Username,PID) ->
	UserPath = "/users/" ++ Username ++ "/online",
	case erlzk:set_data(PID,UserPath,list_to_binary("true")) of
		{error, _} ->
			error;
		_ ->
			ok
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
	case erlzk:get_children(PID,GroupPath) of
		{error, no_node} ->
			no_group;
		{ok, L} ->
			{ok,L};
		_ ->
			error
	end.













	


 





