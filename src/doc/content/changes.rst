.. Copyright (C) 2005 - 2011  Eric Van Dewoestine

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

.. _1.6.3:

1.6.3 (2011-04-16)
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

.. _1.6.2:

1.6.2 (2011-02-26)
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

.. _1.6.1:

1.6.1 (2010-10-23)
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
  - Added a :ref:`:TreeTab <:TreeTab>` command like :ref:`:ProjectTab
    <:ProjectTab>` but for any arbitrary directory.
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

.. _1.5.8:

1.5.8 (Jun. 26, 2010)
----------------------

Bug Fixes:
  - Fixed possible NPE during installation when one or more eclim dependent
    eclipse features needs to be upgraded.
  - Fixed code completion and search for php as well as search for ruby, all of
    which were all affected by dltk module caching introduced in galileo SR2.

.. _1.5.7:

1.5.7 (Jun. 20, 2010)
----------------------

Bug Fixes:
  - Fixed launching of MacVim from the eclipse gui.
  - Various other bug fixes.

Installer:
  - The installer has undergone some extensive changes to make it more reliable
    and to better support various environments.

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
  - Added support for :ref:`automated installs <install-automated>`.

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

.. _1.5.5:

1.5.5 (Feb. 22, 2010)
----------------------

Bug Fixes:
  - Fixed error using :ProjectTree if the project name has non-word characters
    in it.

Install:
  - Fixed issue downloading content.jar from eclipse update site.

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

.. _1.5.3:

1.5.3 (Dec. 12, 2009)
----------------------

Bug Fixes:
  - Various bug fixes.

Install:
  - Fixed issues properly detecting write permissions on Windows machines.

Docs:
  - Added a guide on :ref:`running eclim on a headless server
    <guides/headless>`.

Common:
  - Added full support for :ref:`running multiple eclimd instances
    <guides/multi_workspace>`, each backed by a separate eclipse workspace.
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
  - Added support for hosting third party nailgun apps, like
    :ref:`VimClojure <guides/clojure/vimclojure>`, in eclim via an
    :ref:`ext dir <eclimd_extdir>`.

Java:
  - Updated :ref:`:JavaImpl`, :ref:`:JavaDelegate`, and
    :ref:`:JUnitImpl <:JUnitImpl>` to better support generics.
  - Updated :ref:`:JUnitImpl <:JUnitImpl>` to support junit 4 method
    signatures.
  - Updated :ref:`:JavaImport` and :ref:`:JavaImportSort` to honor eclipse's
    import order preference and added the ability to edit that preference via
    :ref:`:ProjectSettings` and :ref:`:EclimSettings`.
  - Added initial :ref:`refactoring <vim/java/refactor>` support.

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
    validation results (:ref:`java <vim/java/validate>`, :ref:`c/c++
    <vim/c/validate>`, :ref:`php <vim/php/validate>`, etc.) by priority
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

.. _1.5.0:

1.5.0 (Jul. 12, 2009)
----------------------

Bug Fixes:
  - Many bug fixes and refinements.

Eclipse:
  - Eclim now requires the latest version of eclipse (Galileo, 3.5.x).

Ruby:
  - Added ruby support for
    :ref:`code completion <vim/ruby/complete>`,
    :ref:`searching <vim/ruby/search>`, and
    :ref:`validation <vim/ruby/validate>`.

Java:
  - Added ability to configure java indentation globally via
    :ref:`:EclimSettings` or per project using :ref:`:ProjectSettings`.

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

.. _1.4.7:

1.4.7 (May 02, 2009)
----------------------

Bug Fixes:
  - Fixed installation error on unix based operating systems.

.. _1.4.6:

1.4.6 (May 02, 2009)
----------------------

Bug Fixes:
  - Various bug fixes.

C/C++:
  - Added c/c++ support for
    :ref:`code completion <vim/c/complete>`,
    :ref:`searching <vim/c/search>`, and
    :ref:`validation <vim/c/validate>`.
    Requires the `eclipse cdt`_ plugin.

Java:
  - Added command to run :ref:`java <:Java>`.
  - Added command to run :ref:`javac <:Javac>`.
  - Added command to run :ref:`javadoc <:Javadoc>`.

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
    :ref:`local history <vim/common/history>` support.

Java:
  - Added command to view :ref:`type hierarchy <:JavaHierarchy>`.
  - Added command to :ref:`import all undefined types <:JavaImportMissing>`.

