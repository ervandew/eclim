" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/taglist.html
"
" License:
"
" Copyright (C) 2005 - 2009  Eric Van Dewoestine
"
" This program is free software: you can redistribute it and/or modify
" it under the terms of the GNU General Public License as published by
" the Free Software Foundation, either version 3 of the License, or
" (at your option) any later version.
"
" This program is distributed in the hope that it will be useful,
" but WITHOUT ANY WARRANTY; without even the implied warranty of
" MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
" GNU General Public License for more details.
"
" You should have received a copy of the GNU General Public License
" along with this program.  If not, see <http://www.gnu.org/licenses/>.
"
" }}}

if exists('g:taglisttoo_loaded') ||
   \ (exists('g:taglisttoo_disabled') && g:taglisttoo_disabled)
  finish
endif
let g:taglisttoo_loaded = 1

" Global Variables {{{

if !exists("g:EclimTaglistEnabled")
  let g:EclimTaglistEnabled = 1
endif

if !exists('g:Tlist_Ctags_Cmd')
  if executable('exuberant-ctags')
    let g:Tlist_Ctags_Cmd = 'exuberant-ctags'
  elseif executable('ctags')
    let g:Tlist_Ctags_Cmd = 'ctags'
  elseif executable('ctags.exe')
    let g:Tlist_Ctags_Cmd = 'ctags.exe'
  elseif executable('tags')
    let g:Tlist_Ctags_Cmd = 'tags'
  endif
endif

" always set the taglist title since eclim references it in a few places.
if !exists('g:TagList_title')
  let g:TagList_title = "__Tag_List__"
endif

" no ctags found, no need to continue.
if !exists('g:Tlist_Ctags_Cmd')
  finish
endif

let eclimAvailable = eclim#EclimAvailable()

let g:Tlist_Ctags_Cmd_Ctags = g:Tlist_Ctags_Cmd
let g:Tlist_Ctags_Cmd_Eclim =
  \ eclim#client#nailgun#GetEclimCommand() .
  \ ' -command taglist -c "' . g:Tlist_Ctags_Cmd . '"'
" for windows, need to add a trailing quote to complete the command.
if g:Tlist_Ctags_Cmd_Eclim =~ '^"[a-zA-Z]:'
  let g:Tlist_Ctags_Cmd_Eclim = g:Tlist_Ctags_Cmd_Eclim . '"'
endif

" set eclim command for taglist if user wants it and eclim is running.
if g:EclimTaglistEnabled && eclimAvailable
  let g:Tlist_Ctags_Cmd = g:Tlist_Ctags_Cmd_Eclim
endif

" don't conflict with original taglist if that is what the user is using.
if !exists('loaded_taglist')
  " Automatically open the taglist window on Vim startup
  if !exists('g:Tlist_Auto_Open')
    let g:Tlist_Auto_Open = 0
  endif

  if g:Tlist_Auto_Open && !exists('g:Tlist_Temp_Disable')
    augroup taglisttoo_autoopen
      autocmd!
      autocmd VimEnter * nested call eclim#taglist#taglisttoo#AutoOpen()
    augroup END

    " Auto open on new tabs as well.
    if v:version >= 700
      autocmd BufWinEnter *
        \ if tabpagenr() > 1 &&
        \     !exists('t:Tlist_Auto_Opened') &&
        \     !exists('g:SessionLoad') |
        \   call eclim#taglist#taglisttoo#AutoOpen() |
        \   let t:Tlist_Auto_Opened = 1 |
        \ endif
    endif
  endif

  augroup taglisttoo_file_session
    autocmd!
    autocmd SessionLoadPost * call eclim#taglist#taglisttoo#Restore()
  augroup END
endif
" }}}

" Command Declarations {{{
if !exists(":Tlist") && !exists(":TlistToo")
  command TlistToo :call eclim#taglist#taglisttoo#Taglist()
endif
" }}}

