" Author:  Eric Van Dewoestine
"
" Description: {{{
"   Plugin that integrates vim with the eclipse plugin eclim (ECLipse
"   IMproved).
"
"   This plugin contains shared functions that can be used regardless of the
"   current file type being edited.
"
" License:
"
" Copyright (C) 2005 - 2014  Eric Van Dewoestine
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

" Global Variables {{{

" at least one ubuntu user had serious performance issues using the python
" client, so we are only going to default to python on windows machines
" where there is an actual potential benefit to using it.
call eclim#AddVimSetting(
  \ 'Core', 'g:EclimNailgunClient',
  \ has('python') && (has('win32') || has('win64')) ? 'python' : 'external',
  \ 'Sets the eclim nailgun client to use when communicating with eclimd.',
  \ '\(external\|python\)')

call eclim#AddVimSetting(
  \ 'Core', 'g:EclimLogLevel', 'info',
  \ 'Sets the eclim logging level within vim.',
  \ '\(trace\|debug\|info\|warning\|error\|off\)')

call eclim#AddVimSetting(
  \ 'Core', 'g:EclimHighlightTrace', 'Normal',
  \ 'Sets the vim highlight group to be used for trace messages.')
call eclim#AddVimSetting(
  \ 'Core', 'g:EclimHighlightDebug', 'Normal',
  \ 'Sets the vim highlight group to be used for debug messages.')
call eclim#AddVimSetting(
  \ 'Core', 'g:EclimHighlightInfo', 'Statement',
  \ 'Sets the vim highlight group to be used for info messages/signs.')
call eclim#AddVimSetting(
  \ 'Core', 'g:EclimHighlightWarning', 'WarningMsg',
  \ 'Sets the vim highlight group to be used for warning messages/signs.')
call eclim#AddVimSetting(
  \ 'Core', 'g:EclimHighlightError', 'Error',
  \ 'Sets the vim highlight group to be used for error messages/signs.')
call eclim#AddVimSetting(
  \ 'Core', 'g:EclimHighlightSuccess', 'MoreMsg',
  \ 'Sets the vim highlight group to be used for success messages/signs.')

call eclim#AddVimSetting(
  \ 'Core', 'g:EclimMenus', 1,
  \ 'When enabled, eclim will generate gvim menu items for eclim commands',
  \ '\(0\|1\)')

call eclim#AddVimSetting(
  \ 'Core', 'g:EclimDefaultFileOpenAction', 'split',
  \ "The global default action used to open files from various eclim commands.\n" .
  \ "Any command that provides a setting for their open action will use this\n" .
  \ "value as their default unless otherwise overridden.")

call eclim#AddVimSetting(
  \ 'Core', 'g:EclimCompletionMethod', 'completefunc',
  \ "Determines whether eclim's various completion functions are registed\n" .
  \ "to vim's completefunc (<c-x><c-u>) or omnifunc (<c-x><c-o>).",
  \ '\(completefunc\|omnifunc\)')
call eclim#AddVimSetting(
  \ 'Core', 'g:EclimValidateSortResults', 'occurrence',
  \ 'Sets how validation results from the various language validators will be sorted',
  \ '\(occurrence\|severity\)')
call eclim#AddVimSetting(
  \ 'Core', 'g:EclimTempFilesEnable', 0,
  \ 'Should eclim ever use temp files for code completion, etc.',
  \ '\(0\|1\)')
call eclim#AddVimSetting(
  \ 'Core', 'g:EclimFileTypeValidate', 1,
  \ "Allows you to disable all eclim lang validators at once.",
  \ '\(0\|1\)')
call eclim#AddVimSetting(
  \ 'Core', 'g:EclimRefactorDiffOrientation', 'vertical',
  \ "When viewing a diff for a refactoring, should the diff split be\n" .
  \ "vertical or horizontal.",
  \ '\(horizontal\|vertical\)')

call eclim#AddVimSetting(
  \ 'Core', 'g:EclimShowCurrentError', 1,
  \ "When enabled, eclim with echo the quickfix/location list message,\n" .
  \ "if any, for the line under the cursor.",
  \ '\(0\|1\)')
