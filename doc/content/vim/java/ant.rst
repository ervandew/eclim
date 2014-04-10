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

Ant
======

Running
-------

For those that use Vim as an editor and ant as a build tool, is is common to set
your Vim 'makeprg' option to execute ant so that you may navigate compilation
errors via Vim's quickfix functionality.

Eclim utilizes this same paradigm to provide users with ant execution
functionality from any file without any of the setup required by Vim.

.. _\:Ant:

Eclim provides the following command:

**:Ant** [<target> ...]

which performs the following steps:

- Saves any previous 'makeprg' and 'errorformat' option settings so that you can
  define your own settings for the :make command.
- Sets 'makeprg' to execute ant with the -find option so that it will search for
  your build.xml file in the current directory or in a parent directory.
- Sets 'errorformat' to recognize the following errors:

  - javac errors.
  - javadoc errors.
  - jasper jsp compilattion errors.
  - junit errors / failures.
  - cactus errors / failures.
- Executes :make.
- Restores your previous 'makeprg' and 'errorformat' option settings.

Additionally, if :ref:`g:EclimMakeLCD` is enabled (which it is by default),
then the execution of ant will be performed from the current buffer's project
root directory, ensuring that ant's build file discovery method is performed
from the buffer's working directory and not your own.

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

Code Completion
---------------

Ant code completion uses the standard
:doc:`Vim code completion mechanism </vim/code_completion>` like so:

::

  <ja<Ctrl-X><Ctrl-U>
  <jar de<Ctrl-X><Ctrl-U>
  <jar destfile="${bas<Ctrl-X><Ctrl-U>
  <jar destfile="${basdir
  ...

Screenshot of completion in action:

.. image:: ../../images/screenshots/java/ant/completion.png

.. warning::

  If your ant file has a lot of imports, then the code completion may be slow as
  Eclipse parses all the imports when creating the ant model.  You will notice
  the same slow behavior when using Eclipse directly.

Validation
----------

When editing an ant xml file eclim will default to validating the file when it
is written.  Any errors will be added to the current window's location list
(:help location-list) and their corresponding line number noted via Vim's sign
functionality.

Currently the Eclipse ant file validation isn't as robust as one might hope.  It
doesn't validate that element attributes are correct, that child elements are
valid, etc., but it does perform the following:

- If a default target is specified, validate that it exists and that the target
  dependencies exist.
- Check for missing dependencies.
- Check for circular dependencies.

Eclim also combines the above validation with :ref:`xml validation
<xml-validation>` to validate that the ant file is well formed.

If you do not want your ant files validated automatically when saved, you can
set the :ref:`g:EclimAntValidate` variable described in the configuration
section below.

.. _\:Validate_ant:

Whether or not auto validation has been enabled, eclim also exposes the command
**:Validate** to manually execute the validation of the ant file.

.. _\:AntDoc:

Documentation Lookup
--------------------

When editing an ant build file eclim defines a command named **:AntDoc** which
will attempt to lookup and open in your
:ref:`configured browser <g:EclimBrowser>` the documentation for the element
under the cursor or, if supplied, the element passed to it.

This command will only lookup element names, not attribute names or values.

By default this plugin is configured to find all the standard ant tasks, types,
etc, as well as those defined by the antcontrib_ project.

.. _\:AntUserDoc:

If you have other tasks that you wish to add to this plugin, you can do so by
defining the global variable **g:EclimAntUserDocs**. The value of this variable
is expected to be a map of element names to the url where the documentation for
that element can be found.  The url also supports a substitution variable,
<element> which will be substituted with the lower case version of the element
name.

The following is an example which adds the tasks from the apache cactus project.

.. code-block:: vim

  let s:cactus =
    \ 'http://jakarta.apache.org/cactus/integration/ant/task_<element>.html'
  let g:EclimAntUserDocs = {
      \  'cactifywar'     : s:cactus,
      \  'cactifyear'     : s:cactus,
      \  'cactus'         : s:cactus,
      \  'runservertests' : s:cactus,
      \  'webxmlmerge'    : s:cactus,
    \ }

Configuration
-------------

:doc:`Vim Settings </vim/settings>`

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

.. _g\:EclimAntErrorsEnabled:

- **g:EclimAntErrorsEnabled** (Default: 0) -
  When non-zero, build file error messages will be added to vim's quickfix if
  encountered during **:Ant** invocations.  Disabled by default because it's
  difficult to distinguish between actual issues with the build file (invalid
  property, task, etc.) and build failure messages which occur under normal
  usage (junit task failed due to test failure, javac failures due to compile
  error, etc.) leading to false positives.

.. _g\:EclimAntValidate:

- **g:EclimAntValidate** (Default: 1) -
  If set to 0, disables ant xml validation when saving the file.

Suggested Mappings
------------------

Here are some mappings for the ant funtionality provided by eclim.  To make use
of these mappings, simply create a ftplugin file for ant and place your mappings
there (:help ftplugin-name).

- Lookup and open the documentation for the ant element under the cursor with
  <enter>.

  .. code-block:: vim

    noremap <silent> <buffer> <cr> :AntDoc<cr>

.. _antcontrib: http://ant-contrib.sourceforge.net