" Eclim groovy enhanced settings for taglist or taglisttoo {{{
" taglist.vim settings
if eclimAvailable
  if !exists(':TlistToo')
    if !exists("g:tlist_ant_settings")
      let g:tlist_ant_settings = 'ant;p:project;i:import;r:property;t:target'
    endif

    if !exists("g:tlist_commonsvalidator_settings")
      let g:tlist_commonsvalidator_settings = 'commonsvalidator;c:constant;f:form'
    endif

    if !exists("g:tlist_dtd_settings")
      let g:tlist_dtd_settings = 'dtd;e:element'
    endif

    if !exists("g:tlist_eclimhelp_settings")
      let g:tlist_eclimhelp_settings = 'eclimhelp;s:section;a:anchor'
    endif

    if !exists("g:tlist_forrestdocument_settings")
      let g:tlist_forrestdocument_settings = 'forrestdocument;s:section'
    endif

    if !exists("g:tlist_forreststatus_settings")
      let g:tlist_forreststatus_settings = 'forreststatus;t:todo;r:release'
    endif

    if !exists("g:tlist_gant_settings")
      let g:tlist_gant_settings = 'gant;t:target;f:function'
    endif

    "if !exists("g:tlist_help_settings")
    "  let g:tlist_help_settings = 'help;a:anchor'
    "endif

    if !exists("g:tlist_hibernate_settings")
      let g:tlist_hibernate_settings = 'hibernate;t:typedef;f:filter-def;i:import;q:query;s:sql-query;c:class;j:joined-subclass'
    endif

    if !exists("g:tlist_html_settings")
      let g:tlist_html_settings = 'html;a:anchor;i:id;f:function'
    endif

    if !exists("g:tlist_htmldjango_settings")
      let g:tlist_htmldjango_settings = 'htmldjango;a:anchor;i:id;f:function;b:block'
    endif

    if !exists("g:tlist_htmljinja_settings")
      let g:tlist_htmljinja_settings = 'htmljinja;a:anchor;i:id;f:function;m:macro;b:block'
    endif

    if !exists("g:tlist_javascript_settings")
      let g:tlist_javascript_settings = 'javascript;f:function'
    endif

    if !exists("g:tlist_junitresult_settings")
      let g:tlist_junitresult_settings = 'junitresult;t:testcase;o:output'
    endif

    if !exists("g:tlist_jproperties_settings")
      let g:tlist_jproperties_settings = 'jproperties;p:property'
    endif

    if !exists("g:tlist_log4j_settings")
      let g:tlist_log4j_settings = 'log4j;a:appender;c:category;l:logger;r:root'
    endif

    if !exists("g:tlist_php_settings")
      let g:tlist_php_settings = 'php;i:interface;c:class;f:function'
    endif

    if !exists("g:tlist_rst_settings")
      let g:tlist_rst_settings = 'rst;s:section;a:anchor'
    endif

    if !exists("g:tlist_spring_settings")
      let g:tlist_spring_settings = 'spring;i:import;a:alias;b:bean'
    endif

    if !exists("g:tlist_sql_settings")
      let g:tlist_sql_settings = 'sql;g:group / role;r:role;u:user;m:user;p:tablespace;z:tablespace;s:schema;t:table;v:view;q:sequence;x:trigger;f:function;c:procedure'
    endif

    if !exists("g:tlist_tld_settings")
      let g:tlist_tld_settings = 'tld;t:tag'
    endif

    if !exists("g:tlist_webxml_settings")
      let g:tlist_webxml_settings = 'webxml;p:context-param;f:filter;i:filter-mapping;l:listener;s:servlet;v:servlet-mapping'
    endif

    if !exists("g:tlist_wsdl_settings")
      let g:tlist_wsdl_settings = 'wsdl;t:types;m:messages;p:ports;b:bindings'
    endif

    if !exists("g:tlist_xsd_settings")
      let g:tlist_xsd_settings = 'xsd;e:elements;t:types'
    endif

  " taglisttoo.vim settings
  else
    if !exists("g:tlist_ant_settings")
      let g:tlist_ant_settings = {
          \ 'lang': 'ant', 'tags': {
            \ 'p': 'project',
            \ 'i': 'import',
            \ 'r': 'property',
            \ 't': 'target'
          \ }
        \ }
    endif

    if !exists("g:tlist_commonsvalidator_settings")
      let g:tlist_commonsvalidator_settings = {
          \ 'lang': 'commonsvalidator', 'tags': {'c': 'constant', 'f': 'form'}
        \ }
    endif

    if !exists("g:tlist_dtd_settings")
      let g:tlist_dtd_settings = {'lang': 'dtd', 'tags': {'e': 'element'}}
    endif

    if !exists("g:tlist_eclimhelp_settings")
      let g:tlist_eclimhelp_settings = {
          \ 'lang': 'eclimhelp', 'tags': {'s': 'section', 'a': 'anchor'}
        \ }
    endif

    if !exists("g:tlist_forrestdocument_settings")
      let g:tlist_forrestdocument_settings = {
          \ 'lang': 'forrestdocument', 'tags': {'s': 'section'}
        \ }
    endif

    if !exists("g:tlist_forreststatus_settings")
      let g:tlist_forreststatus_settings = {
          \ 'lang': 'forreststatus', 'tags': {'t': 'todo', 'r': 'release'}
        \ }
    endif

    if !exists("g:tlist_gant_settings")
      let g:tlist_gant_settings = {
          \ 'lang': 'gant', 'tags': {'t': 'target', 'f': 'function'}
        \ }
    endif

    "if !exists("g:tlist_help_settings")
    "  let g:tlist_help_settings = {'lang': 'help', 'tags': {'a': 'anchor'}}
    "endif

    if !exists("g:tlist_hibernate_settings")
      let g:tlist_hibernate_settings = {
          \ 'lang': 'hibernate', 'tags': {
            \ 't': 'typedef',
            \ 'f': 'filter-def',
            \ 'i': 'import',
            \ 'q': 'query',
            \ 's': 'sql-query',
            \ 'c': 'class',
            \ 'j': 'joined-subclass'
          \ }
        \ }
    endif

    if !exists("g:tlist_html_settings")
      let g:tlist_html_settings = {
          \ 'lang': 'html', 'tags': {'a': 'anchor', 'i': 'id', 'f': 'function'}
        \ }
    endif

    if !exists("g:tlist_htmldjango_settings")
      let g:tlist_htmldjango_settings = {
          \ 'lang': 'htmldjango',
          \ 'tags': {'a': 'anchor', 'i': 'id', 'f': 'function', 'b': 'block'}
        \ }
    endif

    if !exists("g:tlist_htmljinja_settings")
      let g:tlist_htmljinja_settings = {
          \ 'lang': 'htmljinja', 'tags': {
            \ 'a': 'anchor',
            \ 'i': 'id',
            \ 'f': 'function',
            \ 'm': 'macro',
            \ 'b': 'block'
          \ }
        \ }
    endif

    if !exists("g:tlist_javascript_settings")
      let g:tlist_javascript_settings = {
          \ 'lang': 'javascript', 'tags': {'o': 'object', 'f': 'function'}
        \ }
    endif

    if !exists("g:tlist_junitresult_settings")
      let g:tlist_junitresult_settings = {
          \ 'lang': 'junitresult', 'tags': {'t': 'testcase', 'o': 'output'}
        \ }
    endif

    if !exists("g:tlist_jproperties_settings")
      let g:tlist_jproperties_settings = {
          \ 'lang': 'jproperties', 'tags': {'p': 'property'}
        \ }
    endif

    if !exists("g:tlist_log4j_settings")
      let g:tlist_log4j_settings = {
          \ 'lang': 'log4j', 'tags': {
            \ 'a': 'appender',
            \ 'c': 'category',
            \ 'l': 'logger',
            \ 'r': 'root',
          \ }
        \ }
    endif

    if !exists("g:tlist_php_settings")
      let g:tlist_php_settings = {
          \ 'lang': 'php', 'tags': {
            \ 'i': 'interface',
            \ 'c': 'class',
            \ 'f': 'function',
          \ }
        \ }
    endif

    if !exists("g:tlist_rst_settings")
      let g:tlist_rst_settings = {
          \ 'lang': 'rst', 'tags': {'s': 'section', 'a': 'anchor'}
        \ }
    endif

    if !exists("g:tlist_spring_settings")
      let g:tlist_spring_settings = {
          \ 'lang': 'spring', 'tags': {'i': 'import', 'a': 'alias', 'b': 'bean'}
        \ }
    endif

    if !exists("g:tlist_sql_settings")
      let g:tlist_sql_settings = {
          \ 'lang': 'sql', 'tags': {
            \ 'g': 'group / role',
            \ 'r': 'role',
            \ 'u': 'user',
            \ 'm': 'user',
            \ 'p': 'tablespace',
            \ 'z': 'tablespace',
            \ 's': 'schema',
            \ 't': 'table',
            \ 'v': 'view',
            \ 'q': 'sequence',
            \ 'x': 'trigger',
            \ 'f': 'function',
            \ 'c': 'procedure'
          \ }
        \ }
    endif

    if !exists("g:tlist_tld_settings")
      let g:tlist_tld_settings = {'lang': 'tld', 'tags': {'t': 'tag'}}
    endif

    if !exists("g:tlist_webxml_settings")
      let g:tlist_webxml_settings = {
          \ 'lang': 'webxml', 'tags': {
            \ 'p': 'context-param',
            \ 'f': 'filter',
            \ 'i': 'filter-mapping',
            \ 'l': 'listener',
            \ 's': 'servlet',
            \ 'v': 'servlet-mapping'
          \ }
        \ }
    endif

    if !exists("g:tlist_wsdl_settings")
      let g:tlist_wsdl_settings = {
          \ 'lang': 'wsdl', 'tags': {
            \ 't': 'types',
            \ 'm': 'messages',
            \ 'p': 'ports',
            \ 'b': 'bindings'
          \ }
        \ }
    endif

    if !exists("g:tlist_xsd_settings")
      let g:tlist_xsd_settings = {
          \ 'lang': 'xsd', 'tags': {'e': 'elements', 't': 'types'}
        \ }
    endif
  endif
endif
" }}}

" vim:ft=vim:fdm=marker
