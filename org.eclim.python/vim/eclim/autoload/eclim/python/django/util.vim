" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/python/django.html
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

" GetLoadList(project_dir) {{{
" Returns a list of tag/filter files loaded by the current template.
function eclim#python#django#util#GetLoadList(project_dir)
  let pos = getpos('.')

  call cursor(1, 1)
  let loaded = []
  while search('{%\s*load\s', 'cW')
    let elements = split(
      \ substitute(getline('.'), '.*{%\s*load\s\+\(.\{-}\)\s*%}.*', '\1', ''))
    let loaded += elements
    call cursor(line('.') + 1, 1)
  endwhile
  call setpos('.', pos)

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
function eclim#python#django#util#GetProjectPath(...)
  let path = len(a:000) > 0 ? a:000[0] : escape(expand('%:p:h'), ' ')
  let dir = findfile("manage.py", path . ';')
  if dir != ''
    let dir = substitute(fnamemodify(dir, ':p:h'), '\', '/', 'g')
    " secondary check on the dir, if settings.py exists, then probably the
    " right dir, otherwise, search again from the parent.
    if !filereadable(dir . '/settings.py')
      return eclim#python#django#util#GetProjectPath(path . '/..')
    endif
  endif
  return dir
endfunction " }}}

" GetProjectApps(project_dir) {{{
" Gets a list of applications for the supplied project directory.
function eclim#python#django#util#GetProjectApps(project_dir)
  if a:project_dir != ''
    let apps = split(globpath(a:project_dir, '*/views.py'), '\n')
    call map(apps, "fnamemodify(v:val, ':p:h:t')")
    return apps
  endif
  return []
endfunction " }}}

" GetSetting(project_dir, name) {{{
function eclim#python#django#util#GetSetting(project_dir, name)
  let cwd = getcwd()
  try
    exec (haslocaldir() ? 'lcd ' : 'cd') . a:project_dir
    let setting = eclim#util#System(
      \ "python -c \"import settings; print(settings." . a:name . ")\"")
    if v:shell_error
      " try going up a dir and using that as a top level namespace
      let ns = fnamemodify(a:project_dir, ':t')
      exec (haslocaldir() ? 'lcd ' : 'cd') . fnamemodify(a:project_dir, ':h')
      let setting = eclim#util#System(
        \ "python -c \"from " . ns  . " import settings; print(settings." . a:name . ")\"")
      if v:shell_error
        return ''
      endif
    endif

    let setting = substitute(setting, "\n$", '', '')
  finally
    exec (haslocaldir() ? 'lcd ' : 'cd') . cwd
  endtry

  return setting
endfunction " }}}

" GetSqlEngine(project_dir) {{{
" Gets the configured sql engine for the project at the supplied project directory.
function eclim#python#django#util#GetSqlEngine(project_dir)
  let engine = 'postgresql'
  let setting = eclim#python#django#util#GetSetting(a:project_dir, 'DATABASE_ENGINE')
  let setting = substitute(setting, "^['\"]\\(.\\{-}\\)['\"]$", '\1', '')
  if setting !~ '^\s*$'
    let engine = setting
  endif
  return engine
endfunction " }}}

" GetTemplateDirs(project_dir) {{{
" Gets the configured list of template directories relative to the project
" dir.
function eclim#python#django#util#GetTemplateDirs(project_dir)
  let setting = eclim#python#django#util#GetSetting(a:project_dir, 'TEMPLATE_DIRS')
  let setting = substitute(setting, '^[\[(]\(.\{-}\)[\])]$', '\1', '')
  let dirs = split(setting, ',\s*')
  return map(dirs, "substitute(v:val, \"^['\\\"]\\\\(.\\\\{-}\\\\)['\\\"]$\", '\\1', '')")
endfunction " }}}

" vim:ft=vim:fdm=marker
