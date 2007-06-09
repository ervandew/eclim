" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/python/django.html
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

" Command Declarations {{{
if !exists(':DjangoTemplateOpen')
  command DjangoTemplateOpen :call eclim#python#django#FindTemplate(
    \ eclim#python#django#GetProjectPath(), eclim#util#GrabUri())
endif

if !exists(':DjangoViewOpen')
  command DjangoViewOpen :call eclim#python#django#FindView(
    \ eclim#python#django#GetProjectPath(), eclim#util#GrabUri())
endif

if !exists(':DjangoContextOpen')
  command DjangoContextOpen :call eclim#python#django#ContextFind()
endif
" }}}

" vim:ft=vim:fdm=marker
