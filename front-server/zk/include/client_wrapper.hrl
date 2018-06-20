%% -*- coding: utf-8 -*-
%% Automatically generated, do not edit
%% Generated by gpb_compile version 4.1.1

-ifndef(client_wrapper).
-define(client_wrapper, true).

-define(client_wrapper_gpb_version, "4.1.1").

-ifndef('GROUPUSERS_PB_H').
-define('GROUPUSERS_PB_H', true).
-record('GroupUsers',
        {groupUsers = <<>>      :: iodata() | undefined % = 1
        }).
-endif.

-ifndef('JOINGROUP_PB_H').
-define('JOINGROUP_PB_H', true).
-record('JoinGroup',
        {group = <<>>           :: iodata() | undefined % = 1
        }).
-endif.

-ifndef('REGISTER_PB_H').
-define('REGISTER_PB_H', true).
-record('Register',
        {username = <<>>        :: iodata() | undefined, % = 1
         password = <<>>        :: iodata() | undefined, % = 2
         name = <<>>            :: iodata() | undefined % = 3
        }).
-endif.

-ifndef('ONLINEUSERS_PB_H').
-define('ONLINEUSERS_PB_H', true).
-record('OnlineUsers',
        {onlineUsers = <<>>     :: iodata() | undefined % = 1
        }).
-endif.

-ifndef('TORRENTWRAPPER_PB_H').
-define('TORRENTWRAPPER_PB_H', true).
-record('TorrentWrapper',
        {group = <<>>           :: iodata() | undefined, % = 1
         content = <<>>         :: binary() | undefined, % = 2
         id = <<>>              :: iodata() | undefined % = 3
        }).
-endif.

-ifndef('CREATEGROUP_PB_H').
-define('CREATEGROUP_PB_H', true).
-record('CreateGroup',
        {group = <<>>           :: iodata() | undefined % = 1
        }).
-endif.

-ifndef('RESPONSE_PB_H').
-define('RESPONSE_PB_H', true).
-record('Response',
        {rep = false            :: boolean() | 0 | 1 | undefined % = 1
        }).
-endif.

-ifndef('LOGIN_PB_H').
-define('LOGIN_PB_H', true).
-record('Login',
        {username = <<>>        :: iodata() | undefined, % = 1
         password = <<>>        :: iodata() | undefined % = 2
        }).
-endif.

-ifndef('CLIENTMESSAGE_PB_H').
-define('CLIENTMESSAGE_PB_H', true).
-record('ClientMessage',
        {msg                    :: {login, #'Login'{}} | {register, #'Register'{}} | {response, #'Response'{}} | {createGroup, #'CreateGroup'{}} | {joinGroup, #'JoinGroup'{}} | {torrentWrapper, #'TorrentWrapper'{}} | {onlineUsers, #'OnlineUsers'{}} | {groupUsers, #'GroupUsers'{}} | undefined % oneof
        }).
-endif.

-endif.
