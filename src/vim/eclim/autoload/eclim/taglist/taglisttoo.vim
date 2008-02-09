" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/taglist.html
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

" Global Variables {{{

" Tag listing sort type - 'name' or 'order'
if !exists('Tlist_Sort_Type')
  let Tlist_Sort_Type = 'order'
endif

" Open the vertically split taglist window on the left or on the right
" side.  This setting is relevant only if Tlist_Use_Horiz_Window is set to
" zero (i.e.  only for vertically split windows)
if !exists('Tlist_Use_Right_Window')
  let Tlist_Use_Right_Window = 0
endif

" Vertically split taglist window width setting
if !exists('Tlist_WinWidth')
  let Tlist_WinWidth = 30
endif

" }}}

" Script Variables {{{
  let s:taglisttoo_ignore = g:TagList_title . '\|ProjectTree'
" }}}

" Language Settings {{{
" assembly language
let s:tlist_def_asm_settings = {
    \ 'd': 'define',
    \ 'l': 'label',
    \ 'm': 'macro',
    \ 't': 'type'
  \ }

" aspperl language
let s:tlist_def_aspperl_settings = {
    \ 'f': 'function',
    \ 's': 'sub',
    \ 'v': 'variable'
  \ }

" aspvbs language
let s:tlist_def_aspvbs_settings = {
    \ 'f': 'function',
    \ 's': 'sub',
    \ 'v': 'variable'
  \ }

" awk language
let s:tlist_def_awk_settings = {'f': 'function'}

" beta language
let s:tlist_def_beta_settings = {
    \ 'f': 'fragment',
    \ 's': 'slot',
    \ 'v': 'pattern'
  \ }

" c language
let s:tlist_def_c_settings = {
    \ 'd': 'macro',
    \ 'g': 'enum',
    \ 's': 'struct',
    \ 'u': 'union',
    \ 't': 'typedef',
    \ 'v': 'variable',
    \ 'f': 'function'
  \ }

" c++ language
let s:tlist_def_cpp_settings = {
    \ 'n': 'namespace',
    \ 'v': 'variable',
    \ 'd': 'macro',
    \ 't': 'typedef',
    \ 'c': 'class',
    \ 'g': 'enum',
    \ 's': 'struct',
    \ 'u': 'union',
    \ 'f': 'function'
  \ }

" c# language
let s:tlist_def_cs_settings = {
    \ 'd': 'macro',
    \ 't': 'typedef',
    \ 'n': 'namespace',
    \ 'c': 'class',
    \ 'E': 'event',
    \ 'g': 'enum',
    \ 's': 'struct',
    \ 'i': 'interface',
    \ 'p': 'properties',
    \ 'm': 'method'
  \ }

" cobol language
let s:tlist_def_cobol_settings = {
    \ 'd': 'data',
    \ 'f': 'file',
    \ 'g': 'group',
    \ 'p': 'paragraph',
    \ 'P': 'program',
    \ 's': 'section'
  \ }

" eiffel language
let s:tlist_def_eiffel_settings = {
    \ 'c': 'class',
    \ 'f': 'feature'
  \ }

" erlang language
let s:tlist_def_erlang_settings = {
    \ 'd': 'macro',
    \ 'r': 'record',
    \ 'm': 'module',
    \ 'f': 'function'
  \ }

" expect (same as tcl) language
let s:tlist_def_expect_settings = {
    \ 'c': 'class',
    \ 'f': 'method',
    \ 'p': 'procedure'
  \ }

" fortran language
let s:tlist_def_fortran_settings = {
    \ 'p': 'program',
    \ 'b': 'block data',
    \ 'c': 'common',
    \ 'e': 'entry',
    \ 'i': 'interface',
    \ 'k': 'type',
    \ 'l': 'label',
    \ 'm': 'module',
    \ 'n': 'namelist',
    \ 't': 'derived',
    \ 'v': 'variable',
    \ 'f': 'function',
    \ 's': 'subroutine'
  \ }

" HTML language
let s:tlist_def_html_settings = {
    \ 'a': 'anchor',
    \ 'f': 'javascript function'
  \ }

