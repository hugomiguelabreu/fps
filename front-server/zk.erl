-module(zk).
-include_lib("erlzk.hrl").
-export([init/2,register/3]).


init(Host, Port) ->
	application:start(crypto),
	application:start(erlzk),
	{ok, Pid} = erlzk:connect([{Host, Port}], 30000),
	io:format("> ZooKeeper connected.\n"),
	register(?MODULE,spawn(fun() -> loop(Pid) end)).

loop(Pid) -> 
	receive 
		{{register, U, P, N}, From} ->
			From ! register(Pid, U, P, N),
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

	case 	erlzk:create(Pid, UserPath, list_to_binary(Password)) of
		{error, _} -> error end,

	erlzk:create(Pid, NamePath, list_to_binary(Name)),
	erlzk:create(Pid, SvPath, list_to_binary("")),
	erlzk:create(Pid, OnPath, list_to_binary("false")).













	


 






