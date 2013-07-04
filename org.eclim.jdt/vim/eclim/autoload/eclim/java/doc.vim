" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/java/doc.html
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

" Script Variables {{{
let s:command_comment =
  \ '-command javadoc_comment -p "<project>" -f "<file>" -o <offset> -e <encoding>'
let s:command_element_doc =
  \ '-command java_element_doc -p "<project>" -f "<file>" -o <offset> -l <length> -e <encoding>'
let s:command_doc_link = '-command java_element_doc -u "<url>"'
let s:command_source_dirs = '-command java_src_dirs -p "<project>"'
" }}}

function! eclim#java#doc#Comment() " {{{
  " Add / update the comments for the element under the cursor.

  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  call eclim#lang#SilentUpdate()

  let project = eclim#project#util#GetCurrentProjectName()
  let file = eclim#project#util#GetProjectRelativeFilePath()
  let offset = eclim#util#GetCurrentElementOffset()

  let command = s:command_comment
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')
  let command = substitute(command, '<offset>', offset, '')
  let command = substitute(command, '<encoding>', eclim#util#GetEncoding(), '')

  let result =  eclim#Execute(command)

  if result != "0"
    call eclim#util#Reload({'retab': 1})
    write
  endif
endfunction " }}}

function! eclim#java#doc#Preview() " {{{
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  if !eclim#java#util#IsValidIdentifier(expand('<cword>'))
    call eclim#util#EchoError
      \ ("Element under the cursor is not a valid java identifier.")
    return 0
  endif

  exec 'pedit +:call\ eclim#java#doc#PreviewOpen(' . bufnr('%') . ') [javadoc]'
endfunction " }}}

function! eclim#java#doc#PreviewOpen(bufnr_or_url) " {{{
  if a:bufnr_or_url =~ '^\d\+$'
    let curwin = winnr()
    exec bufwinnr(a:bufnr_or_url) . 'winc w'

    let project = eclim#project#util#GetCurrentProjectName()
    let file = eclim#lang#SilentUpdate(1, 1)
    let position = eclim#util#GetCurrentElementPosition()
    let offset = substitute(position, '\(.*\);\(.*\)', '\1', '')
    let length = substitute(position, '\(.*\);\(.*\)', '\2', '')

    exec curwin . 'winc w'

    let command = s:command_element_doc
    let command = substitute(command, '<project>', project, '')
    let command = substitute(command, '<file>', file, '')
    let command = substitute(command, '<offset>', offset, '')
    let command = substitute(command, '<length>', length, '')
    let command = substitute(command, '<encoding>', eclim#util#GetEncoding(), '')
  else
    let command = s:command_doc_link
    let command = substitute(command, '<url>', a:bufnr_or_url, '')
  endif

  let result =  eclim#Execute(command)
  if type(result) == g:DICT_TYPE
    if !exists('b:eclim_javadoc_stack')
      let b:eclim_javadoc_stack = []
      let b:eclim_javadoc_index = -1
    elseif b:eclim_javadoc_index >= 0
      let b:eclim_javadoc_stack = b:eclim_javadoc_stack[:b:eclim_javadoc_index]
    endif
    call add(b:eclim_javadoc_stack, result)
    let b:eclim_javadoc_index += 1
    let b:eclim_javadoc = result

    setlocal modifiable
    call append(0, split(result.text, '\n'))
    retab
    if getline('$') =~ '^\s*$'
      $,$delete _
    endif
    call cursor(1, 1)

  elseif type(result) == g:STRING_TYPE
    if result == ''
      call eclim#util#EchoWarning('No javadoc found.')
    else
      call eclim#util#EchoError(result)
    endif

    return
  endif

  setlocal wrap
  setlocal nomodifiable
  setlocal nolist
  setlocal noswapfile
  setlocal nobuflisted
  setlocal buftype=nofile
  setlocal bufhidden=delete
  setlocal conceallevel=2 concealcursor=ncv

  set ft=javadoc_preview
  hi link javadocPreviewLink Label
  syntax match javadocPreviewLinkStart contained /|/ conceal
  syntax match javadocPreviewLinkEnd contained /\[\d\+\]|/ conceal
  syntax region javadocPreviewLink start="|" end="" concealends
  syntax match javadocPreviewLink /|.\{-}\[\d\+\]|/
    \ contains=JavadocPreviewLinkStart,JavadocPreviewLinkEnd

  nnoremap <silent> <buffer> <cr> :call eclim#java#doc#PreviewLink()<cr>
  nnoremap <silent> <buffer> <c-]> :call eclim#java#doc#PreviewLink()<cr>
  nnoremap <silent> <buffer> <c-o> :call eclim#java#doc#PreviewHistory(-1)<cr>
  nnoremap <silent> <buffer> <c-i> :call eclim#java#doc#PreviewHistory(1)<cr>
endfunction " }}}

function! eclim#java#doc#PreviewLink() " {{{
  let line = getline('.')
  let cnum = col('.')
  if line[cnum - 1] == '|'
    let cnum += cnum > 1 && line[cnum - 2] == ']' ? -1 : 1
  endif
  let text = substitute(line, '.*|\(.\{-}\%' . cnum . 'c.\{-}\)|.*', '\1', '')
  if text == line || text !~ '\[\d\+]$'
    return
  endif

  exec 'let index = ' . substitute(text, '.*\[\(\d\+\)\]$', '\1', '')
  if !exists('b:eclim_javadoc') || len(b:eclim_javadoc.links) <= index
    return
  endif

  let url = b:eclim_javadoc.links[index].href
  if url =~ '^eclipse-javadoc:'
    exec 'pedit +:call\ eclim#java#doc#PreviewOpen("' . url . '") [javadoc]'
  else
    call eclim#web#OpenUrl(url)
  endif
endfunction " }}}

function! eclim#java#doc#PreviewHistory(offset) " {{{
  if !exists('b:eclim_javadoc_stack')
    return
  endif

  let index = b:eclim_javadoc_index + a:offset
  if index < 0 || index > len(b:eclim_javadoc_stack) -1
    return
  endif

  let result = b:eclim_javadoc_stack[index]
  let b:eclim_javadoc = result
  let b:eclim_javadoc_index = index

  setlocal modifiable
  1,$delete _
  call append(0, split(result.text, '\n'))
  retab
  if getline('$') =~ '^\s*$'
    $,$delete _
  endif
  setlocal nomodifiable
  call cursor(1, 1)
endfunction " }}}

function! eclim#java#doc#Javadoc(bang, ...) " {{{
  " Run javadoc for all, or the supplied, source files.
  " Optional args:
  "   file, file, file, ...: one ore more source files.

  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  let project_path = eclim#project#util#GetCurrentProjectRoot()
  let project = eclim#project#util#GetCurrentProjectName()
  let args = '-p "' . project . '"'

  if len(a:000) > 0 && (len(a:000) > 1 || a:000[0] != '')
    let args .= ' -f "' . join(a:000, ' ') . '"'
  endif

  let cwd = getcwd()
  try
    exec 'lcd ' . escape(project_path, ' ')
    call eclim#util#MakeWithCompiler('eclim_javadoc', a:bang, args)
  finally
    exec 'lcd ' . escape(cwd, ' ')
  endtry
endfunction " }}}

function! eclim#java#doc#CommandCompleteJavadoc(argLead, cmdLine, cursorPos) " {{{
  let dir = eclim#project#util#GetCurrentProjectRoot()

  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let args = eclim#util#ParseCmdLine(cmdLine)
  let argLead = cmdLine =~ '\s$' ? '' : args[len(args) - 1]

  let project = eclim#project#util#GetCurrentProjectName()
  let command = substitute(s:command_source_dirs, '<project>', project, '')
  let result =  eclim#Execute(command)
  let paths = []
  if result != '' && result != '0'
    let paths = map(split(result, "\n"),
      \ "eclim#project#util#GetProjectRelativeFilePath(v:val)")
  endif

  let results = []

  if argLead !~ '^\s*$'
    let follow = 0
    for path in paths
      if argLead =~ '^' . path
        let follow = 1
        break
      elseif  path =~ '^' . argLead
        call add(results, path)
      endif
    endfor

    if follow
      let results = split(eclim#util#Glob(dir . '/' . argLead . '*', 1), '\n')
      call filter(results, "isdirectory(v:val) || v:val =~ '\\.java$'")
      call map(results, "substitute(v:val, '\\', '/', 'g')")
      call map(results, 'isdirectory(v:val) ? v:val . "/" : v:val')
      call map(results, 'substitute(v:val, dir, "", "")')
      call map(results, 'substitute(v:val, "^\\(/\\|\\\\\\)", "", "g")')
      call map(results, "substitute(v:val, ' ', '\\\\ ', 'g')")
    endif
  else
    let results = paths
  endif

  return eclim#util#ParseCommandCompletionResults(argLead, results)
endfunction " }}}

" vim:ft=vim:fdm=marker
