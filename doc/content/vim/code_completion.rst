:orphan:

.. Copyright (C) 2005 - 2013  Eric Van Dewoestine

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.

Code Completion
===============

All the code completion functionality provided by eclim (ant, java, etc) makes
use of the new "User Defined Completion" added to Vim 7.  To initiate code
completion enter insert mode and type *Ctrl-X Ctrl-U*.  By default Vim will
open a popup if there is more than one completion.

.. _g\:EclimCompletionMethod:

.. note::

  If you would prefer to have eclim use vim's omni code completion instead, you
  can add the following to your vimrc:

  ::

    let g:EclimCompletionMethod = 'omnifunc'

  When using omnifunc you will use *Ctrl-X Ctrl-O* to start code completion.

Example with java completion

.. image:: ../images/screenshots/java/completion.png

Once you have started the completion you can use *Ctrl-N* to proceed to the
next match and *Ctrl-P* to move to the previous match.

If you are like me and you find those key strokes a bit cumbersome, then you
might want to check out one of the following plugins which can make completion
usage less cumbersome:

- SuperTab_ This plugin's aim is to allow you to use ``<tab>`` for all your
  code completion needs.
- AutoComplPop_ This plugin will automatically open the completion popup
  for you after you've typed a preconfigured number of characters. You can use
  AutoComplPop in lieu of, on in conjunction with SuperTab.

  AutoComplPop by default only supports triggering code completion for file types
  who have an omni completion that ships with vim, but you can configure it to
  support eclim code completion. Here is an example of some vim script you can
  add to your vimrc to enabled AutoComlPop for java file types (this example will
  trigger the completion popup when at least 3 characters have been typed after a
  dot, but you can tweak this to your tastes):

  .. code-block:: vim

    let g:acp_behaviorJavaEclimLength = 3
    function MeetsForJavaEclim(context)
      return g:acp_behaviorJavaEclimLength >= 0 &&
            \ a:context =~ '\k\.\k\{' . g:acp_behaviorJavaEclimLength . ',}$'
    endfunction
    let g:acp_behavior = {
        \ 'java': [{
          \ 'command': "\<c-x>\<c-u>",
          \ 'completefunc' : 'eclim#java#complete#CodeComplete',
          \ 'meets'        : 'MeetsForJavaEclim',
        \ }]
      \ }
- YouCompleteMe_: Like AutoComplPop, YouCompleteMe will automatically open
  the completion popup for you and it also adds fuzzy matching of completion
  results. This plugin does have a compiled component to it so be sure to read
  their install docs thoroughly.

  Once installed, you'll need to add the following to your vimrc so that eclim
  and YouCompleteMe play nice together:

  .. code-block:: vim

    let g:EclimCompletionMethod = 'omnifunc'

To find out more about Vim's insert completion execute the following from
within Vim:

::

  :h ins-completion

.. _supertab: https://github.com/ervandew/supertab
.. _autocomplpop: https://bitbucket.org/ns9tks/vim-autocomplpop
.. _youcompleteme: https://github.com/Valloric/YouCompleteMe