" java language
let s:tlist_format_java = 'eclim#taglist#java#FormatJava'
let s:tlist_def_java_settings = {
    \ 'p': 'package',
    \ 'c': 'class',
    \ 'i': 'interface',
    \ 'f': 'field',
    \ 'm': 'method'
  \ }

let s:tlist_format_javascript = 'eclim#taglist#javascript#FormatJavascript'
let s:tlist_def_javascript_settings = {
    \ 'o': 'object',
    \ 'm': 'member',
    \ 'f': 'function',
  \ }

" javascript language
let s:tlist_def_javascript_settings = {'f': 'function'}

" lisp language
let s:tlist_def_lisp_settings = {'f': 'function'}

" lua language
let s:tlist_def_lua_settings = {'f': 'function'}

" makefiles
let s:tlist_def_make_settings = {'m': 'macro'}

" pascal language
let s:tlist_def_pascal_settings = {
    \ 'f': 'function',
    \ 'p': 'procedure'
  \ }

" perl language
let s:tlist_def_perl_settings = {
    \ 'c': 'constant',
    \ 'l': 'label',
    \ 'p': 'package',
    \ 's': 'subroutine'
  \ }

" php language
let s:tlist_format_php = 'eclim#taglist#php#FormatPhp'
let s:tlist_def_php_settings = {
    \ 'c': 'class',
    \ 'd': 'constant',
    \ 'v': 'variable',
    \ 'f': 'function'
  \ }

" python language
let s:tlist_format_python = 'eclim#taglist#python#FormatPython'
let s:tlist_def_python_settings = {
    \ 'c': 'class',
    \ 'm': 'member',
    \ 'f': 'function'
  \ }

" rexx language
let s:tlist_def_rexx_settings = {'s': 'subroutine'}

" ruby language
let s:tlist_def_ruby_settings = {
    \ 'c': 'class',
    \ 'f': 'method',
    \ 'F': 'function',
    \ 'm': 'singleton method'
  \ }

" scheme language
let s:tlist_def_scheme_settings = {
    \ 's': 'set',
    \ 'f': 'function'
  \ }

" shell language
let s:tlist_def_sh_settings = {'f': 'function'}

" C shell language
let s:tlist_def_csh_settings = {'f': 'function'}

" Z shell language
let s:tlist_def_zsh_settings = {'f': 'function'}

" slang language
let s:tlist_def_slang_settings = {
    \ 'n': 'namespace',
    \ 'f': 'function'
  \ }

" sml language
let s:tlist_def_sml_settings = {
    \ 'e': 'exception',
    \ 'c': 'functor',
    \ 's': 'signature',
    \ 'r': 'structure',
    \ 't': 'type',
    \ 'v': 'value',
    \ 'f': 'function'
  \ }

" sql language
let s:tlist_def_sql_settings = {
    \ 'c': 'cursor',
    \ 'F': 'field',
    \ 'P': 'package',
    \ 'r': 'record',
    \ 's': 'subtype',
    \ 't': 'table',
    \ 'T': 'trigger',
    \ 'v': 'variable',
    \ 'f': 'function',
    \ 'p': 'procedure'
  \ }

" tcl language
let s:tlist_def_tcl_settings = {
    \ 'c': 'class',
    \ 'f': 'method',
    \ 'm': 'method',
    \ 'p': 'procedure'
  \ }

" vera language
let s:tlist_def_vera_settings = {
    \ 'c': 'class',
    \ 'd': 'macro',
    \ 'e': 'enumerator',
    \ 'f': 'function',
    \ 'g': 'enum',
    \ 'm': 'member',
    \ 'p': 'program',
    \ 'P': 'prototype',
    \ 't': 'task',
    \ 'T': 'typedef',
    \ 'v': 'variable',
    \ 'x': 'externvar'
  \ }

"verilog language
let s:tlist_def_verilog_settings = {
    \ 'm': 'module',
    \ 'c': 'constant',
    \ 'P': 'parameter',
    \ 'e': 'event',
    \ 'r': 'register',
    \ 't': 'task',
    \ 'w': 'write',
    \ 'p': 'port',
    \ 'v': 'variable',
    \ 'f': 'function'
  \ }

