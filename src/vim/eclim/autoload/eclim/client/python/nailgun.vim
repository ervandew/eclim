" Author:  Anton Sharonov
" Author:  Eric Van Dewoestine
"
" Description: {{{
"
" License:
"
" Copyright (C) 2005 - 2010  Eric Van Dewoestine
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

" Script Variables {{{
  let s:python_dir = expand("<sfile>:h")
" }}}

" Execute(port, command) {{{
" Sends to the eclimd server command, supplied as argument string.
" Returns server's respond.
function! eclim#client#python#nailgun#Execute(port, command)
  call s:InitClient(a:port)
  let result_viml = ""
  let retcode = 0

  let begin = localtime()
  try
python << PYTHONEOF
command = vim.eval('a:command')
(retcode, result) = client.send(command)
vim.command('let retcode = %i' % retcode)
vim.command("let result = '%s'" % result.replace("'", "''"))
PYTHONEOF
  finally
    call eclim#util#EchoTrace(
      \ 'nailgun.py (port: ' . a:port . '): ' . a:command, localtime() - begin)
  endtry

  return [retcode, result]
endfunction " }}}

" Reconnect(port) {{{
" Does unconditional reconnect of the python_if
" (useful to manual recover from errors in the python_if)
function! eclim#client#python#nailgun#Reconnect(port)
  call s:InitClient(a:port)
python << PYTHONEOF
client.reconnect()
PYTHONEOF
endfunction " }}}

" SetKeepAlive(port, value) {{{
" Updates the in runtime value of the keepAlive flag.
function! eclim#client#python#nailgun#SetKeepAlive(port, value)
  call s:InitClient(a:port)
python << PYTHONEOF
client.keepAlive = int(vim.eval('a:value'))
PYTHONEOF
endfunction " }}}

" GetKeepAlive(port) {{{
" Retrieves the value of the keepAlive flag.
function! eclim#client#python#nailgun#GetKeepAlive(port)
  call s:InitClient(a:port)
  let result = 0
python << PYTHONEOF
vim.command("let result = %s" % client.keepAlive)
PYTHONEOF
  return result
endfunction " }}}

" GetReconnectCounter(port) {{{
" Retrieves the value of the reconnect counter.
function! eclim#client#python#nailgun#GetReconnectCounter(port)
  call s:InitClient(a:port)
  let result = 0
python << PYTHONEOF
vim.command("let result = %d" % client.reconnectCounter)
PYTHONEOF
  return result
endfunction " }}}

" s:InitClient(port) {{{
" Initializes the python interface to the nailgun server.
function! s:InitClient(port)
python << PYTHONEOF
if not vars().has_key('clients'):
  import sys, vim
  sys.path.append(vim.eval('s:python_dir'))
  import nailgun

  clients = {}

port = int(vim.eval('a:port'))
if not clients.has_key(port):
  clients[port] = nailgun.Nailgun(
    port=port,
    keepAlive=vim.eval('g:EclimNailgunKeepAlive'),
  )
client = clients[port]
PYTHONEOF
endfunction " }}}

" vim:ft=vim:fdm=marker
