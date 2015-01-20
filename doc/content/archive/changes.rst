:orphan:

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

Eclim Changes Archive
=====================

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

.. _1.7.2:

1.7.2 (Sep. 10, 2011)
----------------------

Bug Fixes:
  - Various small bug fixes.

Installer:
  - Fixed deadlock on the vim install dir pane for java 7.

Eclipse:
  - Disabled listening for change events on embedded gvim. Should fix most
    causes of gvim crashing.
  - Other improvements for embedded gvim support.

Eclimd:
  - Improved OSX detection.
  - Fix for passing jvm args to java when supplied as args to eclimd script.

Java:
  - Fix passing of dash prefixed :Java args (ex. -p) to the class to be run.

Php:
  - Improved completion of magic properties.
  - Support new php 5.3 version in pdt.

:gitlog:`Git Log (1.7.2) <1.7.1...1.7.2>`

.. _1.7.1:

1.7.1 (Jul. 02, 2011)
----------------------

Bug Fixes:
  - Fixed `org.eclipse.swt.SWTError: Not implemented [multiple displays]` error
    when starting the eclipse gui after running eclimd.

Eclipse:
  - Enable vim embedding on Solaris, AIX, and HP-UX versions of eclipse.

:gitlog:`Git Log (1.7.1) <1.7.0...1.7.1>`

.. _1.7.0:

1.7.0 (Jun. 26, 2011)
----------------------

Bug Fixes:
  - Bug fixes for eclipse 3.7 (Indigo) compatability.
  - Other bug fixes.

Eclipse:
  - Eclim now requires the latest version of eclipse (Indigo, 3.7).

:gitlog:`Git Log (1.7.0) <1.6.3...1.7.0>`

.. _1.6.3:

1.6.3 (Apr. 16, 2011)
----------------------

Bug Fixes:
  - Fixed bug where one or more closed projects would prevent working with open
    projects.
  - Other small bug fixes.

Installer:
  - Prevent possible OutOfMemoryError when invoking eclipse to install plugin
    dependencies by setting a larger heap space.

Java:
  - Added :ref:`:JavaClasspath <:JavaClasspath>` to echo the project's current
    classpath.

:gitlog:`Git Log (1.6.3) <1.6.2...1.6.3>`

.. _1.6.2:

1.6.2 (Feb. 26, 2011)
----------------------

