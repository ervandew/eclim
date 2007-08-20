" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"  Syntax file for template files using groovy's simple template syntax.
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

syn region groovySimpleTemplateSection start="<%" end="%>"
syn match groovySimpleTemplateVariable '\${.\{-}}'

hi link groovySimpleTemplateSection Statement
hi link groovySimpleTemplateVariable Constant

" vim:ft=vim:fdm=marker
