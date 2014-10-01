" Author:  Eric Van Dewoestine
"
" License: {{{
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

" Script Varables {{{
  let s:search_src = "java_search"
  let s:search_doc = "java_docsearch"
  let s:search_element =
    \ '-command <search> -n "<project>" -f "<file>" ' .
    \ '-o <offset> -e <encoding> -l <length> <args>'
  let s:search_pattern = '-command <search>'
  let s:options_map = {
      \ '-p': [],
      \ '-i': [],
      \ '-a': ['split', 'vsplit', 'edit', 'tabnew', 'lopen'],
      \ '-s': ['all', 'project'],
      \ '-x': ['all', 'declarations', 'implementors', 'references'],
      \ '-t': [
        \ 'annotation',
        \ 'class',
        \ 'classOrEnum',
        \ 'classOrInterface',
        \ 'constructor',
        \ 'enum',
        \ 'field',
        \ 'interface',
        \ 'method',
        \ 'package',
        \ 'type',
      \ ],
    \ }

  let s:search_alt_all = '\<<element>\>'
  let s:search_alt_references = s:search_alt_all
  let s:search_alt_implementors =
    \ '\(implements\|extends\)\_[0-9A-Za-z,[:space:]]*\<<element>\>\_[0-9A-Za-z,[:space:]]*{'
" }}}