call eclim#AddVimSetting(
  \ 'Core', 'g:EclimShowCurrentErrorBalloon', 1,
  \ "When enabled, eclim will display a balloon popup (gvim only) containing\n" .
  \ "the quickfix/location list message, if any, for the line under the cursor.",
  \ '\(0\|1\)')

call eclim#AddVimSetting(
  \ 'Core', 'g:EclimLargeFileEnabled', 0,
  \ "When enabled, eclim will disable some vim features to speed up\n" .
  \ "opening, navigating, etc, large files.",
  \ '\(0\|1\)')
call eclim#AddVimSetting(
  \ 'Core', 'g:EclimLargeFileSize', 5,
  \ "The minimum size of the file in mb before the file is considered\n" .
  \ "large enough to apply large file settings (if enabled).",
  \ '\d\+')

call eclim#AddVimSetting(
  \ 'Core', 'g:EclimPromptListStartIndex', 0,
  \ 'The starting index to use for list based prompts.',
  \ '\(0\|1\)')

call eclim#AddVimSetting(
  \ 'Core', 'g:EclimMakeLCD', 1,
  \ "When set to a non-0 value, all eclim based make commands\n" .
  \ "(:Ant, :Maven, :Mvn, etc) will change to the current file's\n" .
  \ "project root before executing.",
  \ '\(0\|1\)')
call eclim#AddVimSetting(
  \ 'Core', 'g:EclimMakeDispatchEnabled', 1,
  \ "When tpope/vim-dispatch is installed and this option is set\n" .
  \ "to a non-0 value, then eclim will run its make based commands\n" .
  \ "(:Ant, :Maven, :Mvn, etc) via dispatch.",
  \ '\(0\|1\)')

call eclim#AddVimSetting(
  \ 'Core', 'g:EclimQuickFixOpen', 'botright copen',
  \ 'Determines the command to use when eclim opens the quickfix window.')
call eclim#AddVimSetting(
  \ 'Core', 'g:EclimQuickFixHeight', 10,
  \ 'Determines the height of the quickfix window when eclim opens it.',
  \ '\d\+')

call eclim#AddVimSetting(
  \ 'Core/Signs', 'g:EclimSignLevel', has('signs') ? 'info' : 'off',
  \ 'Sets the level of signs (markers) that will be placed by eclim.',
  \ '\(info\|warning\|error\|off\)')

call eclim#AddVimSetting(
  \ 'Core/Signs', 'g:EclimShowQuickfixSigns', 0,
  \ 'Determines if a sign is placed on lines found in the quickfix list.',
  \ '\(0\|1\)')
call eclim#AddVimSetting(
  \ 'Core/Signs', 'g:EclimShowLoclistSigns', 1,
  \ 'Determines if a sign is placed on lines found in the location list.',
  \ '\(0\|1\)')
call eclim#AddVimSetting(
  \ 'Core/Signs', 'g:EclimQuickfixSignText', '>',
  \ 'Sets the one or two character text used for quickfix list signs.',
  \ '.\{1,2}')
call eclim#AddVimSetting(
  \ 'Core/Signs', 'g:EclimLoclistSignText', '>>',
  \ 'Sets the one or two character text used for location list signs.',
  \ '.\{1,2}')
call eclim#AddVimSetting(
  \ 'Core/Signs', 'g:EclimUserSignText', '#',
  \ 'Sets the one or two character text used for user placed signs.',
  \ '.\{1,2}')
call eclim#AddVimSetting(
  \ 'Core/Signs', 'g:EclimHighlightUserSign', g:EclimHighlightInfo,
  \ 'Sets the vim highlight group to be used for user placed signs.')

call eclim#AddVimSetting(
  \ 'Core/:Buffers', 'g:EclimBuffersTabTracking', 1,
  \ "When enabled, eclim will keep track of which tabs buffers have\n" .
  \ "been opened on, allowing the :Buffers command to limit results\n" .
  \ "based on the current tab.",
  \ '\(0\|1\)')
