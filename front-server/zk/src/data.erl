-module(data).
-include_lib("stdlib/include/qlc.hrl"). 

% o primeiro atributo do record e a Key
-record(connections, {user, pid}).
-record(counter, {id, current}).

-export ([init/0, start/0, register_pid/2, get_pid/1, delete_pid/1, startCounter/0, getCurrentCounter/0, incrementAndGet/0]).

%%====================================================================
%% start mnesia
%%====================================================================

start() ->
	mnesia:create_schema([node()]),
	mnesia:start(),
	mnesia:create_table(connections, [{attributes, record_info(fields, connections)}]),	
	mnesia:wait_for_tables([connections,counter], 5000),
	io:format("> db started\n").

init() ->
	mnesia:create_table(counter, [{attributes, record_info(fields, counter)}, {disc_copies, [node()]}]),
	start().


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

%%====================================================================
%% API -- counter
%%====================================================================

startCounter() ->
 	F = fun() ->
		mnesia:write(#counter {id = counter,
				   			   current = 0}),
		ok
	end,
	mnesia:activity(transaction, F).


getCurrentCounter() ->
	F = fun() ->
		case mnesia:wread({counter, counter}) of
			[#counter{current = Counter}] ->
				{ok, Counter};
			_ -> 
				error
		end
	end,
	mnesia:activity(transaction, F).

incrementAndGet() ->
	F = fun() ->
		X = mnesia:dirty_update_counter({counter, counter}, 1),
		case X of
			{aborted, _} ->
				error;
			_ ->
				{ok,X}
		end
	end,
	mnesia:activity(transaction, F).

%% ========
%% FILES
%% ========

writeFile(Name, File) ->
	file:write_file("./torrents/" ++ Name, File).
