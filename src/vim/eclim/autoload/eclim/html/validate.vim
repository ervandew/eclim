" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/html/validate.html
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

" Validate(on_save) {{{
" Validates the current html file.
function! eclim#html#validate#Validate (on_save)
  if eclim#util#WillWrittenBufferClose()
    return
  endif

  if !eclim#project#util#IsCurrentFileInProject(!a:on_save)
    return
  endif

  call eclim#display#signs#SetPlaceholder()

  call eclim#common#validate#Validate('html', a:on_save)

  let html_errors = getloclist(0)
  let css_errors = []
  let js_errors = []

  if search('<script', 'cnw')
    call eclim#javascript#validate#Validate(a:on_save)
    let js_errors = getloclist(0)
  endif

  if search('<style', 'cnw')
    call eclim#common#validate#Validate('css', a:on_save)
    let css_errors = getloclist(0)
  endif

  call eclim#util#SetLocationList(html_errors + css_errors + js_errors)

  call eclim#display#signs#RemovePlaceholder()
endfunction " }}}

" vim:ft=vim:fdm=marker
