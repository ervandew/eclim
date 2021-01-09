:orphan:

.. Copyright (C) 2005 - 2012  Eric Van Dewoestine

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

Refactoring
===========

Described below are some common commands and configuration for eclim's
refactoring support.

.. note::

   Eclim does not provide refactoring support for all languages, so be sure to
   check the available features for the language of your choice.

.. _\:RefactorUndo:
.. _\:RefactorRedo:
.. _\:RefactorUndoPeek:
.. _\:RefactorRedoPeek:

.. begin-refactor-undo-redo

Refactor Undo/Redo
------------------

In the event that you need to undo a refactoring, eclim provides the
**:RefactorUndo** command. When executed, the last refactoring will be
reverted. If you are unsure what the last refactoring was, the
**:RefactorUndoPeek** command will print the name of the top most refactoring
on the undo stack.

Eclim also provides the **:RefactorRedo** and **:RefactorRedoPeek** commands
which provide the redo counterpart to the undo commands.

.. end-refactor-undo-redo

.. begin-refactor-config

Configuration
-------------

:doc:`Vim Settings </vim/settings>`

.. _g\:EclimRefactorDiffOrientation:

- **g:EclimRefactorDiffOrientation** (Default: 'vertical') -
  Specifies the orientation used when previewing a refactoring and performing a
  diff split between the current file contents and the changes to be performed
  by the refactoring.  Possible values include 'vertical' or 'horizontal'.

.. end-refactor-config
