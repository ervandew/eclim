" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/python/django.html
"
" License:
"
" Copyright (c) 2005 - 2008
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

" GetLoadList(project_dir) {{{
" Returns a list of tag/filter files loaded by the current template.
function eclim#python#django#util#GetLoadList (project_dir)
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
function eclim#python#django#util#GetSetting (project_dir, name)
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
    let clnum = line('.')
    let ccnum = col('.')
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

    cal cursor(clnum, ccnum)
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
function eclim#python#django#util#GetSqlEngine (project_dir)
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
function eclim#python#django#util#GetTemplateDirs (project_dir)
  let setting = eclim#python#django#util#GetSetting(a:project_dir, 'TEMPLATE_DIRS')
  let setting = substitute(setting, '^[\[(]\(.\{-}\)[\])]$', '\1', '')
  let dirs = split(setting, ',')
  return map(dirs, "substitute(v:val, \"^['\\\"]\\\\(.\\\\{-}\\\\)['\\\"]$\", '\\1', '')")
endfunction " }}}

" vim:ft=vim:fdm=marker
