" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Extension to default java syntax to fix issues or make improvements.
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

source $VIMRUNTIME/syntax/java.vim

" annotations can be fully qualified.
syn match   javaAnnotation      "@[_$a-zA-Z][_$a-zA-Z0-9_.]*\>"

" allow folding of blocks and java doc comments.
syn region javaBlockFold start="{" end="}" transparent fold
syn region javaDocComment start="/\*\*" end="\*/" keepend contains=javaCommentTitle,@javaHtml,javaDocTags,javaDocSeeTag,javaTodo,@Spell fold

" vim:ft=vim:fdm=marker
