" Author:  Eric Van Dewoestine
" Version: ${eclim.version}
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/taglist.html
"
" License:
"
" Copyright (c) 2005 - 2006
"
" Licensed under the Apache License, Version 2.0 (the "License");
" you may not use this file except in compliance with the License.
" You may obtain a copy of the License at
"
"      http://www.apache.org/licenses/LICENSE-2.0
"
" Unless required by applicable law or agreed to in writing, software
" distributed under the License is distributed on an "AS IS" BASIS,
" WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
" See the License for the specific language governing permissions and
" limitations under the License.
"
" }}}

" Global Variables {{{
if !exists("g:EclimTaglistEnabled")
  let g:EclimTaglistEnabled = 1
endif

" first check if user has taglist plugin, wants the eclim enhancement, and
" eclimd is running
if !exists("g:Tlist_Ctags_Cmd") || !g:EclimTaglistEnabled || !eclim#PingEclim(0)
  finish
endif

" set command for taglist.vim
let g:Tlist_Ctags_Cmd =
  \ eclim#GetEclimCommand() . ' -command taglist -c "' . g:Tlist_Ctags_Cmd . '"'

" }}}

" Taglist Settings {{{
if !exists("g:tlist_ant_settings")
  let g:tlist_ant_settings='ant;p:project;i:import;r:property;t:target'
endif

if !exists("g:tlist_commonsvalidator_settings")
  let g:tlist_commonsvalidator_settings='commonsvalidator;c:constant;f:form'
endif

if !exists("g:tlist_dtd_settings")
  let g:tlist_dtd_settings='dtd;e:element'
endif

if !exists("g:tlist_forrestdocument_settings")
  let g:tlist_forrestdocument_settings='forrestdocument;s:section'
endif

if !exists("g:tlist_forreststatus_settings")
  let g:tlist_forreststatus_settings='forreststatus;t:todo;r:release'
endif

if !exists("g:tlist_hibernate_settings")
  let g:tlist_hibernate_settings='hibernate;t:typedef;f:filter-def;i:import;q:query;s:sql-query;c:class;j:joined-subclass'
endif

if !exists("g:tlist_junitresult_settings")
  let g:tlist_junitresult_settings='junitresult;t:testcase;o:output'
endif

if !exists("g:tlist_jproperties_settings")
  let g:tlist_jproperties_settings='jproperties;p:property'
endif

if !exists("g:tlist_spring_settings")
  let g:tlist_spring_settings='spring;i:import;a:alias;b:bean'
endif

if !exists("g:tlist_sql_settings")
  let g:tlist_sql_settings='sql;g:group / role;r:role;u:user;m:user;p:tablespace;z:tablespace;s:schema;t:table;v:view;q:sequence;f:function'
endif

if !exists("g:tlist_webxml_settings")
  let g:tlist_webxml_settings='webxml;p:context-param;f:filter;i:filter-mapping;l:listener;s:servlet;v:servlet-mapping'
endif
" }}}

" vim:ft=vim:fdm=marker
