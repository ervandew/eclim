" Author:  Eric Van Dewoestine

" Only load this indent file when no other was loaded.
if exists("b:did_htmldjango_indent")
  finish
endif

runtime indent/html.vim
let b:did_htmldjango_indent = 1

let g:HtmlDjangoIndentOpenElements = ''
let g:HtmlDjangoIndentMidElements = ''
for element in g:HtmlDjangoBodyElements
  if len(g:HtmlDjangoIndentOpenElements) > 0
    let g:HtmlDjangoIndentOpenElements .= '\|'
  endif
  let g:HtmlDjangoIndentOpenElements .= element[0]

  for tag in element[1:-2]
    if len(g:HtmlDjangoIndentMidElements) > 0
      let g:HtmlDjangoIndentMidElements .= '\|'
    endif
    let g:HtmlDjangoIndentMidElements .= tag
  endfor
endfor

let HtmlSettings = function('HtmlIndentAnythingSettings')
function! HtmlIndentAnythingSettings ()
  call HtmlSettings()

  let b:indentTrios = [
      \ [ '<\w', '', '\(/>\|</\)' ],
      \ [ '{%\s*\(' . g:HtmlDjangoIndentOpenElements . '\)\s\+.\{-}%}',
        \ '{%\s*\(' . g:HtmlDjangoIndentMidElements . '\)\s\+.\{-}%}',
        \ '{%\s*end\w\+\s*%}' ],
    \ ]
endfunction
