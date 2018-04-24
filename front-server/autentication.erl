
-module(autentication).

-include("account.hrl").
-include("response.hrl").

-export([startAutentication/0]).

% registo -> true
% login -> false

%%====================================================================
%% API
%%====================================================================

startAutentication() ->
	io:format("> autentication staterd\n"),
	{ok, LSock} = gen_tcp:listen(2000, [binary, {reuseaddr, true}, {packet, 1}]),1
	acceptor(LSock),
	receive
		kek ->
			io:format("Kek\n")
	end.

%%====================================================================
%% Internal functions
%%====================================================================

acceptor(LSock) ->
	{ok, Sock} = gen_tcp:accept(LSock),
	spawn(fun() -> acceptor(LSock) end),
	aut(Sock).

auth(Sock) ->
	ProtAcc =  account:decode_msg(recvData(Sock), 'Account'),
	Username = ProtAcc#'Account'.username,
	Password = ProtAcc#'Account'.password,
	Type = ProtAcc#'Account'.type,

	case Type of 
		true ->
			register(Username, Password, Name);
		false -> 
			login(Username, Password)
	end.


register(Username, Password, Name) -> 


login(Username, Password) ->






