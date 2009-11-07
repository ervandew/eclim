" Author:  Eric Van Dewoestine
"
" Description: {{{
"  Enhancement to default ant syntax file to add support for ant-contrib and
"  allow user to define list of additional tasks to be recognized.
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

" antcontrib elements
syn keyword antElement if then else elseif for foreach switch
syn keyword antElement throw trycatch try catch finally
syn keyword antElement propertycopy propertyselector propertyregex var
syn keyword antElement antcallback antfetch runtarget
syn keyword antElement outofdate timestampselector osfamily shellscript
syn keyword antElement pathtofileset sortlist urlencode compilewithwalls
syn keyword antElement forget assert bool limit math post stopwatch
syn keyword antElement inifile antclipse antserver remoteant

" ant 1.7 tasks
syn keyword antElement antversion echoxml
" ant 1.7 resources and resource collections
syn keyword antElement bzip2resource file gzipresource javaresource
syn keyword antElement propertyresource string tarentry zipentry
syn keyword antElement files first restrict resources sort tokens
syn keyword antElement union intersect difference
" ant 1.7 selectors
syn keyword antElement date depend depth different filename present containsregexp
syn keyword antElement size type modified signedselector scriptselector
syn match antElement 'contains'
" ant 1.7 conditions
syn keyword antElement hasfreespace hasmethod isfailure length matches
syn keyword antElement resourcecount resourcesmatch
" ant 1.7 misc elements
syn keyword antElement preserveintarget service

if exists("g:AntSyntaxElements")
  let elements = string(g:AntSyntaxElements)
  let elements = substitute(elements, '\[\(.*\)\]', '\1', '')
  let elements = substitute(elements, ',', '', 'g')
  let elements = substitute(elements, "'", '', 'g')
  exec 'syn keyword antElement ' . elements
endif

" vim:ft=vim:fdm=marker
