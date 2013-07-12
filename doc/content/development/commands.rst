.. Copyright (C) 2013  Eric Van Dewoestine

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

Commands
========

For each eclipse feature that is exposed in eclim, there is a corresponding
command on the daemon which handles calling the appropriate eclipse APIs and
returning a result back to the client. This page will walk you through creating
a simple command to familiarize you with the process.

Creating a Command
------------------

Commands are simple classes which extend ``AbstractCommand`` and are registered
using the ``@Command`` annotation. They then define an ``execute`` method which
can return any object that can be serialized appropriately using `gson`_.

Here is an example of a trivial command which returns a map of the arguments it
was supplied, with the supplied project and file paths converted to absolute
paths and the file byte offset converted to a character offset (eclim's vim
function ``eclim#util#GetOffset()`` returns the offset in bytes since getting a
character offset in vim with multi byte characters is less reliable, but most
eclipse APIs expect character offsets):

.. note::

  Eclim's source code is grouped by bundles (org.eclim, org.eclim.core, etc),
  each of which has ``java`` directory containing the java source code for that
  bundle.

.. code-block:: java

  package org.eclim.plugin.core.command.sample;

  import java.util.HashMap;

  import org.eclim.annotation.Command;

  import org.eclim.command.CommandLine;
  import org.eclim.command.Options;

  import org.eclim.plugin.core.command.AbstractCommand;

  import org.eclim.plugin.core.util.ProjectUtils;

  import org.eclipse.core.resources.IProject;

  @Command(
    name = "echo",
    options =
      "REQUIRED p project ARG," +
      "REQUIRED f file ARG," +
      "REQUIRED o offset ARG," +
      "OPTIONAL e encoding ARG"
  )
  public class EchoCommand
    extends AbstractCommand
  {
    @Override
    public Object execute(CommandLine commandLine)
      throws Exception
    {
      String projectName = commandLine.getValue(Options.PROJECT_OPTION);
      String file = commandLine.getValue(Options.FILE_OPTION);

      IProject project = ProjectUtils.getProject(projectName);

      // translates client supplied byte offset to a character offset using the
      // 'project', 'file', 'offset', and 'encoding' command line args.
      int offset = getOffset(commandLine);

      HashMap<String,Object> result = new HashMap<String,Object>();
      result.put("project", ProjectUtils.getPath(project));
      result.put("file", ProjectUtils.getFilePath(project, file));
      result.put("offset", offset);
      if (commandLine.hasOption(Options.ENCODING_OPTION)){
        result.put("encoding", commandLine.getValue(Options.ENCODING_OPTION));
      }

      return result;
    }
  }

When registering the command with the ``@Command`` annotation, you give it a
name and a comma separated list of options. Each option consists of 4 parts in
the form of:

::

  REQUIRED|OPTIONAL s longname ARG|NOARG|ANY

Where each part is defined as:

1. ``REQUIRED`` or ``OPTIONAL``
2. a single letter short name for the option
3. a long name for the option
4. whether the option requires an argument, no argument, or can have any number
   of additional arguments. In the case of ``ANY``, you should only have one
   option with that value and when running the command from the command line,
   that option should be supplied last.

That should give you the basics on what's involved with creating a new command,
but the biggest hurdle for creating most commands is locating and deciphering
the eclipse API calls that are necessary to implement the feature you want.
Unfortunately most of the eclipse code that you'll need to hook into will most
likely have little to no documentation so you're going to have to dig through
the eclipse code. Eclim does provide a couple ant tasks to at least help you to
quickly extract any docs or source code found in your eclipse install:

.. begin-eclipse-doc-src

- **eclipse.doc:** This target will extract any doc jars from your eclipse
  install to a 'doc' directory in your eclipse home (or user local eclipse
  home).

- **eclipse.src:** This target will extract any src jars from your eclipse
  install to a 'src' directory in your eclipse home (or user local eclipse
  home). If you download the sdk version of eclipse then the jdt and all the
  core eclipse source will be available. Some other plugins provide sdk versions
  which include the source code and this target can extract those as well, but
  some plugins don't seem to have this option when installing via eclipse's
  update manager (and may not include the source when installed from a system
  package manager). For those you can often download a zip version of their
  update site which should include source bundles. Once you've extracted that
  file, you can tell this target to extract source bundles from a specified
  directory. Here is an example of extracting the source from an unpacked dltk
  update site:

  ::

    $ ant -Dsrc.dir=/home/ervandew/downloads/dltk-core-5.0.0/plugins eclipse.src

.. end-eclipse-doc-src

Running a Command
------------------

Once you've created your command you then need to compile the code using eclim's
ant build file. After you've done that you can then start eclimd and execute
your command from the command line to test it:

::

  $ eclim -pretty -command echo -p eclim -f org.eclim.core/plugin.properties -o 42 -e utf-8

.. note::

  As you are developing your commands, you can avoid restarting eclimd after
  every change by using eclim's ``reload`` command which will reload all of
  eclim's plugin bundles with the exception of org.eclim.core (so unfortunately
  it won't help with our example above if we put that command in the
  org.eclim.core bundle):

  ::

    $ eclim -command reload

Adding to Vim
-------------

Continuing with our ``echo`` command example, we can add the command to vim by
first defining a new vim command in
``org.eclim.core/vim/eclim/plugin/eclim.vim``:

.. note::

  If the command should only be available for a specific file type, then you'd
  put it in a ``vim/eclim/ftplugin/somefiltetype.vim`` file instead.

.. code-block:: vim

  command EclimEcho :call eclim#echo#Echo()

Now that we've created the command, we then need to define our
``eclim#echo#Echo()`` function accordingly in
``org.eclim.core/vim/eclim/autoload/eclim/echo.vim``:

.. code-block:: vim

  " Script Variables {{{
    let s:echo_command =
      \ '-command echo -p "<project>" -f "<file>" ' .
      \ '-o <offset> -e <encoding>'
  " }}}

  function! eclim#echo#Echo() " {{{
    if !eclim#project#util#IsCurrentFileInProject(0)
      return
    endif

    let project = eclim#project#util#GetCurrentProjectName()
    let file = eclim#project#util#GetProjectRelativeFilePath()

    let command = s:echo_command
    let command = substitute(command, '<project>', project, '')
    let command = substitute(command, '<file>', file, '')
    let command = substitute(command, '<offset>', eclim#util#GetOffset(), '')
    let command = substitute(command, '<encoding>', eclim#util#GetEncoding(), '')

    let response = eclim#Execute(command)

    " if we didn't get back a dict as expected, then there was probably a
    " failure in the command, which eclim#Execute will handle alerting the user
    " to.
    if type(response) != g:DICT_TYPE
      return
    endif

    " simply print the response for the user.
    call eclim#util#Echo(string(response))
  endfunction " }}}

And that's all there is to it. After re-building eclim, restarting eclimd, and
restarting vim, you can now execute the command ``:EclimEcho`` to see the
response printed in vim.

Now that you know the basics, you can explore the many existing eclim commands
found in the eclim source code to see detailed examples of how to access various
eclipse features to expose them for use in vim or the editor of your choice.

You should also take a look at the eclim :doc:`/development/plugins`
documentation which documents how to create a new eclim plugin, including
information on adding new eclim settings, managing the plugin's dependencies
through its ``META-INF/MANIFEST.MF``, etc.

.. _gson: http://code.google.com/p/google-gson/
