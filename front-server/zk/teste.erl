-module(teste).
-export([test/0]).

test() ->
	L = [1,3,4,5,6],
	lists:map(fun(X) -> func(X) end,L).

func(X) ->
	X+2.