call eclim#AddVimSetting(
  \ 'Core/:Buffers', 'g:EclimBuffersSort', 'file',
  \ 'Determines how the results are sorted in the :Buffers window.',
  \ '\(file\|path\|bufnr\)')
call eclim#AddVimSetting(
  \ 'Core/:Buffers', 'g:EclimBuffersSortDirection', 'asc',
  \ 'Determines the direction of the sort in the :Buffers window.',
  \ '\(asc\|desc\)')
call eclim#AddVimSetting(
  \ 'Core/:Buffers', 'g:EclimBuffersDefaultAction', g:EclimDefaultFileOpenAction,
  \ 'Sets the default command used to open selected entries in the :Buffers window.')
call eclim#AddVimSetting(
  \ 'Core/:Buffers', 'g:EclimBuffersDeleteOnTabClose', 0,
  \ "When buffer tab tracking is enabled, this determines if all the\n" .
  \ "buffers associated with a tab are deleted whenthat tab is closed.",
  \ '\(0\|1\)')

call eclim#AddVimSetting(
  \ 'Core/:Only', 'g:EclimOnlyExclude', '^NONE$',
  \ "Vim regex pattern to match against buffer names that when matched,\n" .
  \ "will not be closed upon calling eclim's :Only command.")
call eclim#AddVimSetting(
  \ 'Core/:Only', 'g:EclimOnlyExcludeFixed', 1,
  \ "When running eclim's :Only command, should 'fixed' windows (quickfix,\n" .
  \ "project tree, tag list, etc) be preserved (excluded from being closed).",
  \ '\(0\|1\)')

call eclim#AddVimSetting(
  \ 'Core/:History', 'g:EclimKeepLocalHistory', exists('g:vimplugin_running'),
  \ 'Whether or not to update the eclipse local history for project files.',
  \ '\(0\|1\)')
call eclim#AddVimSetting(
  \ 'Core/:History', 'g:EclimHistoryDiffOrientation', 'vertical',
  \ "When viewing a diff from the history buffer, should the diff split be\n" .
  \ "vertical or horizontal.",
  \ '\(horizontal\|vertical\)')

call eclim#AddVimSetting(
  \ 'Core/:LocateFile', 'g:EclimLocateFileDefaultAction', g:EclimDefaultFileOpenAction,
  \ 'The default action to use when opening files from :LocateFile')
call eclim#AddVimSetting(
  \ 'Core/:LocateFile', 'g:EclimLocateFileScope', 'project',
  \ 'The default search scope when searching from the context of a project.')
call eclim#AddVimSetting(
  \ 'Core/:LocateFile', 'g:EclimLocateFileNonProjectScope', 'workspace',
  \ 'The default search scope when searching from outside the context of a project.')
call eclim#AddVimSetting(
  \ 'Core/:LocateFile', 'g:EclimLocateFileFuzzy', 1,
  \ 'Whether or not to use fuzzy matching when searching.',
  \ '\(0\|1\)')
call eclim#AddVimSetting(
  \ 'Core/:LocateFile', 'g:EclimLocateFileCaseInsensitive', 'lower',
  \ 'Sets under what condition will the search be case insensitive.',
  \ '\(lower\|never\|always\)')
call eclim#AddVimSetting(
  \ 'Core/:LocateFile', 'g:EclimLocateFileCaseInsensitive', 'lower',
  \ 'Sets under what condition will the search be case insensitive.',
  \ '\(lower\|never\|always\)')

call eclim#AddVimSetting(
  \ 'Lang/Xml', 'g:EclimXmlValidate', 1,
  \ 'Whether or not to validate xml files on save.',
  \ '\(0\|1\)')

if !exists('g:EclimLocateUserScopes')
  let g:EclimLocateUserScopes = []
endif

if !exists("g:EclimLocationListHeight")
  let g:EclimLocationListHeight = 10
endif

if !exists("g:EclimMakeQfFilter")
  let g:EclimMakeQfFilter = 1
endif

