" Author:  Eric Van Dewoestine

let IndentAnything_Dbg = 0
let IndentAnything_Dbg = 1

" Only load this indent file when no other was loaded.
if exists("b:did_indent") && ! IndentAnything_Dbg
  finish
endif

runtime indent/html.vim

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
