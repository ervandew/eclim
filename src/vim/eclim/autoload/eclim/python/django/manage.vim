" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/python/django.html
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

" Global Variables {{{
if !exists('g:EclimPythonInterpreter')
  let g:EclimPythonInterpreter = 'python'
endif
if !exists('g:EclimDjangoAdmin')
  let g:EclimDjangoAdmin = 'django-admin.py'
endif
" }}}

" Script Variables {{{
" reset and runfcgi removed?
" test requires django > 0.95
let s:manage_actions = [
    \ 'adminindex',
    \ 'createcachetable',
    \ 'dbshell',
    \ 'diffsettings',
    \ 'inspectdb',
    \ 'install',
    \ 'reset',
    \ 'runfcgi',
    \ 'runserver',
    \ 'shell',
    \ 'sql',
    \ 'sqlall',
    \ 'sqlclear',
    \ 'sqlindexes',
    \ 'sqlinitialdata',
    \ 'sqlreset',
    \ 'sqlsequencereset',
    \ 'startapp',
    \ 'startproject',
    \ 'syncdb',
    \ 'test',
    \ 'validate',
  \ ]

let s:app_actions = [
    \ 'adminindex',
    \ 'install',
    \ 'reset',
    \ 'sql',
    \ 'sqlall',
    \ 'sqlclear',
    \ 'sqlindexes',
    \ 'sqlinitialdata',
    \ 'sqlreset',
    \ 'sqlsequencereset'
  \ ]

let s:output_actions = [
    \ 'adminindex',
    \ 'diffsettings',
    \ 'inspectdb',
    \ 'sql',
    \ 'sqlall',
    \ 'sqlclear',
    \ 'sqlindexes',
    \ 'sqlinitialdata',
    \ 'sqlreset',
    \ 'sqlsequencereset'
  \ ]

let s:sql_dialects = {
    \ 'ado_mysql': 'mysql.vim',
    \ 'mysql': 'mysql.vim',
    \ 'mysql_old': 'mysql.vim',
    \ 'postgresql': 'plsql.vim',
    \ 'postgresql_psycopg2': 'plsql.vim',
    \ 'sqlite3': 'sql.vim',
  \ }

" }}}

" DjangoManage(args) {{{
function! eclim#python#django#manage#Manage(args)
  let cwd = getcwd()
  if a:args =~ '^startproject\s'
    if !executable(g:EclimDjangoAdmin)
      call eclim#util#EchoError(
        \ g:EclimDjangoAdmin . ' is either not executable or not in your path.')
      return
    endif
    let command = g:EclimDjangoAdmin
  else
    if !executable(g:EclimPythonInterpreter)
      call eclim#util#EchoError(
        \ g:EclimPythonInterpreter . ' is either not executable or not in your path.')
      return
    endif
    let command = g:EclimPythonInterpreter . ' manage.py'

    " change to project directory before executing manage script.
    let path = eclim#python#django#util#GetProjectPath()
    if path == ''
      call eclim#util#EchoError('Current file not in a django project.')
      return
    endif
    exec 'cd ' . escape(path, ' ')
  endif

  try
    let action = substitute(a:args, '^\(.\{-}\)\(\s.*\|$\)', '\1', '')
    if eclim#util#ListContains(s:output_actions, action)
      let result = eclim#util#System(command . ' ' . a:args)
      if v:shell_error
        if result =~ '^Error:'
          let error = substitute(result, '^\(.\{-}\)\n.*', '\1', '')
        else
          let error = 'Error: ' .
            \ substitute(result, '.*\n\s*\(' . action . '\s.\{-}\)\n.*', '\1', '')
        endif
        call eclim#util#EchoError(error)
      else
        let engine = eclim#python#django#util#GetSqlEngine(path)
        let dialect = has_key(s:sql_dialects, engine) ? s:sql_dialects[engine] : 'plsql'

        let filename = expand('%')
        let name = '__' . action . '__'
        call eclim#util#TempWindow(name, split(result, '\n'))
        if action =~ '^sql'
          set filetype=sql
          if exists('b:current_syntax') && dialect !~ b:current_syntax
            exec 'SQLSetType ' . dialect
          endif
        elseif action == 'adminindex'
          set filetype=html
        elseif action =~ '\(diffsettings\|inspectdb\)'
        endif
        set nomodified
        " Store filename so that plugins can use it if necessary.
        let b:filename = filename

        augroup temp_window
          autocmd! BufWinLeave <buffer>
          call eclim#util#GoToBufferWindowRegister(filename)
        augroup END
      endif
    else
      exec '!' . command . ' ' . a:args
    endif
  finally
    " change back to original directory if necessary.
    exec 'cd ' . escape(cwd, ' ')
  endtry
endfunction " }}}

" CommandCompleteManage(argLead, cmdLine, cursorPos) {{{
function! eclim#python#django#manage#CommandCompleteManage(argLead, cmdLine, cursorPos)
  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let args = eclim#util#ParseCmdLine(cmdLine)
  let argLead = cmdLine =~ '\s$' ? '' : args[len(args) - 1]

  if cmdLine =~ '^' . args[0] . '\s*' . escape(argLead, '~.\') . '$'
    let actions = copy(s:manage_actions)
    if cmdLine !~ '\s$'
      call filter(actions, 'v:val =~ "^' . argLead . '"')
    endif
    return actions
  endif

  " complete app names if action support one
  let action = args[1]
  if eclim#util#ListContains(s:app_actions, action)
    let apps = eclim#python#django#util#GetProjectApps(
      \ eclim#python#django#util#GetProjectPath())
    if cmdLine !~ '\s$'
      call filter(apps, 'v:val =~ "^' . argLead . '"')
    endif
    return apps
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
