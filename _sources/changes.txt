.. Copyright (C) 2005 - 2021  Eric Van Dewoestine

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

History of Changes
==================

.. _2.18.0:

2.18.0 (Jan. 09, 2021)
----------------------

Eclim:
  - Eclim updated for Eclipse 4.18 (2020-12), so java 11 or newer is also
    required.

Removals:
  .. note::

    Since eclim's inception 15 years ago, some better alternatives have been
    created, especially in the last 2-5 years. The creation of the `Language
    Server Protocol`_ in particular provides the means for IDEs, etc to expose
    their functionality to editors like vim. So instead of trying to solely
    maintain all of eclim's features, I've decided to strip out much of the
    stuff I don't use and refer people to actively maintained projects that
    hopefully have the time and resources to keep their features current and
    expand on them. So below I've listed several plugins/features that have been
    removed from eclim.

  - Android: google stopped supporting eclipse years ago.
  - C/C++
  - Php
  - Ruby
  - Groovy
  - Scala: Metals_ looks like a good alternative that supports LSP + Vim.
  - Vimplugin: this was never part of eclim's goal, just seemed like something
    that was easy enough to include at the time.

.. _Language Server Protocol: https://langserver.org/
.. _Metals: https://scalameta.org/metals/

.. _2.8.0:

2.8.0 (Jul. 05, 2018)
---------------------

Eclim:
  - Eclim updated to supports, and requires, Eclipse 4.8 (Photon)
  - Fix eclimd script for jdk 10
  - Update the installer to be compatible with python 2.6
  - Fix the installer on OSX when libedit is not installed.

Java:
  - Added a :ref:`:JavaOutline <:JavaOutline>` command which opens a buffer with
    an outline of the current class.
    |br| Thanks to g0dj4ck4l
  - Updated checkstyle support to 8.11

Python:
  - Updated for pydev 6.4.3

Groovy:
  - Groovy support has been omitted from this release since the groovy eclipse
    feature does not yet support Eclipse 4.8 (Photon).

| :gitlog:`Git Log (2.8.0) <2.7.2...2.8.0>`

.. _2.7.2:

2.7.2 (Feb. 11, 2018)
---------------------

Eclim:
  - Installer fixed to handle spaces in the eclipse path.
  - Eclimd fixed to run under java 9.

| :gitlog:`Git Log (2.7.2) <2.7.1...2.7.2>`

.. _2.7.1:

2.7.1 (Jan. 01, 2018)
---------------------

Eclim:
  - Eclim updated to support Eclipse 4.7.2 (Oxygen)
  - Eclim updated to latest versions of dltk, pydev, and scalaide.
  - All new installer that supports several different eclipse file system
    layouts, including the one from the eclipse GUI installer.
  - Windows support has been removed.

| :gitlog:`Git Log (2.7.1) <2.7.0...2.7.1>`

.. _2.7.0:

2.7.0 (Jul. 12, 2017)
---------------------

Eclim:
  - Eclim now supports, and requires, Eclipse 4.7 (Oxygen)

| :gitlog:`Git Log (2.7.0) <2.6.0...2.7.0>`

.. _2.6.0:

2.6.0 (Jul. 21, 2016)
---------------------

Eclim:
  - Eclim now supports, and requires, Eclipse 4.6 (Neon)

| :gitlog:`Git Log (2.6.0) <2.5.0...2.6.0>`

.. _2.5.0:

2.5.0 (Jul. 25, 2015)
---------------------

Eclim:
  - Eclim now supports, and requires, Eclipse 4.5 (Mars)

| :gitlog:`Git Log (2.5.0) <2.4.1...2.5.0>`

.. _2.4.1:

2.4.1 (Jan. 22, 2015)
---------------------

Install:
  - Fixed unattended installation.

Eclim:
  - Updated the search behavior across all eclim's supported languages to now
    use vim's quickfix list instead of the location list, and to only do so if
    there is more than one result.
  - Updated eclim's sign support for quickfix results to now be disabled by
    default.
  - Updated eclim's signs to use more unique ids to support multiple different
    sign types on a single line and to avoid clashing with other plugins that
    may be setting signs on the same lines as eclim.

Project:
  - Added a :ProjectRun command to invoke your project's eclipse run
    configuration.
    |br| Thanks to Daniel Leong
  - Added :ref:`:ProjectImportDiscover <:ProjectImportDiscover>` to bulk import
    projects from a common parent directory.
    |br| Thanks to Kannan Rajah

Java:
  - Added support for java debugging.
    |br| Thanks to Kannan Rajah
  - Added a :ref:`:JavaNew <:JavaNew>` command to create new classes,
    interfaces, etc in your project.
    |br| Thanks to Daniel Leong
  - Added ability to configure :JavaImpl to :ref:`insert methods at the current
    cursor position <g:EclimJavaImplInsertAtCursor>`.
    |br| Thanks to Daniel Leong
  - Updated :JavaSearch to support :ref:`sorting <org.eclim.java.search.sort>`
    the results by relative path.
    |br| Thanks to Kannan Rajah

