" Author:  Eric Van Dewoestine
"
" License:  {{{
"
" Copyright (C) 2005 - 2014  Eric Van Dewoestine
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

function! eclim#python#django#find#FindFilterOrTag(argline, project_dir, element, type) " {{{
  " Finds and opens the supplied filter or tag definition.

  let filenames = []
  let django_path = eclim#python#django#util#GetDjangoPath()

  " Get a list of tag/filter files loaded by the current template.
  let pos = getpos('.')
  try
    call cursor(1, 1)
    let loaded = []
    let pattern = '.*{%\s*load\s\+\(.\{-}\s\+from\s\+\)\?\(.\{-}\)\s*%}.*'
    while search('{%\s*load\s', 'cW')
      let loaded += split(substitute(getline('.'), pattern, '\2', ''))
      call cursor(line('.') + 1, 1)
    endwhile
  finally
    call setpos('.', pos)
  endtry
  let tag_dirs = [
      \ a:project_dir . '/templatetags/',
      \ a:project_dir . '/*/templatetags/',
      \ eclim#python#django#util#GetDjangoPath() . '/templatetags/',
    \ ]
  let paths = eclim#python#django#util#GetProjectAppPaths(a:project_dir)
  for path in paths
    let path .= "/templatetags"
    if isdirectory(path)
      call add(tag_dirs, path)
    endif
  endfor
  for load in loaded
    for dir in tag_dirs
      let file = findfile(load . '.py', dir)
      if file != ''
        call add(filenames, file)
        break
      endif
    endfor
  endfor

  " add to the list templatetags dirs of installed apps
  let paths = eclim#python#django#util#GetProjectAppPaths(a:project_dir)
  for path in paths
    let path .= "/templatetags"
    if isdirectory(path)
      call add(filenames, path . "/templatetags/**.py")
    endif
  endfor

  " add builtin django tags/filters
  if django_path != ''
    if a:type == 'filter'
      call add(filenames, django_path . "/template/defaultfilters.py")
    elseif a:type == 'tag'
      call add(filenames, django_path . "/template/defaulttags.py")
    endif
  endif

  let def = '\<def\s\+' . a:element . '\>'
  let register = '@register\.' . a:type . "(['\"]" . a:element . "['\"]"
  let cmd = 'lvimgrep /\(' . def . '\|' . register . '\)/j '
  let cmd .= join(filenames)
  silent! exec cmd

  let results = getloclist(0)
  if len(results) > 0
    let [action_args, argline] = eclim#util#ExtractCmdArgs(a:argline, '-a:')
    let action = len(action_args) == 2 ? action_args[1] : g:EclimDjangoFindAction

    call eclim#util#GoToBufferWindowOrOpen(bufname(results[0].bufnr), action)
    lfirst
    return 1
  endif
  call eclim#util#EchoError(
    \ 'Unable to find the ' . a:type . ' "' . a:element . '"')
endfunction " }}}

function! eclim#python#django#find#FindFilterTagFile(argline, project_dir, file, element) " {{{
  " Finds and opens the supplied tag/file definition file.
  let tag_dirs = [
      \ a:project_dir . '/templatetags/',
      \ a:project_dir . '/*/templatetags/',
      \ eclim#python#django#util#GetDjangoPath() . '/templatetags/',
    \ ]
  let paths = eclim#python#django#util#GetProjectAppPaths(a:project_dir)
  for path in paths
    let path .= "/templatetags"
    if isdirectory(path)
      call add(tag_dirs, path)
    endif
  endfor

  for dir in tag_dirs
    let file = findfile(a:file . '.py', dir)
    if file != ''
      let [action_args, argline] = eclim#util#ExtractCmdArgs(a:argline, '-a:')
      let action = len(action_args) == 2 ? action_args[1] : g:EclimDjangoFindAction

      call eclim#util#GoToBufferWindowOrOpen(file, action)
      if a:element != ''
        let def = '\<def\s\+' . a:element . '\>'
        let register = "@register\.tag(['\"]" . a:element . "['\"]"
        call search('\(' . def . '\|' . register . '\)')
      endif
      return 1
    endif
  endfor
  call eclim#util#EchoError('Could not find tag/filter file "' . a:file . '.py"')
endfunction " }}}

