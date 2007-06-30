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
if !exists('g:EclimDjangoFindAction')
  let g:EclimDjangoFindAction = 'split'
endif
if !exists('g:EclimDjangoStaticPaths')
  let g:EclimDjangoStaticPaths = []
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
        let engine = eclim#python#django#GetSqlEngine(path)
        let dialect = has_key(s:sql_dialects, engine) ? s:sql_dialects[engine] : 'plsql'

        let filename = expand('%')
        let name = '__' . action . '__'
        call eclim#util#TempWindow(name, split(result, '\n'), 0)
        if action =~ '^sql'
          set filetype=sql
          exec 'SQLSetType ' . dialect
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

" GetLoadList(project_dir) {{{
" Returns a list of tag/filter files loaded by the current template.
function eclim#python#django#GetLoadList (project_dir)
  let line = line('.')
  let col = col('.')

  call cursor(1, 1)
  let loaded = []
  while search('{%\s*load\s', 'cW')
    call add(loaded, substitute(getline('.'), '.*{%\s*load\s\+\(\w\+\)\s*%}.*', '\1', ''))
    call cursor(line('.') + 1, 1)
  endwhile
  call cursor(line, col)

  let file_names = []
  for load in loaded
    let file = findfile(load . '.py', a:project_dir . '*/templatetags/')
    if file != ''
      call add(file_names, file)
    endif
  endfor

  return file_names
endfunction " }}}

" GetProjectPath([path]) {{{
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

" GetProjectApps(project_dir) {{{
" Gets a list of applications for the supplied project directory.
function eclim#python#django#GetProjectApps(project_dir)
  if a:project_dir != ''
    let apps = split(globpath(a:project_dir, '*/views.py'), '\n')
    call map(apps, "fnamemodify(v:val, ':p:h:t')")
    return apps
  endif
  return []
endfunction " }}}

" GetSqlEngine(project_dir) {{{
" Gets the configured sql engine for the project at the supplied project directory.
function eclim#python#django#GetSqlEngine (project_dir)
  let engine = 'postgresql'
  " determine user's sql dialect
  let restore = winrestcmd()
  try
    exec 'silent split ' . a:project_dir . '/settings.py'
    if search('DATABASE_ENGINE')
      let engine = substitute(getline('.'),
        \ "^.*DATABASE_ENGINE\\s*=\\s*['\"]\\(\\w\\+\\)['\"].*$", '\1', '')
    endif
    close
  finally
    silent exec restore
  endtry
  return engine
endfunction " }}}

" FindFilterOrTag(project_dir, element, type) {{{
" Finds and opens the supplied filter or tag definition.
function eclim#python#django#FindFilterOrTag (project_dir, element, type)
  let loaded = eclim#python#django#GetLoadList(a:project_dir)
  let cmd = 'lvimgrep /\<def\s\+' . a:element . '\>/j '
  for file in loaded
    let cmd .= ' ' . file
  endfor

  silent exec cmd

  let results = getloclist(0)
  if len(results) > 0
    call eclim#util#GoToBufferWindowOrOpen(
      \ bufname(results[0].bufnr), g:EclimDjangoFindAction)
    lfirst
    return
  endif
  call eclim#util#EchoError(
    \ 'Unable to find the definition for tag/file "' . a:element . '"')
endfunction " }}}

" FindFilterTagFile(project_dir, file) {{{
" Finds and opens the supplied tag/file definition file.
function eclim#python#django#FindFilterTagFile (project_dir, file)
  let file = findfile(a:file . '.py', a:project_dir . '*/templatetags/')
  if file != ''
    call eclim#util#GoToBufferWindowOrOpen(file, g:EclimDjangoFindAction)
    return
  endif
  call eclim#util#EchoError('Could not find tag/filter file "' . a:file . '.py"')
endfunction " }}}

" FindStaticFile(project_dir, file) {{{
" Finds and opens the supplied static file name.
function eclim#python#django#FindStaticFile (project_dir, file)
  if !len(g:EclimDjangoStaticPaths)
    call eclim#util#EchoWarning(
      \ 'Attemping to find static file but your g:EclimDjangoStaticPaths is not set.')
    return
  endif

  for path in g:EclimDjangoStaticPaths
    if path !~ '^\(/\|\w:\)'
      let path = a:project_dir . '/' . path
    endif
    let file = findfile(a:file, path)
    if file != ''
      call eclim#util#GoToBufferWindowOrOpen(file, g:EclimDjangoFindAction)
      return
    endif
  endfor
  call eclim#util#EchoError('Could not find the static file "' . a:file . '"')
