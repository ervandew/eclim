" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Functions for working with version control systems.
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

runtime autoload/eclim/vcs/util.vim

" Global Variables {{{
  if !exists('g:EclimVcsLogMaxEntries')
    let g:EclimVcsLogMaxEntries = 50
  endif

  if !exists('g:EclimVcsDiffOrientation')
    let g:EclimVcsDiffOrientation = 'vertical'
  endif

  if !exists('g:EclimVcsTrackerIdPatterns')
    let g:EclimVcsTrackerIdPatterns = ['#\(\d\+\)']
  endif
" }}}

" Script Variables {{{
  let eclim#vcs#command#EclimVcsTrackerIdPatterns = g:EclimVcsTrackerIdPatterns
  let s:trackerIdPattern = join(eclim#vcs#command#EclimVcsTrackerIdPatterns, '\|')
" }}}

" Annotate([revision]) {{{
function! eclim#vcs#command#Annotate(...)
  if exists('b:vcs_annotations')
    call s:AnnotateOff()
    return
  endif

  let path = exists('b:vcs_props') ? b:vcs_props.path : expand('%:p')
  let revision = len(a:000) > 0 ? a:000[0] : ''

  " let the vcs annotate the current working version so that the results line
  " up with the contents (assuming the underlying vcs supports it).
  "if revision == ''
  "  let revision = eclim#vcs#util#GetRevision()
  "endif

  let dir = fnamemodify(path, ':h')
  let cwd = getcwd()
  if isdirectory(dir)
    exec 'lcd ' . escape(dir, ' ')
  endif
  try
    let Annotate = eclim#vcs#util#GetVcsFunction('GetAnnotations')
    if type(Annotate) != 2
      call eclim#util#EchoError(
        \ 'Current file is not under cvs, svn, hg, or git version control.')
      return
    endif
    let annotations = Annotate(revision)
  finally
    exec 'lcd ' . escape(cwd, ' ')
  endtry

  call s:ApplyAnnotations(annotations)
endfunction " }}}

