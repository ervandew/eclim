" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"  Syntax for log4j.xml files.
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

runtime! syntax/xml.vim

syn cluster xmlTagHook add=log4jElement

syn keyword log4jElement display log4j configuration
syn keyword log4jElement display renderer appender category logger root categoryFactory
syn keyword log4jElement display errorHandler param layout filter appender-ref
syn keyword log4jElement display root-ref logger-ref
syn keyword log4jElement display priority level
syn keyword log4jElement display eventSet event
syn keyword log4jElement display message NDC throwable locationInfo

hi def link log4jElement Statement

let b:current_syntax = "ant"

" vim:ft=vim:fdm=marker
