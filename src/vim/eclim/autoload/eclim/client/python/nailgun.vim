" Author:  Anton Sharonov
" Author:  Eric Van Dewoestine
"
" Description: {{{
"
" License:
"
" Copyright (C) 2005 - 2008  Eric Van Dewoestine
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

" Execute(command) {{{
" Sends to the eclimd server command, supplied as argument string.
" Returns server's respond.
function! eclim#client#python#nailgun#Execute(command)
  call s:InitClient()
  let result_viml = ""
  let retcode = 0

  call eclim#util#EchoTrace('nailgun.py: ' . a:command)

python << PYTHONEOF
command = vim.eval('a:command')
(retcode, result) = client.send(command)
vim.command('let retcode = %i' % retcode)
vim.command("let result = '%s'" % result.replace("'", "''"))
PYTHONEOF

  return [retcode, result]
endfunction " }}}

" Reconnect() {{{
" Does unconditional reconnect of the python_if
" (useful to manual recover from errors in the python_if)
function! eclim#client#python#nailgun#Reconnect()
  call s:InitClient()
python << PYTHONEOF
client.reconnect()
PYTHONEOF
endfunction " }}}

" SetKeepAlive(value) {{{
" Updates the in runtime value of the keepAlive flag.
function! eclim#client#python#nailgun#SetKeepAlive(value)
  call s:InitClient()
python << PYTHONEOF
client.keepAlive = int(vim.eval('a:value'))
PYTHONEOF
endfunction " }}}

" GetKeepAlive() {{{
" Retrieves the value of the keepAlive flag.
function! eclim#client#python#nailgun#GetKeepAlive()
  call s:InitClient()
  let result = 0
python << PYTHONEOF
vim.command("let result = %s" % client.keepAlive)
PYTHONEOF
  return result
endfunction " }}}

" GetReconnectCounter() {{{
" Retrieves the value of the reconnect counter.
function! eclim#client#python#nailgun#GetReconnectCounter()
  call s:InitClient()
  let result = 0
python << PYTHONEOF
vim.command("let result = %d" % client.reconnectCounter)
PYTHONEOF
  return result
endfunction " }}}

" s:InitClient() {{{
" Initializes the python interface to the nailgun server.
function! s:InitClient()
  if exists('s:client_initialized') && s:client_initialized
    return
  endif

  let s:client_initialized = 1

python << PYTHONEOF
import sys, vim
sys.path.append(vim.eval('s:python_dir'))
import nailgun

client = nailgun.Nailgun(
  keepAlive=vim.eval('g:EclimNailgunKeepAlive'),
  vimFiles=vim.eval('g:EclimBaseDir'),
)
PYTHONEOF
endfunction " }}}

" vim:ft=vim:fdm=marker
