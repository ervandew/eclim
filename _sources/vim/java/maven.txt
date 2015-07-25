.. Copyright (C) 2005 - 2014  Eric Van Dewoestine

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

Maven
======

.. _\:Maven:

.. _\:Mvn:

Running
-------

Much like the provided :doc:`ant </vim/java/ant>` execution functionality,
eclim also provides commands for running maven 1.x or 2.x.

Eclim provides the following commands\:

.. code-block:: vim

  :Maven [<goal> ...]
  :Mvn [<goal> ...]

which perform the following steps\:

- Save any previous 'makeprg' and 'errorformat' option settings so that
  you can define your own settings for the :make command.
- Set 'makeprg' to execute maven or mvn with the --find option so that it
  will search for your pom file in the current directory or in a parent
  directory.
- Set 'errorformat' to recognize the following errors\:

  - javac errors.
  - javadoc errors.
  - junit errors / failures.
- Execute :make.
- Restore your previous 'makeprg' and 'errorformat' option settings.

Additionally, if :ref:`g:EclimMakeLCD` is enabled (which it is by default),
then the execution of maven will be performed from the current buffer's project
root directory, ensuring that mavens's build file discovery method is performed
from the buffer's working directory and not your own.

Note that **:Mvn** MUST have this enabled since maven 2.x no
longer has support for the ``--find`` option.

.. note::

  Both **:Maven** and **:Mvn** also supports use of '!' (:Maven!) just like
  :make does, which tells Vim not to jump to the first error if one exists.
