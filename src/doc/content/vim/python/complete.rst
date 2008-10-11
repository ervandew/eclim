.. Copyright (C) 2005 - 2008  Eric Van Dewoestine

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

.. _vim/python/complete:

Python Code Completion
======================

Python code completion uses the standard
:ref:`Vim code completion mechanism <vim/code_completion>` like so\:

.. code-block:: python

  class Test (object):
    def testMethod (self):
      pass

  t = Test()
  t.te<C-X><C-U>
  t.testMethod

.. note::

  Code completion support is provided via eclim's integration with
  :ref:`rope <vim/python/rope>`.

  Please see the :ref:`rope <vim/python/rope>` docs for more information.
