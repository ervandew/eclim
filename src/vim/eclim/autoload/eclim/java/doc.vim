" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/java/doc.html
"
" License:
"
" Copyright (C) 2005 - 2010  Eric Van Dewoestine
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
let s:command_comment =
  \ '-command javadoc_comment -p "<project>" -f "<file>" -o <offset> -e <encoding>'
let s:command_source_dirs = '-command java_src_dirs -p "<project>"'
" }}}

" Comment() {{{
" Add / update the comments for the element under the cursor.
function! eclim#java#doc#Comment()
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  call eclim#java#util#SilentUpdate()

  let project = eclim#project#util#GetCurrentProjectName()
  let file = eclim#project#util#GetProjectRelativeFilePath()
  let offset = eclim#util#GetCurrentElementOffset()

  let command = s:command_comment
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')
  let command = substitute(command, '<offset>', offset, '')
  let command = substitute(command, '<encoding>', eclim#util#GetEncoding(), '')

  let result =  eclim#ExecuteEclim(command)

  if result != "0"
    call eclim#util#RefreshFile()
    silent retab
  endif
endfunction " }}}

" Javadoc(bang, [file, file, ...]) {{{
" Run javadoc for all, or the supplied, source files.
function! eclim#java#doc#Javadoc(bang, ...)
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  let project_path = eclim#project#util#GetCurrentProjectRoot()
  let project = eclim#project#util#GetCurrentProjectName()
  let args = '-p "' . project . '"'

  if len(a:000) > 0 && (len(a:000) > 1 || a:000[0] != '')
    let args .= ' -f "' . join(a:000, ' ') . '"'
  endif

  let cwd = getcwd()
  try
    exec 'lcd ' . escape(project_path, ' ')
    call eclim#util#MakeWithCompiler('eclim_javadoc', a:bang, args)
  finally
    exec 'lcd ' . escape(cwd, ' ')
  endtry
endfunction " }}}

" CommandCompleteJavadoc(argLead, cmdLine, cursorPos) {{{
" Custom command completion for :Javadoc
function! eclim#java#doc#CommandCompleteJavadoc(
    \ argLead, cmdLine, cursorPos)
  let dir = eclim#project#util#GetCurrentProjectRoot()

  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let args = eclim#util#ParseCmdLine(cmdLine)
  let argLead = cmdLine =~ '\s$' ? '' : args[len(args) - 1]

  let project = eclim#project#util#GetCurrentProjectName()
  let command = substitute(s:command_source_dirs, '<project>', project, '')
  let result =  eclim#ExecuteEclim(command)
  let paths = []
  if result != '' && result != '0'
    let paths = map(split(result, "\n"),
      \ "eclim#project#util#GetProjectRelativeFilePath(v:val)")
  endif

  let results = []

  if argLead !~ '^\s*$'
    let follow = 0
    for path in paths
      if argLead =~ '^' . path
        let follow = 1
        break
      elseif  path =~ '^' . argLead
        call add(results, path)
      endif
    endfor

    if follow
      let results = split(eclim#util#Glob(dir . '/' . argLead . '*', 1), '\n')
      call filter(results, "isdirectory(v:val) || v:val =~ '\\.java$'")
      call map(results, "substitute(v:val, '\\', '/', 'g')")
      call map(results, 'isdirectory(v:val) ? v:val . "/" : v:val')
      call map(results, 'substitute(v:val, dir, "", "")')
      call map(results, 'substitute(v:val, "^\\(/\\|\\\\\\)", "", "g")')
      call map(results, "substitute(v:val, ' ', '\\\\ ', 'g')")
    endif
  else
    let results = paths
  endif

  return eclim#util#ParseCommandCompletionResults(argLead, results)
endfunction " }}}

" vim:ft=vim:fdm=marker
