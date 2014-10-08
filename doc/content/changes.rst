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

.. _2.2.5:
.. _1.7.13:

2.2.5 / 1.7.13 (Nov. 25, 2012)
------------------------------

Eclim:
  - 1.7.13 and above will now support Eclipse 3.8 as well as 3.7.
  - Fix :ref:`:ProjectGrep <:ProjectGrep>` and :ref:`:ProjectTodo
    <:ProjectTodo>` to search in all of the project's links as well.
  - Other minor bug fixes.

| :gitlog:`Git Log (2.2.5) <2.2.4...2.2.5>`
| :gitlog:`Git Log (1.7.13) <1.7.12...1.7.13>`

.. _2.2.4:
.. _1.7.12:

2.2.4 / 1.7.12 (Nov. 18, 2012)
------------------------------

Eclim:
  - Updated :ref:`:ProjectTree <:ProjectTree>` and :ref:`:ProjectTab
    <:ProjectTab>` to support an arbitrary directory as an argument, allowing
    you to use the command for project's not managed by eclipse/eclim. :TreeTab
    has been removed since the update to :ProjectTab makes it redundant.
  - Creation of projects in nested directories in the eclipse workspace (vs at
    the root of the workspace) is now properly supported through eclim.

Android:
  - Updated for ADT 21.0.0.

C/C++:
  - Fix placement of some error markers.

Php:
  - Some indentation fixes.

| :gitlog:`Git Log (2.2.4) <2.2.3...2.2.4>`
| :gitlog:`Git Log (1.7.12) <1.7.11...1.7.12>`

.. _2.2.3:
.. _1.7.11:

2.2.3 / 1.7.11 (Oct. 19, 2012)
------------------------------

Eclim:
  - Fixes execution of eclim commands from vim on Windows when using the
    external nailgun client (vs the python client).

| :gitlog:`Git Log (2.2.3) <2.2.2...2.2.3>`
| :gitlog:`Git Log (1.7.11) <1.7.10...1.7.11>`

.. _2.2.2:
.. _1.7.10:

2.2.2 / 1.7.10 (Oct. 07, 2012)
------------------------------

Eclimd:
  - Updated eclimd script for Linux/OSX to supply reasonable defaults for heap
    and perm gen space if not already set by ~/.eclimrc.

C/C++:
  - Fixed C++ project creation to auto add the required C nature.
  - Fixed C/C++ issues introduced by the eclipse 4.2.1 release (project
    create/refresh and call hierarchy).

Java:
  - :JavaImportSort, :JavaImportClean, and :JavaImportMissing all removed in
    favor of a new command which performs the functionality of all three:
    :ref:`:JavaImportOrganize <:JavaImportOrganize>`
  - The vim option g:EclimJavaImportExclude has been replaced with the eclim
    setting :ref:`org.eclim.java.import.exclude <org.eclim.java.import.exclude>`.
  - The vim option g:EclimJavaImportPackageSeparationLevel has been replaced
    with the eclim setting :ref:`org.eclim.java.import.package_separation_level
    <org.eclim.java.import.package_separation_level>`.
  - g:EclimJavaBeanInsertIndexed vim variable removed in favor of suffixing
    :JavaGetSet methods with '!'.
  - :JavaCorrect, :JavaImpl, :JavaDelegate, :JavaConstructor, and :JUnitImpl
    all now perform their code manipulations using eclipse operations.
  - Initial support added for running :JavaSearch commands from source file
    results (library source files) not in a project.
  - g:EclimJavaCheckstyleOnSave replaced with the eclim setting
    :ref:`org.eclim.java.checkstyle.onvalidate
    <org.eclim.java.checkstyle.onvalidate>`.
  - g:EclimJavaSrcValidate renamed to g:EclimJavaValidate.
  - :JUnitExecute replaced with a new and improved :ref:`:JUnit <:JUnit>`
    command.
  - Added the command :ref:`:JUnitFindTest <:JUnitFindTest>` to open the
    corresponding test for the current file.
  - Removed :Javac command since eclipse's continuous incremental builds
    typically make the :Javac call a no op, and in cases where you need to
    induce compilation, :ref:`:ProjectBuild <:ProjectBuild>` does so in a
    language agnostic way.
  - Added :ref:`:JavaMove <:JavaMove>` command to move a java source file from
    one package to another.
  - Added :ref:`:JavaDocPreview <:JavaDocPreview>` to display the javadoc of
    the element under the cursor in vim's preview window.

| :gitlog:`Git Log (2.2.2) <2.2.1...2.2.2>`
| :gitlog:`Git Log (1.7.10) <1.7.9...1.7.10>`

.. _1.7.9:

1.7.9 (Sep. 09, 2012)
---------------------

Scala:
  - Add support for scala :doc:`code completion </vim/scala/complete>`,
    :doc:`code validation </vim/scala/validate>`, and :doc:`element definition
    searches </vim/scala/search>`.

:gitlog:`Git Log (1.7.9) <1.7.8...1.7.9>`

.. _2.2.1:
.. _1.7.8:

2.2.1 / 1.7.8 (Sep. 01, 2012)
-----------------------------

Documentation:
  - Redesigned the eclim website using the
    `sphinx bootstrap theme <https://github.com/ervandew/sphinx-bootstrap-theme>`_.
  - Reorganized many of the docs to consolidate similar features to hopefully
    make them easier to find and make the docs less sprawling.
  - Improved the translation of the docs to vim help files.

