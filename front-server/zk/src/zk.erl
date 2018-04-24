-module(zk).
-export([init/2,register/3, login/2]).


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

login(U,P) -> rpc({register,U,P}).
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













	


 






