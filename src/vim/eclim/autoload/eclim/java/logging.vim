" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"  see http://eclim.sourceforge.net/vim/java/logging.html
"
" License:
"
" Copyright (c) 2005 - 2006
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

" LoggingInit(var) {{{
function! eclim#java#logging#LoggingInit (var)
  let char = nr2char(getchar())
  " only execute if the user types a '.' for a method call and if the logger
  " is not already present.
  if char == '.' && s:InitLoggingSettings() && !search('final Log\(ger\)\?\s', 'n')
    let offset = 2
    let line = line('.')
    let col = col('.')
    let position = eclim#java#util#GetClassDeclarationPosition(1)
    if position
      let logger = s:logger
      let logger = substitute(logger, '<class>', eclim#java#util#GetClassname(), '')
      let logger = substitute(logger, '<var>', a:var, '')
      if strlen(logger) > &textwidth
        let logger = substitute(logger,
          \ '\(.*\)\s\(.*\)', '\1\n' . g:EclimIndent . g:EclimIndent . '\2', '')
        let offset = 3
      endif

      let position = search('{')
      call append(position, '')
      let saved = @"
      let @" = logger
      silent put
      let @" = saved
      call cursor(line + offset, col)
      for import in s:logger_imports
        call eclim#java#import#InsertImport(import)
      endfor
    endif
  endif
  return char
endfunction " }}}

" InitLoggingSettings() {{{
" Initializes the necessary logging settings.
function! s:InitLoggingSettings ()
  let s:EclimLoggingImpl =
    \ eclim#project#GetProjectSetting("org.eclim.java.logging.impl")
  if s:EclimLoggingImpl == "commons-logging"
    let s:logger = g:EclimIndent .
      \ "private static final Log <var> = LogFactory.getLog(<class>.class);"
    let s:logger_imports = [
      \ "org.apache.commons.logging.Log",
      \ "org.apache.commons.logging.LogFactory"]
  elseif s:EclimLoggingImpl == "slf4j"
    let s:logger = g:EclimIndent .
      \ "private static final Logger <var> = LoggerFactory.getLogger(<class>.class);"
    let s:logger_imports = ["org.slf4j.Logger", "org.slf4j.LoggerFactory"]
  elseif s:EclimLoggingImpl == "log4j"
    let s:logger = g:EclimIndent .
      \ "private static final Logger <var> = Logger.getLogger(<class>.class);"
    let s:logger_imports = ["org.apache.log4j.Logger"]
  elseif s:EclimLoggingImpl == "jdk"
    let s:logger = g:EclimIndent .
      \ "private static final Logger <var> = Logger.getLogger(<class>.class.getName());"
    let s:logger_imports = ["java.util.logging.Logger"]
  elseif s:EclimLoggingImpl == ""
    " no setting returned, probably not in a project, or user is attempting to
    " disable this functionality for the current project.
    return 0
  elseif s:EclimLoggingImpl == "0"
    " GetProjectSetting call failed.
    return 0
  else
    echoe "Invalid logging implementation '" . s:EclimLoggingImpl . "' configured."
    return 0
  endif
  return 1
endfunction " }}}

" vim:ft=vim:fdm=marker
