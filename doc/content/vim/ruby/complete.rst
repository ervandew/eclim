.. Copyright (C) 2005 - 2009  Eric Van Dewoestine

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

.. _vim/ruby/complete:

Ruby Code Completion
====================

Ruby code completion uses the standard
:ref:`Vim code completion mechanism <vim/code_completion>` like so\:

.. code-block:: ruby

  class Test
    def getName()
    end

    def getValue()
    end
  end

  test = Test.new
  test.get<C-X><C-U>
  test.getName()

.. note::
  This feature depends on the eclipse dltk_ ruby plugin which should be
  installed for you when choosing ruby support in the eclim installer.

.. _dltk: http://eclipse.org/dltk
