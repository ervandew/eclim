.. Copyright (C) 2005 - 2008  Eric Van Dewoestine

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

.. _vim/cheatsheet:

Cheatsheet
==========

Here you will find a comprehensive list of eclim commands that you can use as a
reference.


Global Commands
---------------

- :ref:`:PingEclim <pingeclim>` - Pings eclimd server.
- :ref:`:ShutdownEclim <shutdowneclim>` - Shuts down eclimd server.
- :ref:`:EclimSettings <eclimsettings>` - View / edit global settings.


Project Commands
----------------

- :ref:`:ProjectCreate <projectcreate>`
  <folder> [-p <project_name>] -n <nature> ... [-d <project_dependency> ...]
  Create a new project.
- :ref:`:ProjectList <projectlist>` - List current projects.
- :ref:`:ProjectSettings <projectsettings>` [<project>] -
  View / edit project settings.
- :ref:`:ProjectDelete <projectdelete>` <project> - Delete a project.
- :ref:`:ProjectRefresh <projectrefresh>` [<project> <project> ...] -
  Refresh supplied list of projects against the current files on disk.  If
  no project names supplied, refresh the current project.
- :ref:`:ProjectRefreshAll <projectrefreshall>` - Refresh all projects.
- :ref:`:ProjectInfo <projectinfo>` [<project>] -
  Echos info for the current or supplied project.
- :ref:`:ProjectOpen <projectopen>` <project> -
  Opens a project.
- :ref:`:ProjectClose <projectclose>` <project> - Closes a project.
- :ref:`:ProjectNatures <projectnatures>` [<project>] -
  View the configured natures for one or all projects.
- :ref:`:ProjectNatureAdd <projectnatureadd>` <project> [<nature> ...] -
  Add one or more natures to a project.
- :ref:`:ProjectNatureRemove <projectnatureremove>` <project> [<nature> ...] -
  Remove one or more natures from a project.
- :ref:`:ProjectCD <projectcd>` -
  Changes the global current working directory to the root directory of the
  current file's project (executes a :cd).
- :ref:`:ProjectLCD <projectlcd>` -
  Changes the current working directory of the current window to the root
  directory of the current file's project (executes a :lcd).
- :ref:`:ProjectTree <projecttree>` [<project> <project> ...] -
  Opens navigable a tree for one or more projects.
- :ref:`:ProjectsTree <projectstree>` - Opens a tree containing all projects.
- :ref:`:ProjectGrep <projectgrep>`
  /<pattern>/ file_pattern [file_pattern ...] -
  Issues a vimgrep starting at the root of the current project.
- :ref:`:ProjectGrepAdd <projectgrepadd>`
  /<pattern>/ file_pattern [file_pattern ...] -
  Issues a vimgrepadd starting at the root of the current project.
- :ref:`:ProjectLGrep <projectlgrep>`
  /<pattern>/ file_pattern [file_pattern ...] -
  Issues a lvimgrep starting at the root of the current project.
- :ref:`:ProjectLGrepAdd <projectlgrepadd>`
  /<pattern>/ file_pattern [file_pattern ...] -
  Issues a lvimgrepadd starting at the root of the current project.


Ant Commands
------------

- :ref:`:Ant <ant>` [<target> ...] - Execute ant from the current project.
- :ref:`:AntDoc <antdoc>` [<element>] -
  Find and open the documentation for the element under the cursor or the
  element supplied as an argument.
- :ref:`:Validate <validate>` - Validate the current ant build file.


Css Commands
-----------------

- :ref:`:Validate <validate>` - Validate the current file.


Dtd Commands
-----------------

- :ref:`:Validate <validate>` - Validate the current file.


Html Commands
-----------------

- :ref:`:Validate <validate>` - Validate the current file.
- :ref:`:BrowserOpen <browseropen>` -
  Opens the current file in your configured browser.


Ivy Commands
-----------------

- :ref:`:IvyRepo <ivyrepo>` <path> -
  Sets the necessary IVY_REPO classpath variable for supporting automatic
  updates to .classpath files upon ivy.xml updates.
- :ref:`:IvyDependencySearch <ivydependencysearch>` <artifact> -
  Searches online repository and opens a window with results that can be added
  to the current ivy file by hitting <Enter> on a result.  Available when
  editing an ``ivy.xml`` file.


Java Commands
-----------------

- :ref:`:JavaGet <javaget>` - Create a java bean getter method.
- :ref:`:JavaSet <javaset>` - Create a java bean setter method.
- :ref:`:JavaGetSet <javagetset>` -
  Create both a java bean getter and setter method.
- :ref:`:JavaConstructor <javaconstructor>` -
  Creates class constructor, either empty or based on selected class fields.
