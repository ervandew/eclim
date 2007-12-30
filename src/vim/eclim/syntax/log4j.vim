" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"  Syntax for log4j.xml files.
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

runtime! syntax/xml.vim

syn cluster xmlTagHook add=log4jElement

syn keyword log4jElement display renderer appender category logger root categoryFactory
syn keyword log4jElement display errorHandler param layout filter
syn keyword log4jElement display priority level

syn match log4jElement /appender-ref/
syn match log4jElement /logger-ref/
syn match log4jElement /root-ref/

syn match log4jElement /log4j:configuration/
syn match log4jElement /log4j:event/
syn match log4jElement /log4j:eventSet/
syn match log4jElement /log4j:message/
syn match log4jElement /log4j:NDC/
syn match log4jElement /log4j:throwable/
syn match log4jElement /log4j:locationInfo/

hi def link log4jElement Statement

let b:current_syntax = "ant"

" vim:ft=vim:fdm=marker
