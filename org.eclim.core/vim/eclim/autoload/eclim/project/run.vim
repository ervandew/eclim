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
  \ '-x "<vim_executable>" -v "<vim_servername>"'
let s:command_project_run_config = '-command project_run -p "<project>" ' .
  \ '-n "<config>" -x "<vim_executable>" -v "<vim_servername>"'
let s:command_project_run_list = '-command project_run -p "<project>" -l'
let s:command_project_run_terminate = '-command project_run_terminate ' .
  \ '-l "<launch_id>"'
let s:command_project_run_terminate_all = '-command project_run_terminate'
let s:flag_project_run_force = ' -c'
" }}}

" Python functions {{{
  " Requiring python is gross, but it's the only way to append to
  "  a buffer that isn't visible, and that is surely required
function! s:append(bufno, type, line) " {{{
  if !has('python')
    return
  endif

  " prepare vars so python can pick them up
  let bufnr = a:bufno
  let ltype = a:type
  let lines = split(a:line, '\r', 1)

py << PYEOF
import vim
bufnr = int(vim.eval('bufnr')) # NB int() is crucial
buf = vim.buffers[bufnr]
if buf:
  lines = vim.eval('lines')
  ltype = vim.eval('ltype')
  prefixed = map(lambda l: "%s>%s" % (ltype, l), lines)
  oldEnd = len(buf)

  buf.options['readonly'] = False
  buf.options['modifiable'] = True
  buf.append(prefixed)
  buf.options['readonly'] = True
  buf.options['modifiable'] = False

  # find windows for this buffer
  for tab in vim.tabpages:
    for win in tab.windows:
      if win.buffer.number == buf.number:
        # scroll to bottom if still there
        row, col = win.cursor
        if row == oldEnd:
          win.cursor = [len(buf), col]
        break
PYEOF

  redraw
endfunction " }}}

function! s:onTerminated(bufno) " {{{
  if !has('python')
    return
  endif

  call s:append(a:bufno, "out", "<terminated>")

  " rename the buffer
  let bufnr = a:bufno
py << PYEOF
import vim
bufnr = int(vim.eval('bufnr'))
buf = vim.buffers[bufnr]
if buf is not None:
  buf.name = "[TERMINATED %s]" % buf.vars['launch_id']
PYEOF

  redraw!
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
  
  let force = a:0 > 1 ? a:2 != "" : 0
  if force
    let command = command . s:flag_project_run_force
  endif

  " TODO include warning about --servername?
  call eclim#util#Echo("Running project '" . project . "'...")
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<config>', config, '')
  let command = substitute(command, '<vim_servername>', v:servername, '')
  let command = substitute(command, '<vim_executable>', exepath(v:progpath), '')
  let result = eclim#Execute(command, {'project': project})
  call eclim#util#EchoError(result)
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

function! eclim#project#run#TerminateLaunch(launchId) " {{{
  if !eclim#EclimAvailable()
    return
  endif

  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif
  let project = eclim#project#util#GetCurrentProjectName()

  let command = s:command_project_run_terminate
  let command = substitute(command, '<launch_id>', a:launchId, '')
  let result = eclim#Execute(command, {'project': project})
  call eclim#util#Echo(result)
endfunction " }}}

function! eclim#project#run#TerminateAllLaunches() " {{{
  if !eclim#EclimAvailable()
    return
  endif

  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif
  let project = eclim#project#util#GetCurrentProjectName()

  let command = s:command_project_run_terminate_all
  let result = eclim#Execute(command, {'project': project})
  call eclim#util#Echo(result)
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

  " is there a terminated launch window?
  " NB missing open bracket is intentional, and
  "  it returns the wrong buffer if not omitted
  let terminated = 'TERMINATED ' . a:launchId . ']'
  let terminatedBuf = bufnr(terminated)
  if terminatedBuf != -1
    exe 'bdelete ' . terminatedBuf
  endif

  call eclim#util#TempWindow('[' . a:launchId . ' Output]', [])
  let no = bufnr('%')
  let b:launch_id = a:launchId

  augroup eclim_async_launch_cleanup
    autocmd!
    autocmd VimLeavePre * call eclim#project#run#TerminateAllLaunches()
  augroup END

  if g:EclimTerminateLaunchOnBufferClosed
    exe 'autocmd BufWipeout <buffer> call eclim#project#run#TerminateLaunch("' .
          \ b:launch_id . '")'
  else
    " need to keep the buffer around, then
    setlocal bufhidden=hide
  endif

  " supply a Terminate command
  exe 'command -nargs=0 -buffer Terminate ' .
        \ ':call eclim#project#run#TerminateLaunch("' .
        \ b:launch_id . '")'

  " beautiful highlighting for error lines vs out> lines
  syntax region Error matchgroup=Quote start=/err>/ end=/\n/ concealends oneline
  syntax region Normal matchgroup=Quote start=/out>/ end=/\n/ concealends oneline

  set conceallevel=3
  set concealcursor=nc

  " pop back and show
  exe current . "winc w"
  redraw!
  return no
endfunction " }}}

function! eclim#project#run#onOutput(bufNo, type, line) " {{{
  if has('python')
    if "terminated" == a:type
      call s:onTerminated(a:bufNo)
    else
      call s:append(a:bufNo, a:type, a:line)
    endif
  endif
endfunction " }}}

" vim:ft=vim:fdm=marker
