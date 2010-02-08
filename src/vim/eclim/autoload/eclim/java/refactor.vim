" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/java/refactor.html
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

" Global Varables {{{
  if !exists('g:EclimRefactorDiffOrientation')
    let g:EclimRefactorDiffOrientation = 'vertical'
  endif
" }}}

" Script Varables {{{
  let s:command_rename = '-command java_refactor_rename ' .
    \ '-p "<project>" -f "<file>" -o <offset> -e <encoding> -l <length> -n <name>'
  let s:command_undoredo = '-command java_refactor_<operation>'
" }}}

" Rename(name) {{{
function eclim#java#refactor#Rename(name)
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  let element = expand('<cword>')
  if !eclim#java#util#IsValidIdentifier(element)
    call eclim#util#EchoError
      \ ("Element under the cursor is not a valid java identifier.")
    return
  endif

  let line = getline('.')
  let package_pattern = '^\s*package\s\+\(.*\%' . col('.') . 'c\w*\).*;'
  if line =~ package_pattern
    let element = substitute(line, package_pattern, '\1', '')
  endif

  let prompt = printf('Rename "%s" to "%s"', element, a:name)
  let result = exists('g:EclimRefactorPromptDefault') ?
    \ g:EclimRefactorPromptDefault : s:Prompt(prompt)
  if result <= 0
    return
  endif

  " update the file before vim makes any changes.
  call eclim#java#util#SilentUpdate()
  wall

  let project = eclim#project#util#GetCurrentProjectName()
  let file = eclim#project#util#GetProjectRelativeFilePath()
  let position = eclim#util#GetCurrentElementPosition()
  let offset = substitute(position, '\(.*\);\(.*\)', '\1', '')
  let length = substitute(position, '\(.*\);\(.*\)', '\2', '')

  let command = s:command_rename
  let command = substitute(command, '<project>', project, '')
  let command = substitute(command, '<file>', file, '')
  let command = substitute(command, '<offset>', offset, '')
  let command = substitute(command, '<length>', length, '')
  let command = substitute(command, '<encoding>', eclim#util#GetEncoding(), '')
  let command = substitute(command, '<name>', a:name, '')
  " user chose preview at the prompt
  if result == 2
    let command .= ' -v'
    call s:Preview(command)
    return
  endif

  call s:Refactor(command)
endfunction " }}}

" UndoRedo(operation, peek) {{{
function eclim#java#refactor#UndoRedo(operation, peek)
  if !eclim#project#util#IsCurrentFileInProject()
    return
  endif

  " update the file before vim makes any changes.
  call eclim#java#util#SilentUpdate()
  wall

  let command = s:command_undoredo
  let command = substitute(command, '<operation>', a:operation, '')
  if a:peek
    let command .= ' -p'
  endif

  call s:Refactor(command)
endfunction " }}}

" s:Prompt(prompt) {{{
function s:Prompt(prompt)
  exec "echohl " . g:EclimInfoHighlight
  try
    " clear any previous messages
    redraw
    echo a:prompt . "\n"
    let response = input("([e]xecute / [p]review / [c]ancel): ")
    while response != '' &&
        \ response !~ '^\c\s*\(e\(xecute\)\?\|p\(review\)\?\|c\(ancel\)\?\)\s*$'
      let response = input("You must choose either e, p, or c. (Ctrl-C to cancel): ")
    endwhile
  finally
    echohl None
  endtry

  if response == ''
    return -1
  endif

  if response =~ '\c\s*\(c\(ancel\)\?\)\s*'
    return 0
  endif

  return response =~ '\c\s*\(e\(execute\)\?\)\s*' ? 1 : 2 " preview
endfunction " }}}

" s:Preview(command) {{{
function s:Preview(command)
  let result = eclim#ExecuteEclim(a:command)
  if result == "0"
    return
  endif

  if result !~ '^-command'
    call eclim#util#Echo(result)
    return
  endif

  let lines = split(result, "\n")
  let command = lines[0]
  let lines = lines[1:]

  " normalize the lines a bit
  call map(lines, 'substitute(v:val, "^other:", " other:", "")')
  call map(lines, 'substitute(v:val, "^diff:", "|diff|:", "")')
  call add(lines, '')
  call add(lines, '|Execute Refactoring|')
  call eclim#util#TempWindow('[Refactor Preview]', lines)
  let b:refactor_command = command

  set ft=refactor_preview
  hi link RefactorLabel Identifier
  hi link RefactorLink Label
  syntax match RefactorLabel /^\s*\w\+:/
  syntax match RefactorLink /|\S.\{-}\S|/

  nnoremap <silent> <buffer> <cr> :call <SID>PreviewLink()<cr>
endfunction " }}}

" s:PreviewLink() {{{
function s:PreviewLink()
  let line = getline('.')
  if line =~ '^|'
    let args = split(b:refactor_command, ',')
    call map(args, 'substitute(v:val, "^\\([^-].*\\)", "\"\\1\"", "")')
    let command = join(args)

    let winend = winnr('$')
    let winnum = 1
    while winnum <= winend
      let bufnr = winbufnr(winnum)
      if getbufvar(bufnr, 'refactor_preview_diff') != ''
        exec bufnr . 'bd'
        continue
      endif
      let winnum += 1
    endwhile

    if line == '|Execute Refactoring|'
      let command = substitute(command, '\s*-v', '', '')
      call s:Refactor(command)
      let winnr = b:winnr
      close
      " the filename might change, so we have to use the winnr to get back to
      " where we were.
      exec winnr . 'winc w'

    elseif line =~ '^|diff|'
      let file = substitute(line, '^|diff|:\s*', '', '')
      let command .= ' -d "' . file . '"'

      let result = eclim#ExecuteEclim(command)
      if result == "0"
        return
      endif

      " split relative to the original window
      exec b:winnr . 'winc w'

      if has('win32unix')
        let file = eclim#cygwin#CygwinPath(file)
      endif
      let name = fnamemodify(file, ':t:r')
      let ext = fnamemodify(file, ':e')
      exec printf('silent below new %s.current.%s', name, ext)
      silent 1,$delete _ " counter-act any templating plugin
      exec 'read ' . escape(file, ' ')
      silent 1,1delete _
      let winnr = winnr()
      let b:refactor_preview_diff = 1
      setlocal readonly nomodifiable
      setlocal noswapfile nobuflisted
      setlocal buftype=nofile bufhidden=delete
      diffthis

      let orien = g:EclimRefactorDiffOrientation == 'horizontal' ? '' : 'vertical'
      exec printf('silent below %s split %s.new.%s', orien, name, ext)
      silent 1,$delete _ " counter-act any templating plugin
      call append(1, split(result, "\n"))
      let b:refactor_preview_diff = 1
      silent 1,1delete _
      setlocal readonly nomodifiable
      setlocal noswapfile nobuflisted
      setlocal buftype=nofile bufhidden=delete
      diffthis
      exec winnr . 'winc w'
    endif
  endif
endfunction " }}}

" s:Refactor(command) {{{
function s:Refactor(command)
  let cwd = substitute(getcwd(), '\', '/', 'g')
  let cwd_return = 1

  try
    " turn off swap files temporarily to avoid issues with folder/file
    " renaming.
    let bufend = bufnr('$')
    let bufnum = 1
    while bufnum <= bufend
      if bufexists(bufnum)
        call setbufvar(bufnum, 'save_swapfile', getbufvar(bufnum, '&swapfile'))
        call setbufvar(bufnum, '&swapfile', 0)
      endif
      let bufnum = bufnum + 1
    endwhile

    " cd to the project root to avoid folder renaming issues on windows.
    exec 'cd ' . escape(eclim#project#util#GetCurrentProjectRoot(), ' ')

    let result = eclim#ExecuteEclim(a:command)
    if result == "0"
      return
    endif

    if result !~ '^files:'
      call eclim#util#Echo(result)
      return
    endif

    " reload affected files.
    let files = split(result, "\n")[1:]
    let curwin = winnr()
    try
      for file in files
        let file = substitute(file, '\', '/', 'g')
        let newfile = ''
        " handle file renames
        if file =~ '\s->\s'
          let newfile = substitute(file, '.*->\s*', '', '')
          if has('win32unix')
            let newfile = eclim#cygwin#CygwinPath(newfile)
          endif
          let file = substitute(file, '\s*->.*', '', '')
        endif
        if has('win32unix')
          let file = eclim#cygwin#CygwinPath(file)
        endif

        " ignore unchanged directories
        if isdirectory(file)
          continue
        endif

        " handle current working directory moved.
        if newfile != '' && isdirectory(newfile)
          if file =~ '^' . cwd . '\(/\|$\)'
            while cwd !~ '^' . file . '\(/\|$\)'
              let file = fnamemodify(file, ':h')
              let newfile = fnamemodify(newfile, ':h')
            endwhile
          endif

          if cwd =~ '^' . file . '\(/\|$\)'
            let dir = substitute(cwd, file, newfile, '')
            exec 'cd ' . escape(dir, ' ')
            let cwd_return = 0
          endif
          continue
        endif

        let winnr = bufwinnr(file)
        if winnr > -1
          exec winnr . 'winc w'
          if newfile != ''
            let bufnr = bufnr('%')
            enew
            exec 'bdelete ' . bufnr
            exec 'edit ' . escape(eclim#util#Simplify(newfile), ' ')
          else
            edit
          endif
        endif
      endfor
    finally
      exec curwin . 'winc w'
      if cwd_return
        exec 'cd ' . escape(cwd, ' ')
      endif
    endtry
  finally
    " re-enable swap files
    let bufnum = 1
    while bufnum <= bufend
      if bufexists(bufnum)
        let save_swapfile = getbufvar(bufnum, 'save_swapfile')
        if save_swapfile != ''
          call setbufvar(bufnum, '&swapfile', save_swapfile)
        endif
      endif
      let bufnum = bufnum + 1
    endwhile
  endtry
endfunction " }}}

" vim:ft=vim:fdm=marker