function! eclim#python#django#find#FindSettingDefinition(argline, project_dir, value) " {{{
  " Finds and opens the definition for the supplied setting middleware,
  " context processor, or template loader.
  let file = substitute(a:value, '\(.*\)\..*', '\1', '')
  let def = substitute(a:value, '.*\.\(.*\)', '\1', '')
  let file = substitute(file, '\.', '/', 'g') . '.py'
  let init = substitute(file, '\.py', '/__init__.py', '')

  let search_dirs = [
    \ a:project_dir,
    \ fnamemodify(a:project_dir, ':h'),
    \ fnamemodify(eclim#python#django#util#GetDjangoPath(), ':h'),
  \ ]
  for dir in search_dirs
    let found = findfile(file, dir)
    if found == ''
      let found = findfile(init, dir)
    endif

    if found != ''
      let [action_args, argline] = eclim#util#ExtractCmdArgs(a:argline, '-a:')
      let action = len(action_args) == 2 ? action_args[1] : g:EclimDjangoFindAction

      call eclim#util#GoToBufferWindowOrOpen(found, action)
      call search('\(def\|class\)\s\+' . def . '\>', 'cw')
      return 1
    endif
  endfor

  call eclim#util#EchoError('Could not definition of "' . a:value . '"')
endfunction " }}}

function! eclim#python#django#find#FindStaticFile(argline, project_dir, file) " {{{
  " Finds and opens the supplied static file name.
  let [action_args, argline] = eclim#util#ExtractCmdArgs(a:argline, '-a:')
  let action = len(action_args) == 2 ? action_args[1] : g:EclimDjangoFindAction

  for path in g:EclimDjangoStaticPaths + ['.', 'static', '../static']
    if path !~ '^\(/\|\w:\)'
      let path = a:project_dir . '/' . path
    endif
    let file = findfile(a:file, path)
    if file != ''
      call eclim#util#GoToBufferWindowOrOpen(file, action)
      return 1
    endif
  endfor

  call eclim#common#locate#LocateFile(action, a:file)
endfunction " }}}

function! eclim#python#django#find#FindTemplate(argline, project_dir, template) " {{{
  " Finds and opens the supplied template definition.

  " First try searching the configured template dirs (including installed apps)
  let file = ''
  let dirs = eclim#python#django#util#GetTemplateDirs(a:project_dir)
  let app_paths = eclim#python#django#util#GetProjectAppPaths(a:project_dir)
  for app_path in app_paths
    let dir = app_path . '/templates'
    if isdirectory(dir)
      call add(dirs, dir)
    endif
  endfor
  for dir in dirs
    let template_dir = dir
    if template_dir !~ '^' . a:project_dir && !isdirectory(template_dir)
      if fnamemodify(a:project_dir, ':t') == split(dir, '/')[0]
        let template_dir = fnamemodify(a:project_dir, ':h') . '/' . dir
      else
        let template_dir = a:project_dir . '/' . dir
      endif
    endif
    let file = findfile(a:template, template_dir)
    if file != ''
      break
    endif
  endfor

  " Couldn't find an exact match, so try globbing the project
  if file == ''
    let project_root = eclim#project#util#GetCurrentProjectRoot()
    let results = globpath(project_root, '**/' . a:template)
    if results != ''
      let files = split(results, '\n')
      if len(files) == 1
        let file = files[0]
      else
        let response = eclim#util#PromptList('Choose a file to open', files)
        if response != -1
          let file = files[response]
        endif
      endif
    endif
  endif

  if file != ''
    let [action_args, argline] = eclim#util#ExtractCmdArgs(a:argline, '-a:')
    let action = len(action_args) == 2 ? action_args[1] : g:EclimDjangoFindAction

    call eclim#util#GoToBufferWindowOrOpen(file, action)
    let b:eclim_django_project = a:project_dir
    return 1
  endif

  call eclim#util#EchoError('Could not find the template "' . a:template . '"')
endfunction " }}}

function! eclim#python#django#find#FindView(argline, project_dir, view) " {{{
  " Finds and opens the supplied view.
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
    let start = search('patterns\_s*(', 'bnW')
    let end = search("patterns\\_s*(\\_s*['\"]", 'bnWe')
    if start && end
      let line = getline(start)
      if end != start
        let line .= getline(end)
      endif
      let prefix = substitute(
        \ line, ".*patterns\\s*(\\s*['\"]\\(.\\{-}\\)['\"].*", '\1', '')
      if prefix != ''
        let view = prefix . '.' . view
      endif
    endif

    let function = split(view, '\.')[-1]
    let view = join(split(view, '\.')[0:-2], '.')
  endif

  let parts = split(substitute(view, '\.', '/', 'g'), '/')
  let possibles = [
      \ join(parts, '/') . '.py',
      \ join(parts, '/') . '/__init__.py',
      \ join(parts[1:], '/') . '.py',
      \ join(parts[1:], '/') . '/__init__.py',
    \ ]
  for possible in possibles
    let file = findfile(possible, a:project_dir)
    if file != ''
      break
    endif
  endfor

  if file != ''
    let [action_args, argline] = eclim#util#ExtractCmdArgs(a:argline, '-a:')
    let action = len(action_args) == 2 ? action_args[1] : g:EclimDjangoFindAction

    call eclim#util#GoToBufferWindowOrOpen(file, action)
    if function != ''
      let found = search('def\s\+' . function . '\>', 'cws')
      if !found
        call eclim#util#EchoWarning(
          \ 'Could not find the view function "' . function . '" in file ' . file)
      endif
    endif
    return 1
  endif
  call eclim#util#EchoError('Could not find the view "' . view . '"')
