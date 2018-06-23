-module(data).
-include_lib("stdlib/include/qlc.hrl"). 

% o primeiro atributo do record e a Key
-record(connections, {user, pid}).

-export ([start/0, register_pid/2, get_pid/1, delete_pid/1]).

%%====================================================================
%% start mnesia
%%====================================================================

start() ->
	mnesia:create_schema([node()]),
	mnesia:start(),
	mnesia:create_table(connections, [{attributes, record_info(fields, connections)}]),	
	mnesia:wait_for_tables([connections], 5000),
	io:format("> db started\n").

%%====================================================================
%% API -- connections
%%====================================================================

register_pid(Username, Pid) ->
	F = fun() ->
		mnesia:write(#connections{user = Username,
				   				  pid = Pid}),
		ok
	end,
	mnesia:activity(transaction, F).

delete_pid(Username) ->
	Delete=#connections{user = Username, _ = '_'},
	Fun = fun() ->
              List = mnesia:match_object(Delete),
              lists:foreach(fun(X) ->
                                    mnesia:delete_object(X)
                            end, List)
    end,
	mnesia:transaction(Fun).

get_pid(Username) ->
	F = fun() ->
		case mnesia:wread({connections, Username}) of
			[#connections{pid = Pid}] ->
				{ok, Pid};
			_ -> 
				io:format("> Unexistent user.\n"),
				{error, undefined}
		end
	end,
	mnesia:activity(transaction, F).