.. _1.4.4:

1.4.4 (Jan. 10, 2009)
----------------------

Bug Fixes:
  - Various bug fixes.

Java:
  - :ref:`:Checkstyle <vim/java/checkstyle>` command now creates a project
    classloader giving checkstyle access to any classes reachable via your
    project's .classpath file.

Eclim:
  - Added the ability to run :ref:`eclimd inside of eclipse <eclimd_headed>`.
  - Added support for :ref:`embedding gvim inside of eclipse <gvim_embedded>`.
  - eclimd start scripts now available in the eclipse home.
  - Consolidated the various **:LocateFile\*** commands into a single
    :ref:`:LocateFile <vim/common/util>` command with a new setting to specify
    the default means to open a result and various key bindings for opening via
    other means.

Php:
  - Restored :ref:`php support <vim/php/index>` via the new `eclipse pdt`_
    2.0.

Vcs:
  - Added option to set the split orientation (horizontal or vertical) used
    when executing diffs.
  - Added option to allow users to change the pattern used to match tracker
    ticket numbers in :VcsLog.

.. _1.4.3:

1.4.3 (Nov. 15, 2008)
----------------------

Bug Fixes:
  - Various bug fixes.

Installer:
  - Updated to make use of the new ganymede p2 provisioning system.

Eclim:
  - Rewrote :ref:`:LocateFile* <vim/common/util>` commands to provide
    functionality similar to eclipse's "Open Resource" command or Textmate's
    "Find in Project".

Python:
  - Added support for :ref:`code completion <vim/python/complete>`.
  - Added support for :ref:`finding an element definition <vim/python/search>`.
  - Improved :ref:`:PyLint` support.

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
  - Added :ref:`:OtherWorkingCopyDiff`,
    :ref:`:OtherWorkingCopyEdit`,
    :ref:`:OtherWorkingCopySplit`, and
    :ref:`:OtherWorkingCopyTabopen`.

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
    :ref:`code completion <vim/php/complete>`,
    :ref:`searching <vim/php/search>`, and
    :ref:`validation <vim/php/validate>`.
    Requires the `eclipse pdt`_ plugin.

.. _1.3.3:

1.3.3 (Dec. 15, 2007)
---------------------

Bug Fixes:
  - Installer bug fixes.

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
  - Added :ref:`css validation <vim/css/validate>`.

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
  - Updated the :ref:`java logger <vim/java/logging>` functionality to support
    a custom logger template.

Javascript:
  - Added :ref:`javascript validation <vim/javascript/validate>` using jsl_.

Python:
  - Added basic :ref:`python validation <vim/python/validate>` using pyflakes_
    and the python compiler.
  - Added support for pylint_ using new :ref:`:PyLint` command.

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

.. _1.3.1:

1.3.1 (July 13, 2007)
---------------------

Bug Fixes:
  - Fixed eclimd startup issues on non-gentoo linux machines as well as
    similar issue in the installer when attempting to handle plugin
    dependencies for wst integration.
  - Fixed installer to not exclude html/util.vim when not installing wst
    integrations (fixes dependent code like java code completion).

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
  - Added :ref:`css code completion <vim/css/complete>`.  Requires the
    `eclipse wst`_ plugin.

Dtd:
  - Added :ref:`dtd validation <vim/dtd/validate>`.  Requires the
    `eclipse wst`_ plugin.

Html:
  - Added :ref:`html code completion <vim/html/complete>`.  Requires the
    `eclipse wst`_ plugin.
  - Added :ref:`html validation <vim/html/validate>`.  Requires the
    `eclipse wst`_ plugin.

Log4j:
  - Added :ref:`log4j xml file validation <vim/java/log4j/validate>`.

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
  - Added :ref:`web.xml file validation <vim/java/webxml/validate>`.

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
  - Added :ref:`xsd validation <vim/xsd/validate>`.  Requires the
    `eclipse wst`_ plugin.

Xml:
  - Added :ref:`xml code completion <vim/xml/complete>`.  Requires the
    `eclipse wst`_ plugin.

.. _eclipse cdt: http://eclipse.org/cdt/
.. _eclipse pdt: http://eclipse.org/pdt/
.. _eclipse wst: http://eclipse.org/webtools/main.php
.. _jsl: http://www.javascriptlint.com/
.. _pyflakes: http://www.divmod.org/trac/wiki/DivmodPyflakes
.. _pylint: http://www.logilab.org/857
