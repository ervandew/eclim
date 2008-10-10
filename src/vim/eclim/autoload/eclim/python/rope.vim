" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/python/rope.html
"
" License:
"
" Copyright (C) 2005 - 2008  Eric Van Dewoestine
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

" Init() {{{
function eclim#python#rope#Init ()
  if !has('python')
    call eclim#util#EchoError(
      \ "This functionality requires 'python' support compiled into vim.")
    return 0
  endif

  let ropepath = eclim#python#rope#RopePath()
  if ropepath == ''
    return 0
  endif

python << EOF
import sys, vim
ropepath = vim.eval('ropepath')
if ropepath not in sys.path:
  sys.path.insert(0, ropepath)
EOF

  return 1
endfunction " }}}

" RopePath () {{{
" Gets the base directory where the rope code is located.
function eclim#python#rope#RopePath ()
  if !exists("g:RopePath")
    let savewig = &wildignore
    set wildignore=""
    let file = findfile('autoload/eclim/python/rope.vim', escape(&runtimepath, ' '))
    let &wildignore = savewig

    if file == ''
      echoe 'Unable to determine rope basedir.'
      return ''
    endif
    let basedir = substitute(fnamemodify(file, ':p:h'), '\', '/', 'g')

    let g:RopePath = escape(basedir, ' ')
  endif

  return g:RopePath
endfunction " }}}

" Completions (project, filename, offset) {{{
" Attempts to suggest code completions for a given project path, project
" relative file path and offset.
function eclim#python#rope#Completions (project, filename, offset)
  if !eclim#python#rope#Init()
    return []
  endif

  let results = []
  let completion_error = ''

python << EOF
from rope.base import project
from rope.base.exceptions import RopeError
from rope.contrib import codeassist
project = project.Project(vim.eval('a:project'))

resource = project.get_resource(vim.eval('a:filename'))
code = resource.read()

# code completion
try:
  proposals = codeassist.code_assist(project, code, int(vim.eval('a:offset')))
  proposals = codeassist.sorted_proposals(proposals)
  proposals = [[p.name, p.kind] for p in proposals]
  vim.command("let results = %s" % repr(proposals))
except IndentationError, e:
  vim.command(
    "let completion_error = 'Completion failed due to indentation error.'"
  )
except RopeError, e:
  message = 'Completion failed due to rope error: %s' % type(e)
  vim.command("let completion_error = %s" % repr(message))
EOF

  if completion_error != ''
    call eclim#util#EchoError(completion_error)
  endif

  return results

endfunction " }}}

" FindDefinition (project, filename, offset) {{{
" Attempts to find the definition of the element at the supplied offset.
function eclim#python#rope#FindDefinition (project, filename, offset)
  if !eclim#python#rope#Init()
    return []
  endif

  let result = ''

python << EOF
from rope.base import project
from rope.contrib import codeassist
project = project.Project(vim.eval('a:project'))

resource = project.get_resource(vim.eval('a:filename'))
code = resource.read()

# code completion
location = codeassist.get_definition_location(
  project, code, int(vim.eval('a:offset'))
)
if location:
  path = location[0] and \
    location[0].real_path or \
    '%s/%s' % (vim.eval('a:project'), vim.eval('a:filename'))
  vim.command("let result = '%s|%s col 1|'" % (path, location[1]))
EOF

  return result

endfunction " }}}

" Validate (project, filename) {{{
" Attempts to validate the supplied file.
function eclim#python#rope#Validate (project, filename)
  if !eclim#python#rope#Init()
    return []
  endif

  let results = []

python << EOF
from rope.base import project
from rope.contrib import finderrors
project = project.Project(vim.eval('a:project'))

resource = project.get_resource(vim.eval('a:filename'))
filepath = '%s/%s' % (vim.eval('a:project'), vim.eval('a:filename'))

# code completion
errors = finderrors.find_errors(project, resource)
errors = ['%s:%s:%s' % (filepath, e.lineno, e.error) for e in errors]
vim.command("let results = %s" % repr(errors))
EOF

  return results

endfunction " }}}

" vim:ft=vim:fdm=marker
