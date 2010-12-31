" Author:  Eric Van Dewoestine
"
" Description: {{{
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

" Format(types, tags) {{{
function! eclim#taglisttoo#lang#cproject#Format(types, tags)
  let pos = getpos('.')

  let formatter = taglisttoo#util#Formatter(a:tags)
  call formatter.filename()

  let config_contents = []

  let configs = filter(copy(a:tags), 'v:val.type == "c"')
  let entries = filter(copy(a:tags), 'v:val.type == "e"')
  let toolchains = filter(copy(a:tags), 'v:val.type == "t"')
  let tools = filter(copy(a:tags), 'v:val.type == "l"')
  let includes = filter(copy(a:tags), 'v:val.type == "i"')
  let symbols = filter(copy(a:tags), 'v:val.type == "s"')
  for config in configs
    let config_start = config.line
    call cursor(config_start, 1)
    call search('<configuration', 'c', config_start)
    let config_end = searchpair(
      \ '<configuration', '', '</configuration', 'Wn', 's:SkipComments()')

    let entrs = []
    for entry in entries
      if len(entry) > 3
        let line = entry.line
        if line > config_start && line < config_end
          call add(entrs, entry)
        endif
      endif
    endfor

    let tcs = []
    for tool in toolchains
      if len(tool) > 3
        let line = tool.line
        if line > config_start && line < config_end
          call add(tcs, tool)
        endif
      endif
    endfor

    let tls = []
    for tool in tools
      if len(tool) > 3
        let line = tool.line
        if line > config_start && line < config_end
          call add(tls, tool)
          let tool_start = tool.line
          call cursor(tool_start, 1)
          if getline('.') =~ '/>\s*$'
            continue
          endif
          call search('<tool', 'c', tool_start)
          let tool_end = searchpair(
                \ '<tool', '', '</tool', 'W', 's:SkipComments()')

          let index = 0
          for include in includes[:]
            if len(include) > 3
              let line = include.line
              if line > tool_start && line < tool_end
                let include.name = "\t" . include.name
                call add(tls, include)
                call remove(includes, index)
              endif
            endif
            let index += 1
          endfor

          let index = 0
          for symbol in symbols[:]
            if len(symbol) > 3
              let line = symbol.line
              if line > tool_start && line < tool_end
                let symbol.name = "\t" . symbol.name
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
    call formatter.blank()
    call formatter.heading(a:types['c'], config_content.config, '')
    call formatter.format(a:types['e'], config_content.entries, "\t")
    call formatter.format(a:types['t'], config_content.toolchains, "\t")
    call formatter.format(a:types['l'], config_content.tools, "\t")
  endfor

  call setpos('.', pos)

  return formatter
endfunction " }}}

" Parse(file, settings) {{{
function! eclim#taglisttoo#lang#cproject#Parse(file, settings)
  return taglisttoo#util#Parse(a:file, [
      \ ['c', "<configuration\\s+[^>]*?name=['\"](.*?)['\"]", 1],
      \ ['e', "<entry\\s+[^>]*?name=['\"](.*?)['\"]", 1],
      \ ['t', "<toolChain\\s+[^>]*?name=['\"](.*?)['\"]", 1],
      \ ['l', "<tool\\s+[^>]*?name=['\"](.*?)['\"]", 1],
      \ ['i', "<option\\s+[^>]*?valueType=['\"]includePath['\"]", 'includes'],
      \ ['s', "<option\\s+[^>]*?valueType=['\"]definedSymbols['\"]", 'symbols'],
    \ ])
endfunction " }}}

" s:SkipComments() {{{
function s:SkipComments()
  let synname = synIDattr(synIDtrans(synID(line('.'), col('.'), 1)), "name")
  return synname =~? 'Comment\|String'
endfunction " }}}

" vim:ft=vim:fdm=marker
