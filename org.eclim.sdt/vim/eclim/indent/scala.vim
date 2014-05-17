" Vim indent file
" Language   : Scala (http://scala-lang.org/)
" Maintainer : Stefan Matthias Aust
" Last Change: 2006 Apr 13

if exists("b:did_indent")
  finish
endif
let b:did_indent = 1

setlocal indentexpr=GetScalaIndent()

setlocal indentkeys=0{,0},0),!^F,<>>,o,O

setlocal autoindent shiftwidth=2 tabstop=2 softtabstop=2 expandtab

if exists("*GetScalaIndent")
  finish
endif

function! CountParens(line)
  let line = substitute(a:line, '"\(.\|\\"\)*"', '', 'g')
  let open = substitute(line, '[^(]', '', 'g')
  let close = substitute(line, '[^)]', '', 'g')
  return strlen(open) - strlen(close)
endfunction

function! GetScalaIndent()
  " Find a non-blank line above the current line.
  let lnum = prevnonblank(v:lnum - 1)

  " Hit the start of the file, use zero indent.
  if lnum == 0
    return 0
  endif

  let ind = indent(lnum)
  let prevline = getline(lnum)

  "Indent html literals
  if prevline !~ '/>\s*$' && prevline =~ '^\s*<[a-zA-Z][^>]*>\s*$'
    return ind + &shiftwidth
  endif

  " Add a 'shiftwidth' after lines that start a block
  " If if, for or while end with ), this is a one-line block
  " If val, var, def end with =, this is a one-line block
  if prevline =~ '^\s*\<\(\(else\s\+\)\?if\|for\|while\)\>.*[)]\s*$'
        \ || prevline =~ '^\s*\<\(\(va[lr]\|def\)\>.*[=]\s*$'
        \ || prevline =~ '^\s*\<else\>\s*$'
        \ || prevline =~ '{\s*$'
    let ind = ind + &shiftwidth
  endif

  " If parenthesis are unbalanced, indent or dedent
  let c = CountParens(prevline)
  echo c
  if c > 0
    let ind = ind + &shiftwidth
  elseif c < 0
    let ind = ind - &shiftwidth
  endif
  
  " Dedent after if, for, while and val, var, def without block
  let pprevline = getline(prevnonblank(lnum - 1))
  if pprevline =~ '^\s*\<\(\(else\s\+\)\?if\|for\|while\)\>.*[)]\s*$'
        \ || pprevline =~ '^\s*\<\(\va[lr]\|def\)\>.*[=]\s*$'
        \ || pprevline =~ '^\s*\<else\>\s*$'
    let ind = ind - &shiftwidth
  endif

  " Align 'for' clauses nicely
  if prevline =~ '^\s*\<for\> (.*;\s*$'
    let ind = ind - &shiftwidth + 5
  endif

  " Subtract a 'shiftwidth' on '}' or html
  let thisline = getline(v:lnum)
  if thisline =~ '^\s*[})]' 
        \ || thisline =~ '^\s*</[a-zA-Z][^>]*>'
    let ind = ind - &shiftwidth
  endif

  " Indent multi-lines comments
  if prevline =~ '^\s*\/\*\($\|[^*]\(\(\*\/\)\@!.\)*$\)'
    let ind = ind + 1
  endif

  " Indent multi-lines ScalaDoc
  if prevline =~ '^\s*\/\*\*\($\|[^*]\(\(\*\/\)\@!.\)*$\)'
    let ind = ind + 2
  endif

  " Dedent after multi-lines comments & ScalaDoc
  if prevline =~ '^\s*\(\(\/\*\)\@!.\)*\*\/.*$'
    " Dedent 1
    let ind = ind - 1
    " Align to any multiple of 'shiftwidth'
    let ind = ind - (ind % &shiftwidth)
  endif

  return ind
endfunction