" vim language
let s:tlist_def_vim_settings = {
    \ 'a': 'autocmds',
    \ 'v': 'variable',
    \ 'f': 'function'
  \ }

" yacc language
let s:tlist_def_yacc_settings = {'l': 'label'}
" }}}

" AutoOpen() {{{
function! eclim#taglist#taglisttoo#AutoOpen()
  let open_window = 0

  let i = 1
  let buf_num = winbufnr(i)
  while buf_num != -1
    let filename = fnamemodify(bufname(buf_num), ':p')
    if s:FileSupported(filename, getbufvar(buf_num, '&filetype'))
      let open_window = 1
      break
    endif
    let i = i + 1
    let buf_num = winbufnr(i)
  endwhile

  if open_window
    call eclim#taglist#taglisttoo#Taglist()
  endif
endfunction " }}}

" Taglist() {{{
function! eclim#taglist#taglisttoo#Taglist ()
  if !exists('g:Tlist_Ctags_Cmd')
    call eclim#util#EchoError('Unable to find a version of ctags installed.')
    return
  endif

  if bufname('%') == g:TagList_title
    call s:CloseTaglist()
    return
  endif

  let winnum = bufwinnr(g:TagList_title)
  if winnum != -1
    exe winnum . 'wincmd w'
    call s:CloseTaglist()
    return
  endif

  call s:ProcessTags()
  call s:StartAutocmds()

  augroup taglisttoo
    autocmd!
    autocmd BufEnter __Tag_List__ nested call s:ExitOnlyWindow()
    autocmd BufUnload __Tag_List__ call s:Cleanup()
    autocmd CursorHold * call s:ShowCurrentTag()
  augroup END
endfunction " }}}

" s:StartAutocmds() {{{
function! s:StartAutocmds ()
  augroup taglisttoo_file
    autocmd!
    autocmd BufEnter,BufWritePost *
      \ if bufwinnr(g:TagList_title) != -1 |
      \   call s:ProcessTags() |
      \ endif
  augroup END
endfunction " }}}

" s:StopAutocmds() {{{
function! s:StopAutocmds ()
  augroup taglisttoo_file
    autocmd!
  augroup END
endfunction " }}}

" s:CloseTaglist() {{{
function! s:CloseTaglist ()
  close
  call s:Cleanup()
endfunction " }}}

" s:Cleanup() {{{
function! s:Cleanup ()
  augroup taglisttoo_file
    autocmd!
  augroup END

  augroup taglisttoo
    autocmd!
  augroup END
endfunction " }}}