endfunction " }}}

" FindTemplate(project_dir, template) {{{
" Finds and opens the supplied template definition.
function eclim#python#django#FindTemplate (project_dir, template)
  let file = findfile(a:template, a:project_dir . '/*/templates')
  if file != ''
    call eclim#util#GoToBufferWindowOrOpen(file, g:EclimDjangoFindAction)
    return
  endif
  call eclim#util#EchoError('Could not find the template "' . a:template . '"')
endfunction " }}}

" FindView(project_dir, template) {{{
" Finds and opens the supplied view.
function eclim#python#django#FindView (project_dir, view)
  let view = a:view
  let function = ''

  " basic check to see if on a url pattern instead of the view.
  if view =~ '[?(*^$]'
    call eclim#util#EchoError(
      \ 'String under the curser does not appear to be a view: "' . view . '"')
    return
  endif

  if getline('.') !~ "\\(include\\|patterns\\)\\s*(\\s*['\"]" . view
    " see if a view prefix was defined.
    let result = search('patterns\s*(', 'bnW')
    if result
      let prefix = substitute(
        \ getline(result), ".*patterns\\s*(\\s*['\"]\\(.\\{-}\\)['\"].*", '\1', '')
      if prefix != ''
        let view = prefix . '.' . view
      endif
    endif

    let function = split(view, '\.')[-1]
    let view = join(split(view, '\.')[0:-2], '.')
  endif

  let view = join(split(substitute(view, '\.', '/', 'g') . '.py', '/')[1:], '/')
  let file = findfile(view, a:project_dir)
  if file != ''
    call eclim#util#GoToBufferWindowOrOpen(file, g:EclimDjangoFindAction)
    if function != ''
      call search('def\s\+' . function . '\>', 'cw')
    endif
    return
  endif
  call eclim#util#EchoError('Could not find the view "' . view . '"')
endfunction " }}}

" TemplateFind() {{{
" Find the template, tag, or filter under the cursor.
function eclim#python#django#TemplateFind ()
  let project_dir = eclim#python#django#GetProjectPath()
  if project_dir == ''
    call eclim#util#EchoError(
      \ 'Unable to locate django project path with manage.py and settings.py')
    return
  endif

  let line = getline('.')
  let element = eclim#util#GrabUri()
  if element =~ '|'
    let element = substitute(element, '.\{-}|\(\w*\).*', '\1', 'g')
    call eclim#python#django#FindFilterOrTag(project_dir, element, 'filter')
  elseif line =~ '{%\s*' . element . '\>'
    call eclim#python#django#FindFilterOrTag(project_dir, element, 'tag')
  elseif line =~ '{%\s*load\s\+' . element . '\>'
    call eclim#python#django#FindFilterTagFile(project_dir, element)
  elseif line =~ "{%\\s*\\(extends\\|include\\)\\s\\+['\"]" . element . "['\"]"
    call eclim#python#django#FindTemplate(project_dir, element)
  elseif line =~ "\\(src\\|href\\)\\s*=\\s*['\"]\\?\\s*" . element
    let element = substitute(element, '^/', '', '')
    let element = substitute(element, '?.*', '', '')
    call eclim#python#django#FindStaticFile(project_dir, element)
  else
    call eclim#util#EchoError(
      \ 'Element under the cursor does not appear to be a ' .
      \ 'valid tag, filter, or template reference.')
  endif
endfunction " }}}

" ContextFind() {{{
" Execute DjangoViewOpen, DjangoTemplateOpen, or PythonFindDefinition based on
" the context of the text under the cursor.
function! eclim#python#django#ContextFind ()
  if getline('.') =~ "['\"][^'\" ]*\\%" . col('.') . "c[^'\" ]*['\"]"
    if search('urlpatterns\s\+=\s\+patterns(', 'nw') &&
        \ eclim#util#GrabUri() !~ '\.html'
      DjangoViewOpen
    else
      DjangoTemplateOpen
    endif
  "else
  "  PythonFindDefinition
  endif
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
