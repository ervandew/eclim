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

C/C++ Code Completion
=====================

C/C++ code completion uses the standard
:doc:`Vim code completion mechanism </vim/code_completion>` like so\:

.. code-block:: c

  #include <st<C-X><C-U>
  #include <stio.h>

  int main(void) {
    pu<C-X><C-U>
    puts(
    puts("Hello World");
    return EX<C-X><C-U>
    return EXIT_SUCCESS;
  }
