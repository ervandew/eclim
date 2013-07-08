" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Filesystem explorer.
"
" License:
"
" Copyright (C) 2005 - 2013  Eric Van Dewoestine
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
  if !exists("g:TreeDirHighlight")
    let g:TreeDirHighlight = "Statement"
  endif
  if !exists("g:TreeFileHighlight")
    let g:TreeFileHighlight = "Normal"
  endif
  if !exists("g:TreeFileExecutableHighlight")
    let g:TreeFileExecutableHighlight = "Constant"
  endif
  if !exists("g:TreeActionHighlight")
    let g:TreeActionHighlight = "Statement"
  endif
  if !exists('g:TreeExpandSingleDirs')
    let g:TreeExpandSingleDirs = 0
  endif
  if !exists('g:TreeIndent')
    let g:TreeIndent = 4
  endif
  if g:TreeIndent < 2
    call eclim#util#EchoWarning('g:TreeIndent can be no less than 2.')
    let g:TreeIndent = 2
  endif
" }}}

" Script Variables {{{
  let s:node_prefix = ''
  let index = 0
  while index < (g:TreeIndent - 2)
    let s:node_prefix .= ' '
    let index += 1
  endwhile

  let s:dir_opened_prefix = '- '
  let s:dir_closed_prefix = '+ '
  let s:file_prefix = '  '

  let s:indent_length = len(s:node_prefix) + len(s:file_prefix)

  let s:node_regex = s:node_prefix .  '\(' .
    \ s:dir_opened_prefix . '\|' .
    \ s:dir_closed_prefix . '\|' .
    \ s:file_prefix . '\)'
  " \1 - indent, \2, node prefix + element prefix, \3 name
  let s:nodevalue_regex = '\(\s*\)' . s:node_regex . '\(.*\)'
  let s:root_regex = '^[/[:alpha:]]'

  let s:settings_loaded = 0

  let s:tree_count = 0
  let s:refresh_nesting = 0

  let s:has_ls = executable('ls') && !(has('win32') || has('win64'))

  let s:vcol = 0
" }}}

" TreeHome() {{{
function! eclim#tree#TreeHome()
  let name = "Tree"
  if s:tree_count > 0
    let name .= s:tree_count
  endif
  let s:tree_count += 1

  call eclim#tree#Tree(name, [eclim#UserHome()], [], 1, [])
endfunction " }}}

" TreePanes() {{{
function! eclim#tree#TreePanes()
  call eclim#tree#TreeHome()
  vertical new
  call eclim#tree#TreeHome()
  1winc w
endfunction " }}}

" Tree(name, roots, aliases, expand, filters) {{{
" name - The name to use for the tree buffer.
" roots - List of paths to use as tree roots.
" aliases - List of aliases for root paths, or an empty list for no aliasing.
" expand - 1 to pre expand the root directories, 0 otherwise.
" filters - List of file name patterns to include in directory listings, or an
"   empty list for no filtering.
function! eclim#tree#Tree(name, roots, aliases, expand, filters)
  silent exec 'edit ' . escape(a:name, ' ')
  setlocal ft=tree
  setlocal nowrap
  setlocal noswapfile
  setlocal nobuflisted
  setlocal buftype=nofile
  setlocal bufhidden=delete
  setlocal foldmethod=manual
  setlocal foldtext=getline(v:foldstart)
  setlocal sidescrolloff=0

  call s:Mappings()
  call eclim#tree#Syntax()

  " initialize autocmds before loading custom settings so that settings can
  " add autocmd events.
  augroup eclim_tree
    "autocmd! BufEnter,User <buffer>
    autocmd BufEnter <buffer> silent doautocmd eclim_tree User <buffer>
    exec 'autocmd BufDelete,BufUnload <buffer> ' .
      \ 'autocmd! eclim_tree * <buffer=' . bufnr('%') . '>'
  augroup END

  " register setting prior to listing any directories
  if exists("g:TreeSettingsFunction")
    let l:Settings = function(g:TreeSettingsFunction)
    call l:Settings()
    let s:settings_loaded = 1
  endif

  setlocal noreadonly modifiable
  silent 1,$delete _

  let roots = map(copy(a:roots), 'substitute(v:val, "\\([^/]\\)$", "\\1/", "")')
  let [roots, _] = s:NormalizeEntries(roots)
  let b:roots = copy(roots)
  let b:filters = a:filters
  let b:view_hidden = 0

  if len(a:aliases) > 0
    let b:aliases = {}
    let index = 0
    for alias in a:aliases
      if alias != ''
        let b:aliases[alias] = roots[index]
      endif
      let index += 1
    endfor

    call map(roots, 's:PathToAlias(v:val)')
  endif

  call append(line('$'), roots)

  if a:expand
    let index = len(roots)
    while index > 0
      call cursor(index + 1, 1)
      call eclim#tree#ExpandDir()
      let index = index - 1
    endwhile
  endif

  " delete empty first line.
  setlocal modifiable
  1,1delete _
  setlocal nomodifiable