Android:
  - Eclim now has support for :ref:`creating android projects
    <gettingstarted-android>`.

Java:
  - Fixed searching for JDK classes on OSX.
  - Added support for searching for inner classes and their methods.
  - Fixed remaining tab vs spaces indenting related issues with code added via
    eclipse.

Vimplugin:
  - Fixed disabling of conflicting Eclipse keybindings on Juno while the
    embedded vim has focus (fixes usage of Ctrl+V for blockwise visual
    selections).

| :gitlog:`Git Log (2.2.1) <2.2.0...2.2.1>`
| :gitlog:`Git Log (1.7.8) <1.7.7...1.7.8>`

.. _2.2.0:
.. _1.7.7:

2.2.0 / 1.7.7 (Aug. 07, 2012)
-----------------------------

Eclipse:
  - Eclim 2.2.0 and above now requires Java 6 or later.
  - Eclim 2.2.0 and above now requires the latest version of eclipse (Juno,
    4.2).

Eclimd:
  - Updated eclimd script to always set the jvm architecture argument,
    preventing possible issue starting eclimd on OSX if the default
    architecture order of the java executable doesn't match the eclipse
    architecture.

C/C++:
  - Semantic errors are now included in the validation results.
  - Added folding support to C/C++ call hierarchy buffer.
  - :ref:`:ProjectRefresh <:ProjectRefresh>` now waits on the C/C++ indexer to
    finish before returning focus to the user.
  - Fixed auto selecting of the tool chain when creating C/C++ projects from
    eclim.
  - Fixed :ref:`:CCallHierarchy <:CCallHierarchy>` from possibly using a cached
    version of the file resulting in incorrect or no results.

Java:
  - Fixed inserted code from :ref:`:JavaCorrect <:JavaCorrect>` when file
    format is 'dos'.
  - Fixed off by one issue prevent several code correction suggestions from
    being suggested.

Ruby:
  - Fixed to prompt for the path to the ruby interpreter if necessary when
    importing a ruby project or adding the ruby nature to an existing project.

Vimplugin:
  - Fixed executing of some operations when vim is currently in insert mode
    (opening new file from eclipse in a new external vim tab, using "Save As"
    from eclipse, and jumping to a line number from the project tree etc.)

| :gitlog:`Git Log (2.2.0) <1.7.6...2.2.0>`
| :gitlog:`Git Log (1.7.7) <1.7.6...1.7.7>`

.. _1.7.6:

1.7.6 (Jun. 07, 2012)
----------------------

Bug Fixes:
  - Couple other minor bug fixes.

Installer:
  - Fixed install location of eclim's vim help files (broken in the previous
    release).

:gitlog:`Git Log (1.7.6) <1.7.5...1.7.6>`

.. _1.7.5:

1.7.5 (Jun. 03, 2012)
----------------------

.. note::

  This release is not compatible with Eclipse Juno (4.2). The next major
  release of eclim (2.2.0) will be built for Juno.

Installer:
  - Added :ref:`uninstall <uninstall>` support to the eclim installer.
  - Updated the installer to fully embrace eclipse's provisioning framework
    (p2).

Common:
  - Added :ref:`:ProjectTreeToggle <:ProjectTreeToggle>`.

Vimplugin
  - Fixed key binding conflict handling to not inadvertently switch your key
    binding scheme back to the default scheme.

Java:
  - Added support for importing the necessary type during code completion.
  - Improved location of a project's main class for the :ref:`:Java <:Java>`
    command, when not explicitly set.

:gitlog:`Git Log (1.7.5) <1.7.4...1.7.5>`

.. _1.7.4:

1.7.4 (Apr. 22, 2012)
----------------------

Bug Fixes:
  - Fixed possible NPE saving eclim settings.
  - Several other small bug fixes.

C/C++:
  - Fixed code completion by disabling the use of temp files.

Java:
  - Fixed :Java on windows as well as handling of stdin for ant 1.8.2+.

:gitlog:`Git Log (1.7.4) <1.7.3...1.7.4>`

.. _1.7.3:

1.7.3 (Mar. 18, 2012)
----------------------

Bug Fixes:
  - Lots of various bug fixes.

Common:
  - Added :ref:`:ProjectBuild <:ProjectBuild>` to build the current or
    supplied project.
  - Updated :ref:`:ProjectProblems <:ProjectProblems>` to support optional bang
    (`:ProjectProblems!`) to only show project errors.
  - Updating eclipse's :doc:`local history </vim/core/history>` when writing
    in vim is now only enabled by default when gvim is opened from the eclipse
    gui.

C/C++:
  - Fixed project creation issue encountered on some systems.

Java:
  - Added project settings for specifying default jvm args for
    :ref:`:Java <:Java>` and default args for :Javac.
  - Code inserted by
    :ref:`:JavaConstructor <:JavaConstructor>`,
    :ref:`:JavaGetSet <:JavaGetSet>`,
    :ref:`:JavaImpl <:JavaImpl>`,
    :ref:`:JavaDelegate <:JavaDelegate>`, and
    :ref:`:JUnitImpl <:JUnitImpl>`
    is now formatted according to the eclipse code formatter settings
    configured from the eclipse gui.

Maven:
  - Now when saving your pom.xml file your .classpath will be
    :ref:`auto updated <classpath-maven-pom>` with the dependencies found in
    your pom.xml.

Php:
  - Now handles completion from within php short tags.

:gitlog:`Git Log (1.7.3) <1.7.2...1.7.3>`

:doc:`/archive/changes`