endfunction " }}}

function! eclim#python#django#find#TemplateFind(argline) " {{{
  " Find the template, tag, or filter under the cursor.
  let project_dir = eclim#python#django#util#GetProjectPath()
  if project_dir == ''
    call eclim#util#EchoError(
      \ 'Unable to locate django project path with manage.py and settings.py')
    return
  endif

  let line = getline('.')
  let element = eclim#util#GrabUri()
  if element =~ '|'
    let element = substitute(
      \ getline('.'), '.*|.*\(\<\w*\%' . col('.') . 'c\w*\>\).*', '\1', '')
    if element == getline('.')
      return
    endif
    return eclim#python#django#find#FindFilterOrTag(
      \ a:argline, project_dir, element, 'filter')
  elseif line =~ '{%\s*' . element . '\>'
    return eclim#python#django#find#FindFilterOrTag(
      \ a:argline, project_dir, element, 'tag')
  elseif line =~ '{%\s*load\s\+[^%]\{-}\%' . col('.') . 'c'
    let element = expand('<cword>')
    if element !~ '^\w\+$'
      return
    endif

    let tag = substitute(
      \ line, '.*\({%\s*load\s\+[^%]\{-}\%' . col('.') . 'c.\{-}\(%}\|$\)\).*', '\1', '')
    let from_regex = '.*\<from\>\s\+\(\w\+\)\s*\(%}\|$\)'
    if tag =~ from_regex
      let file = substitute(tag, from_regex, '\1', '')
    else
      let file = element
    endif
    return eclim#python#django#find#FindFilterTagFile(
      \ a:argline, project_dir, file, element)
  elseif line =~ "{%\\s*\\(extends\\|include\\)\\s\\+['\"]" . element . "['\"]"
    return eclim#python#django#find#FindTemplate(a:argline, project_dir, element)
  elseif line =~ "\\(src\\|href\\)\\s*=\\s*['\"]\\?\\s*" . element
    let element = substitute(element, '^/', '', '')
    let element = substitute(element, '?.*', '', '')
    return eclim#python#django#find#FindStaticFile(
      \ a:argline, project_dir, element)
  elseif g:EclimDjangoStaticPattern != '' &&
      \ line =~ substitute(g:EclimDjangoStaticPattern, '<element>', element, '')
    let element = substitute(element, '^/', '', '')
    let element = substitute(element, '[?#].*', '', '')
    return eclim#python#django#find#FindStaticFile(
      \ a:argline, project_dir, element)
  endif
  call eclim#util#EchoError(
    \ 'Element under the cursor does not appear to be a ' .
    \ 'valid tag, filter, or template reference.')
endfunction " }}}

function! eclim#python#django#find#ContextFind(argline) " {{{
  " Execute DjangoViewOpen, DjangoTemplateOpen, or PythonSearchContext based on
  " the context of the text under the cursor.
  if getline('.') =~ "['\"][^'\" ]*\\%" . col('.') . "c[^'\" ]*['\"]"
    if eclim#util#GrabUri() !~ '\.html' && (
        \ search("reverse\\_s*(\\_s*['\"][^'\" ]*\\%" . col('.') . "c[^'\" ]*['\"]", 'nw') ||
        \ search('urlpatterns\s\+=\s\+patterns(', 'nw'))
      return eclim#python#django#find#FindView(
        \ a:argline, eclim#python#django#util#GetProjectPath(), eclim#util#GrabUri())
    elseif expand('%:t') == 'settings.py'
      return eclim#python#django#find#FindSettingDefinition(
        \ a:argline, eclim#python#django#util#GetProjectPath(), eclim#util#GrabUri())
    else
      return eclim#python#django#find#FindTemplate(
        \ a:argline, eclim#python#django#util#GetProjectPath(), eclim#util#GrabUri())
    endif
  else
    exec 'PythonSearchContext ' . a:argline
  endif
endfunction " }}}

function! eclim#python#django#find#CommandCompleteAction(argLead, cmdLine, cursorPos) " {{{
  let options_map = {'-a': ['split', 'vsplit', 'edit', 'tabnew', 'lopen']}
  return eclim#util#CommandCompleteOptions(
    \ a:argLead, a:cmdLine, a:cursorPos, options_map)
endfunction " }}}

" vim:ft=vim:fdm=marker
