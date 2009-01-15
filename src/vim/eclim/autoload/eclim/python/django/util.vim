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

" GetLoadList(project_dir) {{{
" Returns a list of tag/filter files loaded by the current template.
function eclim#python#django#util#GetLoadList(project_dir)
  let pos = getpos('.')

  call cursor(1, 1)
  let loaded = []
  while search('{%\s*load\s', 'cW')
    call add(loaded, substitute(getline('.'), '.*{%\s*load\s\+\(\w\+\)\s*%}.*', '\1', ''))
    call cursor(line('.') + 1, 1)
  endwhile
  call setpos('.', col)

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
  let setting = ''
  let restore = winrestcmd()
  try
    let settings = a:project_dir . '/settings.py'
    let winnr = bufwinnr(bufnr(settings))
    if winnr == -1
      exec 'silent sview ' . settings
    else
      let orig = winnr()
      exec winnr . 'winc w'
    endif
    let pos = getpos('.')
    call cursor(1, 1)

    " GET SETTING
    let start = search('^\s*\<' . a:name . '\>\s*=', 'c')
    if start
      let end = search('^\s*[a-zA-Z_][^#]*\s*=', 'w')
      let lnum = start
      while lnum != end
        let line = substitute(getline(lnum), '#.*', '', '')
        if line !~ '^\s*$'
          let line = substitute(line, '^\s*', '', '')
          let line = substitute(line, '\s*$', '', '')
          let setting .= line
        endif
        let lnum += 1
      endwhile
    endif
    let setting = substitute(setting, '^\s*\<'. a:name . '\>\s*=\s*', '', '')

    cal setpos('.', pos)
    if winnr == -1
      bd
    else
      exec orig . 'winc w'
    endif
  finally
    silent exec restore
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
  let dirs = split(setting, ',')
  return map(dirs, "substitute(v:val, \"^['\\\"]\\\\(.\\\\{-}\\\\)['\\\"]$\", '\\1', '')")
endfunction " }}}

" vim:ft=vim:fdm=marker
