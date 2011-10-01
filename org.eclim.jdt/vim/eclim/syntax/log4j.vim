" Author:  Eric Van Dewoestine
"
" Description: {{{
"  Syntax for log4j.xml files.
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
