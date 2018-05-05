" Author:  Eric Van Dewoestine
"
" License: {{{
"
" Copyright (C) 2005 - 2018  Eric Van Dewoestine
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

function! eclim#python#django#util#GetDjangoPath() " {{{
  return eclim#python#django#util#PythonExec("import django; print(django.__path__[0])")
endfunction " }}}

function! eclim#python#django#util#GetProjectPath(...) " {{{
  " set by eclim#python#django#find#FindTemplate so that the user can
  " continue to navigate templates, etc when viewing templates outside of the
  " project's path (app templates).
  if exists('b:eclim_django_project')
    return b:eclim_django_project
  endif

  let path = len(a:000) > 0 ? a:000[0] : escape(expand('%:p:h'), ' ')
  let root = eclim#project#util#GetCurrentProjectRoot(path)
  if root == ''
    return ''
  endif

  let manage_paths = split(globpath(root, '**/manage.py'), '\n')

  let dirs = []
  for manage_path in manage_paths
    if manage_path == ''
      continue
    endif
    let dir = fnamemodify(manage_path, ":h")
    if filereadable(dir . '/settings.py')
      call add(dirs, dir)
    else
      " settings.py may be down one directory
      let settings_paths = filter(
        \ split(globpath(dir, '*/settings.py'), '\n'),
        \ 'v:val != ""')
      if len(settings_paths)
        let dirs += settings_paths
      endif
    endif
  endfor

  if len(dirs) == 0
    return ''
  endif

  let options = map(dirs, 'fnamemodify(v:val, ":p:h")')
  if len(dirs) > 1
    " try to narrow it down to one result before falling back to prompting the
    " user
    let possibles = copy(dirs)
    while 1
      let index = 0
      while index < len(possibles)
        let dir = possibles[index]
        if match(path, dir . '/') == 0
          return dirs[index]
        endif
        let next = fnamemodify(dir, ':h')
        if next == '/'
          call remove(possibles, index)
        else
          let possibles[index] = next
          let index += 1
        endif
      endwhile

      if len(possibles) == 0
        break
      endif
    endwhile

    let response = eclim#util#PromptList(
      \ 'Choose the location of your django project', dirs)
    if response == -1
      return ''
    endif
    return dirs[response]
  endif

  return dirs[0]
endfunction " }}}

function! eclim#python#django#util#GetProjectAppModules(project_dir) " {{{
  " Gets a list of application module names for the supplied project directory.
  if a:project_dir != ''
    let setting = eclim#python#django#util#GetSetting(a:project_dir, 'INSTALLED_APPS')
    let setting = substitute(setting, '[^a-zA-Z0-9_.,]', '', 'g')
    let apps = split(setting, ',\s*')
    return apps
  endif
  return []
endfunction " }}}

function! eclim#python#django#util#GetProjectAppNames(project_dir) " {{{
  " Gets a list of application names for the supplied project directory.
  if a:project_dir != ''
    let apps = eclim#python#django#util#GetProjectAppModules(a:project_dir)
    call map(apps, "split(v:val, '\\.')[-1]")
    return apps
  endif
  return []
endfunction " }}}

function! eclim#python#django#util#GetProjectAppPaths(project_dir) " {{{
  " Gets a list of application paths for the supplied project directory.
  if a:project_dir != ''
    let code = "print([" .
      \ "__import__(a, None, None, '__init__').__path__[0] " .
      \ "for a in settings.INSTALLED_APPS])"
    let setting = eclim#python#django#util#PythonExec(code, a:project_dir)
    return setting != '' ? eval(setting) : []
  endif
  return []
endfunction " }}}

function! eclim#python#django#util#GetSetting(project_dir, name) " {{{
  let interpreter = eclim#python#project#GetInterpreter()
  if interpreter == ''
    return ''
  endif

  " FIXME: support evaluating the setting into the appropriate vim type
  let setting = eclim#python#django#util#PythonExec(
    \ 'print(settings.' . a:name . ')', a:project_dir)
  return setting
endfunction " }}}

function! eclim#python#django#util#GetSqlEngine(project_dir) " {{{
  " Gets the configured sql engine for the project at the supplied project directory.
  let engine = 'postgresql'
  let setting = eclim#python#django#util#GetSetting(
    \ a:project_dir, "DATABASES['default']['ENGINE']")
  if setting !~ '^\s*$'
    let setting = split(setting, '\.')[-1]
    let setting = split(setting, '_')[0]
    let engine = setting
  endif
  return engine
endfunction " }}}

function! eclim#python#django#util#GetTemplateDirs(project_dir) " {{{
  " Gets the configured list of template directories relative to the project
  " dir.
  let setting = eclim#python#django#util#GetSetting(a:project_dir, 'TEMPLATE_DIRS')
  let setting = substitute(setting, '^[\[(]\(.\{-}\)[\])]$', '\1', '')
  let dirs = split(setting, ',\s*')
  return map(dirs, "substitute(v:val, \"^['\\\"]\\\\(.\\\\{-}\\\\)['\\\"]$\", '\\1', '')")
endfunction " }}}

function! eclim#python#django#util#PythonExec(code, ...) " {{{
  " Optional args:
  "   project_dir: if set, then the project settings for the supplied project
  "                path will be imported before executing the supplied code.
  let interpreter = eclim#python#project#GetInterpreter()
  if interpreter == ''
    return ''
  endif

  if a:0
    let cwd = getcwd()
    try
      let project_dir = a:1
      exec (haslocaldir() ? 'lcd ' : 'cd ') . project_dir
      let code =
        \ "import os; " .
        \ "import settings; " .
        \ "os.environ['DJANGO_SETTINGS_MODULE'] = 'settings'; " . a:code
      call eclim#util#EchoDebug('----------')
      call eclim#util#EchoDebug(getcwd())
      call eclim#util#EchoDebug(code)
      let result = eclim#util#System(interpreter . " -W ignore -c \"" . code . "\"")
      if v:shell_error
        call eclim#util#EchoDebug(result)
        " try going up a dir and using that as a top level namespace
        let ns = fnamemodify(project_dir, ':t')
        exec (haslocaldir() ? 'lcd ' : 'cd ') . fnamemodify(project_dir, ':h')
        let code =
          \ "import os; " .
          \ "from " . ns . " import settings; " .
          \ "os.environ['DJANGO_SETTINGS_MODULE'] = '" . ns . ".settings'; " . a:code
        call eclim#util#EchoDebug('----------')
        call eclim#util#EchoDebug(getcwd())
        call eclim#util#EchoDebug(code)
        let result = eclim#util#System(interpreter . " -W ignore -c \"" . code . "\"")
        if v:shell_error
          call eclim#util#EchoDebug(result)
          return ''
        endif
      endif
    finally
      exec (haslocaldir() ? 'lcd ' : 'cd ') . cwd
    endtry
  else
    let result = eclim#util#System(interpreter . " -c \"" . a:code . "\"")
    if v:shell_error
      return ''
    endif
  endif

  return substitute(result, "\n$", '', '')
endfunction " }}}

" vim:ft=vim:fdm=marker
