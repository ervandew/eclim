" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/xml/format.html
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

" Format(start, end) {{{
function! eclim#xml#format#Format (start, end)
  if !exists('b:root')
    let b:root = s:GetRootLine()

    if b:root < 1
      return
    endif
  endif

  let clnum = line('.')
  let ccnum = col('.')

  let start = a:start
  let end = a:end
  if start >= b:root
    call cursor(start, 1)
    let outer = s:SelectOuterTag(1)
    let inner = s:SelectInnerTag()

    " inner tags on the same line, push to a new line.
    if outer.lstart == inner.lstart &&
        \ (inner.lstart != outer.lstart || inner.cstart != outer.cstart)
      call s:InsertCr(inner.lstart, inner.cstart)
      let end += 1
    endif

    " handle repositioning parent ending tag if necessary.
    if getline(inner.lstart) =~ '^\s*<\w'
      let element = s:SelectOuterTag(1)
      let parent = s:SelectOuterTag(2)
    else
      let element = s:SelectInnerTag()
      let parent = s:SelectOuterTag(1)
    endif
    if element.lend == parent.lend &&
        \ (parent.lstart != parent.lend || parent.cstart != parent.cend)
      call s:InsertCr(element.lend, element.cend + 1)
      let end += 1
    endif

    " handle sibling elements on the same line.
    let element = s:SelectOuterTag(1)
    if len(getline(element.lend)) > element.cend
      call cursor(element.lend, element.cend + 1)
      let sibling = s:SelectOuterTag(1)
      if element.lend == sibling.lstart
        call s:InsertCr(element.lend, element.cend + 1)
        let end += 1
      endif
    endif

    " let vim re-indent
    call cursor(start, 1)
    normal ==
  endif

  " recurse.
  if start < end
    call cursor(start + 1, 1)
    call eclim#xml#format#Format(start + 1, end)
  else
    unlet b:root
  endif

  call cursor(clnum, ccnum)
endfunction " }}}

" SelectOuterTag (count) {{{
function! s:SelectOuterTag (count)
  let clnum = line('.')
  let ccnum = col('.')

  exec 'silent! normal v' . a:count . 'atv'
  call cursor(clnum, ccnum)

  return s:VisualSelectionMap()
endfunction " }}}

" SelectInnerTag () {{{
function! s:SelectInnerTag ()
  silent! normal vit
  normal v
  call cursor(line("'<"), col("'<"))

  return s:VisualSelectionMap()
endfunction " }}}

" VisualSelectionMap () {{{
function! s:VisualSelectionMap ()
  let lstart = line("'<")
  let cstart = col("'<")
  let lend = line("'>")
  let cend = col("'>")

  if cstart > len(getline(lstart))
    let lstart += 1
    let cstart = 1
  endif

  if strpart(getline(lend), 0, cend) =~ '^\s*$'
    let lend -= 1
    let cend = len(getline(lend))
  endif

  return {'lstart': lstart, 'cstart': cstart, 'lend': lend, 'cend': cend}
endfunction " }}}

" InsertCr(line, col) {{{
function! s:InsertCr (line, col)
  call cursor(a:line, a:col)
  exec "normal i\<cr>\<esc>"
endfunction " }}}

" GetRootLine() {{{
function! s:GetRootLine ()
  let clnum = line('.')
  let ccnum = col('.')

  let line = -1
  call cursor(1, 1)
  while getline('.') !~ '^\s*<\w'
    let line = line('.') + 1
    if line > line('$')
      break
    endif
    call cursor(line, 1)
  endwhile

  call cursor(clnum, ccnum)
  return line
endfunction " }}}

" vim:ft=vim:fdm=marker
