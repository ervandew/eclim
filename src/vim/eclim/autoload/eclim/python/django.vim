" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/python/django/manage.html
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

" }}}

" DjangoManage(args) {{{
function! eclim#python#django#Manage (args)
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
    let b:savedir = getcwd()

    " change to project directory before executing manage script.
    let path = eclim#python#django#GetProjectPath()
    if path != ''
      exec 'lcd ' . path
    else
      call eclim#util#EchoError('Current file not in a django project.')
      return
    endif
  endif

  try
    let action = substitute(a:args, '^\(.\{-}\)\(\s.*\|$\)', '\1', '')
    if eclim#util#ListContains(s:output_actions, action)
      let result = system(command . ' ' . a:args)
      if v:shell_error
        if result =~ '^Error:'
          let error = substitute(result, '^\(.\{-}\)\n.*', '\1', '')
        else
          let error = 'Error: ' .
            \ substitute(result, '.*\n\s*\(' . action . '\s.\{-}\)\n.*', '\1', '')
        endif
        call eclim#util#EchoError(error)
      else
        let filename = expand('%')
        let name = '__' . action . '__'
        call eclim#util#TempWindow(name, split(result, '\n'), 0)
        if action =~ '^sql'
          set filetype=sql
        elseif action == 'adminindex'
          set filetype=html
        elseif action =~ '\(diffsettings\|inspectdb\)'
        endif
        set nomodified
        " Store filename so that plugins can use it if necessary.
        let b:filename = filename

        augroup temp_window
          autocmd! BufUnload <buffer>
          call eclim#util#GoToBufferWindowRegister(filename)
        augroup END
      endif
    else
      exec '!' . command . ' ' . a:args
    endif
  finally
    " change back to original directory if necessary.
    if exists('b:savedir')
      exec 'lcd ' . b:savedir
      unlet b:savedir
    endif
  endtry
endfunction " }}}

" GetProjectPath ([path]) {{{
function eclim#python#django#GetProjectPath(...)
  let path = len(a:000) > 0 ? a:000[0] : escape(expand('%:p:h'), ' ')
  let dir = findfile("manage.py", path . ';')
  if dir != ''
    let dir = fnamemodify(dir, ':p:h')
    " secondary check on the dir, if settings.py exists, then probably the
    " right dir, otherwise, search again from the parent.
    if !filereadable(dir . '/settings.py')
      return eclim#python#django#GetProjectPath(path . '/..')
    endif
  endif
  return dir
endfunction " }}}

" GetProjectApps (project_dir) {{{
" Gets a list of applications for the supplied project directory.
function eclim#python#django#GetProjectApps(project_dir)
  if a:project_dir != ''
    let apps = split(globpath(a:project_dir, '*/views.py'), '\n')
    call map(apps, "fnamemodify(v:val, ':p:h:t')")
    return apps
  endif
  return []
endfunction " }}}

" CommandCompleteManage(argLead, cmdLine, cursorPos) {{{
function! eclim#python#django#CommandCompleteManage (argLead, cmdLine, cursorPos)
  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let args = eclim#util#ParseArgs(cmdLine)
  let argLead = len(args) > 1 ? args[len(args) - 1] : ""

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
    let apps = eclim#python#django#GetProjectApps(eclim#python#django#GetProjectPath())
    if cmdLine !~ '\s$'
      call filter(apps, 'v:val =~ "^' . argLead . '"')
    endif
    return apps
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
