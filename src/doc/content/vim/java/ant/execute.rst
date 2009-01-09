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

.. _vim/java/ant/execute:

Ant Execution
=============

For those that use Vim as an editor and ant as a build tool, is is common to set
your Vim 'makeprg' option to execute ant so that you may navigate compilation
errors via Vim's quickfix functionality.

Eclim utilizes this same paradigm to provide users with ant execution
functionality from any file without any of the setup required by Vim.

.. _\:Ant:

Eclim provides the following command\:

**:Ant** [<target> ...]

which performs the following steps\:

- Saves any previous 'makeprg' and 'errorformat' option settings so that you can
  define your own settings for the :make command.
- Sets 'makeprg' to execute ant with the -find option so that it will search for
  your build.xml file in the current directory or in a parent directory.
- Sets 'errorformat' to recognize the following errors\:

  - javac errors.
  - javadoc errors.
  - jasper jsp compilattion errors.
  - junit errors / failures.
  - cactus errors / failures.
- Executes :make.
- Restores your previous 'makeprg' and 'errorformat' option settings.

Additionally, if :ref:`g:EclimMakeLCD` is enabled (which it is by default),
then the execution of ant will be performed from the current buffer's local
directory, ensuring that ant's build file discovery method is performed from
the buffer's working directory and not your own.

.. note::

  **:Ant** also supports use of '!' (:Ant!) just like :make does, which tells
  Vim not to jump to the first error if one exists.

The **:Ant** command also has the added benefit of command completion.

.. code-block:: vim

  :Ant com<Tab>
  :Ant compile

.. warning::

  If your ant file has a lot of imports, then the command completion may be slow
  as Eclipse parses all the imports when creating the ant model.  You will
  notice the same slow behavior when using Eclipse directly to perform ant code
  completion.


Configuration
-------------

Vim Variables

.. _g\:EclimAntCompilerAdditionalErrorFormat:

- **g:EclimAntCompilerAdditionalErrorFormat** (Default: '') -
  Since there are many more ant tasks beyond javac, javadoc, etc., eclim
  provides this variable as a means to add error format information for
  any additional ant tasks that you may be using.

  Example: Adding support for xslt

  .. code-block:: vim

    let g:EclimAntCompilerAdditionalErrorFormat =
      \ '\%A%.%#[xslt]\ Loading\ stylesheet\ %f,' .
      \ '\%Z%.%#[xslt]\ %.%#:%l:%c:\ %m,'

  .. note::

    The xslt task is a bit flaky when it comes to reporting the file name on
    errors, so the above format will catch successful runs as well.  If anyone
    has a better solution, please submit it.