endfunction " }}}

" ToggleCollapsedDir(Expand) {{{
function! eclim#tree#ToggleCollapsedDir(Expand)
  if eclim#tree#GetPath() =~ '/$'
    if getline('.') =~ '\s*' . s:node_prefix . s:dir_closed_prefix ||
        \ (getline('.') =~ s:root_regex && eclim#tree#GetLastChildPosition() == line('.'))
      call a:Expand()
    else
      call s:CollapseDir()
    endif
  endif
endfunction " }}}

" ToggleFoldedDir(Expand) {{{
function! eclim#tree#ToggleFoldedDir(Expand)
  if eclim#tree#GetPath() =~ '/$'
    if foldclosed(line('.')) != -1
      call s:UnfoldDir()
    elseif getline('.') =~ '\s*' . s:node_prefix . s:dir_opened_prefix ||
        \ (getline('.') =~ s:root_regex && eclim#tree#GetLastChildPosition() != line('.'))
      call s:FoldDir()
    else
      call a:Expand()
    endif
  endif
endfunction " }}}

" ToggleViewHidden() {{{
function! eclim#tree#ToggleViewHidden()
  let b:view_hidden = (b:view_hidden + 1) % 2

  let line = getline('.')
  let path = eclim#tree#GetPath()
  call cursor(1, 1)
  call eclim#tree#Refresh()
  while search(s:root_regex, 'W') != 0
    call eclim#tree#Refresh()
  endwhile

  call cursor(1, 1)
  while search(line, 'W') != 0 && eclim#tree#GetPath() != path
  endwhile
  call eclim#tree#Cursor(line, 0)
endfunction " }}}

" GetFileInfo(file) {{{
function! eclim#tree#GetFileInfo(file)
  if executable('ls')
    return split(eclim#util#System("ls -ld '" . a:file . "'"), '\n')[0]
  endif
  return ''
endfunction "}}}

" GetPath([resolve_links]) {{{
function! eclim#tree#GetPath(...)
  let line = getline('.')
  let node = substitute(line, s:nodevalue_regex, '\3', '')

  let node = eclim#tree#GetParent(a:0 ? a:1 : 1) . node
  let path = s:AliasToPath(node)

  " handle symbolic links
  if path =~ '->'
    if !a:0 || a:1 " resolve links
      let link = substitute(path, '.* -> \(.*\)', '\1', '')
      if link !~ '^/' && link !~ '^[a-zA-Z]:/'
        let parent = substitute(path, '\(.*\) -> .*', '\1', '')
        let parent = fnamemodify(substitute(parent, '/$', '', ''), ':h')
        let link = parent . '/' . link
      endif
      let path = link
    else
      let path = substitute(path, '\(.*\) -> .*', '\1', '')
      if node =~ '/$'
        let path .= '/'
      endif
    endif
  endif

  " handle executable files.
  if path =~ '\*$'
    let path = strpart(path, 0, len(path) - 1)
  endif

  return path
endfunction "}}}

" GetParent([resolve_links]) {{{
function! eclim#tree#GetParent(...)
  let parent = ''

  let lnum = eclim#tree#GetParentPosition()
  if lnum
    let pos = getpos('.')
    call cursor(lnum, 1)
    let parent = eclim#tree#GetPath(a:0 ? a:1 : 1)
    call setpos('.', pos)
  endif

  return parent
endfunction " }}}

" GetParentPosition() {{{
function! eclim#tree#GetParentPosition()
  let lnum = 0
  let line = getline('.')
  if line =~ '\s*' . s:node_prefix
    if line =~ '^' . s:node_regex . '\S'
      let search = s:root_regex
    else
      let search = '^'
      let index = 0
      let indent = s:GetIndent(line('.'))
      while index < indent - s:indent_length
        let search .= ' '
        let index += 1
      endwhile
      let search .= s:node_prefix .  s:dir_opened_prefix
    endif

    let lnum = search(search, 'bnW')
  endif

  return lnum
endfunction " }}}

" GetLastChildPosition() {{{
function! eclim#tree#GetLastChildPosition()
  let line = getline('.')

  " a root node
  if line =~ s:root_regex
    let lnum = search(s:root_regex, 'nW')
    return lnum > 0 ? lnum  - 1 : s:GetLastLine()
  endif

  " non root node
  let sibling = '^' .
    \ substitute(line, s:nodevalue_regex, '\1' . escape(s:node_regex. '[.[:alnum:]_]', '\'), '')
  let lnum = line('.') + 1
  let indent = s:GetIndent(line('.'))
  while getline(lnum) !~ sibling &&
      \ s:GetIndent(lnum) >= indent &&
      \ lnum != s:GetLastLine()
    let lnum += 1
  endwhile

  " back up one if on a node of equal or less depth
  if s:GetIndent(lnum) <= indent
    let lnum -= 1
  endif

  " no sibling below, use parent's value
  if lnum == line('.') && getline(lnum + 1) !~ sibling
    let pos = getpos('.')

    call cursor(eclim#tree#GetParentPosition(), 1)
    let lnum = eclim#tree#GetLastChildPosition()

    call setpos('.', pos)
  endif

  return lnum
endfunction " }}}

" Execute(alt) {{{
function! eclim#tree#Execute(alt)
  if getline('.') =~ '^"\|^\s*$'
    return
  endif

  let path = eclim#tree#GetPath()

  " execute action on dir
  if path =~ '/$'
    if a:alt || foldclosed(line('.')) != -1
      call eclim#tree#ToggleFoldedDir(function('eclim#tree#ExpandDir'))
    else
      call eclim#tree#ToggleCollapsedDir(function('eclim#tree#ExpandDir'))
    endif

  " execute action on file
  else
    if !filereadable(path)
      echo "File is not readable or has been deleted."
    endif

    let actions = eclim#tree#GetFileActions(path)
    if len(actions) == 0
      echo "No registered actions for file: " . path
      return
    endif

    if a:alt
      call eclim#tree#DisplayActionChooser(
        \ path, actions, 'eclim#tree#ExecuteAction')
    else
      call eclim#tree#ExecuteAction(path, actions[0].action)
    endif
  endif
endfunction " }}}

" ExecuteAction(file, command) {{{
function! eclim#tree#ExecuteAction(file, command)
  let file = eclim#util#Simplify(a:file)
  let file = escape(file, ' &()')
  let file = escape(file, ' &()') " need to double escape
  let file = escape(file, '&') " '&' needs to be escaped 3 times.

  let command = a:command
  let command = substitute(command, '<file>', file, 'g')
  if command =~ '^!\w'
    silent call eclim#util#Exec(command)
    redraw!
  else
    exec command
  endif

  if command =~ '^!\w' && v:shell_error
    call eclim#util#EchoError('Error executing command: ' . command)
  endif
endfunction " }}}

" RegisterFileAction(regex, name, action) {{{
" regex - Pattern to match the file name against.
" name - Name of the action used for display purposes.
" action - The action to execute where <file> is replaced with the filename.
function! eclim#tree#RegisterFileAction(regex, name, action)
  if !exists('b:file_actions')
    let b:file_actions = []
  endif

  let entry = {}
  for e in b:file_actions
    if e.regex == a:regex
      let entry = e
      break
    endif
  endfor

  if len(entry) == 0
    let entry = {'regex': a:regex, 'actions': []}
    call add(b:file_actions, entry)
  endif

  call add(entry.actions, {'name': a:name, 'action': a:action})
endfunction " }}}

" RegisterDirAction(action) {{{
" action - A funcref which will be invoked when expanding a directory with the
" directory path and a mutable list of current directory contents.
function! eclim#tree#RegisterDirAction(action)
  if !exists('b:dir_actions')
    let b:dir_actions = []
  endif
  call add(b:dir_actions, a:action)
endfunction " }}}

" GetFileActions(file) {{{
" Returns a list of dictionaries with keys 'name' and 'action'.
function! eclim#tree#GetFileActions(file)
  let actions = []
  let thefile = tolower(a:file)
  let bufnr = bufnr('%')
  for entry in b:file_actions
    if thefile =~ entry.regex
      let actions += entry.actions
    endif
  endfor

  return actions
endfunction " }}}

" Shell(external) {{{
" Opens a shell either in the current vim session or externally.
function! eclim#tree#Shell(external)
  let path = eclim#tree#GetPath()
  if !isdirectory(path)
    let path = fnamemodify(path, ':h')
  endif

  let cwd = getcwd()
  silent exec "lcd " . escape(path, ' &')
  if a:external
    if !exists("g:TreeExternalShell")
      echo "No external shell configured via 'g:TreeExternalShell' variable."
    else
      silent call eclim#util#Exec(g:TreeExternalShell)
      redraw!
    endif
  else
    shell
  endif
  silent exec "lcd " . escape(cwd, ' &')
endfunction " }}}

" Cursor(line, prevline) {{{
function! eclim#tree#Cursor(line, prevline)
  let lnum = a:line
  let line = getline(lnum)

  if line =~ s:root_regex
    call cursor(lnum, 1)
  else
    " get the starting column of the current line and the previous line
    let start = len(line) - len(substitute(line, '^\s\+\W', '', ''))

    " only use the real previous line if we've only moved one line
    let moved = a:prevline - lnum
    if moved < 0
      let moved = -moved
    endif
    let pline = moved == 1 ? getline(a:prevline) : ''
    let pstart = pline != '' ?
      \ len(pline) - len(substitute(pline, '^\s\+\W', '', '')) : -1

    " only change the cursor column if the hasn't user has moved it to the
    " right to view more of the entry
    let cnum = start == pstart ? 0 : start
    call cursor(lnum, cnum)

    " attempt to maximize the amount of text on the current line that is in
    " view, but only if we've changed column position
    let winwidth = winwidth(winnr())
    let vcol = exists('s:vcol') ? s:vcol : 0
    let col = col('.')
    if cnum != 0 && (!vcol || ((len(line) - vcol) > winwidth))
      if len(line) > winwidth
        normal! zs
        " scroll back enough to keep the start of the parent in view
        normal! 6zh
        let s:vcol = col - 6
      endif
    endif

    " when the text view is shifted by vim it appears to always shift back one
    " half of the window width, so recalculate our visible column accordingly
    " if we detect such a shift... may not always be accurate.
    if s:vcol > col
      let s:vcol = max([start - (winwidth / 2), 0])
    endif
  endif
endfunction " }}}

" GetRoot() {{{
function! eclim#tree#GetRoot()
  if getline('.') =~ s:root_regex
    return s:AliasToPath(getline('.'))
  endif
  let start = search(s:root_regex, 'bcnW')
  return s:AliasToPath(getline(start))
endfunction " }}}

" SetRoot(path) {{{
function! eclim#tree#SetRoot(path)
  let path = s:AliasToPath(a:path)
  let path = s:NormalizeEntries([fnamemodify(path, ':p')])[0][0]
  if !isdirectory(path)
    echo 'Directory does not exist or may have been deleted.'
    return
  endif

  let path = s:PathToAlias(path)

  " if on a root node
  if getline('.') =~ s:root_regex
    let start = line('.')

  " not on a root node
  else
    let start = search(s:root_regex, 'bW')
  endif
  let end = eclim#tree#GetLastChildPosition()

  setlocal noreadonly modifiable
  silent exec start . ',' . end . 'delete _'

  let line = line('.')
  if line == 1
    let line = 0
  endif
  call append(line, path)

  " delete blank first line if any
  if getline(1) =~ '^$'
    silent 1,1delete _
  endif
  " delete blank last line if any
  if getline('$') =~ '^$'
    silent exec line('$') . ',' . line('$') . 'delete _'
  endif

  call cursor(line + 1, 1)
  call eclim#tree#ExpandDir()
  setlocal nomodifiable
endfunction " }}}

" Refresh() {{{
" FIXME: in need of a serious rewrite (probably need to rewrite the whole
" plugin)
function! eclim#tree#Refresh()
  let ignore_pattern = ''
  if &wildignore != ''
    let ignore_pattern = substitute(escape(&wildignore, '.'), '\*', '.*', 'g')
    let ignore_pattern = '\(' . join(split(ignore_pattern, ','), '\|') . '\)$'
  endif

  let clnum = line('.')
  let ccnum = col('.')

  let startpath = eclim#tree#GetPath()
  if s:refresh_nesting == 0
    let s:startpath = startpath
    " let vim track shifts in line numbers with a mark
    mark Z
  endif

  " if on a file or closed directory, refresh it's parent
  if startpath !~ '/$' ||
      \ getline('.') =~ '^\s*' . s:node_prefix . s:dir_closed_prefix
    call cursor(eclim#tree#GetParentPosition(), 1)
    let startpath = eclim#tree#GetPath()
  endif

  let start = line('.')
  let end = eclim#tree#GetLastChildPosition()

  " first check the node we are on
  if (!isdirectory(startpath) && !filereadable(startpath)) ||
      \ (getline('.') !~ s:root_regex && s:IsHidden(startpath, ignore_pattern))
    setlocal modifiable
    silent exec start . ',' . end . 'delete _'
    setlocal nomodifiable
    silent doautocmd eclim_tree User <buffer>
    return
  endif

  if s:refresh_nesting == 0
    call eclim#util#Echo('Refreshing...')
  endif
  let s:refresh_nesting += 1

  " move cursor to first child
  call cursor(start + 1, 1)
  " get pattern to use to match children.
  let match = substitute(getline('.'), '^' . s:nodevalue_regex, '\1', '')
  let match = '^' . match . s:node_regex . '[.[:alnum:]_]'

  " walk the tree
  let lnum = line('.')
  while lnum <= end && lnum <= s:GetLastLine()

    let line = getline('.')

    " open dir that needs to be refreshed as well.
    if line =~ '\s*' . s:node_prefix . s:dir_opened_prefix
      call eclim#tree#Refresh()
      let lnum = eclim#tree#GetLastChildPosition()
      let ldiff = lnum - line('.')
      let end += ldiff
      call cursor(lnum, 1)
    endif

    let path = eclim#tree#GetPath()

    " delete files, and dirs that do not exist, or are hidden.
    if (path =~ '/$' && !isdirectory(path)) ||
     \ (path !~ '/$' && !filereadable(path)) ||
     \ s:IsHidden(path, ignore_pattern)
      let last = eclim#tree#GetLastChildPosition()
      setlocal modifiable
      silent exec lnum . ',' . last . 'delete _'
      setlocal nomodifiable
      let end -= (last - lnum) + 1
      continue
    endif

    let lnum += 1
    call cursor(lnum, 1)
  endwhile
  call cursor(start + 1, ccnum)

  " merge in any dirs that have been added
  let contents = eclim#tree#ListDir(startpath)
  let [dirs, files] = s:NormalizeEntries(contents)
  let contents = dirs + files
  let root = eclim#tree#GetRoot()
  let indent = eclim#tree#GetChildIndent(start)
  let lnum = line('.')
  setlocal modifiable
  for entry in contents
    let path = eclim#tree#GetPath(0)
    let path_link = eclim#tree#GetPath(0)
    let rewrote = s:RewriteSpecial(entry)
    let norm_entry = substitute(entry, '[*@]$', '', '')
    if rewrote =~ '/$' && norm_entry !~ '/$'
      let norm_entry .= '/'
    endif

    " ugly
    if exists('b:links')
      for [link, target] in items(b:links)
        if path =~ '^' . root . link
          let path = substitute(path, root . link, target, '')
          break
        endif
      endfor
    endif

    if path != norm_entry && path_link != norm_entry
      " if we are adding a new entry we'll just add one that has the correct
      " index + prefix and let the next block set the proper display path.
      if s:MatchesFilter(norm_entry)
        if isdirectory(entry)
          let initial = fnamemodify(substitute(entry, '/$', '', ''), ':t') . '/'
        else
          let initial = fnamemodify(entry, ':t')
        endif

        if index(dirs, entry) != -1
          let display_entry = indent . s:node_prefix . s:dir_closed_prefix . initial
        else
          let display_entry = indent . s:node_prefix . s:file_prefix . initial
        endif
        if lnum <= s:GetLastLine()
          call append(lnum - 1, display_entry)
        else
          call append(s:GetLastLine(), display_entry)
        endif
        call cursor(lnum, 0)
      endif
    endif

    let parent = eclim#tree#GetParent(0)
    let parent_link = eclim#tree#GetParent()
    if index(dirs, entry) != -1
      let dir_prefix = s:dir_closed_prefix
      if getline(lnum) =~ '\s*' . s:node_prefix . s:dir_opened_prefix
        let dir_prefix = s:dir_opened_prefix
      endif
      let display_entry = indent . s:node_prefix . dir_prefix .
        \ substitute(substitute(rewrote, parent, '', ''), parent_link, '', '')
    else
      let display_entry = indent . s:node_prefix . s:file_prefix .
        \ substitute(substitute(rewrote, parent, '', ''), parent_link, '', '')
    endif

    call setline(lnum, display_entry)
    if getline(lnum) =~ '\s*' . s:node_prefix . s:dir_opened_prefix
      call cursor(eclim#tree#GetLastChildPosition() + 1, 1)
      let lnum = line('.')
    else
      let lnum += 1
      call cursor(lnum, 1)
    endif

  endfor
  setlocal nomodifiable

  call cursor(clnum, ccnum)
  let s:refresh_nesting -= 1

  if s:refresh_nesting == 0
    call eclim#util#Echo(' ')
    " return to marked position.
    call cursor(line("'Z"), col("`Z"))
    " if the entry that we started on is gone, move the cursor up a line.
    if s:startpath != eclim#tree#GetPath()
      call cursor(line('.') - 1, col('.'))
    endif
  endif
  silent doautocmd eclim_tree User <buffer>
endfunction " }}}

" MoveToLastChild() {{{
function! eclim#tree#MoveToLastChild()
  mark '
  if getline('.') !~ '^\s*' . s:node_prefix . s:dir_opened_prefix . '[.[:alnum:]_]'
    call cursor(eclim#tree#GetParentPosition(), 1)
  endif
  call eclim#tree#Cursor(eclim#tree#GetLastChildPosition(), 0)
endfunction " }}}

" MoveToParent() {{{
function! eclim#tree#MoveToParent()
  mark '
  call eclim#tree#Cursor(eclim#tree#GetParentPosition(), 0)
endfunction " }}}

" Mkdir() {{{
function! eclim#tree#Mkdir()
  let path = eclim#tree#GetPath()
  if !isdirectory(path)
    let path = fnamemodify(path, ':h') . '/'
  endif

  let response = input('mkdir: ', path, 'dir')
  if response == '' || response == path
    return
  endif

  " work around apparent vim bug attempting to create a dir with a trailing
  " slash.
  if response[-1:] == '/'
    let response = response[:-2]
  endif

  call mkdir(response, 'p')
  call eclim#tree#Refresh()
endfunction " }}}

" s:AliasToPath(alias) {{{
function! s:AliasToPath(alias)
  if exists('b:aliases')
    let alias = ''
    for alias in keys(b:aliases)
      if alias != '' && a:alias =~ '^' . alias . '\>/'
        return substitute(a:alias, '^' . alias . '/', b:aliases[alias], '')
      endif
    endfor
  endif
  return a:alias
endfunction " }}}

" s:PathToAlias(path) {{{
function! s:PathToAlias(path)
  if exists('b:aliases')
    let path = ''
    for alias in keys(b:aliases)
      let path = b:aliases[alias]
      if alias != '' && a:path =~ '^' . path
        return substitute(a:path, '^' . path, alias . '/', '')
      endif
    endfor
  endif
  return a:path
endfunction " }}}

" s:Depth() {{{
function! s:Depth()
  return len(split(eclim#tree#GetPath(), '/'))
endfunction " }}}

" ExpandDir() {{{
function! eclim#tree#ExpandDir()
  let dir = eclim#tree#GetPath()

  if !isdirectory(dir)
    echo "Not a directory or directory may have been removed."
    return
  endif

  let contents = eclim#tree#ListDir(dir)
  let [dirs, files] = s:NormalizeEntries(contents)

  if s:has_ls
    call map(dirs, 'substitute(v:val, "@$", "", "")')
    call map(files, 'substitute(v:val, "@$", "", "")')
  endif

  " filter files
  let filtered = []
  for file in files
    if s:MatchesFilter(file)
      call add(filtered, file)
    endif
  endfor
  let files = filtered

  " rewrite any special files (executables, symbolic links, etc).
  call map(dirs, 's:RewriteSpecial(v:val)')
  call map(files, 's:RewriteSpecial(v:val)')

  call eclim#tree#WriteContents(dir, dirs, files)
  if g:TreeExpandSingleDirs && len(files) == 0 && len(dirs) == 1 && s:Depth() < 50
    TreeNextPrevLine j
    call eclim#tree#ExpandDir()
  endif
endfunction " }}}

" ExpandPath(name, path) {{{
" Given the buffer name of a tree and a full path in that tree, either with an
" alias or real root path at the beginning, expand the tree node to reveal
" that path.
function! eclim#tree#ExpandPath(name, path)
  let winnr = winnr()
  let treewin = bufwinnr(a:name)
  if treewin == -1
    return
  endif

  exec treewin . 'winc w'

  let path = a:path
  let root = ''
  for r in b:roots
    let r = substitute(r, '/$', '', '')
    if path =~ '^' . r . '\>'
      let root = r
      break
    endif
  endfor

  " try aliases
  if root == ''
    let path = substitute(path, '^/', '', '')
    for r in keys(b:aliases)
      if path =~ '^' . r . '\>'
        let root = r
        break
      endif
    endfor
  endif

  if root != ''
    let path = substitute(path, '^' . root, '', '')

    for dir in split(path, '/')
      let line = search('+ \<' . dir . '\>/', 'n')
      if line
        call eclim#tree#Cursor(line, 0)
        call eclim#tree#Execute(1)
      else
        break
      endif
    endfor
  endif

  exec winnr . 'winc w'
endfunction " }}}

" WriteContents(dir, dirs, files) {{{
function! eclim#tree#WriteContents(dir, dirs, files)
  let dirs = a:dirs
  let files = a:files
  let indent = eclim#tree#GetChildIndent(line('.'))
  call map(dirs,
    \ 'substitute(v:val, a:dir, indent . s:node_prefix . s:dir_closed_prefix, "")')
  call map(files,
    \ 'substitute(v:val, a:dir, indent . s:node_prefix . s:file_prefix, "")')

  " update current line
  call s:UpdateLine(s:node_prefix . s:dir_closed_prefix,
    \ s:node_prefix . s:dir_opened_prefix)

  setlocal noreadonly modifiable
  let content = dirs + files
  call append(line('.'), content)
  setlocal nomodifiable
  return content
endfunction " }}}

" s:RewriteSpecial(file) {{{
function! s:RewriteSpecial(file)
  let file = a:file
  if s:has_ls
    let info = ''
    let file = substitute(file, '@$', '', '')

    " symbolic links
    let tmpfile = file =~ '/$' ? strpart(file, 0, len(file) - 1) : file
    if getftype(tmpfile) == 'link'
      if info == ''
        let info = eclim#util#System('ls -ldF ' . tmpfile)
      endif
      let linkto = substitute(info, '.*-> \(.*\)\n', '\1', '')

      if linkto =~ '//$'
        let linkto = strpart(linkto, 0, len(linkto) - 1)
      endif

      let file = tmpfile . ' -> ' . linkto
    endif
  endif

  if exists('b:links')
    if file =~ '/$'
      let path = substitute(file, '/$', '', '')
      let entry = substitute(path, eclim#tree#GetRoot(), '', '')
      if has_key(b:links, entry)
        let file = path . ' -> ' . get(b:links, entry)
        let file = substitute(file, '\([^/]\)$', '\1/', '')
      endif
    endif
  endif

  return file
endfunction " }}}

" s:CollapseDir() {{{
function! s:CollapseDir()
  " update current line
  call s:UpdateLine(s:node_prefix . s:dir_opened_prefix,
    \ s:node_prefix . s:dir_closed_prefix)

  let lnum = line('.')
  let cnum = col('.')
  let start = lnum + 1
  let end = eclim#tree#GetLastChildPosition()

  if start > end
    return
  endif

  setlocal noreadonly modifiable
  silent exec start . ',' . end . 'delete _'
  setlocal nomodifiable

  call cursor(lnum, cnum)
endfunction " }}}

" s:UnfoldDir() {{{
function! s:UnfoldDir()
  foldopen
endfunction " }}}

" s:FoldDir() {{{
function! s:FoldDir()
  let start = line('.')
  let end = eclim#tree#GetLastChildPosition()

  exec start . ',' . end . 'fold'
endfunction " }}}

" ListDir(dir, [execute_actions]) {{{
function! eclim#tree#ListDir(dir, ...)
  if s:has_ls
    let ls = 'ls -1F'
    if b:view_hidden
      let ls .= 'A'
    endif
    let contents = split(eclim#util#System(ls . " '" . a:dir . "'"), '\n')
    if !b:view_hidden && &wildignore != ''
      let pattern = substitute(escape(&wildignore, '.~'), '\*', '.*', 'g')
      let pattern = '\(' . join(split(pattern, ','), '\|') . '\)$'
      " Note: symlinks have a trailing @, so remove that before comparing
      " against pattern
      call filter(contents, 'substitute(v:val, "@$", "", "") !~ pattern')
    endif
    call map(contents, 'a:dir . v:val')
  else
    if !b:view_hidden
      let contents = split(eclim#util#Globpath(escape(a:dir, ','), '*', 1), '\n')
    else
      let contents = split(eclim#util#Globpath(escape(a:dir, ','), '*'), '\n')
      let contents = split(eclim#util#Globpath(escape(a:dir, ','), '.*'), '\n') + contents
    endif

    " append trailing '/' to dirs if necessary
    call map(contents,
      \ 'isdirectory(v:val) ? substitute(v:val, "\\([^/]\\)$", "\\1/", "") : v:val')
  endif

  if exists('b:dir_actions') && (!a:0 || a:1)
    for l:Action in b:dir_actions
      call l:Action(a:dir, contents)
    endfor
  endif

  return contents
endfunction " }}}

" s:GetIndent() {{{
function! s:GetIndent(line)
  let indent = indent(a:line)
  if getline(a:line) =~ s:file_prefix . '[.[:alnum:]_]' && s:file_prefix =~ '^\s*$'
    let indent -= len(s:file_prefix)
  endif
  if s:node_prefix =~ '^\s*$'
    let indent -= len(s:node_prefix)
  endif

  return indent
endfunction " }}}

" s:GetLastLine() {{{
function! s:GetLastLine()
  let line = line('$')
  while getline(line) =~ '^"\|^\s*$' && line > 1
    let line -= 1
  endwhile
  return line
endfunction " }}}

" GetChildIndent() {{{
function! eclim#tree#GetChildIndent(line)
  let indent = ''
  if getline(a:line) =~ '\s*' . s:node_prefix
    let num = indent(a:line)

    if s:node_prefix =~ '^\s*$'
      let num -= len(s:node_prefix)
    endif

    let index = 0
    while index < num + s:indent_length
      let indent .= ' '
      let index += 1
    endwhile
  endif

  return indent
endfunction " }}}

" s:MatchesFilter(file) {{{
function! s:MatchesFilter(file)
  if len(b:filters) > 0
    for filter in b:filters
      if entry =~ filter
        return 1
      endif
    endfor
    return 0
  endif

  return 1
endfunction " }}}

" s:IsHidden(path, ignore_pattern) {{{
function! s:IsHidden(path, ignore_pattern)
  if !b:view_hidden
    let path = a:path
    if isdirectory(path)
      let path = fnamemodify(path, ':h')
    endif
    let path = fnamemodify(path, ':t')
    return path =~ '^\.' || (a:ignore_pattern != '' && path =~ a:ignore_pattern)
  endif
  return 0
endfunction " }}}

" s:NormalizeEntries(dirs) {{{
function! s:NormalizeEntries(dirs)
  " normalize path separators
  call map(a:dirs, 'substitute(v:val, "\\\\", "/", "g")')

  let dirs = filter(copy(a:dirs),
    \ 'v:val =~ "/$" || (v:val =~ "@$" && isdirectory(substitute(v:val, "@$", "", "")))')
  let files = filter(copy(a:dirs), 'index(dirs, v:val) == -1')

  return [dirs, files]
endfunction " }}}

" s:UpdateLine(pattern, substitution) {{{
function! s:UpdateLine(pattern, substitution)
  let lnum = line('.')
  let line = getline(lnum)
  let line = substitute(line, a:pattern, a:substitution, '')

  setlocal noreadonly modifiable
  call append(lnum, line)
  silent exec lnum . ',' . lnum . 'delete _'
  setlocal nomodifiable
endfunction " }}}

" DisplayActionChooser(file, actions, executeFunc) {{{
function! eclim#tree#DisplayActionChooser(file, actions, executeFunc)
  new
  let height = len(a:actions) + 1

  exec 'resize ' . height

  setlocal noreadonly modifiable
  let b:actions = a:actions
  let b:file = a:file
  for action in a:actions
    call append(line('$'), action.name)
  endfor

  exec 'nnoremap <buffer> <silent> <cr> ' .
    \ ':call eclim#tree#ActionExecute("' . a:executeFunc . '")<cr>'
  nnoremap <buffer> q :q<cr>

  exec "hi link TreeAction " . g:TreeActionHighlight
  syntax match TreeAction /.*/

  1,1delete _
  setlocal nomodifiable
  setlocal noswapfile
  setlocal buftype=nofile
  setlocal bufhidden=delete
endfunction "}}}

" ActionExecute(executeFunc) {{{
function! eclim#tree#ActionExecute(executeFunc)
  let command = ''
  let line = getline('.')
  for action in b:actions
    if action.name == line
      let command = action.action
      break
    endif
  endfor

  let file = b:file
  close
  call function(a:executeFunc)(file, command)
endfunction "}}}

" s:Mappings() {{{
function! s:Mappings()
  nnoremap <buffer> <silent> <cr> :call eclim#tree#Execute(0)<cr>
  nnoremap <buffer> <silent> o    :call eclim#tree#Execute(1)<cr>

  nnoremap <buffer> <silent> i    :call eclim#util#Echo(
    \ eclim#tree#GetFileInfo(eclim#tree#GetPath()))<cr>
  nnoremap <buffer> <silent> I    :call eclim#util#Echo(
    \ eclim#tree#GetFileInfo(eclim#tree#GetPath()))<cr>

  nnoremap <buffer> <silent> s    :call eclim#tree#Shell(0)<cr>
  nnoremap <buffer> <silent> S    :call eclim#tree#Shell(1)<cr>

  nnoremap <buffer> <silent> R    :call eclim#tree#Refresh()<cr>

  nnoremap <buffer> <silent> A    :call eclim#tree#ToggleViewHidden()<cr>

  nnoremap <buffer> <silent> ~    :call eclim#tree#SetRoot(eclim#UserHome())<cr>
  nnoremap <buffer> <silent> C    :call eclim#tree#SetRoot(eclim#tree#GetPath())<cr>
  nnoremap <buffer> <silent> K    :call eclim#tree#SetRoot(substitute(
    \ <SID>PathToAlias(eclim#tree#GetRoot()),
    \ '^\([^/]*/\).*', '\1', ''))<cr>
  nnoremap <buffer> <silent> B    :call eclim#tree#SetRoot(
    \ fnamemodify(eclim#tree#GetRoot(), ':h:h'))<cr>

  nnoremap <buffer> <silent> j    :TreeNextPrevLine j<cr>
  nnoremap <buffer> <silent> k    :TreeNextPrevLine k<cr>
  nnoremap <buffer> <silent> p    :call eclim#tree#MoveToParent()<cr>
  nnoremap <buffer> <silent> P    :call eclim#tree#MoveToLastChild()<cr>

  nnoremap <buffer> <silent> D    :call eclim#tree#Mkdir()<cr>

  let ctrl_l = escape(maparg('<c-l>'), '|')
  exec 'nnoremap <buffer> <silent> <c-l> :silent doautocmd eclim_tree User <buffer><cr>' . ctrl_l

  command! -nargs=1 -complete=dir -buffer CD :call eclim#tree#SetRoot('<args>')
  command! -nargs=1 -complete=dir -buffer Cd :call eclim#tree#SetRoot('<args>')

  " only needed as a command to support counts on the j/k mappings
  command! -nargs=? -count=1 -buffer TreeNextPrevLine
    \ let c = <count> |
    \ let c = c > 1 ? c - line('.') + 1 : c |
    \ let prev = line('.') |
    \ exec 'normal! ' . c . '<args>' |
    \ call eclim#tree#Cursor(line('.'), prev)
endfunction " }}}

" s:Syntax() {{{
function! eclim#tree#Syntax()
  exec "hi link TreeDir " . g:TreeDirHighlight
  exec "hi link TreeFile " . g:TreeFileHighlight
  exec "hi link TreeFileExecutable " . g:TreeFileExecutableHighlight
  hi link TreeMarker Normal
  syntax match TreeMarker /^\s*[-+]/
  syntax match TreeDir /\S.*\// contains=TreeMarker
  syntax match TreeFile /\S.*[^\/]$/
  syntax match TreeFileExecutable /\S.*[^\/]\*$/
  syntax match Comment /^".*/
endfunction " }}}

" vim:ft=vim:fdm=marker