Groovy:
  - Added support for groovy validation and code completion.
    |br| Thanks to Yves Zoundi

Python:
  - Fixed eclim's python support to work against the pydev 3.9.1

Scala:
  - Scala support has been re-enabled now that ScalaIDE 4.0.0 for Luna has been
    released.

| :gitlog:`Git Log (2.4.1) <2.4.0...2.4.1>`

.. _2.4.0:

2.4.0 (Aug. 24, 2014)
---------------------

Eclim:
  - Eclim now supports, and requires, Eclipse Luna
  - **:EclimSettings** renamed to **:WorkspaceSettings** to better reflect what
    is being edited.
  - Added :ref:`:VimSettings <:VimSettings>` command to make viewing/editing of
    eclim's vim client settings easier.
  - All eclim ``make`` based commands (:Ant, :Mvn, etc.) will now use dispatch_
    if available. If you would like to disable this behavior, add the following
    to your vimrc:

    .. code-block:: vim

      let g:EclimMakeDispatchEnabled = 0

  - Lot of bugs have also been fixed.

Scala:
  - Scala support is disabled in this release. I tried waiting for a final
    release of ScalaIDE 4.0.0 for Luna, but I don't want to hold up the rest of
    eclim any longer. If you want to use eclim's scala support, you'll need to
    install the ScalaIDE 4.0.0 milestone and build eclim from the master git
    branch.

| :gitlog:`Git Log (2.4.0) <2.3.4...2.4.0>`

.. _dispatch: https://github.com/tpope/vim-dispatch

.. _1.7.19:

1.7.19 (May 07, 2014)
---------------------

Indigo release which includes most of the changes from 2.3.3 and 2.3.4,
excluding the new pydev support. This will most likely be the final eclim
release for indigo.

| :gitlog:`Git Log (1.7.19) <1.7.18...1.7.19>`

.. _2.3.4:

2.3.4 (Apr. 12, 2014)
---------------------

Project:
  - Fixed the ``projects`` command result on Windows.

| :gitlog:`Git Log (2.3.4) <2.3.3...2.3.4>`

.. _2.3.3:

2.3.3 (Apr. 06, 2014)
---------------------

Installer:
  - Fixed running of the installer with Java 8.

Eclimd:
  - Starting eclimd in the background is now done using the ``-b`` flag instead
    of passing a ``start`` argument.
  - Eclimd debug logging can now be enabled at startup using the ``--debug``
    flag.

Ant:
  - Fixed ant target completion for newer ant versions (those that require java
    5).

C/C++:
  - Fixed adding of includes etc. in the C++ sections of **:CProjectConfig**.
  - Fixed searching to include macro results.
  - TODO/FIXME markers are now ignored by validation.

Html:
  - Fixed indentation after unclosed ``<br>`` and ``<input>`` tags.

Java:
  - Fixed possible infinite loop when adding imports using **:JavaImport**.
  - Fixed an edge case which caused an additional empty line to be added between
    imports that should be grouped together when using **:JavaImport**.
  - Fixed **:Java** command if the main class has no package declaration.
  - Fixed issue with large portions of code being re-formatted when applying a
    correction suggestion.
  - TODO/FIXME markers are now ignored by validation.
  - Some of the eclipse java code completion settings are now available via
    **:ProjectSettings**.

Javascript:
  - Let tern supersede eclim's limited javascript completion.

Maven/Ivy:
  - Removed dependency searching since the search provider no longer exists.

Python:
  - Eclim's python support been re-written to use pydev instead of rope.

    .. note::

      Any exiting eclim python projects you have should be re-created with the
      new ``python`` nature:

      ::

        :ProjectCreate /path/to/project -n python

Scala:
  - Disabled use of temp files which could cause some validation errors.
  - Added support for automated imports.

Xml:
  - Fixed validation of xml files with no dtd/schema to not raise errors on
    missing dtd for non-english users.

| :gitlog:`Git Log (2.3.3) <2.3.2...2.3.3>`

.. _2.3.2:
.. _1.7.18:

2.3.2 / 1.7.18 (Sep. 12, 2013)
------------------------------

Installer:
  - Fixed extraction of scala vim files when installing scala support.

Php:
  - Fixed completion of php namespaces.

| :gitlog:`Git Log (2.3.2) <2.3.1...2.3.2>`
| :gitlog:`Git Log (1.7.18) <1.7.17...1.7.18>`

.. _2.3.1:

2.3.1 (Jul. 27, 2013)
---------------------

Installer:
  - Fixed dependencies to point at the Kepler update site.

| :gitlog:`Git Log (2.3.1) <2.3.0...2.3.1>`

