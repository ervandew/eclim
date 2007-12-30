" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   Default xml.vim only defines the xmlRegion if xml folding is enabled, but
"   xmlRegion is needed to allow spell check highlighting of xml text.
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

source $VIMRUNTIME/syntax/xml.vim

if !exists('g:xml_syntax_folding')
  " taken from syntax/xml.vim, but removed unecessary portions.
  syn region   xmlRegion
    \ start=+<\z([^ /!?<>"']\+\)+
    \ skip=+<!--\_.\{-}-->+
    \ end=+</\z1\_\s\{-}>+
    \ contains=xmlTag,xmlEndTag,xmlCdata,xmlRegion,xmlComment,xmlEntity,xmlProcessing,@xmlRegionHook,@Spell
endif

" vim:ft=vim:fdm=marker