function! s:Search(command, ...) " {{{
  " Executes a search.
  " Usage closely resebles eclim command line client usage.
  " When doing a non-pattern search the element under the cursor is searched for.
  "   Search for declarations of element under the cursor
  "     call s:Search("-x", "declarations")
  "   Search for references of HashMap
  "     call s:Search("-p", "HashM*", "-t", "class", "-x", "references")
  " Or all the arguments can be passed in at once:
  "   call s:Search("-p 'HashM*' -t class -x references")

  let argline = ""
  let index = 1
  while index <= a:0
    if index != 1
      let argline = argline . " "
    endif
    let argline = argline . a:{index}
    let index = index + 1
  endwhile

  " check if pattern supplied without -p.
  if argline !~ '^\s*-[a-z]' && argline !~ '^\s*$'
    let argline = '-p ' . argline
  endif

  let in_project = eclim#project#util#IsCurrentFileInProject(0)

  " element search
  if argline !~ '-p\>'
    if &ft != 'java'
      call eclim#util#EchoWarning
        \ ("Element searches only supported in java source files.")
      return 0
    endif

    if !eclim#java#util#IsValidIdentifier(expand('<cword>'))
      call eclim#util#EchoError
        \ ("Element under the cursor is not a valid java identifier.")
      return 0
    endif

    if !in_project
      " build a pattern search and execute it
      let results = s:SearchAlternate('-p ' . s:BuildPattern() . ' ' . argline, 1)
      " kind of gross. if there was no alternate result and eclimd is not
      " running, then make sure a message is echoed to the user so they know
      " that eclimd not running *may* be the cause of no results.
      if len(results) == 0 && !eclim#EclimAvailable()
        return 0
      endif
      return results
    endif

    let project = eclim#project#util#GetCurrentProjectName()
    let file = eclim#project#util#GetProjectRelativeFilePath()
    let position = eclim#util#GetCurrentElementPosition()
    let offset = substitute(position, '\(.*\);\(.*\)', '\1', '')
    let length = substitute(position, '\(.*\);\(.*\)', '\2', '')

    let search_cmd = s:search_element
    let search_cmd = substitute(search_cmd, '<project>', project, '')
    let search_cmd = substitute(search_cmd, '<search>', a:command, '')
    let search_cmd = substitute(search_cmd, '<file>', file, '')
    let search_cmd = substitute(search_cmd, '<offset>', offset, '')
    let search_cmd = substitute(search_cmd, '<encoding>', eclim#util#GetEncoding(), '')
    let search_cmd = substitute(search_cmd, '<length>', length, '')
    let search_cmd = substitute(search_cmd, '<args>', argline, '')

    let result = eclim#Execute(search_cmd)

  " pattern search
  else
    let project = eclim#project#util#GetCurrentProjectName()

    " pattern search
    let search_cmd = s:search_pattern
    let search_cmd = substitute(search_cmd, '<search>', a:command, '')
    if project != ''
      let search_cmd .= ' -n "' . project . '"'
    endif
    let file = eclim#project#util#GetProjectRelativeFilePath()
    if file != ''
      let search_cmd .= ' -f "' . file . '"'
    endif
    let search_cmd .= ' ' . argline
    " quote the search pattern
    let search_cmd =
      \ substitute(search_cmd, '\(.*-p\s\+\)\(.\{-}\)\(\s\|$\)\(.*\)', '\1"\2"\3\4', '')

    let result =  eclim#Execute(search_cmd)

    if !in_project && filereadable(expand('%'))
      return result + s:SearchAlternate(argline, 0)
    endif
  endif

  return result
endfunction " }}}

function! s:SearchAlternate(argline, element) " {{{
  " Alternate search for non-project src files using vimgrep and &path.

  call eclim#util#EchoInfo("Executing alternate search...")
  if a:argline =~ '-t'
    call eclim#util#EchoError
      \ ("Alternate search doesn't support the type (-t) option yet.")
    return []
  endif
  let search_pattern = ""
  if a:argline =~ '-x all'
    let search_pattern = s:search_alt_all
  elseif a:argline =~ '-x implementors'
    let search_pattern = s:search_alt_implementors
  elseif a:argline =~ '-x references'
    let search_pattern = s:search_alt_references
  endif

  let pattern = substitute(a:argline, '.*-p\s\+\(.\{-}\)\(\s.*\|$\)', '\1', '')
  let file_pattern = substitute(pattern, '\.', '/', 'g') . ".java"

  " search relative to the current dir first.
  let package_path = substitute(eclim#java#util#GetPackage(), '\.', '/', 'g')
  let path = substitute(expand('%:p:h'), '\', '/', 'g')
  let path = substitute(path, package_path, '', '')
  let files = split(eclim#util#Globpath(path, "**/" . file_pattern), '\n')

  " if none found, then search the path.
  if len(files) == 0
    let files = eclim#util#FindFileInPath(file_pattern, 1)
    let path = ""
  endif

  let results = []

  if len(files) > 0 && search_pattern != ''
    " narrow down to, hopefully, a distribution path for a narrower search.
    let response = eclim#util#PromptList(
      \ "Multiple type matches. Please choose the relevant file.",
      \ files, g:EclimHighlightInfo)
    if response == -1
      return
    endif

    let file = substitute(get(files, response), '\', '/', 'g')
    if path == ""
      let path = eclim#util#GetPathEntry(file)
    endif
    let path = escape(path, '/\')
    let path = substitute(file, '\(' . path . '[/\\]\?.\{-}[/\\]\).*', '\1', '')
    let pattern = substitute(pattern, '\*', '.\\\\{-}', 'g')
    let search_pattern = substitute(search_pattern, '<element>', pattern, '')
    let command = "vimgrep /" . search_pattern . "/gj " . path . "**/*.java"
    silent! exec command

    let loclist = getloclist(0)
    for entry in loclist
      let bufname = bufname(entry.bufnr)
      let result = {
          \ 'filename': bufname,
          \ 'message': entry.text,
          \ 'line': entry.lnum,
          \ 'column': entry.col,
        \ }
      " when searching for implementors, prevent dupes from the somewhat
      " greedy pattern search (may need some more updating post conversion to
      " dict results).
      if a:argline !~ '-x implementors' || !eclim#util#ListContains(results, result)
        call add(results, result)
      endif
    endfor
  elseif len(files) > 0
    for file in files
      let fully_qualified = eclim#java#util#GetPackage(file) . '.' .
        \ eclim#java#util#GetClassname(file)
      " if an element search, filter out results that are not imported.
      if !a:element || eclim#java#util#IsImported(fully_qualified)
        call add(results, {
            \ 'filename': file,
            \ 'message': fully_qualified,
            \ 'line': 1,
            \ 'column': 1,
          \ })
      endif
    endfor
  endif
  call eclim#util#Echo(' ')
  return results
endfunction " }}}

function! s:BuildPattern() " {{{
  " Builds a pattern based on the cursors current position in the file.

  let class = expand('<cword>')
  " see if the classname element selected is fully qualified.
  let line = getline('.')
  let package =
    \ substitute(line, '.*\s\([0-9A-Za-z._]*\)\.' . class . '\>.*', '\1', '')

  " not fully qualified, so attempt to determine package from import.
  if package == line
    let package = eclim#java#util#GetPackageFromImport(class)

    " maybe the element is the current class?
    if package == ""
      if eclim#java#util#GetClassname() == class
        let package = eclim#java#util#GetPackage()
      endif
    endif
  endif

  if package != ""
    return package . "." . class
  endif
  return class
endfunction " }}}

function! eclim#java#search#SearchAndDisplay(type, args) " {{{
  " Execute a search and displays the results via quickfix.

  " if running from a non java source file, no SilentUpdate needed.
  if &ft == 'java'
    call eclim#lang#SilentUpdate()
  endif

  let argline = a:args

  " check if just a pattern was supplied.
  if argline =~ '^\s*\w'
    let argline = '-p ' . argline
  endif

  " check for user supplied open action
  let [action_args, argline] = eclim#util#ExtractCmdArgs(argline, '-a:')
  let action = len(action_args) == 2 ? action_args[1] : g:EclimJavaSearchSingleResult

  let results = s:Search(a:type, argline)
  if type(results) != g:LIST_TYPE
    return
  endif
  if !empty(results)
    if a:type == 'java_search'
      call eclim#lang#SearchResults(results, action)
      return 1
    elseif a:type == 'java_docsearch'
      let window_name = "javadoc_search_results"
      let filename = expand('%:p')
      call eclim#util#TempWindowClear(window_name)

      if len(results) == 1 && g:EclimJavaDocSearchSingleResult == 'open'
        let entry = results[0]
        call s:ViewDoc(entry)
      else
        call eclim#util#TempWindow(
          \ window_name, results, {'height': g:EclimLocationListHeight})

        nnoremap <silent> <buffer> <cr> :call <SID>ViewDoc()<cr>
        augroup temp_window
          autocmd! BufWinLeave <buffer>
          call eclim#util#GoToBufferWindowRegister(filename)
        augroup END
      endif
    endif
    return 1
  else
    if argline =~ '-p '
      let searchedFor = substitute(argline, '.*-p \(.\{-}\)\( .*\|$\)', '\1', '')
      call eclim#util#EchoInfo("Pattern '" . searchedFor . "' not found.")
    elseif &ft == 'java'
      if !eclim#java#util#IsValidIdentifier(expand('<cword>'))
        return
      endif

      let searchedFor = expand('<cword>')
      call eclim#util#EchoInfo("No results for '" . searchedFor . "'.")
    endif
  endif
endfunction " }}}

function! s:ViewDoc(...) " {{{
  " View the supplied file in a browser, or if none proved, the file under the
  " cursor.
  let url = a:0 > 0 ? a:1 : substitute(getline('.'), '\(.\{-}\)|.*', '\1', '')
  call eclim#web#OpenUrl(url)
endfunction " }}}

function! eclim#java#search#CommandCompleteSearch(argLead, cmdLine, cursorPos) " {{{
  let options_map = s:options_map
  " omit the -a args on a javadoc search since those results are opened in a
  " browser
  if a:cmdLine =~ '^JavaDocS'
    let options_map = copy(options_map)
    unlet options_map['-a']
  endif
  return eclim#util#CommandCompleteOptions(
    \ a:argLead, a:cmdLine, a:cursorPos, options_map)
endfunction " }}}

function! eclim#java#search#CommandCompleteSearchContext(argLead, cmdLine, cursorPos) " {{{
  let options_map = {'-a': s:options_map['-a']}
  return eclim#util#CommandCompleteOptions(
    \ a:argLead, a:cmdLine, a:cursorPos, options_map)
endfunction " }}}

function! eclim#java#search#FindClassDeclaration() " {{{
  " Used by non java source files to find the declaration of a classname under
  " the cursor.
  let line = getline('.')
  let class = substitute(line,
    \ '.\{-}\([0-9a-zA-Z_.]*\%' . col('.') . 'c[0-9a-zA-Z_.]*\).*', '\1', '')
  if class != line && class != '' && class =~ '^[a-zA-Z]'
    call eclim#java#search#SearchAndDisplay(
      \ 'java_search', '-t classOrInterface -p ' . class)
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