" s:ProcessTags() {{{
function! s:ProcessTags ()
  let file = expand('%')
  if file =~ s:taglisttoo_ignore || file == ''
    return
  endif

  let tags = []
  if s:FileSupported(expand('%:p'), &ft)
    if exists('g:tlist_{&ft}_settings')
      let settings = g:tlist_{&ft}_settings
      let types = join(keys(g:tlist_{&ft}_settings), '')
    else
      let settings = s:tlist_def_{&ft}_settings
      let types = join(keys(s:tlist_def_{&ft}_settings), '')
    endif

    let file = substitute(expand('%:p'), '\', '/', 'g')
    let command = g:Tlist_Ctags_Cmd . ' -f - --format=2 --excmd=pattern ' .
        \ '--fields=nks --sort=no --language-force=<lang> ' .
        \ '--<lang>-types=<types> "<file>"'
    let command = substitute(command, '<lang>', &ft, 'g')
    let command = substitute(command, '<types>', types, 'g')
    let command = substitute(command, '<file>', file, '')

    call eclim#util#EchoTrace("command: " . command)
    let results = split(eclim#util#System(command), '\n')
    if v:shell_error
      return
    endif

    if g:Tlist_Sort_Type == 'name'
      call sort(results)
    endif

    for result in results
      let pre = substitute(result, '\(.\{-}\)\t\/\^.*', '\1', '')
      let pattern = substitute(result, '.\{-}\(\/\^.*\$\/;"\).*', '\1', '')
      let post = substitute(result, '.*\$\/;"\t', '\1', '')
      let values = split(pre, '\t') + [pattern] + split(post, '\t')

      " filter false positives found in comments.
      if values[-1] =~ 'line:[0-9]\+'
        exec 'let lnum = ' . substitute(values[-1], 'line:\([0-9]\+\).*', '\1', '')
        let line = getline(lnum)
        let col = len(line) - len(substitute(line, '^\s*', '', '')) + 1
        if synIDattr(synID(lnum, col, 1), "name") =~ '\([Cc]omment\|[Ss]tring\)'
          continue
        endif
      endif

      call add(tags, values)
    endfor

    if exists('s:tlist_format_{&ft}')
      exec 'call s:Window(settings, tags, ' . s:tlist_format_{&ft} . '(settings, tags))'
    else
      call s:Window(settings, tags, s:FormatDefault(settings, tags))
    endif
  else
    call s:Window({}, tags, [[],[]])
  endif

  winc p
endfunction " }}}

" s:FormatDefault(types, tags) {{{
" All format functions must return a two element list containing:
" result[0] - A list of length len(result[1]) where each value specifies the
"             tag index such that result[0][line('.') - 1] == tag index for
"             the current line.
"             For content lines that do no map to a tag, use -1 as the value.
" result[1] - A list of lines to be inserted as content into the taglist
"             window.
function! s:FormatDefault (types, tags)
  let lines = []
  let content = []

  call add(content, expand('%:t'))
  call add(lines, -1)

  for key in keys(a:types)
    let values = filter(copy(a:tags), 'len(v:val) > 3 && v:val[3] == key')
    call eclim#taglist#util#FormatType(a:tags, a:types[key], values, lines, content, "\t")
  endfor

  return [lines, content]
endfunction " }}}

" s:JumpToTag() {{{
function! s:JumpToTag ()
  let index = b:taglisttoo_content[0][line('.') - 1]
  if index == -1
    return
  endif

  let tag_info = b:taglisttoo_tags[index]

  call s:StopAutocmds()
  exec bufwinnr(b:taglisttoo_file_bufnr) . 'winc w'
  call s:StartAutocmds()

  let lnum = s:GetTagLineNumber(tag_info)
  let pattern = eclim#taglist#util#GetTagPattern(tag_info)

  if getline(lnum) =~ escape(pattern, '*[]')
    mark '
    call cursor(lnum, 1)
    call s:ShowCurrentTag()
  else
    let clnum = line('.')
    let ccnum = col('.')

    call cursor(lnum, 1)

    let up = search(pattern, 'bcnW')
    let down = search(pattern, 'cnW')

    " pattern found below recorded line
    if !up && down
      let line = down

    " pattern found above recorded line
    elseif !down && up
      let line = up

    " pattern found above and below recorded line
    elseif up && down
      " use the closest match to the recorded line
      if (lnum - up) < (down - lnum)
        let line = up
      else
        let line = down
      endif

    " pattern not found.
    else
      let line = 0
    endif

    call cursor(clnum, ccnum)
    if line
      mark '
      call cursor(line, 1)
      call s:ShowCurrentTag()
    endif
  endif
endfunction " }}}

" s:Window(types, tags, content) {{{
function! s:Window (types, tags, content)
  let filename = expand('%:t')
  let file_bufnr = bufnr('%')

  let winnum = bufwinnr(g:TagList_title)
  if winnum != -1
    exe winnum . 'wincmd w'
  else
    let bufnum = bufnr(g:TagList_title)
    if bufnum == -1
      let wcmd = g:TagList_title
    else
      let wcmd = '+buffer' . bufnum
    endif

    "botright vertical new
    exec 'silent! topleft vertical ' . g:Tlist_WinWidth . 'split ' . wcmd

    setlocal filetype=taglist

    setlocal buftype=nofile
    setlocal bufhidden=delete
    setlocal noswapfile
    setlocal nobuflisted
    setlocal nonumber
    setlocal nowrap
    setlocal winfixwidth
    setlocal tabstop=2

    syn match TagListFileName "^.*\%1l.*"
    hi link TagListFileName Identifier
    hi link TagListKeyword Statement
    hi TagListCurrentTag term=bold,underline cterm=bold,underline gui=bold,underline

    nnoremap <silent> <buffer> <cr> :call <SID>JumpToTag()<cr>
  endif

  let saved = @"

  let clnum = line('.')
  let ccnum = line('.')
  setlocal modifiable
  silent 1,$delete
  call append(1, a:content[1])
  silent retab
  silent 1,1delete
  setlocal nomodifiable
  call cursor(clnum, ccnum)

  let @" = saved

  silent! syn clear TagListKeyword
  for value in values(a:types)
    exec 'syn keyword TagListKeyword ' . value
  endfor

  let b:taglisttoo_content = a:content
  let b:taglisttoo_tags = a:tags
  let b:taglisttoo_file_bufnr = file_bufnr
endfunction " }}}

" s:ShowCurrentTag() {{{
function! s:ShowCurrentTag ()
  if s:FileSupported(expand('%:p'), &ft) && bufwinnr(g:TagList_title) != -1
    let tags = getbufvar(g:TagList_title, 'taglisttoo_tags')
    let content = getbufvar(g:TagList_title, 'taglisttoo_content')

    let clnum = line('.')
    let tlnum = 0
    let tindex = -1

    let index = 0
    for tag in tags
      let lnum = s:GetTagLineNumber(tag)
      let diff = clnum - lnum
      if diff >= 0 && (diff < (clnum - tlnum))
        let tlnum = lnum
        let current = tag
        let tindex = index
      endif
      let index += 1
    endfor

    if exists('current')
      let cwinnum = winnr()
      let twinnum = bufwinnr(g:TagList_title)

      call eclim#util#ExecWithoutAutocmds(twinnum . 'winc w')

      let index = index(content[0], tindex) + 1
      syn clear TagListCurrentTag
      exec 'syn match TagListCurrentTag "\S*\%' . index . 'l\S*"'
      if index != line('.')
        call cursor(index, 0)
        call winline()
      endif

      call eclim#util#ExecWithoutAutocmds(cwinnum . 'winc w')
    endif
  endif
endfunction " }}}

" s:ExitOnlyWindow () {{{
function! s:ExitOnlyWindow()
  " Before quitting Vim, delete the taglist buffer so that
  " the '0 mark is correctly set to the previous buffer.
  if v:version < 700
    if winbufnr(2) == -1
      bdelete
      quit
    endif
  else
    if winbufnr(2) == -1
      if tabpagenr('$') == 1
        " Only one tag page is present
        bdelete
        quit
      else
        " More than one tab page is present. Close only the current
        " tab page
        close
      endif
    endif
  endif
endfunction " }}}

" s:FileSupported() {{{
" Check whether tag listing is supported for the specified file
function! s:FileSupported(filename, ftype)
  " Skip buffers with no names and buffers with filetype not set
  if a:filename == '' || a:ftype == ''
    return 0
  endif

  " Skip files which are not supported by exuberant ctags
  " First check whether default settings for this filetype are available.
  " If it is not available, then check whether user specified settings are
  " available. If both are not available, then don't list the tags for this
  " filetype
  let var = 's:tlist_def_' . a:ftype . '_settings'
  if !exists(var)
    let var = 'g:tlist_' . a:ftype . '_settings'
    if !exists(var)
      return 0
    endif
  endif

  " Skip files which are not readable or files which are not yet stored
  " to the disk
  if !filereadable(a:filename)
    return 0
  endif

  return 1
endfunction " }}}

" s:GetTagLineNumber(tag) {{{
function! s:GetTagLineNumber (tag)
  if len(a:tag) > 4
    return substitute(a:tag[4], '.*:\(.*\)', '\1', '')
  endif
  return 0
endfunction " }}}

" vim:ft=vim:fdm=marker
