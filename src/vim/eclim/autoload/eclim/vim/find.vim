" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/vim/find.html
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

" Global Variables {{{
if !exists("g:EclimVimPaths")
  let g:EclimVimPaths = &runtimepath
endif
if !exists("g:EclimVimFindSingleResult")
  " possible values ('split', 'edit', 'lopen')
  let g:EclimVimFindSingleResult = g:EclimDefaultFileOpenAction
endif
" }}}

" Script Variables {{{
  let s:search{'cmd_def'} = 'command\s.\{-}\<<name>\>'
  let s:search{'cmd_ref'} = ':\s*<name>\>'
  let s:search{'func_def'} = 'fu\(n\|nc\|nct\|ncti\|nctio\|nction\)\?[!]\?\s\+<name>\>'
  let s:search{'func_ref'} = '\<<name>\>'
  let s:search{'var_def'} = '\<let\s\+\(g:\)\?<name>\>'
  let s:search{'var_ref'} = '\<<name>\>'

  let s:count{'cmd_def'} = '1'
  let s:count{'cmd_ref'} = ''
  let s:count{'func_def'} = '1'
  let s:count{'func_ref'} = ''
  let s:count{'var_def'} = ''
  let s:count{'var_ref'} = ''

  let s:type{'cmd_def'} = 'user defined command'
  let s:type{'cmd_ref'} = s:type{'cmd_def'}
  let s:type{'func_def'} = 'user defined function'
  let s:type{'func_ref'} = s:type{'func_def'}
  let s:type{'var_def'} = 'global variable'
  let s:type{'var_ref'} = s:type{'var_def'}

  let s:valid{'cmd_def'} = '^\w\+$'
  let s:valid{'cmd_ref'} = s:valid{'cmd_def'}
  let s:valid{'func_def'} = '\(:\|#\|^\)[A-Z]\w\+$'
  let s:valid{'func_ref'} = s:valid{'func_def'}
  let s:valid{'var_def'} = '^\w\+$'
  let s:valid{'var_ref'} = s:valid{'var_def'}

  let s:extract{'cmd_def'} = '\(.*:\|.*\s\|^\)\(.*\%<col>c.\{-}\)\(\W.*\|\s.*\|$\)'
  let s:extract{'cmd_ref'} = s:extract{'cmd_def'}
  let s:extract{'func_def'} = '\(.*\s\|^\)\(.*\%<col>c.\{-}\)\((.*\|\s.*\|$\)'
  let s:extract{'func_ref'} = s:extract{'func_def'}
  let s:extract{'var_def'} =
    \ "\\(.*g:\\|.*[[:space:]\"'(\\[{,]\\)" .
    \ "\\(.*\\%<col>c.\\{-}\\)" .
    \ "\\([[:space:]\"')\\]},].*\\|$\\)"
  let s:extract{'var_ref'} = s:extract{'var_def'}

  let s:trim{'cmd_def'} = ''
  let s:trim{'cmd_ref'} = s:trim{'cmd_def'}
  let s:trim{'func_def'} = ''
  let s:trim{'func_ref'} = s:trim{'func_def'}
  let s:trim{'var_def'} = '^\(g:\)\(.*\)'
  let s:trim{'var_ref'} = s:trim{'var_def'}
" }}}

" FindByContext(bang) {{{
" Contextual find that determines the type of element under the cursor and
" executes the appropriate find.
function! eclim#vim#find#FindByContext(bang)
  let line = getline('.')

  let element = substitute(line,
    \ "\\(.*[[:space:]\"'(\\[{]\\|^\\)\\(.*\\%" .
    \ col('.') . "c.\\{-}\\s*(\\).*",
    \ '\2', '')

  " on a function
  if line =~ '\%' . col('.') . 'c[[:alnum:]#:]\+\s*('
    let element = substitute(element, '\s*(.*', '', '')
    let type = 'func'

  " on a command ref
  elseif line =~ '\W:\w*\%' . col('.') . 'c'
    let element = substitute(line, '.*:\(.*\%' . col('.') . 'c\w*\).*', '\1', '')
    let type = 'cmd'

  " on a command def
  elseif line =~ '^\s*:\?\<command\>.*\s\w*\%' . col('.') . 'c\w*\(\s\|$\)'
    let element = substitute(line, '.*\s\(.*\%' . col('.') . 'c\w*\).*', '\1', '')
    let type = 'cmd'

  " on a variable
  else
    let element = substitute(line,
      \ "\\(.*[[:space:]\"'(\\[{]\\|^\\)\\(.*\\%" .
      \ col('.') . "c.\\{-}\\)\\([[:space:]\"')\\]}].*\\|$\\)",
      \ '\2', '')

    let type = 'var'
  endif

  if element == line || element !~ '^[[:alnum:]:#]\+$'
    return
  endif

  let def = substitute(s:search{type . '_def'}, '<name>', element, '')

  " on a definition, search for references
  if line =~ def
    call s:Find(element, a:bang, type . '_ref')

  " on a reference, search for definition.
  else
    call s:Find(element, a:bang, type . '_def')
  endif
