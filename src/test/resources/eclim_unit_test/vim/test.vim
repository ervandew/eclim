" Author:  Eric Van Dewoestine

if v:version >= 700
  command -nargs=+ -complete=customlist,SomeCompletion ACommand :call SomeFunction

  let blah = substitute('', '', '', '')
endif

nnoremap <silent> <buffer> <cr> :FindByContext<cr>

" vim:ft=vim:fdm=marker
