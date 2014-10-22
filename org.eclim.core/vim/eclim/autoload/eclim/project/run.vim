" Author:  Daniel Leong
"
" Description: {{{
"
" License:
"
" Copyright (C) 2005 - 2014  Eric Van Dewoestine
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
let s:command_project_run = '-command project_run -p "<project>" ' .
  \ '-v "<vim_servername>"'
let s:command_project_run_config = '-command project_run -p "<project>" ' .
  \ '-n "<config>" -v "<vim_servername>"'
let s:command_project_run_list = '-command project_run -p "<project>" -l'
" }}}

function! eclim#project#run#ProjectRun(...) " {{{
  " Option args:
  "   config: The name of the configuration to run for the current project
  
  if !eclim#EclimAvailable()
    return
  endif

  let config = a:0 > 0 ? a:1 : ''
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif
  let project = eclim#project#util#GetCurrentProjectName()

  let command = s:command_project_run
  if config != ''
    let command = s:command_project_run_config
  endif

  " TODO include warning about --servername?
  call eclim#util#Echo("Running project '" . project . "'...")
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<config>', config, '')
  let command = substitute(command, '<vim_servername>', v:servername, '')
  let result = eclim#Execute(command, {'project': project})
  " call eclim#util#Echo(result)
endfunction " }}}

function! eclim#project#run#ProjectRunList() " {{{

  if !eclim#EclimAvailable()
    return
  endif

  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif
  let project = eclim#project#util#GetCurrentProjectName()

  let command = s:command_project_run_list

  call eclim#util#Echo("Fetching launch configs for project '" . project . "'...")
  let command = substitute(command, '<project>', project, '')
  let result = eclim#Execute(command, {'project': project})
  if type(result) != g:LIST_TYPE
    call eclim#util#Echo(result)
    return
  endif

  if len(result) == 0
    call eclim#util#Echo("No launch configs for project '" . project . ".")
    return
  endif

  let pad = 0
  for config in result
    let pad = len(config.name) > pad ? len(config.name) : pad
  endfor

  let output = []
  for config in result
    call add(output,
      \ eclim#util#Pad(config.name, pad) . ' - ' . config.type)
  endfor
  call eclim#util#Echo(join(output, "\n"))
endfunction " }}}

function! eclim#project#run#onLaunchProgress(percent, label) " {{{

  let totalBars = 10
  let barChar = '|'
  let barsCount = str2float(a:percent) * totalBars

  let bars = eclim#util#Pad('', barsCount, barChar)
  let bar = eclim#util#Pad(bars, totalBars, ' ')
  let output = '[' . bar . '] ' . a:label
  call eclim#util#Echo(output)
endfunction " }}}

function! eclim#project#run#onPrepareOutput(configName, launchId) " {{{
  let current = winnr()
  call eclim#util#TempWindow('[' . a:launchId . ' Output]', [])
  let no = bufnr('%')
  let b:pending = []
  let b:launch_id = a:launchId

  augroup eclim_project_output
    autocmd! BufEnter <buffer> 
    call s:onBufferReturn()
  augroup END

  exe current . "winc w"
  redraw!
  return no
endfunction " }}}

function! eclim#project#run#onOutput(bufNo, type, line) " {{{
  let current = winnr()

  " TODO fancier?
  let fullLine = a:type . "> " . a:line 

  let bufNr = str2nr(a:bufNo)
  let winnr = bufwinnr(bufNr)
  if -1 == winnr
    " save for later
    let pending = getbufvar(bufNr, "pending")
    if "terminated" == a:type
      call add(pending, "<terminated>")
    else
      call add(pending, fullLine)
    endif
  else 
    exe winnr . "winc w"
    if "terminated" == a:type
      call s:onTerminated()
    else
      call s:append(fullLine)
    endif
  endif

  " pop back
  exe current . "winc w"

endfunction " }}}

function! s:onBufferReturn() " {{{
  for line in b:pending
    if line == "<terminated>"
      call s:onTerminated()
    else
      call s:append(line)
    endif
  endfor

  let b:pending = []
endfunction " }}}

function! s:append(line) " {{{
  setlocal modifiable
  setlocal noreadonly

  let lines = split(a:line, '\r', 1)
  call append(line('$'), lines)
  norm G

  setlocal nomodifiable
  setlocal readonly
endfunction " }}}

function! s:onTerminated() " {{{
  call s:append("<terminated>")

  " rename the buffer
  exe "silent file [TERMINATED " . b:launch_id . "]"
endfunction " }}}

" vim:ft=vim:fdm=marker
