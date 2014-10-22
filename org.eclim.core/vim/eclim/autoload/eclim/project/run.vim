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

" Python functions {{{
  " Requiring python is gross, but it's the only way to append to
  "  a buffer that isn't visible, and that is surely required
function! s:append(bufno, line) " {{{
  if !has('python')
    return
  endif

  file "append"

  let bufnr = a:bufno
  let lines = split(a:line, '\r', 1)
  py vim.current.buffer.vars['apo1'] = "YES "
py << PYEOF
import vim
bufnr = vim.eval('bufnr')
vim.current.buffer.vars['bufnr'] = "YES " + int(bufnr)
buf = vim.buffers[int(bufnr)]
if buf:
  vim.current.buffer.vars['howdy'] = buf.name
  lines = vim.eval('lines')
  vim.current.buffer.vars['howdy1'] = "YES"

  buf.options['readonly'] = False
  buf.options['modifiable'] = True
  vim.current.buffer.vars['howdy2'] = "YES"
  buf.append(lines)
  vim.current.buffer.vars['howdy3'] = "YES"
  buf.options['readonly'] = True
  buf.options['modifiable'] = False

  # find windows for this buffer
  for tab in vim.tabpages:
    for win in tab.windows:
      if win.buffer == buf:
        # scroll to bottom
        win.cursor = [len(buf), 1]
else:
  vim.current.buffer.vars['fail'] = "yup"
  vim.current.buffer.vars['hllo'] = bufnr
  print bufnr
PYEOF
endfunction " }}}

function! s:onTerminated(bufno) " {{{
  if !has('python')
    return
  endif

  call s:append(a:bufno, "<terminated>")

  " rename the buffer
  let bufnr = a:bufno
py << PYEOF
import vim
bufnr = vim.eval('bufnr')
buf = vim.buffers[bufnr]
if buf is not None:
  buf.name = "[TERMINATED %s]" % buf.vars['launch_id']
PYEOF
endfunction " }}}
" }}}

function! eclim#project#run#ProjectRun(...) " {{{
  " Option args:
  "   config: The name of the configuration to run for the current project
  
  if !eclim#EclimAvailable()
    return
  endif

  if !has('python')
    eclim#util#EchoError("Python support is required for :ProjectRun")
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
  let b:launch_id = a:launchId

  exe current . "winc w"
  redraw!
  return no
endfunction " }}}

function! eclim#project#run#onOutput(bufNo, type, line) " {{{
  " TODO fancier?
  let fullLine = a:type . "> " . a:line 

  call eclim#util#Echo("On output " . has('python'))

  if has('python')
    if "terminated" == a:type
      call s:onTerminated(a:bufNo)
    else
      call s:append(a:bufNo, fullLine)
    endif
  endif

endfunction " }}}

" vim:ft=vim:fdm=marker