Bug Fixes:
  - Fixed to use %USERPROFILE% on windows to retrieve the user home (fixes
    several possible issues including "Unable to determine your eclipse
    workspace").
  - Various other bug fixes.

Common:
  - Added rudimentary auto generated menu items for gvim (can be disabled via
    :ref:`g:EclimMenus <g:EclimMenus>`).
  - Added support for displaying :ref:`project info <:ProjectInfo>` vim's
    status line
    (contributed by `Brendan W. McAdams <http://github.com/bwmcadams>`_).

C/C++
  - Updated context search to greatly improve performance in some cases.

Python:
  - Updated all embedded python logic to be compatible with python 2.6 and higher.

    .. note::

      Support for vim embedded python 2.5 or less no longer supported.

Other:
  - All relative commands (:SplitRelative, :EditRelative, etc), along with
    :Split and :Tabnew broken out from eclim to
    http://github.com/ervandew/relative.
  - Archive viewing support broken out from eclim to
    http://github.com/ervandew/archive.
  - Maximize/Minimize vim window support broken out from eclim to
    http://github.com/ervandew/maximize.
  - Sgml (html, xml, etc.) end tag completion broken out from eclim to
    http://github.com/ervandew/sgmlendtag.
  - Vcs support broken out from eclim to http://github.com/ervandew/vcs.
  - Taglist support broken out from eclim to
    http://github.com/ervandew/taglisttoo.
  - Regex testing support (:JavaRegex, :PythonRegex) broken out from eclim to
    http://github.com/ervandew/regex.
  - Vim script help lookup along with user defined variable/command/function
    definition/references lookup support broken out from eclim to
    http://github.com/ervandew/lookup.

:gitlog:`Git Log (1.6.2) <1.6.1...1.6.2>`

.. _1.6.1:

1.6.1 (Oct. 23, 2010)
----------------------

Bug Fixes:
  - Fix for :ref:`:ProjectProblems <:ProjectProblems>` command when any filters
    have been set from the eclipse gui.
  - Merged in JRuby's improvements to nailgun's build scripts to increase
    compatibility with more platforms.
  - Updated the installer and eclimd to not use the eclipse binary and instead
    run the launcher jar directly.  Removes the need to locate the binary which
    varies by platform and some eclipse distributions, and fixes issues with
    options located in the eclipse.ini that are only supported by the IDE app.
  - Various other bug fixes.

Eclim:
  - Add workspace locking to prevent colliding with other running eclipse
    instances on the same workspace.

Common:
  - Added a :TreeTab command like :ref:`:ProjectTab <:ProjectTab>` but for any
    arbitrary directory.
  - Added a :ref:`:BuffersToggle <:BuffersToggle>` command to toggle whether
    the eclim buffers windows is open or closed.
  - Refactored Vcs support, including a new :VcsLog layout.

    .. note::

      Support for cvs and subversion have been discontinued.

Python:
  - Upgraded included rope version to 0.9.3.
  - Improved the detection of the completion entries types.

Php:
  - Fixed validating php files in eclipse 3.6.1.

:gitlog:`Git Log (1.6.1) <1.6.0...1.6.1>`

.. _1.6.0:

1.6.0 (Aug. 01, 2010)
----------------------

Bug Fixes:
  - Several bug fixes for eclipse 3.6 (Helios) compatability.
  - Various other bug fixes.

Eclipse:
  - Eclim now requires the latest version of eclipse (Helios, 3.6).

Common:
  - :ref:`:ProjectTree <:ProjectTree>` now supports eclipse resource links.

:gitlog:`Git Log (1.6.0) <1.5.8...1.6.0>`

.. _1.5.8:

1.5.8 (Jun. 26, 2010)
----------------------

Bug Fixes:
  - Fixed possible NPE during installation when one or more eclim dependent
    eclipse features needs to be upgraded.
  - Fixed code completion and search for php as well as search for ruby, all of
    which were all affected by dltk module caching introduced in galileo SR2.

:gitlog:`Git Log (1.5.8) <1.5.7...1.5.8>`

.. _1.5.7:

1.5.7 (Jun. 20, 2010)
----------------------

Bug Fixes:
  - Fixed launching of MacVim from the eclipse gui.
  - Various other bug fixes.

Installer:
  - The installer has undergone some extensive changes to make it more reliable
    and to better support various environments.

:gitlog:`Git Log (1.5.7) <1.5.6...1.5.7>`

.. _1.5.6:

1.5.6 (Mar. 06, 2010)
----------------------

Bug Fixes:
  - Avoid possible gvim crashes when launched from the eclipse gui by disabling
    documentListen events if the current gvim version doesn't include the patch
    which resolves the crash.
  - Various bug fixes.

Common:
  - Added a :ref:`:ProjectTab <:ProjectTab>` command providing the ability to
    work on one or more projects each with a dedicated vim tab.
  - Added a :ref:`:Tcd <:Tcd>` command to mimic :lcd but local to the tab
    instead of the window.
  - Added branch info to the footer of the project tree when using mercurial or
    git.

Install:
  - Added support for :ref:`automated installs <installer-automated>`.

Eclim:
  - Added initial support for using eclim via vim in cygwin.
  - The eclimd log file has been moved to: <workspace>/.metadata/.log.eclimd
  - Added support for specifying that gvim should be refocused after executing
    an eclipse keybinding from vim using :ref:`eclim#vimplugin#FeedKeys
    <FeedKeys>`.
  - Moved user local eclim resources (templates, taglist scripts, etc) from
    ${vimfiles}/eclim/resources to ~/.eclim/resources.

    .. note::

      The eclim installer will move your existing files from the old location
      to the new location, but you may want to back those files up just in
      case.

:gitlog:`Git Log (1.5.6) <1.5.5...1.5.6>`

.. _1.5.5:

1.5.5 (Feb. 22, 2010)
----------------------

Bug Fixes:
  - Fixed error using :ProjectTree if the project name has non-word characters
    in it.

Install:
  - Fixed issue downloading content.jar from eclipse update site.

:gitlog:`Git Log (1.5.5) <1.5.4...1.5.5>`

.. _1.5.4:

1.5.4 (Dec. 18, 2009)
----------------------

Bug Fixes:
  - Fixed eclim client on OSX.
  - Fixed backspace key in the :ref:`:LocateFile <:LocateFile>` buffer.

Common:
  - Added support for interactively switching scopes from the :ref:`:LocateFile
    <:LocateFile>` buffer.
  - Added new search scopes (buffers, quickfix, vcsmodified) to
    :ref:`:LocateFile <:LocateFile>`.

:gitlog:`Git Log (1.5.4) <1.5.3...1.5.4>`

.. _1.5.3:

1.5.3 (Dec. 12, 2009)
----------------------

Bug Fixes:
  - Various bug fixes.

Install:
  - Fixed issues properly detecting write permissions on Windows machines.

Docs:
  - Added a guide on :ref:`running eclim on a headless server
    <install-headless>`.

Common:
  - Added full support for :ref:`running multiple eclimd instances
    <eclimd-multiworkspace>`, each backed by a separate eclipse workspace.
  - Added 'K' mapping to :ref:`:ProjectTree <:ProjectTree>` to set the tree
    root the either the project root of file system root depending on the
    context.  Added 'D' mapping to create a new directory and 'F' to open a new
    or existing file by name. Note: the 'H' mapping to set the tree root to the
    user's home directory has been changed to '~'.
  - Added setting to allow :ref:`:ProjectTree <:ProjectTree>` instances to be
    shared across vim tabs.
  - Updated :VcsWeb to support github, google code, and bitbucket.

C/C++:
  - Improved :ref:`:CSearchContext <:CSearchContext>` to search for declaration
    when on a definition, allowing you to jump back and forth between
    declaration and definition.
  - Added :ref:`:CCallHierarchy <:CCallHierarchy>` to display the call
    hierarchy for the function or method under the cursor.

Java:
  - Added :ref:`:JavaListInstalls <:JavaListInstalls>` to list all the
    installed JDKs/JREs that eclipse is aware of.

:gitlog:`Git Log (1.5.3) <1.5.2...1.5.3>`

.. _1.5.2:

1.5.2 (Aug. 30, 2009)
----------------------

Bug Fixes:
  - Various bug fixes.

Eclim:
  - Added :ref:`:ProjectRename` and :ref:`:ProjectMove` commands to allow
    renaming and moving of projects.
  - Added :ref:`:ProjectProblems` command to populate vim's quickfix with a
    list of all eclipse build errors and warnings for the current and all
    related projects.

    .. note::

      To have problems reported for java projects created via eclim, you may
      need to recreate your java projects to ensure that the java builder is
      properly added.  As of eclim 1.5.2, eclim's java project creation now
      adds the java builder.

  - Added :ref:`:HistoryDiffNext` and :ref:`:HistoryDiffPrev` commands to view
    history diffs while navigating the history stack.
  - Abbreviation support removed in favor of any one of the third party
    snippets plugins available on vim.org (snipMate, snippetsEmu, etc.).
  - Added support for hosting third party nailgun apps in eclim via an
    :ref:`ext dir <eclimd-extdir>`.

Java:
  - Updated :ref:`:JavaImpl`, :ref:`:JavaDelegate`, and
    :ref:`:JUnitImpl <:JUnitImpl>` to better support generics.
  - Updated :ref:`:JUnitImpl <:JUnitImpl>` to support junit 4 method
    signatures.
  - Updated :ref:`:JavaImport` and :JavaImportSort to honor eclipse's
    import order preference and added the ability to edit that preference via
    :ref:`:ProjectSettings` and **:EclimSettings**.
  - Added initial :doc:`refactoring </vim/java/refactor>` support.

:gitlog:`Git Log (1.5.2) <1.5.1...1.5.2>`

.. _1.5.1:

1.5.1 (Jul. 18, 2009)
----------------------

Bug Fixes:
  - Several minor bug fixes.

Install:
  - Installation on Mac OSX should hopefully work now without manually creating
    a symlink to your eclipse executable.

Eclipse:
  - Fixed possible NPE when exiting or starting eclipse if a gvim tab was left
    open.

Eclim:
  - Added initial support for linked folders in eclipse projects.
  - Added new g:EclimValidateSortResults setting to support sorting
    validation results (:doc:`java </vim/java/validate>`, :doc:`c/c++
    </vim/c/validate>`, :doc:`php </vim/php/validate>`, etc.) by priority
    (errors > warnings > etc.).

C/C++:
  - Fixed :CSearch results on Windows platforms.
  - Re-implemented c/c++ project creation.

    .. note::

      If you created any c or c++ projects via eclim (as opposed to creating
      the project via the eclipse project wizard), then you are strongly
      encouraged to recreate those projects using the following steps:

      1. Delete the project using ``:ProjectDelete project_name``
      2. Remove the .cproject file at the root of your project.
      3. Re-create the the project using
         ``:ProjectCreate /project/path/ -n c`` (or cpp)

      After that you will need to re-configure any src or include folders you
      may have added.

:gitlog:`Git Log (1.5.1) <1.5.0...1.5.1>`

.. _1.5.0:

1.5.0 (Jul. 12, 2009)
----------------------

Bug Fixes:
  - Many bug fixes and refinements.

Eclipse:
  - Eclim now requires the latest version of eclipse (Galileo, 3.5.x).

Ruby:
  - Added ruby support for
    :doc:`code completion </vim/ruby/complete>`,
    :doc:`searching </vim/ruby/search>`, and
    :doc:`validation </vim/ruby/validate>`.

Java:
  - Added ability to configure java indentation globally via
    **:EclimSettings** or per project using :ref:`:ProjectSettings`.

:gitlog:`Git Log (1.5.0) <1.4.9...1.5.0>`

.. _1.4.9:

1.4.9 (Jun. 14, 2009)
----------------------

Bug Fixes:
  - Fixed possible installation issue on Windows.
  - Various other bug fixes.

Eclim:
  - Vimplugin now supports auto starting eclimd view when gvim editor is opened
    from eclipse.
  - Handle possible key binding conflicts when using embedded gvim for two
    common gvim bindings (ctrl-w, ctrl-u).

:gitlog:`Git Log (1.4.9) <1.4.8...1.4.9>`

.. _1.4.8:

1.4.8 (May 30, 2009)
----------------------

Bug Fixes:
  - Fixed C/C++ element search.
  - Fixed possible issue with secondary python element search on Windows.
  - Various other bug fixes.

Eclim:
  - Added :ref:`:ProjectImport` command.

Maven
  - Switched repository searching to a new (hopefully more dependable) site.

Python:
  - Added :ref:`:PythonSearchContext`.

:gitlog:`Git Log (1.4.8) <1.4.7...1.4.8>`

.. _1.4.7:

1.4.7 (May 02, 2009)
----------------------

Bug Fixes:
  - Fixed installation error on unix based operating systems.

:gitlog:`Git Log (1.4.7) <1.4.6...1.4.7>`

.. _1.4.6:

1.4.6 (May 02, 2009)
----------------------

Bug Fixes:
  - Various bug fixes.

C/C++:
  - Added c/c++ support for
    :doc:`code completion </vim/c/complete>`,
    :doc:`searching </vim/c/search>`, and
    :doc:`validation </vim/c/validate>`.
    Requires the `eclipse cdt`_ plugin.

Java:
  - Added command to run :ref:`java <:Java>`.
  - Added command to run javac.
  - Added command to run :ref:`javadoc <:Javadoc>`.

:gitlog:`Git Log (1.4.6) <1.4.5...1.4.6>`

.. _1.4.5:

1.4.5 (Apr. 04, 2009)
----------------------

Bug Fixes:
  - Fixed pdt and wst code completion when invoked from headed eclimd.
  - Fixed closing of gvim from eclipse to cleanup swap files.
  - Fixed python code completion and find support when editing files with dos
    line endings or multi-byte unicode characters.
  - Various other bug fixes.

Eclim:
  - Added integration with eclipse's
    :doc:`local history </vim/core/history>` support.

Java:
  - Added command to view :ref:`type hierarchy <:JavaHierarchy>`.
  - Added command to import all undefined types.

:gitlog:`Git Log (1.4.5) <1.4.4...1.4.5>`

.. _1.4.4:

1.4.4 (Jan. 10, 2009)
----------------------

Bug Fixes:
  - Various bug fixes.

Java:
  - :ref:`:Checkstyle <:Checkstyle>` command now creates a project
    classloader giving checkstyle access to any classes reachable via your
    project's .classpath file.

Eclim:
  - Added the ability to run :ref:`eclimd inside of eclipse <eclimd-headed>`.
  - Added support for :ref:`embedding gvim inside of eclipse <gvim-embedded>`.
  - eclimd start scripts now available in the eclipse home.
  - Consolidated the various **:LocateFile\*** commands into a single
    :doc:`:LocateFile </vim/core/locate>` command with a new setting to specify
    the default means to open a result and various key bindings for opening via
    other means.

Php:
  - Restored :doc:`php support </vim/php/index>` via the new `eclipse pdt`_
    2.0.

Vcs:
  - Added option to set the split orientation (horizontal or vertical) used
    when executing diffs.
  - Added option to allow users to change the pattern used to match tracker
    ticket numbers in :VcsLog.

:gitlog:`Git Log (1.4.4) <1.4.3...1.4.4>`

.. _1.4.3:

1.4.3 (Nov. 15, 2008)
----------------------

Bug Fixes:
  - Various bug fixes.

Installer:
  - Updated to make use of the new ganymede p2 provisioning system.

Eclim:
  - Rewrote :doc:`:LocateFile* </vim/core/locate>` commands to provide
    functionality similar to eclipse's "Open Resource" command or Textmate's
    "Find in Project".

Python:
  - Added support for :doc:`code completion </vim/python/complete>`.
  - Added support for :doc:`finding an element definition </vim/python/search>`.
  - Improved :PyLint support.

:gitlog:`Git Log (1.4.3) <1.4.2...1.4.3>`

.. _1.4.2:

1.4.2 (Sep. 30, 2008)
----------------------

Bug Fixes:
  - Fixed obtaining of character offset used by code completion and various
    other commands.
  - Fixed possible bug with :JavaCorrect when modifying the file after
    obtaining a list of suggestions, and then attempting to apply a suggestion
    that is no longer valid.

Vcs:
  - Added support for git to :Vcs commands

:gitlog:`Git Log (1.4.2) <1.4.1...1.4.2>`

.. _1.4.1:

1.4.1 (Aug. 24, 2008)
-----------------------

Bug Fixes:
  - Fixed determining of project paths outside of the workspace on Windows.
  - Fixed creation of project inside of the workspace on Windows.
  - Fixed some issues with code completion, etc. in files containing multi byte
    characters.
  - Various other bug fixes.

Eclim:
  - Added commands :ref:`:EclimDisable` and :ref:`:EclimEnable` to temporarily
    disable, and then re-enable, communication with eclimd.

Java:
  - Added :ref:`:JavaFormat` command contributed by Anton Sharonov.
  - Added :ref:`:Checkstyle` support.

:gitlog:`Git Log (1.4.1) <1.4.0...1.4.1>`

.. _1.4.0:

1.4.0 (July 27, 2008)
---------------------

Eclipse:
  - Eclim now requires the latest version of eclipse (Ganymede, 3.4.x).

License:
  - Eclim has switched from the Apache 2 license to the GPLv3.

Bug Fixes:
  - Fixed possible issue on Windows determining workspace for users not using
    the default location.
  - Fixed sign placement (used by all validation plugins) on non-english vims.
  - Various other bug fixes.

Eclim:
  - Added translation of html docs to vim doc format accessable via
    :ref:`:EclimHelp` and :ref:`:EclimHelpGrep`.
  - Added :ref:`:Todo` and :ref:`:ProjectTodo`.
  - Added :TrackerTicket for viewing tickets by id in your web based tracking
    system.
  - Renamed setting ``org.eclim.project.vcs.tracker`` to
    ``org.eclim.project.tracker``.

Django:
  - Added :ref:`end tag completion <htmldjango>` support for django templates.

Php:
  - Support for php has been temporarily removed until the eclipse pdt team
    releases a Ganymede (3.4) compatible version.

Vcs:
  - Removed **:VcsAnnotateOff** in favor of invoking **:VcsAnnotate** again to
    remove the annotations.
  - Added vcs editor plugin which allows you to view diff of a file by hitting
    <enter> on a file name in the cvs, svn, or hg commit editor.
  - Removed **:Trac\*** and **:Viewvc\*** commands and replaced them with
    :VcsWeb* commands

Vim:
  - Added :ref:`:Only` as a configurable alternative to vim's :only command.
  - Added :OtherWorkingCopyDiff, :OtherWorkingCopyEdit,
    :OtherWorkingCopySplit, and :OtherWorkingCopyTabopen.

:gitlog:`Git Log (1.4.0) <1.3.5...1.4.0>`

.. _1.3.5:

1.3.5 (Mar. 11, 2008)
---------------------

Bug Fixes:
  - Fixed exclusion of plugins not chosen by the user for installation.
  - Various bug fixes.

Eclim:
  - Added an archive (jar, tar, etc.) viewer.

Html:
  - Updated html validator to validate <style> and <script> tag contents.

Vcs:
  - Added support for limiting the number of log entries returned by
    :VcsLog (limits to 50 entries by default).
  - Updated **:VcsLog**, **:VcsChangeSet**, etc.
    to support cvs and hg where applicable.

Trac:
  - Added :TracLog, :TracAnnotate, :TracChangeSet, and :TracDiff.

:gitlog:`Git Log (1.3.5) <1.3.4...1.3.5>`

.. _1.3.4:

1.3.4 (Feb. 05, 2008)
---------------------

Bug Fixes:
  - Fixed **:JavaImpl** when adding multi-argument methods.
  - Various other bug fixes.

Eclim:
  - Added :ref:`:ProjectInfo`.
  - Added an eclim/after directory to vim's runtime path for any user scripts
    to be sourced after eclim.

Installer:
  - Updated installer to handle eclipse installs which have a local user
    install location for plugins.
  - Fixed some issues with running the installer on the icedtea jvm.

Php:
  - Added php support for
    :doc:`code completion </vim/php/complete>`,
    :doc:`searching </vim/php/search>`, and
    :doc:`validation </vim/php/validate>`.
    Requires the `eclipse pdt`_ plugin.

:gitlog:`Git Log (1.3.4) <1.3.3...1.3.4>`

.. _1.3.3:

1.3.3 (Dec. 15, 2007)
---------------------

Bug Fixes:
  - Installer bug fixes.

:gitlog:`Git Log (1.3.3) <1.3.2...1.3.3>`

.. _1.3.2:

1.3.2 (Dec. 04, 2007)
---------------------

Bug Fixes:
  - Various bug fixes.

Eclim:
  - Added commands to view or manipulate project natures:
    :ref:`:ProjectNatures`,
    :ref:`:ProjectNatureAdd`, and
    :ref:`:ProjectNatureRemove`.

Css:
  - Added :ref:`css validation <css>`.

Html:
  - Added :ref:`:BrowserOpen`

Html / Xml:
  - Added auto completion of end tags when typing '</'.
    This can be disabled by setting
    **g:EclimSgmlCompleteEndTag** to 0.

Java / Python:
  - :JavaRegex and :PythonRegex now support **b:eclim_regex_type** to determine
    if the regex should be applied to the whole sample text at once, or to each
    line individually.

Java:
  - Updated the :doc:`java logger </vim/java/logging>` functionality to support
    a custom logger template.

Javascript:
  - Added :doc:`javascript validation </vim/javascript/index>` using jsl_.

Python:
  - Added basic :doc:`python validation </vim/python/validate>` using pyflakes_
    and the python compiler.
  - Added support for pylint_ using new :PyLint command.

Vcs:
  - Added :VcsInfo, :ViewvcAnnotate, :ViewvcChangeSet, and :ViewvcDiff.

Vcs (subversion):
  - Added :VcsLog, :VcsDiff, and :VcsCat.

Vim:
  - Added vim window maximize and minimize support.
  - Added an alternate implementation of taglist.
  - Added command :ref:`:Buffers`.
  - Added :VimgrepRelative, :VimgrepAddRelative, :LvimgrepRelative,
    :LvimgrepAddRelative, :CdRelative, and :LcdRelative.

:gitlog:`Git Log (1.3.2) <1.3.1...1.3.2>`

.. _1.3.1:

1.3.1 (July 13, 2007)
---------------------

Bug Fixes:
  - Fixed eclimd startup issues on non-gentoo linux machines as well as
    similar issue in the installer when attempting to handle plugin
    dependencies for wst integration.
  - Fixed installer to not exclude html/util.vim when not installing wst
    integrations (fixes dependent code like java code completion).

:gitlog:`Git Log (1.3.1) <1.3.0...1.3.1>`

.. _1.3.0:

1.3.0 (July 01, 2007)
---------------------

Bug Fixes:
  - Bug fixes.

Eclim:
  - New graphical installer for easing the installation and upgrading
    procedure.
  - In previous releases of eclim, any time a command required access to
    the eclipse representation of a source file, eclim would force a full
    refresh of the current project to ensure that any external additions,
    deletions, or changes to other files would be automatically detected.
    However, this approach, while convenient and transparent to the user,
    comes with a performance penalty that grows as the project size grows.

    For some users this performance penalty has been more noticeable than
    for others.  So in response to this feedback, eclim no longer performs
    an automatic project refresh.  What this means for you is that any time
    you perform an action that results in any file additions, deletions, or
    changes, like a svn / cvs update, you should issue a :ref:`:ProjectRefresh`
    to ensure that eclipse and eclim are updated with the latest version of the
    files on disk.
  - :ref:`:ProjectCreate` now supports optional -p argument for specifying the
    project name to use.
  - Created new command :ref:`:ProjectRefreshAll` to support refreshing all
    projects at once, and modified :ref:`:ProjectRefresh` to only refresh the
    current project if no project names are supplied.
  - Added
    :ref:`:ProjectGrep`,
    :ref:`:ProjectGrepAdd`,
    :ref:`:ProjectLGrep`, and
    :ref:`:ProjectLGrepAdd`.
  - Added support for buffer local variable
    **b:EclimLocationListFilter** which can contain a list of
    regular expression patterns used to filter location list entries with
    text / message field matching one of the patterns.  The main intention
    of this new variable is to allow you to filter out validation errors /
    warnings per file type, that you wish to ignore.

    Example which I have in my .vim/ftplugin/html/html.vim file\:

    .. code-block:: vim

      let b:EclimLocationListFilter = [
          \ '<table> lacks "summary" attribute'
        \ ]

Css:
  - Added :ref:`css code completion <css>`.  Requires the `eclipse wst`_
    plugin.

Dtd:
  - Added :ref:`dtd validation <dtd>`.  Requires the `eclipse wst`_ plugin.

Html:
  - Added :doc:`html code completion </vim/html/index>`.  Requires the
    `eclipse wst`_ plugin.
  - Added :doc:`html validation </vim/html/index>`.  Requires the
    `eclipse wst`_ plugin.

Log4j:
  - Added :ref:`log4j xml file validation <log4j>`.

Python:
  - Added support for testing regular expressions.

Django:
  - Added
    :ref:`:DjangoManage`,
    :ref:`:DjangoFind`,
    :ref:`:DjangoTemplateOpen`,
    :ref:`:DjangoViewOpen`, and
    :ref:`:DjangoContextOpen`.

WebXml:
  - Added :doc:`web.xml file validation </vim/java/webxml>`.

Vim:
  - Added :ArgsRelative, :ArgAddRelative, :ReadRelative.
  - Added
    :ref:`:Sign`,
    :ref:`:Signs`,
    :ref:`:SignClearUser`,
    :ref:`:SignClearAll`.

Vcs:
  - Added :VcsAnnotate and :Viewvc.

Wsdl:
  - Added wsdl validation.  Requires the `eclipse wst`_ plugin.

Xsd:
  - Added :ref:`xsd validation <xsd>`.  Requires the
    `eclipse wst`_ plugin.

Xml:
  - Added :doc:`xml code completion </vim/xml/index>`.  Requires the
    `eclipse wst`_ plugin.

:gitlog:`Git Log (1.3.0) <1.2.3...1.3.0>`

.. _1.2.3:

1.2.3 (Oct. 08, 2006)
---------------------

Bug Fixes:
  - Vim scripts now account for possibly disruptive 'wildignore' option.
  - On Windows, vim scripts account for users who have modified the 'shell'
    that vim uses, temporarily restoring the default.
  - Reimplemented **:EclimSettings** and
    **:ProjectSettings** saving to be more fault tolerant.
  - Several other bug fixes.

Eclim:
  - Renamed **:Settings** to **:EclimSettings** to
    increase the uniqueness of the command name in an effort to avoid
    clashing with other vim plugins.

Java:
  - Maven dependency searching now expanded to ivy files via
    :IvyDependencySearch.
  - Fixed junit support to handle execution via maven 1.x and 2.x.

Xml:
  - Added command :ref:`:XmlFormat <:XmlFormat>` to reformat a xml file.

:gitlog:`Git Log (1.2.3) <1.2.2...1.2.3>`

.. _1.2.2:

1.2.2 (Sep. 08, 2006)
---------------------

Bug Fixes:
  - Fixed NullPointerException when accessing eclim preferences containing
    remnant property <code>org.eclim.java.library.root</code>.
  - Fixed plugin/eclim.vim to check vim version earlier to avoid errors on
    pre Vim 7 instances.
  - Fixed all usages of the temp window to account properly for errors.

:gitlog:`Git Log (1.2.2) <1.2.1...1.2.2>`

.. _1.2.1:

1.2.1 (Sep. 07, 2006)
---------------------

Bug Fixes:
  - Fixed issues when eclipse is installed in a directory containing a
    space, like "Program Files".
  - Fixed error when .classpath src dir is "" or ".".
  - Fixed error if taglist.vim is not installed.
  - Fixed auto setting of jre source.
  - Fixed couple java code completion issues.
  - Several other bug fixes.

Ant:
  - Made some improvements to ant code completion.

Eclim:
  - Added support for :ref:`~/.eclimrc <eclimrc>` on unix platforms.

Java:
  - Added :ref:`:VariableList <:VariableList>`,
    :ref:`:VariableCreate <:VariableCreate>` and
    :ref:`:VariableDelete <:VariableDelete>`.
  - | Added camel case searching support\:
    | :ref:`JavaSearch <:JavaSearch>` NPE
  - Removed the preference <code>org.eclim.java.library.root</code>.
  - Updated :ref:`ivy support <classpath-ivy>` to behave more like maven.
  - Added commands to ease setting of classpath repo variables for
    :ref:`maven's <classpath-maven>` and :ref:`mvn's <classpath-maven>` eclipse
    support.
  - Added TestNG to ant compiler's error format.
  - Added :JUnitExecute and :ref:`:JUnitResult <:JUnitResult>`.

Xml:
  - Added :ref:`:DtdDefinition <:DtdDefinition>` and
    :ref:`:XsdDefinition <:XsdDefinition>`.

Vim:
  - Added **:FindCommandDef** and **:FindCommandRef**.
  - Changed **:FindFunctionVariableContext** to **:FindByContext**.
  - Added **:Tabnew** and **:TabnewRelative**.
  - **:Split** and **:SplitRelative** now support '*' and '**' wildcards.

:gitlog:`Git Log (1.2.1) <1.2.0...1.2.1>`

.. _1.2.0:

1.2.0 (July 16, 2006)
---------------------

Bug Fixes:
  - Fixed processing of dtd related xml validation errors on Windows.
  - Using Ctrl-C on a prompt list (like when choosing a java class to
    import), stopped working.  At some point during the vim 7 developement
    the vim behavor was modified.  Eclim, has been fixed to account for
    this.
  - Greatly improved support for projects created from eclipse.
  - Fixed support for projects created from eclipse that reside in the
    workspace.
  - Other various bug fixes.

Eclipse:
  - Eclim now works with and depends on eclipse 3.2.

Eclim:
  - Added :ref:`:ProjectRefresh <:ProjectRefresh>`.
  - Added :ref:`:ProjectOpen <:ProjectOpen>`, :ref:`:ProjectClose
    <:ProjectClose>`, and updated :ref:`:ProjectList <:ProjectList>` to show
    the current status of each project.
  - Added :ref:`:ProjectTree <:ProjectTree>` and
    :ref:`:ProjectsTree <:ProjectsTree>`.
  - Added :ref:`:ProjectCD <:ProjectCD>` and :ref:`:ProjectLCD <:ProjectLCD>`.
  - Added :ref:`:JavaSearchContext <:JavaSearchContext>`.
  - Added means to preserve manually added classpath entries when utilizing
    eclim's integration with ivy or maven dependency files.
  - Updated :ref:`:JavaSearch <:JavaSearch>`
    to provide sensible defaults if command ommits various arguments. Also
    added support for supplying only a pattern to the
    **:JavaSearch** command which will result in a search for
    all types (classes, enums, interfaces) that match that pattern.
  - Added :ref:`:Jps <:Jps>` for viewing java process info.
  - Removed support for auto update of .classpath upon writing of maven
    project.xml in favor of new maven support.
  - Added :ref:`:Maven <:Maven>` and :ref:`:Mvn <:Mvn>` commands for executing
    maven 1.x and 2.x.
  - Added :MavenDependencySearch and :MvnDependencySearch
    for searching for and adding dependencies to your maven pom file.
  - <anchor id="upgrade_1.2.0"/>
    Re-organized eclim files within the vim runtime path.
    Based on suggestion by Marc Weber.

    .. warning::
      This change will require you to remove all the old eclim vim plugins
      prior to installing the new set.  A comprehensive list of plugins to
      be deleted is provided
      <a href="upgrade/resources/1.2.0/vim_plugin_list.txt">here</a>.

      You may also use one of the following scripts to help automate the
      process.  Just download the appropriate file to the directory where
      you extracted the eclim vim plugins and execute it.  Please review the
      script before executing it so that you are aware of what it does.
      Please report any issues as well.

      **\*nix users**:
        | <a href="upgrade/resources/1.2.0/upgrade.sh">upgrade.sh</a>
        | Be sure to either make the file executable
        | $ chmod 755 upgrade.sh
        | or run via sh
        | $ sh upgrade.sh

      **Windows users**:
        | <a href="upgrade/resources/1.2.0/upgrade.bat">upgrade.bat</a>
        | Run via a command prompt so that you can monitor the output.

      After executing either of these scripts you may be left with one or
      more empty directories which are then safe to delete.

      I appologize for this inconvience, and hopefully this change will
      help ease future upgrades.

Vim:
  - Added **:FindFunctionVariableContext** to perform context sensitive
    searching for vim functions or global variables.
  - Added **:Split**, **:SplitRelative**, **:EditRelative**,
    **:LocateFileEdit**, **:LocateFileSplit**, and **:LocateFileTab**.

:gitlog:`Git Log (1.2.0) <1.1.2...1.2.0>`

.. _1.1.2:

1.1.2 (May 07, 2006)
---------------------

Bug Fixes:
  - Fixed eclipse .classpath commands.
  - Fixed java project update commands to refresh the project resources so
    that new jars are recognized when added to the .classpath file.
  - `Bug 1437025 <https://sourceforge.net/tracker/index.php?func=detail&aid=1437025&group_id=145869&atid=763323>`_
  - `Bug 1437005 <http://sourceforge.net/tracker/index.php?func=detail&aid=1437005&group_id=145869&atid=763323>`_
    and other irregularities with calculation of the starting position for
    the completion.
  - `Bug 1440606 <https://sourceforge.net/tracker/index.php?func=detail&aid=1440606&group_id=145869&atid=763323>`_

    .. note::

      The original implementation of :JavaImportClean was written entirely in
      vim (didn't require eclim server).  To properly handle ignoring comments
      when determining what imports are unused, this functionality had to be
      reimplemented with server side help.  However, the vim only version is
      preserved and will be invoked if the current file is not in an eclipse
      project.

  - Other various bug fixes.

Ant:
  - Added :ref:`:Ant <:Ant>` command
    to allow execution of ant from any file.
  - Added :doc:`ant code completion </vim/java/ant>`.
  - Added :doc:`ant file validation </vim/java/ant>`.
  - Added :ref:`:AntDoc <:AntDoc>` command to quickly find ant type / task
    documentation.

Java:
  - Utilizing vim's new dictionary based completion results.
  - Added :ref:`:JavaConstructor <:JavaConstructor>`.
  - :ref:`:JavaImpl <:JavaImpl>` now supports overriding constructors.
  - Added :ref:`:JavaDocComment <:JavaDocComment>`
    command to add or update javadocs comment for the element under the
    cursor.
  - Added **:JavaRegex** for testing java regular expressions.
  - JDT classpath_variables.properties no longer requires system property
    placeholder to use '_' instead of '.'.
  - Velocity templates broken up into more logical templates to ease
    customization.
  - :ref:`:JavaGetSet <:JavaGetSet>` now has variable to determine whether or
    not to add indexed getters and setters.
  - Removed preference org.eclim.java.validation.ignore.warnings in favor of
    new :ref:`g:EclimSignLevel <g:EclimSignLevel>`.

Vim:
  - Added couple miscellaneous vim commands for use in or outside of eclim
    context.
  - Added groovy script based ctags implementation for use with the vim taglist
    plugin.
  - All of the functionality that previously placed results / errors into
    the quickfix window, now utilizes vim's new location list functionality.
  - Added web lookup commands.
  - Added vim script function / global variable searching.
  - Added vim doc lookup.
  - Various improvements to 'sign' support for marking errors, warnings,
    etc.

Xml:
  - Xml validation now caches remote entities (like dtds).

:gitlog:`Git Log (1.1.2) <1.1.1...1.1.2>`

.. _1.1.1:

1.1.1 (Feb. 19, 2006)
---------------------

Bug Fixes:
  - Code completion results now sorted by type and then alphabetically.
  - Code corrections that cannot be applied in the standard way (those
    with no previews) are excluded.
  - Simple searching from spring / hiberate / web.xml files is fixed.
  - Java import command is now restricted to the current project.
  - Java src file location (for almost all java commands) now uses the full
    path to find the file in eclipse rather than build the path from the
    file's package declaration and class name.  Fixes cases where the
    package name doesn't match up with the folder structure.
  - Xml validation errors that occur when no dtd is defined are filtered
    out (limitation of using xerces to support jdk 1.4).
  - Relative xml entities are now resolved when validating an xml file.
  - Fixed logging template code for slf4j.
  - Fixed possible error when removing signs for marking errors / warnings
    for the current file.
  - Fixed :ref:`:JavaImport <:JavaImport>` to not import classes that are in
    the same package as the current file.
  - Fixed java source validation to clear out the quickfix results when all
    errors have been fixed.
  - Fixed :ref:`:JavaImpl <:JavaImpl>` to get the interfaces of superclass
    lineage in addition to directly implemented interfaces of the current
    class.
  - When adding methods from the resulting buffer of :ref:`:JavaImpl
    <:JavaImpl>` or :ref:`:JUnitImpl <:JUnitImpl>`, if the target class was an
    inner class of the src file, then the methods were being added to the outer
    class instead of the inner class.
  - Fixed javadoc search results to restore <enter> as mapping to open
    result in a browser after the quickfix window is closed and then opened
    again.
  - Other various bug fixes.

Eclipse:
  - Eclim now depends on eclispe version 3.1.2.  The Eclipse team made some
    improvements to how inner classes are handled that eclim now depends on.

Eclim:
  - Added support for global settings/preferences via new
    **:Settings** command.

Java:
  - Delegate method creation.
  - Added g:EclimJavaSearchSingleResult setting to determine action to take
    when only a singe result is returned using the java source code searching.
    Based on suggestion by Ivo Danihelka.
  - Added g:EclimJavaDocSearchSingleResult setting to determine action to take
    when only a singe result is returned using the javadoc searching.  Based on
    suggestion by Ivo Danihelka.
  - Added preference to suppress warnings when using java source code
    validation.

Vim:
  - Added CursorHold autocommand that shows the current error, if any, on
    the current cursor line.
  - Removed global variables g:EclimDebug and g:EclimEchoHighlight in favor
    of new :ref:`g:EclimLogLevel <g:EclimLogLevel>` and the corresponding
    highlight varibles.
  - Removed all default key mappings. See the suggested set of mappings.
  - Now utilizing vim's autoload functionality to load functions on demand.

    One vim file was moved as a result, so you should delete the old file
    when upgrading.

    .. warning::

      - ftplugin/java/eclim_util.vim removed.

:gitlog:`Git Log (1.1.1) <1.1.0...1.1.1>`

.. _1.1.0:

1.1.0 (Dec. 26, 2005)
---------------------

Bug Fixes:
  - Code completion, searching, etc fixed on files with fileformat == 'dos'.
  - Several other minor fixes and enhancements.

Java:
  - :doc:`Source validation</vim/java/validate>`.
  - :doc:`Javadoc </vim/java/javadoc>` viewing.
  - :ref:`Override/Impl <:JavaImpl>` stub generation.
  - :ref:`Bean getter/setter <:JavaGetSet>` generation.
  - :doc:`Junit </vim/java/unittests>` test method stub generation.
  - :doc:`Alternate searching </vim/java/search>` in code bases outside of an
    eclipse project.
  - :ref:`Code correction <:JavaCorrect>` via eclipse
    quickfix functionality.
  - Support for viewing source files located in archives (zip, jar) when
    searching.
  - Support for generating a source prototype when viewing search results
    that do not have a corresponding source file attached.
  - Added some handy abbreviations.
  - Added validation of the .classpath file when saving.  Errors are then
    reported via vim's quickfix.

Vim:
  - A few vim scripts were renamed, so you will need to delete the old file
    when upgrading.

    .. warning::
      <ul>
      <li>
      ftplugin/eclipse_classpath/eclipse_classpath.vim moved to
      ftplugin/eclipse_classpath/eclim.vim
      </li>
      <li>
      ftplugin/ivy/ivy.vim moved to
      ftplugin/ivy/eclim.vim
      </li>
      <li>
      ftplugin/maven_project/maven_project.vim moved to
      ftplugin/maven_project/eclim.vim
      </li>
      </ul>

Xml:
  - :ref:`Xml validation <xml-validation>`.

:gitlog:`Git Log (1.1.0) <1.0.0...1.1.0>`

.. _1.0.0:

1.0.0 (Oct. 16, 2005)
---------------------

Eclim:
  - Initial release.

`Git Log (1.0.0) <https://github.com/ervandew/eclim/commits/1.0.0>`_

.. _eclipse cdt: http://eclipse.org/cdt/
.. _eclipse pdt: http://eclipse.org/pdt/
.. _eclipse wst: http://eclipse.org/webtools/main.php
.. _jsl: http://www.javascriptlint.com/
.. _pyflakes: http://www.divmod.org/trac/wiki/DivmodPyflakes
.. _pylint: http://www.logilab.org/857
