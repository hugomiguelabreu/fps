%% -*- coding: utf-8 -*-
%% Automatically generated, do not edit
%% Generated by gpb_compile version 4.1.1

-ifndef(server_wrapper).
-define(server_wrapper, true).

-define(server_wrapper_gpb_version, "4.1.1").

-ifndef('TORRENTWRAPPER_PB_H').
-define('TORRENTWRAPPER_PB_H', true).
-record('TorrentWrapper',
        {id = <<>>              :: iodata() | undefined, % = 1
         users = []             :: [iodata()] | undefined, % = 2
         group = <<>>           :: iodata() | undefined, % = 3
         content = <<>>         :: binary() | undefined % = 4
        }).
-endif.

-endif.
