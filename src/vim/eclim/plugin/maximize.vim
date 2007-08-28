" Author:  Eric Van Dewoestine
" Version: $Revision$
"
" Description: {{{
"   see http://eclim.sourceforge.net/vim/common/maximize.html
"
"   Plugin that allows you to maximize a window, or minimize one or more
"   windows that you have open in vim to allocate more screen space without
"   closing any windows.
"   This plugin is in no way a new idea as there have been several vim tips
"   and a couple other plugins on the subject.  However, I beleive this plugin
"   is unique in its implementation and usage.
"
" Usage:
"   When editing files, the following commands will be available
"     :MaximizeWindow
"     :MinimizeWindow [bufnum] [bufnum] ...
"     :MinimizeRestore
"
"   MaximizeWindow:
"     This command toggles the maximization of the current vim window.
"
"   MinimizeWindow:
"     This command can be used to minimize one or more windows.  To minimize
"     the current window, simply invoke the command with no arguments.
"       :MinimizeWindow
"     To minimize multiple windows, invoke the command with the buffer numbers
"     of the windows to be minimized.
"       :MinimizeWindow 1 5 7
"     By default this plugin will modify the status line of the windows to
"     display that window's buffer number in front of the line number.
"       Eg.
"         .vim/plugin/maximize.vim          1,30,11     4%
"       Where in this example the buffer number is 1 and the cursor is on line
"       30, column 11.
"
"   MinimizeRestore:
"     Un-minimizes all windows.
"
" Configuration:
"   g:MaximizeExcludes
"     This property is a regex used to determine what buffers names to exclude
"     from resizing.  Users will typically like to keep their quickfix, taglist,
"     minibufexplorer, etc. windows at their default height.
"     Defaults to '\(__Tag_List__\|-MiniBufExplorer-\)' to preserve the window
"     sizes for taglist and minibufexpl.
"
"   g:MaximizeMinWinHeight
"     Used to set the lowest height to which a minimized buffer will be
"     resized to.
"     Defaults to 0.
"
"   g:MaximizeQuickfixHeight
"     When altering the sizes of windows, if you happen to have quickfix
"     window open, it's size may become skewed.  The property is used to set
"     the height to which you want the quickfix window resized to.
"     Defaults to 10.
"
"   g:MaximizeStatusLine
"     When using the MinimizeWindow functionality, you will need a quick and
"     easy way to determine the buffer number of a window.  By default this
"     plugin alters the statusline so that you can see the buffer number
"     preceding the line number (bufnum,linenum,colnum...  1,35,12).  You may
"     set this property if you would like to customize the status line to your
"     liking.
"     Defaults to '%<%f\ %M\ %h%r%=%-10.(%n,%l,%c%V%)\ %P' which closely mimics
"     vims default status line.
"
" Todo:
"   - Executing cclose from outside the quickfix window screws up minimize and
"     maximize window sizes. (People have been asking for a post window
"     closing event for vim7... that should help solve this issue).
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

" Global Variables {{{
  if !exists('g:MaximizeStatusLine')
    let g:MaximizeStatusLine = '%<%f\ %M\ %h%r%=%-10.(%n,%l,%c%V%)\ %P'
  endif

  if !exists('g:MaximizeDisableStatusLine')
    exec "set statusline=" . g:MaximizeStatusLine
  endif
" }}}

" Command Declarations {{{
if !exists(":MaximizeWindow")
  command MaximizeWindow :call eclim#display#maximize#MaximizeWindow()
endif
if !exists(":MinimizeWindow")
  command -nargs=* MinimizeWindow :call eclim#display#maximize#MinimizeWindow(<f-args>)
endif
if !exists(":MinimizeRestore")
  command MinimizeRestore
      \ :call eclim#display#maximize#ResetMinimized() |
      \ call eclim#display#maximize#RestoreWindows()
endif
" }}}

" vim:ft=vim:fdm=marker
