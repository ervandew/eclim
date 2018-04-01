" Author: G0dj4ck4l
"
" License: {{{
"
" Copyright (C) 2005 - 2018  Eric Van Dewoestine
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

" Script Variables {{{
let s:command_outline =
			\ '-command java_outline -p "<project>" -f "<file>"'
" }}}

function! eclim#java#outline#Outline() " {{{
	if !eclim#project#util#IsCurrentFileInProject()
		return
	endif

	call eclim#lang#SilentUpdate()

	let project = eclim#project#util#GetCurrentProjectName()
	let file = eclim#project#util#GetProjectRelativeFilePath()
	let command = s:command_outline
	let command = substitute(command, '<project>', project, '')
	let command = substitute(command, '<file>', file, '')
	let result = eclim#Execute(command)

	if type(result) != g:LIST_TYPE
		return
	endif

	if len(result) == 0
		call eclim#util#Echo('No results found.')
		return
	endif

	let lines = []
	let info = []
	call s:OutlineFormat(result, lines, info, '')
	call eclim#util#TempWindow('[Outline]', lines)

	set ft=java
	" fold function calls into their parent
	setlocal foldmethod=expr
	setlocal foldexpr='>'.len(substitute(getline(v:lnum),'^\\(\\s*\\).*','\\1',''))/2
	setlocal foldtext=substitute(getline(v:foldstart),'^\\(\\s*\\)\\s\\s','\\1+\ ','').':\ '.(v:foldend-v:foldstart+1).'\ lines'

	setlocal modifiable noreadonly
	call append(line('$'), ['', '" use ? to view help'])
	setlocal nomodifiable readonly
	syntax match Comment /^".*/

	let b:outline_info = info

	nnoremap <buffer> <silent> <cr> :call <SID>Open(g:EclimJavaOutlineDefaultAction)<cr>
	nnoremap <buffer> <silent> E :call <SID>Open('edit')<cr>
	nnoremap <buffer> <silent> S :call <SID>Open('split')<cr>
	nnoremap <buffer> <silent> T :call <SID>Open("tablast \| tabnew")<cr>

	" assign to buffer var to get around weird vim issue passing list containing
	" a string w/ a '<' in it on execution of mapping.
	let b:outline_help = ['<cr> - open file with default action']
	nnoremap <buffer> <silent> ? :call eclim#help#BufferHelp(b:outline_help, 'vertical', 40)<cr>
endfunction " }}}

function! s:OutlineFormat(result, lines, info, indent) " {{{
	for child in a:result
		call add(a:lines, a:indent . child.name)
		call add(a:info, {
					\ 'file': child.position.filename,
					\ 'line': child.position.line,
					\ 'col': child.position.column
					\ })
		call s:OutlineFormat(child.childrens, a:lines, a:info, a:indent . "\t")
	endfor
endfunction " }}}

function! s:Open(action) " {{{
	let line = line('.')
	if line > len(b:outline_info)
		return
	endif

	let info = b:outline_info[line - 1]
	if info.file != ''
		" go to the buffer that initiated the outline
		exec b:winnr . 'winc w'

		let action = a:action
		call eclim#util#GoToBufferWindowOrOpen(info.file, action, info.line, info.col)

		" force any previous messge from else below to be cleared
		echo ''
	else
		call eclim#util#Echo('No associated file was found.')
	endif
endfunction " }}}

" vim:ft=vim:fdm=marker
