.. Copyright (C) 2005 - 2015  Eric Van Dewoestine

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
  - Added support for :ref:`automated imports <:ScalaImport>`.

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
  - :ref:`:XmlFormat <:XmlFormat>` now uses the DOM3 APIs to improve the
    formatted result, honoring your ``textwidth`` and indent settings.

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
    callee hierarchy of a method. Thanks to Alexandre Fonseca.

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
  - :ref:`:CCallHierarchy <:CCallHierarchy>` now support showing callee tree.

Java:
  - :ref:`:JavaImpl <:JavaImpl>` now supports anonymous inner classes and will
    also properly handle suggesting methods from a nested superclass.

Php:
  - Eclim will no longer run php files through html validation by default (see
    the :doc:`Php Validation </vim/php/validate>` doc for settings to enable
    html validation).

Scala:
  - Scala support updated for Scala IDE 3.0.0.
  - Scala now supported on both versions of eclim (Juno and Indigo).

| :gitlog:`Git Log (2.2.6) <2.2.5...2.2.6>`
| :gitlog:`Git Log (1.7.14) <1.7.13...1.7.14>`

:doc:`/archive/changes`
