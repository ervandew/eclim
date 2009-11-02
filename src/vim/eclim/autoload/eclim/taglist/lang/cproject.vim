" Author:  Eric Van Dewoestine
"
" Description: {{{
"   see http://eclim.org/vim/taglist.html
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

" FormatCProject(types, tags) {{{
function! eclim#taglist#lang#cproject#FormatCProject(types, tags)
  let pos = getpos('.')

  let lines = []
  let content = []

  call add(content, expand('%:t'))
  call add(lines, -1)

  let config_contents = []

  let configs = filter(copy(a:tags), 'v:val[3] == "c"')
  let entries = filter(copy(a:tags), 'v:val[3] == "e"')
  let toolchains = filter(copy(a:tags), 'v:val[3] == "t"')
  let tools = filter(copy(a:tags), 'v:val[3] == "l"')
  let includes = filter(copy(a:tags), 'v:val[3] == "i"')
  let symbols = filter(copy(a:tags), 'v:val[3] == "s"')
  for config in configs
    exec 'let config_start = ' . split(config[4], ':')[1]
    call cursor(config_start, 1)
    let config_end = searchpair(
      \ '<configuration', '', '</configuration', 'W', 's:SkipComments()')

    let entrs = []
    for entry in entries
      if len(entry) > 3
        exec 'let line = ' . split(entry[4], ':')[1]
        if line > config_start && line < config_end
          call add(entrs, entry)
        endif
      endif
    endfor

    let tcs = []
    for tool in toolchains
      if len(tool) > 3
        exec 'let line = ' . split(tool[4], ':')[1]
        if line > config_start && line < config_end
          call add(tcs, tool)
        endif
      endif
    endfor

    let tls = []
    for tool in tools
      if len(tool) > 3
        exec 'let line = ' . split(tool[4], ':')[1]
        if line > config_start && line < config_end
          call add(tls, tool)
          exec 'let tool_start = ' . split(tool[4], ':')[1]
          call cursor(tool_start, 1)
          if getline('.') =~ '/>\s*$'
            continue
          endif
          let tool_end = searchpair(
                \ '<tool', '', '</tool', 'W', 's:SkipComments()')

          let index = 0
          for include in includes[:]
            if len(include) > 3
              exec 'let line = ' . split(include[4], ':')[1]
              if line > tool_start && line < tool_end
                let include[0] = "\t" . include[0]
                call add(tls, include)
                call remove(includes, index)
              endif
            endif
            let index += 1
          endfor

          let index = 0
          for symbol in symbols[:]
            if len(symbol) > 3
              exec 'let line = ' . split(symbol[4], ':')[1]
              if line > tool_start && line < tool_end
                let symbol[0] = "\t" . symbol[0]
                call add(tls, symbol)
                call remove(symbols, index)
              endif
            endif
            let index += 1
          endfor
        endif
      endif
    endfor

    call sort(entrs)
    call sort(tcs)
    "call sort(tls)
    call add(config_contents, {
        \ 'config': config,
        \ 'entries': entrs,
        \ 'toolchains': tcs,
        \ 'tools': tls,
      \ })
  endfor

  for config_content in config_contents
    call add(content, "")
    call add(lines, -1)
    call add(content, "\t" . a:types['c'] . ' ' . config_content.config[0])
    call add(lines, index(a:tags, config_content.config))

    call eclim#taglist#util#FormatType(
        \ a:tags, a:types['e'], config_content.entries, lines, content, "\t\t")
    call eclim#taglist#util#FormatType(
        \ a:tags, a:types['t'], config_content.toolchains, lines, content, "\t\t")
    call eclim#taglist#util#FormatType(
        \ a:tags, a:types['l'], config_content.tools, lines, content, "\t\t")
  endfor

  call setpos('.', pos)

  return [lines, content]
endfunction " }}}

" s:SkipComments() {{{
function s:SkipComments()
  let synname = synIDattr(synID(line('.'), col('.'), 1), "name")
  return synname =~ '\([Cc]omment\|[Ss]tring\)'
endfunction " }}}

" vim:ft=vim:fdm=marker