- :ref:`:JavaImpl <javaimpl>` -
  View implementable / overridable methods from super classes and implemented
  interfaces.
- :ref:`:JavaDelegate <javadelegate>` -
  View list of methods that delegate to the field under the cursor.
- :ref:`:JUnitImpl <junitimpl>` -
  Similar to **:JavaImpl**, but creates test methods.
- :ref:`:JUnitExecute <junitexecute>` - [testcase]
  Allows you to execute test cases in your favorite build tool.
- :ref:`:JUnitResult <junitresult>` - [testcase]
  Allows you to view the results of a test case.
- :ref:`:JavaImport <javaimport>` - Import the class under the cursor.
- :ref:`:JavaSearch <javasearch>`
  [-p <pattern>] [-t <type>] [-x <context>] [-s <scope>] -
  Search for classes, methods, fields, etc.  (With pattern supplied, searches
  for the element under the cursor).
- :ref:`:JavaSearchContext <javasearchcontext>` -
  Perform a context sensitive search for the element under the cursor.
- :ref:`:JavaCorrect <javacorrect>` -
  Suggest possible corrections for a source error.
- :ref:`:JavaRegex <javaregex>` -
  Opens a window for testing java regular expressions.
- :ref:`:JavaDocSearch <javadocsearch>` -
  Search for javadocs.  Same usage as **:JavaSearch**.
- :ref:`:JavaDocComment <javadoccomment>` -
  Adds or updates the comments for the element under the cursor.
- :ref:`:Jps <jps>` -
  Opens window with information about the currently running java processes.
- :ref:`:Validate <validate>` - Manually runs source code validation.


Java .classpath Commands
------------------------

- :ref:`:NewSrcEntry <newsrcentry>` <dir> [<dir> ...] -
  Add a new source dir entry.
- :ref:`:NewProjectEntry <newprojectentry>` <project> [<project> ...] -
  Add a new project entry.
- :ref:`:NewJarEntry <newjarentry>` <file> [<file> ...] -
  Add a jar entry.
- :ref:`:NewVarEntry <newvarentry>` <VAR/file> [<VAR/file> ...] -
  Add a new var entry.
- :ref:`:VariableList <variablelist>`
  List available classpath variables and their corresponding values.
- :ref:`:VariableCreate <variablecreate>` <name> <path> -
  Creates or updates the variable with the supplied name.
- :ref:`:VariableDelete <variabledelete>` <name> -
  Deletes the variable with the supplied name.


Javascript Commands
--------------------

- :ref:`:Validate <validate>` - Validate the current javascript file.


Log4j Commands
-----------------

- :ref:`:Validate <validate>` -
  Validate the current log4j xml configuration file.


Maven Commands
-----------------

- :ref:`:Maven <maven>` [<goal> ...] -
  Execute maven 1.x from the current project.
- :ref:`:Mvn <mvn>` [<goal> ...] -
  Execute maven 2.x from the current project.
- :ref:`:MavenRepo <mavenrepo>` -
  Sets the necessary MAVEN_REPO classpath variable for maven's (1.x) eclipse
  support.
- :ref:`:MvnRepo <mvnrepo>` -
  Sets the necessary M2_REPO classpath variable for maven's (2.x) eclipse
  support.
- :ref:`:MavenDependencySearch <mavendependencysearch>` <artifact> -
  Searches online repository and opens a window with results that can be
  added to the current project file by hitting <Enter> on a result.
  Available when editing a maven 1.x ``project.xml`` file.
- :ref:`:MvnDependencySearch <mvndependencysearch>` <artifact> -
  Searches online repository and opens a window with results that can be
  added to the current pom file by hitting <Enter> on a result.
  Available when editing a maven 2.x ``pom.xml`` file.


Php Commands
-----------------

- :ref:`:PhpSearch <phpsearch>` -p <pattern> -t <type> [-s <scope>]-
  Search for classes, methods, and constants.
- :ref:`:PhpFindDefinition <phpfinddefinition>`
  Find the element under the cursor.
- :ref:`:PhpSearchContext <phpsearchcontext>`
  Find the element under the cursor based on its context.
- :ref:`:Validate <validate>` - Manually runs source code validation.


Php .projectOption Commands
---------------------------

- :ref:`:NewLibEntry <newlibentry>` <file> [<file> ...] -
  Add a lib entry referencing an external folder.
- :ref:`:NewProjectEntry <newprojectentry>` <project> [<project> ...] -
  Add a new project entry.
- :ref:`:NewVarEntry <newvarentry>` <VAR/file> [<VAR/file> ...] -
  Add a new var entry.
- :ref:`:VariableList <variablelist>`
  List available include path variables and their corresponding values.