.. _2.3.0:
.. _1.7.17:

2.3.0 / 1.7.17 (Jul. 21, 2013)
------------------------------

Java:
  - :ref:`:NewJarEntry <:NewJarEntry_java>` now accepts up to 3 arguments
    allowing you to create the jar entry with the path to the jar, path to the
    source, and the path to the javadocs.
  - On javadoc search, all results, including results found in jar files, will
    be fully translated to usable urls to be opened by vim instead of sending
    jar results back to eclipse to let it open them. With this change your
    chosen brower on the vim side will always be used.
  - Fix for import grouping by package on Kepler.

Php:
  .. warning::

    **Reminder:** The format of the h2 database used for php searching and code
    completion has changed in **Kepler** which may result in searching / code
    completion not returning any results, both in eclim and in the standard
    eclipse php editor.  To fix this you'll need to stop eclipse, remove the old
    index, and then restart:

    ::

      $ rm -r <your workspace>/.metadata/.plugins/org.eclipse.dltk.core.index.sql.h2/

Scala:
  - Updated to require 3.0.1 of the Scala IDE which now supports Kepler (for
    eclim 2.3.0 only). Since this version of the Scala IDE seems to only support
    Scala 2.10.x now, the option to install 2.9.x version has been removed,
    however the indigo release of eclim (1.7.17) still supports the Scala 2.9.x
    version of Scala IDE 3.0.0.

Xml:
  - XmlFormat now uses the DOM3 APIs to improve the formatted result, honoring
    your ``textwidth`` and indent settings.

| :gitlog:`Git Log (2.3.0) <2.2.7...2.3.0>`
| :gitlog:`Git Log (1.7.17) <1.7.16...1.7.17>`

.. _2.2.7:
.. _1.7.16:

2.2.7 / 1.7.16 (Jul. 14, 2013)
------------------------------

Java:
  - Fix for some completions on Kepler.
  - Fix for rare case where an invalid classpath entry could result in the
    .classpath file reverted to the eclipse default.
  - :ref:`:JavaCallHierarchy <:JavaCallHierarchy>` added to show the caller or
    callee hierarchy of a method.
    |br| Thanks to Alexandre Fonseca

Php:
  .. warning::

    The format of the h2 database used for php searching and code completion has
    changed in **Kepler** which may result in searching / code completion not
    returning any results, both in eclim and in the standard eclipse php editor.
    To fix this you'll need to stop eclipse, remove the old index, and then
    restart:

    ::

      $ rm -r <your workspace>/.metadata/.plugins/org.eclipse.dltk.core.index.sql.h2/

Ruby:
  - Fixed the inserted completion text for some ruby libraries.

Scala:
  - The graphical installer now includes a compiled eclim sdt bundle for both
    scala 2.9.x and 2.10.x for which one will be chosen for you if you already
    have the scala-ide installed, otherwise you can pick one and the appropriate
    version of the scala-ide will be installed for you.

Vimplugin:
  - The option to force focusing of gvim with a simulated click is now disabled
    by default, but when enabled, it should be less likely to have unintended
    side affects.

Cygwin:
  - Performance optimization for user's with many projects.

Installer:
  - The installer will now properly shutdown eclimd instances registered using
    the old non-json format.

Docs:
  - Expanded the :doc:`developer docs </development/index>` to include docs on
    :doc:`creating new commands </development/commands>` along with some of the
    basics for :doc:`adding new eclim plugins </development/plugins>`.

| :gitlog:`Git Log (2.2.7) <2.2.6...2.2.7>`
| :gitlog:`Git Log (1.7.16) <1.7.14...1.7.16>`

.. _2.2.6:
.. _1.7.14:

2.2.6 / 1.7.14 (May 18, 2013)
------------------------------

Bug Fixes:
  - Fixed eclimd to prevent incorrect addition of -d32 on 64bit systems, which
    prevents eclimd from starting.
  - Fix potential conflicts between syntastic validation vs eclim validation
    (syntastic validation will be diabled if eclim validation is available for
    the current file type).
  - Many more fixes.

Android:
  - Updated for ADT 22.0.0

C/C++:
  - :CCallHierarchy now support showing callee tree.

Java:
  - :ref:`:JavaImpl <:JavaImpl>` now supports anonymous inner classes and will
    also properly handle suggesting methods from a nested superclass.

Php:
  - Eclim will no longer run php files through html validation by default (see
    the Php Validation doc for settings to enable html validation).

Scala:
  - Scala support updated for Scala IDE 3.0.0.
  - Scala now supported on both versions of eclim (Juno and Indigo).

| :gitlog:`Git Log (2.2.6) <2.2.5...2.2.6>`
| :gitlog:`Git Log (1.7.14) <1.7.13...1.7.14>`

:doc:`/archive/changes`

.. |br| raw:: html

  <br/>