endfunction " }}}

" FindCommandDef(name, bang) {{{
" Finds the definition of the supplied user defined command.
function! eclim#vim#find#FindCommandDef(name, bang)
  call s:Find(a:name, a:bang, 'cmd_def')
endfunction " }}}

" FindCommandRef(name, bang) {{{
" Finds the definition of the supplied user defined command.
function! eclim#vim#find#FindCommandRef(name, bang)
  call s:Find(a:name, a:bang, 'cmd_ref')
endfunction " }}}

" FindFunctionDef(name, bang) {{{
" Finds the definition of the supplied user defined function.
function! eclim#vim#find#FindFunctionDef(name, bang)
  call s:Find(a:name, a:bang, 'func_def')
endfunction " }}}

" FindFunctionRef(name, bang) {{{
" Finds the definition of the supplied user defined function.
function! eclim#vim#find#FindFunctionRef(name, bang)
  call s:Find(a:name, a:bang, 'func_ref')
endfunction " }}}

" FindVariableDef(name, bang) {{{
" Finds the definition of the supplied variable.
function! eclim#vim#find#FindVariableDef(name, bang)
  call s:Find(a:name, a:bang, 'var_def')
endfunction " }}}

" FindVariableRef(name, bang) {{{
" Finds the definition of the supplied variable.
function! eclim#vim#find#FindVariableRef(name, bang)
  call s:Find(a:name, a:bang, 'var_ref')
endfunction " }}}

" Find(name, bang, context) {{{
function! s:Find(name, bang, context)
  let name = a:name
  if name == ''
    let line = getline('.')
    let regex = substitute(s:extract{a:context}, '<col>', col('.'), 'g')
    let name = substitute(line, regex, '\2', '')
  endif

  " last chance to clean up the extracted value.
  let regex = s:trim{a:context}
  if regex != ''
    let name = substitute(name, regex, '\2', '')
  endif

  if name !~ s:valid{a:context}
    call eclim#util#EchoInfo('Not a valid ' . s:type{a:context} . ' name.')
    return
  endif

  call eclim#util#EchoInfo("Searching for '" . name . "'...")

  let cnt = s:count{a:context}
  let search = substitute(s:search{a:context}, '<name>', name, '')

  call setloclist(0, [])

  let save_opt = &eventignore
  set eventignore=all
  try
    " if a script local function search current file.
    if name =~ '^s:.*'
      let command = cnt . 'lvimgrepadd /' . search . '/gj %'
      call eclim#util#EchoTrace(command)
      silent! exec command

    " search globally
    else
      for path in split(g:EclimVimPaths, ',')
        " ignore eclim added dir as parent dir will be searched
        if path =~ '\<eclim$'
          continue
        endif

        let path = escape(substitute(path, '\', '/', 'g'), ' ')
        let command = cnt . 'lvimgrepadd /' . search . '/gj' . ' ' . path . '/**/*.vim'
        call eclim#util#EchoTrace(command)
        silent! exec command
        if a:context == 'def' && len(getloclist(0)) > 0
          break
        endif
      endfor
    endif
  finally
    let &eventignore = save_opt
  endtry

  let loclist = getloclist(0)
  if len(loclist) == 0
    call eclim#util#EchoInfo("No results found for '" . name . "'.")
  elseif len(loclist) == 1
    if g:EclimVimFindSingleResult == 'edit'
      lfirst
    elseif g:EclimVimFindSingleResult == 'split'
      let file = bufname(loclist[0].bufnr)
      if file != expand('%')
        silent exec "split " . file
      endif
      call cursor(loclist[0].lnum, loclist[0].col)
    else
      lopen
    endif
  elseif a:bang != ''
    lopen
  else
    lfirst
  endif
  call eclim#util#EchoInfo('')
endfunction " }}}

" vim:ft=vim:fdm=marker