- :ref:`:VariableCreate <variablecreate>` <name> <path> -
  Creates or updates the variable with the supplied name.
- :ref:`:VariableDelete <variabledelete>` <name> -
  Deletes the variable with the supplied name.


Python Commands
-----------------

- :ref:`:Validate <validate>` -
  Validates the current file using <a href="site:pyflakes">pyflakes</a>.
- :ref:`:PyLint <pylint>` -
  Runs <a href="site:pyflakes">pylint</a> on the current file.
- :ref:`:PythonRegex <pythonregex>` -
  Opens a window for testing python regular expressions.
- :ref:`:DjangoManage <djangomanage>` -
  Invokes django's manage.py from any file in the same directory as your
  manage.py or in any of the child directories.
- :ref:`:DjangoFind <djangofind>` -
  Available when editing a django html template file.  Finds tag/filter
  definition, other template files, and static files.
- :ref:`:DjangoTemplateOpen <djangotemplateopen>` -
  Available when editing a python file.  Finds the template referenced under
  the cursor.
- :ref:`:DjangoViewOpen <djangoviewopen>` -
  Available when editing a python file.  When within a django url patterns
  definition, finds the view referenced under the cursor.
- :ref:`:DjangoContextOpen <djangocontextopen>` -
  Available when editing a python file.  Executes **:DjangoViewOpen**,
  **:DjangoTemplateOpen**, or **:PythonFindDefinition** depending on the
  context of the text under the cursor.


Vim Commands
-----------------

- :ref:`:FindCommandDef <findcommanddef>` [<command>] -
  Finds a command definition.
- :ref:`:FindCommandRef <findcommandref>` [<command>] -
  Finds references of a command.
- :ref:`:FindFunctionDef <findfunctiondef>` [<function>] -
  Finds a function definition.
- :ref:`:FindFunctionRef <findfunctionref>` [<function>] -
  Finds references of a function.
- :ref:`:FindVariableDef <findvariabledef>` [<variable>] -
  Finds the definition of a global variable.
- :ref:`:FindVariableRef <findvariableref>` [<variable>] -
  Finds references of a global variable.
- :ref:`:FindByContext <findbycontext>`
  Finds command, function, or variable based on the context of the element
  under the cursor.
- :ref:`:VimDoc <vimdoc>` [<keyword>] - Opens the vim help for a keyword.


WebXml Commands
-----------------

- :ref:`:Validate <validate>` - Validate the current web.xml file.


Wsdl Commands
-----------------

- :ref:`:Validate <validate>` - Validate the current file.


Xml Commands
-----------------

- :ref:`:DtdDefinition <dtddefinition>` [<element>] -
  Open the current xml file's dtd and jump to the element definition if
  supplied.
- :ref:`:XsdDefinition <xsddefinition>` [<element>] -
  Open the current xml file's xsd and jump to the element definition if
  supplied.
- :ref:`:Validate <validate>` [<file>] -
  Validates the supplied xml file or the current file if none supplied.
- :ref:`:XmlFormat <xmlformat>` Reformats the current xml file.


Xsd Commands
-----------------

- :ref:`:Validate <validate>` - Validate the current file.


Version Control Commands
-------------------------

.. note::

  Currently cvs, subversion, and mercurial are supported by the following
  commands where applicable.

- :ref:`:VcsAnnotate <vcsannotate>` -
  Toggles annotation of the currently versioned file using vim signs.
- :ref:`:VcsInfo <vcsinfo>` -
  Echos vcs info about the current versioned file.
- :ref:`:VcsLog <vcslog>` -
  Opens a buffer with log information for the current file.
- :ref:`:VcsChangeSet <vcschangeset>` [revision] -
  Opens a buffer with change set information for the supplied
  repository version or the current revision of the currently open file.
- :ref:`:VcsDiff <vcsdiff>` [revision] -
  Performs a vertical diffsplit of the current file against the last
  committed revision of the current file or the revision supplied.
- :ref:`:VcsCat <vcscat>` [revision] -
  Splits the current file with the contents of the last committed version
  of the current file or the supplied revision.
- :ref:`:VcsWebLog <vcsweblog>` -
  Opens the log for the currently versioned file in the configured vcs web app.
- :ref:`:VcsWebAnnotate <vcswebannotate>` [revision] -
  Opens the annotated view for the currently versioned file in the configured
  vcs web app.
- :ref:`:VcsWebChangeSet <vcswebchangeset>` [revision] -
  Opens the change set for the currently versioned file in the configured vcs
  web app.
- :ref:`:VcsWebDiff <vcswebdiff>` [revision, revision] -
  Opens a diff view for the currently versioned file in the configured in the
  configured vcs web app.


