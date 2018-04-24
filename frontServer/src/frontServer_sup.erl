%%%-------------------------------------------------------------------
%% @doc frontServer top level supervisor.
%% @end
%%%-------------------------------------------------------------------

-module(frontServer_sup).

-behaviour(supervisor).

%% API
-export([start_link/0]).

%% Supervisor callbacks
-export([init/1]).

-define(SERVER, ?MODULE).

%%====================================================================
%% API functions
%%====================================================================

start_link() ->
    supervisor:start_link({local, ?SERVER}, ?MODULE, []).

%%====================================================================
%% Supervisor callbacks
%%====================================================================

%% Child :: {Id,StartFunc,Restart,Shutdown,Type,Modules}
% init([]) ->
%     {ok, { {one_for_all, 0, 1}, []} }.

init([]) ->
%~     Server = {char_case_server, {char_case_server, start_link, []},
%~               permanent, 2000, worker, [char_case_server]},
    Server = {frontServer_app, {frontServer_app, start},
              permanent, 2000, worker, [frontServer_app]},
    Children = [Server],
    RestartStrategy = {one_for_one, 0, 1},
    {ok, {RestartStrategy, Children}}.

%%====================================================================
%% Internal functions
%%====================================================================