" ChangeSet(path, revision) {{{
" Opens a buffer with change set info for the supplied revision.
function! eclim#vcs#command#ChangeSet(path, revision)
  if a:path == ''
    call eclim#util#EchoError('File is not under version control.')
    return
  endif

  let path = substitute(a:path, '\', '/', 'g')
  let revision = a:revision
  if revision == ''
    let revision = eclim#vcs#util#GetRevision(path)
    if type(revision) != 1 && revision == 0
      call eclim#util#Echo('Unable to determine file revision.')
      return
    endif
  elseif revision == 'prev'
    let revision = eclim#vcs#util#GetPreviousRevision(path)
  endif

  let cwd = getcwd()
  let dir = fnamemodify(path, ':h')
  if isdirectory(dir)
    exec 'lcd ' . escape(dir, ' ')
  endif
  try
    let ChangeSet = eclim#vcs#util#GetVcsFunction('ChangeSet')
    if type(ChangeSet) != 2
      return
    endif
    let info = ChangeSet(revision)
  catch /E117:.*/
    let type = eclim#vcs#util#GetVcsType()
    call eclim#util#EchoError('This function is not supported by "' . type . '".')
    return
  finally
    exec 'lcd ' . escape(cwd, ' ')
  endtry
  let info.props = has_key(info, 'props') ? info.props : {}
  let info.props.view = 'changeset'
  let info.props.path = path
  let info.props.revision = revision

  call s:TempWindow(info.changeset)
  call s:LogSyntax()
  call s:LogMappings()

  let b:vcs_props = info.props
  exec 'lcd ' . escape(info.props.root_dir, ' ')

  call s:HistoryPush('eclim#vcs#command#ChangeSet', [path, a:revision])
endfunction " }}}

" Diff(path, revision) {{{
" Diffs the current file against the current or supplied revision.
function! eclim#vcs#command#Diff(path, revision)
  if a:path == ''
    call eclim#util#EchoError('File is not under version control.')
    return
  endif

  let path = substitute(a:path, '\', '/', 'g')
  let revision = a:revision
  if revision == ''
    let revision = eclim#vcs#util#GetRevision(path)
    if revision == ''
      call eclim#util#Echo('Unable to determine file revision.')
      return
    endif
  elseif revision == 'prev'
    let revision = eclim#vcs#util#GetPreviousRevision(path)
  endif

  let filename = expand('%:p')
  let buf1 = bufnr('%')

  let orien = g:EclimVcsDiffOrientation == 'horizontal' ? '' : 'vertical'
  call eclim#vcs#command#ViewFileRevision(path, revision, 'bel ' . orien . ' split')
  diffthis

  let b:filename = filename
  let b:vcs_diff_temp = 1
  augroup vcs_diff
    autocmd! BufWinLeave <buffer>
    call eclim#util#GoToBufferWindowRegister(b:filename)
    autocmd BufWinLeave <buffer> diffoff
  augroup END

  call eclim#util#GoToBufferWindow(buf1)
  diffthis
endfunction " }}}

" Info() {{{
" Retrieves and echos info on the current file.
function! eclim#vcs#command#Info()
  let cwd = getcwd()
  let dir = expand('%:p:h')
  exec 'lcd ' . escape(dir, ' ')
  try
    let Info = eclim#vcs#util#GetVcsFunction('Info')
    if type(Info) == 2
      call Info()
    endif
  finally
    exec 'lcd ' . escape(cwd, ' ')
  endtry
endfunction " }}}

" ListDir(path) {{{
" Opens a buffer with a directory listing of versioned files.
function! eclim#vcs#command#ListDir(path)
  let cwd = getcwd()
  let path = substitute(a:path, '\', '/', 'g')
  if isdirectory(path)
    exec 'lcd ' . escape(path, ' ')
  endif
  try
    let ListDir = eclim#vcs#util#GetVcsFunction('ListDir')
    if type(ListDir) != 2
      return
    endif
    let info = ListDir(path)
  finally
    exec 'lcd ' . escape(cwd, ' ')
  endtry

  call s:TempWindow(info.list)
  call s:LogSyntax()
  call s:LogMappings()

  let b:vcs_props = info.props
  let b:vcs_props.view = 'dir'
  let b:vcs_props.path = path
  exec 'lcd ' . escape(info.props.root_dir, ' ')

  call s:HistoryPush('eclim#vcs#command#ListDir', [path])
endfunction " }}}

" Log(path) {{{
" Opens a buffer with the contents of the log for the supplied url.
function! eclim#vcs#command#Log(path)
  if a:path == ''
    call eclim#util#EchoError('File is not under version control.')
    return
  endif

  let cwd = getcwd()
  let path = substitute(a:path, '\', '/', 'g')
  let dir = fnamemodify(path, ':h')
  if isdirectory(dir)
    exec 'lcd ' . escape(dir, ' ')
  endif
  try
    let Log = eclim#vcs#util#GetVcsFunction('Log')
    if type(Log) != 2
      return
    endif
    let info = Log(path)
  finally
    exec 'lcd ' . escape(cwd, ' ')
  endtry
  if g:EclimVcsLogMaxEntries > 0 && len(info.log) == g:EclimVcsLogMaxEntries
    call add(info.log, '------------------------------------------')
    call add(info.log, 'Note: entries limited to ' . g:EclimVcsLogMaxEntries . '.')
    call add(info.log, '      let g:EclimVcsLogMaxEntries = ' . g:EclimVcsLogMaxEntries)
  endif
  let info.props = has_key(info, 'props') ? info.props : {}
  let info.props.view = 'log'
  let info.props.path = path

  " if annotations are on, jump to the revision for the current line
  let jumpto = ''
  if exists('b:vcs_annotations') && len(b:vcs_annotations) >= line('.')
    let jumpto = split(b:vcs_annotations[line('.') - 1])[0]
  endif

  call s:TempWindow(info.log)
  call s:LogSyntax()
  call s:LogMappings()

  " continuation of annotation support
  if jumpto != ''
    call search('^Revision: |' . jumpto)
    normal! z
  endif

  let b:vcs_props = info.props
  exec 'lcd ' . escape(info.props.root_dir, ' ')

  call s:HistoryPush('eclim#vcs#command#Log', [path])
endfunction " }}}

" ViewFileRevision(path, revision, open_cmd) {{{
" Open a read only view for the revision of the supplied version file.
function! eclim#vcs#command#ViewFileRevision(path, revision, open_cmd)
  if a:path == ''
    call eclim#util#EchoError('File is not under version control.')
    return
  endif

  let path = substitute(a:path, '\', '/', 'g')
  let revision = a:revision
  if revision == ''
    let revision = eclim#vcs#util#GetRevision(path)
    if revision == ''
      call eclim#util#Echo('Unable to determine file revision.')
      return
    endif
  elseif revision == 'prev'
    let revision = eclim#vcs#util#GetPreviousRevision(path)
  endif

  let props = exists('b:vcs_props') ? b:vcs_props : {}

  if exists('b:filename')
    call eclim#util#GoToBufferWindow(b:filename)
  endif
  let vcs_file = 'vcs_' . revision . '_' . fnamemodify(path, ':t')

  let g:EclimTemplateTempIgnore = 1
  try
    let open_cmd = a:open_cmd != '' ? a:open_cmd : 'split'
    if has('win32') || has('win64')
      let vcs_file = substitute(vcs_file, ':', '_', 'g')
    endif
    call eclim#util#GoToBufferWindowOrOpen(vcs_file, open_cmd)
  finally
    unlet g:EclimTemplateTempIgnore
  endtry

  setlocal noreadonly
  setlocal modifiable
  silent 1,$delete _

  let b:vcs_props = copy(props)
  let b:vcs_props.view = 'cat'

  " load in content
  let cwd = getcwd()
  let dir = fnamemodify(path, ':h')
  if has_key(props, 'root_dir')
    let dir = b:vcs_props.root_dir . '/' . dir
  endif
  if isdirectory(dir)
    exec 'lcd ' . escape(dir, ' ')
    let path = fnamemodify(path, ':t')
  endif
  try
    let ViewFileRevision = eclim#vcs#util#GetVcsFunction('ViewFileRevision')
    if type(ViewFileRevision) != 2
      return
    endif
    let lines = ViewFileRevision(path, revision)
  finally
    exec 'lcd ' . escape(cwd, ' ')
  endtry

  call append(1, lines)
  silent 1,1delete
  call cursor(1, 1)

  setlocal nomodified
  setlocal readonly
  setlocal nomodifiable
  setlocal noswapfile
  setlocal nobuflisted
  setlocal buftype=nofile
  setlocal bufhidden=delete
  doautocmd BufReadPost
endfunction " }}}

" s:ApplyAnnotations(annotations) {{{
function! s:ApplyAnnotations(annotations)
  let existing = {}
  let existing_annotations = {}
  for exists in eclim#display#signs#GetExisting()
    if exists.name !~ '^\(vcs_annotate_\|placeholder\)'
      let existing[exists.line] = exists
    else
      let existing_annotations[exists.line] = exists
    endif
  endfor

  let defined = eclim#display#signs#GetDefined()
  let index = 1
  for annotation in a:annotations
    if annotation == 'uncommitted'
      if has_key(existing_annotations, index)
        call eclim#display#signs#Unplace(existing_annotations[index].id)
      endif
      let index += 1
      continue
    endif

    if has_key(existing, index)
      let index += 1
      continue
    endif

    let user = substitute(annotation, '^.\{-})\s\+\(.\{-}\)\s*$', '\1', '')
    let user_abbrv = user[:1]
    let name_parts = split(user)
    " if the user name appears to be in the form of First Last, then try using
    " using the first letter of each as initials
    if len(name_parts) > 1 && name_parts[0] =~ '^\w' && name_parts[1] =~ '^\w'
      let user_abbrv = name_parts[0][0] . name_parts[1][0]
    endif
    let sign_name = 'vcs_annotate_' . substitute(user[:5], ' ', '_', 'g')
    if index(defined, sign_name) == -1
      call eclim#display#signs#Define(sign_name, user_abbrv, g:EclimInfoHighlight)
      call add(defined, sign_name)
    endif
    call eclim#display#signs#Place(sign_name, index)
    let index += 1
  endfor

  let b:vcs_annotations = a:annotations
  call s:AnnotateInfo()

  augroup vcs_annotate
    autocmd!
    autocmd CursorMoved <buffer> call <SID>AnnotateInfo()
    autocmd BufWritePost <buffer>
      \ if !eclim#util#WillWrittenBufferClose() |
      \   if exists('b:vcs_annotations') |
      \     unlet b:vcs_annotations |
      \   endif |
      \   call eclim#vcs#command#Annotate() |
      \ endif
  augroup END
endfunction " }}}

" s:AnnotateInfo() {{{
function! s:AnnotateInfo()
  if exists('b:vcs_annotations') && len(b:vcs_annotations) >= line('.')
    call eclim#util#WideMessage('echo', b:vcs_annotations[line('.') - 1])
  endif
endfunction " }}}

" s:AnnotateOff() {{{
function! s:AnnotateOff()
  if exists('b:vcs_annotations')
    let defined = eclim#display#signs#GetDefined()
    for annotation in b:vcs_annotations
      let user = substitute(annotation, '^.\{-})\s\+\(.\{-}\)\s*$', '\1', '')
      let sign_name = 'vcs_annotate_' . substitute(user[:5], ' ', '_', 'g')
      if index(defined, sign_name) != -1
        let signs = eclim#display#signs#GetExisting(sign_name)
        for sign in signs
          call eclim#display#signs#Unplace(sign.id)
        endfor
        call eclim#display#signs#Undefine(sign_name)
        call remove(defined, index(defined, sign_name))
      endif
    endfor
    unlet b:vcs_annotations
  endif
  augroup vcs_annotate
    autocmd!
  augroup END
endfunction " }}}

" s:FollowLink() {{{
function! s:FollowLink()
  let line = getline('.')
  let link = substitute(
    \ getline('.'), '.*|\(.\{-}\%' . col('.') . 'c.\{-}\)|.*', '\1', '')
  if link == line
    return
  endif

  let view = exists('b:vcs_props') && has_key(b:vcs_props, 'view') ?
    \ b:vcs_props.view : ''

  try
    " link to folder
    if line('.') == 1
      let line = getline('.')
      let path = substitute(
        \ line, '.\{-}/\(.\{-}\%' . col('.') . 'c.\{-}\)|.*', '\1', '')
      if path == line
        let path = ''
      else
        let path = substitute(path, '| / |', '/', 'g')
        let path = substitute(path, '\(^\s\+\||\)', '', 'g')
      endif

      call eclim#vcs#command#ListDir(path)

    " link to file or dir in directory listing view.
    elseif view == 'dir'
      let line = getline(1)

      let path = ''
      if line != '/'
        let path = substitute(line, '.\{-}/ |\?\(.*\)', '\1', '')
        let path = substitute(path, '\(| / |\|| / \)', '/', 'g')
        let path = substitute(path, '\(^\s\+\||\)', '', 'g')
        let path .= '/'
      endif
      let path .= link

      if path =~ '/$'
        call eclim#vcs#command#ListDir(path)
      else
        call eclim#vcs#command#Log(path)
      endif

    " link to file or dir in change set view.
    elseif link !~ '^#' && view == 'changeset'
      let revision = b:vcs_props.revision
      if link == 'M'
        let file = substitute(line, '\s*|M|\s*|\(.\{-}\)|.*', '\1', '')
        let r2 = eclim#vcs#util#GetPreviousRevision(file, revision)
        call eclim#vcs#command#ViewFileRevision(file, revision, '')
        let buf1 = bufnr('%')

        let orien = g:EclimVcsDiffOrientation == 'horizontal' ? '' : 'vertical'
        call eclim#vcs#command#ViewFileRevision(file, r2, 'bel ' . orien . ' split')
        diffthis
        call eclim#util#GoToBufferWindow(buf1)
        diffthis
      elseif link !~ '^\s*$'
        call eclim#vcs#command#Log(link)
      endif

    " link to view a change set
    elseif link =~ '^[0-9a-f.:]\+$'
      let file = s:GetBreadcrumbPath()
      call eclim#vcs#command#ChangeSet(b:vcs_props.path, link)

    " link to view / annotate a file
    elseif link == 'view' || link == 'annotate'
      let file = s:GetBreadcrumbPath()
      let revision = substitute(getline('.'), 'Revision: \(.\{-}\) .*', '\1', '')
      let revision = substitute(revision, '\(^|\||$\)', '', 'g')

      call eclim#vcs#command#ViewFileRevision(file, revision, '')
      if link == 'annotate'
        call eclim#vcs#command#Annotate(revision)
      endif

    " link to diff one version against previous
    elseif link =~ '^previous .*$'
      let file = s:GetBreadcrumbPath()
      let r1 = substitute(getline(line('.') - 2), 'Revision: \(.\{-}\) .*', '\1', '')
      let r1 = substitute(r1, '\(^|\||$\)', '', 'g')
      let r2 = substitute(link, 'previous \(.*\)', '\1', '')

      call eclim#vcs#command#ViewFileRevision(file, r1, '')
      let buf1 = bufnr('%')
      let orien = g:EclimVcsDiffOrientation == 'horizontal' ? '' : 'vertical'
      call eclim#vcs#command#ViewFileRevision(file, r2, 'bel ' . orien . ' split')
      diffthis
      call eclim#util#GoToBufferWindow(buf1)
      diffthis

    " link to diff against working copy
    elseif link == 'working copy'
      let file = s:GetBreadcrumbPath()
      let revision = substitute(
        \ getline(line('.') - 2), 'Revision: |\?\([0-9a-z.]\+\)|\?.*', '\1', '')

      let filename = b:filename
      let orien = g:EclimVcsDiffOrientation == 'horizontal' ? '' : 'vertical'
      call eclim#vcs#command#ViewFileRevision(file, revision, 'bel ' . orien . ' split')
      diffthis

      let b:filename = filename
      augroup vcs_diff
        autocmd! BufWinLeave <buffer>
        call eclim#util#GoToBufferWindowRegister(b:filename)
        autocmd BufWinLeave <buffer> diffoff
      augroup END

      call eclim#util#GoToBufferWindow(filename)
      diffthis

    " link to bug / feature report
    elseif link =~ '^' . s:trackerIdPattern . '$'
      let cwd = getcwd()
      let dir = fnamemodify(b:filename, ':h')
      exec 'lcd ' . escape(dir, ' ')
      try
        let url = eclim#project#util#GetProjectSetting('org.eclim.project.tracker')
      finally
        exec 'lcd ' . escape(cwd, ' ')
      endtry

      if type(url) == 0
        return
      endif

      if url == ''
        call eclim#util#EchoWarning(
          \ "Link to bug report / feature request requires project setting " .
          \ "'org.eclim.project.tracker'.")
        return
      endif

      for pattern in g:EclimVcsTrackerIdPatterns
        if link =~ pattern
          let id = substitute(link, pattern, '\1', '')
          break
        endif
      endfor
      let url = substitute(url, '<id>', id, 'g')
      call eclim#web#OpenUrl(url)
    endif
  catch /vcs error/
    " the error message is printed by eclim#vcs#util#Vcs
  endtry
endfunction " }}}

" s:GetBreadcrumbPath() {{{
function! s:GetBreadcrumbPath()
  let path = substitute(getline(1), ' / ', '/', 'g')
  let path = substitute(path, '.\{-}/\(.*\)', '\1', '')
  let path = substitute(path, '^|', '', 'g')
  let path = substitute(path, '\(|/|\||/\||\)', '/', 'g')
  return path
endfunction " }}}

" s:HistoryPop() {{{
function! s:HistoryPop()
  if exists('w:vcs_history') && len(w:vcs_history) > 1
    call remove(w:vcs_history, -1) " remove current page entry
    exec w:vcs_history[-1]
    call remove(w:vcs_history, -1) " remove entry added by going back
  endif
endfunction " }}}

" s:HistoryPush(command) {{{
function! s:HistoryPush(name, args)
  if !exists('w:vcs_history')
    let w:vcs_history = []
  endif

  let command = 'call ' . a:name . '('
  let index = 0
  for arg in a:args
    if index != 0
      let command .= ', '
    endif
    let command .= '"' . arg . '"'
    let index += 1
  endfor
  let command .= ')'
  call add(w:vcs_history, command)
endfunction " }}}

" s:LogMappings() {{{
function! s:LogMappings()
  nnoremap <silent> <buffer> <cr> :call <SID>FollowLink()<cr>
  nnoremap <silent> <buffer> <c-o> :call <SID>HistoryPop()<cr>
endfunction " }}}

" s:LogSyntax() {{{
function! s:LogSyntax()
  set ft=vcs_log
  hi link VcsDivider Constant
  hi link VcsHeader Identifier
  hi link VcsLink Label
  syntax match VcsDivider /^-\+$/
  syntax match VcsLink /|\S.\{-}\S|/
  syntax match VcsHeader /^\(Revision\|Modified\|Diff\|Changed paths\):/
endfunction " }}}

" s:TempWindow(lines) {{{
function! s:TempWindow(lines)
  let filename = expand('%:p')
  if expand('%') == '[vcs_log]' && exists('b:filename')
    let filename = b:filename
  endif

  call eclim#util#TempWindow('[vcs_log]', a:lines)

  let b:filename = filename
  augroup eclim_temp_window
    autocmd! BufWinLeave <buffer>
    call eclim#util#GoToBufferWindowRegister(b:filename)
  augroup END
endfunction " }}}

" vim:ft=vim:fdm=marker
