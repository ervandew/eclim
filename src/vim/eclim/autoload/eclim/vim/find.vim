" Author:  Eric Van Dewoestine
" Version: ${eclim.version}
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/vim/find.html
"
" License:
"
" Copyright (c) 2005 - 2006
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
if !exists("g:EclimVimPaths")
  let g:EclimVimPaths = &runtimepath
endif
if !exists("g:EclimVimFindSingleResult")
  " possible values ('split', 'edit', 'lopen')
  let g:EclimVimFindSingleResult = "split"
endif
" }}}

" Script Variables {{{
  let s:search{'func_def'} = 'function[!]\?\s\+<name>\>'
  let s:search{'func_ref'} = '\<<name>\>'
  let s:search{'var_def'} = '\<let\s\+\(g:\)\?<name>\>'
  let s:search{'var_ref'} = '\<<name>\>'

  let s:count{'func_def'} = '1'
  let s:count{'func_ref'} = ''
  let s:count{'var_def'} = ''
  let s:count{'var_ref'} = ''

  let s:type{'func_def'} = 'user defined function'
  let s:type{'func_ref'} = s:type{'func_def'}
  let s:type{'var_def'} = 'global variable'
  let s:type{'var_ref'} = s:type{'var_def'}

  let s:valid{'func_def'} = '\(:\|#\|^\)[A-Z]\w\+$'
  let s:valid{'func_ref'} = s:valid{'func_def'}
  let s:valid{'var_def'} = '^\w\+$'
  let s:valid{'var_ref'} = s:valid{'var_def'}

  let s:extract{'func_def'} = '\(.*\s\|^\)\(.*\%<col>c.\{-}\)\((.*\|\s.*\|$\)'
  let s:extract{'func_ref'} = s:extract{'func_def'}
  let s:extract{'var_def'} =
    \ "\\(.*g:\\|.*[[:space:]\"'(\\[{,]\\)" .
    \ "\\(.*\\%<col>c.\\{-}\\)" .
    \ "\\([[:space:]\"')\\]},].*\\|$\\)"
  let s:extract{'var_ref'} = s:extract{'var_def'}

  let s:trim{'func_def'} = ''
  let s:trim{'func_ref'} = s:trim{'func_def'}
  let s:trim{'var_def'} = '^\(g:\)\(.*\)'
  let s:trim{'var_ref'} = s:trim{'var_def'}
" }}}

" FindFunctionDef(name, bang) {{{
" Finds the definition of the supplied function.
function! eclim#vim#find#FindFunctionDef (name, bang)
  call s:Find(a:name, a:bang, 'func_def')
endfunction " }}}

" FindFunctionRef(name, bang) {{{
" Finds the definition of the supplied function.
function! eclim#vim#find#FindFunctionRef (name, bang)
  call s:Find(a:name, a:bang, 'func_ref')
endfunction " }}}

" FindVariableDef(name, bang) {{{
" Finds the definition of the supplied variable.
function! eclim#vim#find#FindVariableDef (name, bang)
  call s:Find(a:name, a:bang, 'var_def')
endfunction " }}}

" FindVariableRef(name, bang) {{{
" Finds the definition of the supplied variable.
function! eclim#vim#find#FindVariableRef (name, bang)
  call s:Find(a:name, a:bang, 'var_ref')
endfunction " }}}

" Find(name, bang, context) {{{
function! s:Find (name, bang, context)
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

  " if a script local function search current file.
  if name =~ '^s:.*'
    silent! exec cnt . 'lvimgrepadd /' . search . '/gj' . ' ' . expand('%:p')

  " search globally
  else
    for path in split(g:EclimVimPaths, ',')
      silent! exec cnt . 'lvimgrepadd /' . search . '/gj' . ' ' . path . '/**/*.vim'
      if a:context == 'def' && len(getloclist(0)) > 0
        break
      endif
    endfor
  endif

  " something is really fubaring the current folding... seems to be
  " something in my vim settings since vim -u NONE works fine.
  " As a result, must issue update + edit in a couple places below to fix it.

  let loclist = getloclist(0)
  if len(loclist) == 0
    silent update
    silent edit
    call eclim#util#EchoInfo("No results found for '" . name . "'.")
  elseif len(loclist) == 1
    if g:EclimVimFindSingleResult == 'edit'
      lfirst
    elseif g:EclimVimFindSingleResult == 'split'
      let file = bufname(loclist[0].bufnr)
      if file != expand('%')
        silent update
        silent edit
        silent exec "split " . file
      endif
      call cursor(loclist[0].lnum, loclist[0].col)
    else
      lopen
    endif
  elseif a:bang != ''
    silent update
    silent edit
    lopen
  else
    lfirst
    silent update
    silent edit
  endif
  call eclim#util#EchoInfo('')
endfunction " }}}

" vim:ft=vim:fdm=marker
