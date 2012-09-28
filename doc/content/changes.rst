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

History of Changes
==================

.. _2.2.2:
.. _1.7.10:

2.2.2 / 1.7.10 (Sep. xx, 2012)
------------------------------

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

.. _1.7.9:

1.7.9 (Sep. 09, 2012)
---------------------

Scala:
  - Add support for scala :doc:`code completion </vim/scala/complete>`,
    :doc:`code validation </vim/scala/validate>`, and :doc:`element definition
    searches </vim/scala/search>`.

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

.. _1.7.6:

1.7.6 (Jun. 07, 2012)
----------------------

Bug Fixes:
  - Couple other minor bug fixes.

Installer:
  - Fixed install location of eclim's vim help files (broken in the previous
    release).

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
    :ref:`:Java <:Java>` and default args for :ref:`:Javac <:Javac>`.
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

:doc:`/archive/changes`