if !exists("g:EclimTemplatesDisabled")
  " Disabled for now.
  let g:EclimTemplatesDisabled = 1
endif

if !exists("g:EclimSeparator")
  let g:EclimSeparator = '/'
  if has("win32") || has("win64")
    let g:EclimSeparator = '\'
  endif
endif

let g:EclimQuote = "['\"]"

if !exists("g:EclimTempDir")
  " NOTE: `expand("$FOO")` might spawn a new shell.
  if len($TMP)
    let g:EclimTempDir = $TMP
  elseif len($TEMP)
    let g:EclimTempDir = $TEMP
  elseif has('unix')
    let g:EclimTempDir = '/tmp'
  endif

  let g:EclimTempDir = substitute(g:EclimTempDir, '\', '/', 'g')
endif
" }}}

" Command Declarations {{{
if !exists(":PingEclim")
  command -nargs=? -complete=customlist,eclim#client#nailgun#CommandCompleteWorkspaces
    \ PingEclim :call eclim#PingEclim(1, '<args>')
endif
if !exists(":ShutdownEclim")
  command ShutdownEclim :call eclim#ShutdownEclim()
endif
if !exists(":VimSettings")
  command VimSettings :call eclim#VimSettings()
endif
if !exists(":WorkspaceSettings")
  command -nargs=? -complete=customlist,eclim#client#nailgun#CommandCompleteWorkspaces
    \ WorkspaceSettings :call eclim#Settings('<args>')
endif
if !exists(":EclimDisable")
  command EclimDisable :call eclim#Disable()
endif
if !exists(":EclimEnable")
  command EclimEnable :call eclim#Enable()
endif
if !exists(':EclimHelp')
  command -nargs=? -complete=customlist,eclim#help#CommandCompleteTag
    \ EclimHelp :call eclim#help#Help('<args>', 0)
endif
if !exists(':EclimHelpGrep')
  command -nargs=+ EclimHelpGrep :call eclim#help#HelpGrep(<q-args>)
endif

if !exists(":RefactorUndo")
  command RefactorUndo :call eclim#lang#UndoRedo('undo', 0)
  command RefactorRedo :call eclim#lang#UndoRedo('redo', 0)
  command RefactorUndoPeek :call eclim#lang#UndoRedo('undo', 1)
  command RefactorRedoPeek :call eclim#lang#UndoRedo('redo', 1)
endif

if !exists(":Buffers")
  command -bang Buffers :call eclim#common#buffers#Buffers('<bang>')
  command -bang BuffersToggle :call eclim#common#buffers#BuffersToggle('<bang>')
endif

if !exists(":Only")
  command Only :call eclim#common#buffers#Only()
endif

if !exists(":DiffLastSaved")
  command DiffLastSaved :call eclim#common#util#DiffLastSaved()
endif

if !exists(":SwapWords")
  command SwapWords :call eclim#common#util#SwapWords()
endif
if !exists(":SwapAround")
  command -nargs=1 SwapAround :call eclim#common#util#SwapAround('<args>')
endif
if !exists(":LocateFile")
  command -nargs=? LocateFile :call eclim#common#locate#LocateFile('', '<args>')
  command -nargs=? LocateBuffer
    \ :call eclim#common#locate#LocateFile('', '<args>', 'buffers')
endif

if !exists(":QuickFixClear")
  command QuickFixClear :call setqflist([]) | call eclim#display#signs#Update()
endif
if !exists(":LocationListClear")
  command LocationListClear :call setloclist(0, []) | call eclim#display#signs#Update()
endif

if !exists(":Tcd")
  command -nargs=1 -complete=dir Tcd :call eclim#common#util#Tcd('<args>')
endif

if !exists(":History")
  command History call eclim#common#history#History()
  command -bang HistoryClear call eclim#common#history#HistoryClear('<bang>')
endif

if has('signs')
  if !exists(":Sign")
    command Sign :call eclim#display#signs#Toggle('user', line('.'))
  endif
  if !exists(":Signs")
    command Signs :call eclim#display#signs#ViewSigns('user')
  endif
  if !exists(":SignClearUser")
    command SignClearUser :call eclim#display#signs#UnplaceAll(
      \ eclim#display#signs#GetExisting('user'))
  endif
  if !exists(":SignClearAll")
    command SignClearAll :call eclim#display#signs#UnplaceAll(
      \ eclim#display#signs#GetExisting())
  endif
endif

if !exists(":OpenUrl")
  command -bang -range -nargs=? OpenUrl
    \ :call eclim#web#OpenUrl('<args>', '<bang>', <line1>, <line2>)
endif

if !exists(":Make")
  command -bang -nargs=* Make :call eclim#util#Make('<bang>', '<args>')
endif
" }}}

" Auto Commands{{{
augroup eclim_archive_read
  autocmd!
  if exists('#archive_read')
    autocmd! archive_read
  endif
  autocmd BufReadCmd
    \ jar:/*,jar:\*,jar:file:/*,jar:file:\*,
    \tar:/*,tar:\*,tar:file:/*,tar:file:\*,
    \tbz2:/*,tbz2:\*,tbz2:file:/*,tbz2:file:\*,
    \tgz:/*,tgz:\*,tgz:file:/*,tgz:file:\*,
    \zip:/*,zip:\*,zip:file:/*,zip:file:\*
    \ call eclim#common#util#ReadFile()
augroup END

if g:EclimShowCurrentError
  " forcing load of util, otherwise a bug in vim is sometimes triggered when
  " searching for a pattern where the pattern is echoed twice.  Reproducable
  " by opening a new vim and searching for 't' (/t<cr>).
  runtime eclim/autoload/eclim/util.vim

  augroup eclim_show_error
    autocmd!
    autocmd CursorMoved * call eclim#util#ShowCurrentError()
  augroup END
endif

if g:EclimShowCurrentErrorBalloon && has('balloon_eval')
  set ballooneval
  set balloonexpr=eclim#util#Balloon(eclim#util#GetLineError(line('.')))
endif

if g:EclimMakeQfFilter
  augroup eclim_qf_filter
    autocmd!
    autocmd QuickFixCmdPost make
      \ if exists('b:EclimQuickFixFilter') |
      \   call eclim#util#SetQuickfixList(getqflist(), 'r') |
      \ endif
  augroup END
endif

if g:EclimSignLevel != 'off'
  augroup eclim_qf
    autocmd WinEnter,BufWinEnter * call eclim#display#signs#Update()
    if has('gui_running')
      " delayed to keep the :make output on the screen for gvim
      autocmd QuickFixCmdPost * call eclim#util#DelayedCommand(
        \ 'call eclim#display#signs#QuickFixCmdPost()')
    else
      autocmd QuickFixCmdPost * call eclim#display#signs#QuickFixCmdPost()
    endif
  augroup END
endif

if g:EclimBuffersTabTracking && exists('*gettabvar')
  call eclim#common#buffers#TabInit()
  augroup eclim_buffer_tab_tracking
    autocmd!
    autocmd BufWinEnter,BufWinLeave * call eclim#common#buffers#TabLastOpenIn()
    autocmd TabEnter * call eclim#common#buffers#TabEnter()
    autocmd TabLeave * call eclim#common#buffers#TabLeave()
  augroup END
endif

if has('gui_running') && g:EclimMenus
  augroup eclim_menus
    autocmd BufNewFile,BufReadPost,WinEnter * call eclim#display#menu#Generate()
    autocmd VimEnter * if expand('<amatch>')=='' | call eclim#display#menu#Generate() | endif
  augroup END
endif

if !g:EclimTemplatesDisabled
  augroup eclim_template
    autocmd!
    autocmd BufNewFile * call eclim#common#template#Template()
  augroup END
endif

if !exists('#LargeFile') && g:EclimLargeFileEnabled
  augroup eclim_largefile
    autocmd!
    autocmd BufReadPre * call eclim#common#largefile#InitSettings()
  augroup END
endif
" }}}

" vim:ft=vim:fdm=marker