Web Lookup Commands
--------------------

- :ref:`:OpenUrl <openurl>` [url] - Opens a url in your configured web browser.
- :ref:`:Google <google>` [word ...] - Looks up a word or phrase with google.
- :ref:`:Clusty <clusty>` [word ...] - Looks up a word or phrase with clusty.
- :ref:`:Wikipedia <wikipedia>` [word ...] -
  Looks up a word or phrase on wikipedia.
- :ref:`:Dictionary <dictionary>` [word] -
  Looks up a word on dictionary.reference.com.
- :ref:`:Thesaurus <thesaurus>` [word] -
  Looks up a word on thesaurus.reference.com.


Misc. Commands
-----------------

- :ref:`:LocateFileEdit <locatefileedit>` [file] -
  Locates a relative file and opens it via :edit.
- :ref:`:LocateFileSplit <locatefilesplit>` [file] -
  Locates a relative file and opens it via :split.
- :ref:`:LocateFileTab <locatefiletab>` [file] -
  Locates a relative file and opens it via :tabnew.
- :ref:`:Split <split>` file [file ...] -
  Behaves like the 'split' command, but allows multiple files to be supplied.
- :ref:`:SplitRelative <splitrelative>` file [file ...] -
  Like **:Split** this command provides splitting of multiple files, but this
  command splits file relative to the file in the current buffer.
- :ref:`:Tabnew <tabnew>` file [file ...] -
  Behaves like **:Split**, but issues a :tabnew on each file.
- :ref:`:TabnewRelative <tabnewrelative>` - file [file ...] -
  Behaves like **:SplitRelative**, but issues a :tabnew on each file.
- :ref:`:EditRelative <editrelative>` file -
  Behaves like **:SplitRelative**, but issues an 'edit' and only supports one
  file at a time.
- :ref:`:ReadRelative <readrelative>` file -
  Behaves like **:SplitRelative**, but issues a 'read' and only supports one
  file at a time.
- :ref:`:ArgsRelative <argsrelative>` file_pattern [ file_pattern ...] -
  Behaves like **:SplitRelative**, but executes 'args'.
- :ref:`:ArgAddRelative <argaddrelative>` file_pattern [ file_pattern ...] -
  Behaves like **:SplitRelative**, but executes 'argadd'.
- :ref:`:VimgrepRelative <vimgreprelative>`
  /regex/ file_pattern [ file_pattern ...] -
  Executes :vimgrep relative to the current file.
- :ref:`:VimgrepAddRelative <vimgrepaddrelative>`
  /regex/ file_pattern [ file_pattern ...] -
  Executes :vimgrepadd relative to the current file.
- :ref:`:LvimgrepRelative <lvimgreprelative>`
  /regex/ file_pattern [ file_pattern ...] -
  Executes :lvimgrep relative to the current file.
- :ref:`:LvimgrepAddRelative <lvimgrepaddrelative>`
  /regex/ file_pattern [ file_pattern ...] -
  Executes :lvimgrepadd relative to the current file.
- :ref:`:CdRelative <cdrelative>` dir -
  Executes :cd relative to the current file.
- :ref:`:LcdRelative <lcdrelative>` dir -
  Executes :lcd relative to the current file.
- :ref:`:DiffLastSaved <difflastsaved>` -
  Performs a diffsplit with the last saved version of the currently modifed
  file.
- :ref:`:SwapWords <swapwords>` -
  Swaps two words (with cursor placed on the first word).  Supports swapping
  around non-word characters like commas, periods, etc.
- :ref:`:Sign <sign>` -
  Toggles adding or removing a vim sign on the current line.
- :ref:`:Signs <signs>` -
  Opens a new window containing a list of signs for the current buffer.
- :ref:`:SignClearUser <signclearuser>` - Removes all vim signs added via :Sign.
- :ref:`:SignClearAll <signclearall>` - Removes all vim signs.
- :ref:`:QuickFixClear <quickfixclear>` -
  Removes all entries from the quick fix window.
- :ref:`:LocationListClear <locationlistclear>` -
  Removes all entries from the location list window.
- :ref:`:MaximizeWindow <maximizewindow>` -
  Toggles maximization of the current window.
- :ref:`:MinimizeWindow <minimizewindow>` [winnr ...] -
  Minimizes the current window or the windows corresponding to the window
  numbers supplied.
- :ref:`:MinimizeRestore <minimizerestore>` - Restore all minimized windows.
- :ref:`:Buffers <buffers>` -
  Opens a temporary window with a list of all the currently listed
  buffers, allowing you to open or remove them.
- :ref:`:Only <only>` -
  Closes all but the current window and any windows excluded by
  **g:EclimOnlyExclude**.
