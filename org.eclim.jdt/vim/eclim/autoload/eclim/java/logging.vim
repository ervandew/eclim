" Author:  Eric Van Dewoestine
"
" Description: {{{
"  see http://eclim.org/vim/java/logging.html
"
" License:
"
" Copyright (C) 2005 - 2013  Eric Van Dewoestine
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

function! eclim#java#logging#LoggingInit(var) " {{{
  let char = nr2char(getchar())
  " only execute if the user types a '.' for a method call and if the logger
  " is not already present.
  if char == '.' && s:InitLoggingSettings() &&
    \ !search('\(final\|static\)\>.\{-}\<Log\(ger\)\?\s', 'n')
    let line = line('.')
    let col = col('.')
    let position = eclim#java#util#GetClassDeclarationPosition(1)
    if position
      let logger = s:logger
      let logger = substitute(logger, '\${class}', eclim#java#util#GetClassname(), '')
      let logger = substitute(logger, '\${var}', a:var, '')
      if strlen(logger) > &textwidth && logger !~ '\n'
        let logger = substitute(logger,
          \ '\(.*\)\s\(.*\)', '\1\n' . eclim#util#GetIndent(2) . '\2', '')
      endif

      let position = search('{')
      let lines = split(logger, '\n')
      let offset = len(lines) + 1
      call append(position, '')
      call append(position, lines)
      call cursor(line + offset, col)
      for import in s:logger_imports
        call eclim#java#import#Import(import)
      endfor
    endif
  endif
  return char
endfunction " }}}

function! s:InitLoggingSettings() " {{{
  let s:EclimLoggingImpl =
    \ eclim#project#util#GetProjectSetting("org.eclim.java.logging.impl")
  if type(s:EclimLoggingImpl) == g:NUMBER_TYPE || s:EclimLoggingImpl == '0'
    unlet s:EclimLoggingImpl
    return
  endif

  let indent = eclim#util#GetIndent(1)
  if s:EclimLoggingImpl == "commons-logging"
    let s:logger = indent .
      \ "private static final Log ${var} = LogFactory.getLog(${class}.class);"
    let s:logger_imports = [
      \ "org.apache.commons.logging.Log",
      \ "org.apache.commons.logging.LogFactory"]
  elseif s:EclimLoggingImpl == "slf4j"
    let s:logger = indent .
      \ "private static final Logger ${var} = LoggerFactory.getLogger(${class}.class);"
    let s:logger_imports = ["org.slf4j.Logger", "org.slf4j.LoggerFactory"]
  elseif s:EclimLoggingImpl == "log4j"
    let s:logger = indent .
      \ "private static final Logger ${var} = Logger.getLogger(${class}.class);"
    let s:logger_imports = ["org.apache.log4j.Logger"]
  elseif s:EclimLoggingImpl == "jdk"
    let s:logger = indent .
      \ "private static final Logger ${var} = Logger.getLogger(${class}.class.getName());"
    let s:logger_imports = ["java.util.logging.Logger"]
  elseif s:EclimLoggingImpl == "custom"
    let instance = eclim#client#nailgun#ChooseEclimdInstance()
    if type(instance) != g:DICT_TYPE
      return
    endif

    let name = eclim#project#util#GetProjectSetting("org.eclim.java.logging.template")
    if type(name) == g:NUMBER_TYPE || name == ''
      return
    endif

    let local = eclim#UserHome() . '/.eclim/resources/jdt/templates/' . name
    let remote = substitute(instance.home, 'org.eclim_', 'org.eclim.jdt_', '') .
      \ '/resources/templates/' . name
    if filereadable(local)
      let template = local
    elseif filereadable(remote)
      let template = remote
    else
      call eclim#util#EchoError(
        \ "Custom logger template not found local or remote location:\n" .
        \ "  local: " . local . "\n" .
        \ "  remote: " . remote)
      return
    endif
    let lines = readfile(template)
    let s:logger_imports = lines[:]
    call filter(s:logger_imports, "v:val =~ '^\\s*import\\>'")
    call map(s:logger_imports,
      \ "substitute(v:val, '^\\s*import\\>\\s*\\(.*\\);\\s*', '\\1', '')")
    call filter(lines, "v:val !~ '\\(^\\s*$\\|^\\s*import\\>\\)'")
    let s:logger = indent . join(lines, "\n" . indent)
  elseif s:EclimLoggingImpl == ''
    " no setting returned, probably not in a project, or user is attempting to
    " disable this functionality for the current project.
    return
  else
    echoe "Invalid logging implementation '" . s:EclimLoggingImpl . "' configured."
    return
  endif
  return 1
endfunction " }}}

" vim:ft=vim:fdm=marker
