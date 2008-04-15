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

History of Changes
==================

.. _1.3.6:

1.3.6 (2008)
--------------------

License:
  - Eclim has switched from the Apache 2 license to the GPLv3.

Bug Fixes:
  - Fixed possible issue on Windows determining workspace for users not using
    the default location.
  - Fixed sign placement (used by all validation plugins) on non-english vims.
  - Various bug fixes.

Vim:
  - Added <a href="vim/common/util.html#Only">**:Only**</a> as
    a configurable alternative to vim's :only command.

Django:
  - Added <a href="vim/python/django.html#template">end tag completion</a>
    support for django templates.

Vcs:
  - Removed **:VcsAnnotateOff** in favor of invoking
    **:VcsAnnotate** again to remove the annotations.
  - Added <a href="vim/common/vcs.html#editor">vcs editor</a> plugin which
    allows you to view diff of a file by hitting <enter> on a file name
    in the cvs, svn, or hg commit editor.

.. _1.3.5:

1.3.5 (Mar. 11, 2008)
---------------------

Bug Fixes:
  - Fixed exclusion of plugins not chosen by the user for installation.
  - Various bug fixes.

Eclim:
  - Added an <a href="vim/common/archive.html">archive</a> (jar, tar, etc.)
    viewer.

Html:
  - Updated html validator to validate <style> and <script> tag contents.

Vcs:
  - Added support for limiting the number of log entries returned by
    <a href="vim/common/vcs.html#VcsLog">**:VcsLog**</a>
    (limits to 50 entries by default).
  - Updated **:VcsLog**, **:VcsChangeSet**, etc.
    to support cvs and hg where applicable.

Trac:
  - Added
    <a href="vim/common/trac.html#TracLog">**:TracLog**</a>,
    <a href="vim/common/trac.html#TracAnnotate">**:TracAnnotate**</a>,
    <a href="vim/common/trac.html#TracChangeSet">**:TracChangeSet**</a>, and
    <a href="vim/common/trac.html#TracDiff">**:TracDiff**</a>.

.. _1.3.4:

1.3.4 (Feb. 05, 2008)
---------------------

Bug Fixes:
  - Fixed **:JavaImpl** when adding multi-argument methods.
  - Various other bug fixes.

Eclim:
  - Added
    <a href="vim/common/project.html#ProjectInfo">**:ProjectInfo**</a>.
  - Added an eclim/after directory to vim's runtime path for any user scripts
    to be sourced after eclim.

Installer:
  - Updated installer to handle eclipse installs which have a local user
    install location for plugins.
  - Fixed some issues with running the installer on the icedtea jvm.

Php:
  - Added php support for
    <a href="vim/php/complete.html">code completion</a>,
    <a href="vim/php/search.html">searching</a>, and
    <a href="vim/php/validate.html">validation</a>.
    Requires the <a href="site:eclipse_pdt">eclipse pdt plugin</a>.

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
    <a href="vim/common/project.html#ProjectNatures">**:ProjectNatures**</a>,
    <a href="vim/common/project.html#ProjectNatureAdd">**:ProjectNatureAdd**</a>, and
    <a href="vim/common/project.html#ProjectNatureRemove">**:ProjectNatureRemove**</a>.

Css:
  - Added <a href="vim/css/validate.html">css validation</a>.

Html:
  - Added <a href="vim/html/util.html#BrowserOpen">**:BrowserOpen**</a>

Html / Xml:
  - Added auto completion of end tags when typing '</'.
    This can be disabled by setting
    **g:EclimSgmlCompleteEndTag** to 0.

Java / Python:
  - <a href="vim/java/regex.html">**:JavaRegex**</a> and
    <a href="vim/python/regex.html">**:PythonRegex**</a>
    now support **b:eclim_regex_type** to determine if the
    regex should be applied to the whole sample text at once, or to each
    line individually.

Java:
  - Updated the <a href="vim/java/logging.html">java logger</a> functionality
    to support a custom logger template.

Javascript:
  - Added <a href="vim/javascript/validate.html">javascript validation</a>
    using <a href="site:jsl">jsl</a>.

Python:
  - Added basic <a href="vim/python/validate.html">python validation</a>
    using <a href="site:pyflakes">pyflakes</a> and the python compiler.
  - Added support for <a href="site:pylint">pylint</a> using new
    <a href="vim/python/validate.html#PyLint">**:PyLint**</a>
    command.

Vcs:
  - Added
    <a href="vim/common/vcs.html#VcsInfo">**:VcsInfo**</a>,
    <a href="vim/common/vcs.html#ViewvcAnnotate">**:ViewvcAnnotate**</a>,
    <a href="vim/common/vcs.html#ViewvcChangeSet">**:ViewvcChangeSet**</a>, and
    <a href="vim/common/vcs.html#ViewvcDiff">**:ViewvcDiff**</a>.

Vcs (subversion):
  - Added
    <a href="vim/common/vcs.html#VcsLog">**:VcsLog**</a>,
    <a href="vim/common/vcs.html#VcsChangeSet">**:VcsChangeSet**</a>,
    <a href="vim/common/vcs.html#VcsDiff">**:VcsDiff**</a>, and
    <a href="vim/common/vcs.html#VcsCat">**:VcsCat**</a>.

Vim:
  - Added vim <a href="vim/common/maximize.html">window maximize and minimize</a>
    support.
  - Added an alternate implementation of
    <a href="vim/common/taglist.html#taglisttoo">taglist</a>.
  - Added command <a href="vim/common/util.html#Buffers">**:Buffers**</a>.
  - Added
    <a href="vim/common/util.html#VimgrepRelative">**VimgrepRelative**</a>,
    <a href="vim/common/util.html#VimgrepAddRelative">**VimgrepAddRelative**</a>,
    <a href="vim/common/util.html#LvimgrepRelative">**LvimgrepRelative**</a>,
    <a href="vim/common/util.html#LvimgrepAddRelative">**LvimgrepAddRelative**</a>,
    <a href="vim/common/util.html#CdRelative">**CdRelative**</a>, and
    <a href="vim/common/util.html#LcdRelative">**LcdRelative**</a>.

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
    changes, like a svn / cvs update, you should issue a
    <a href="vim/common/project.html#ProjectRefresh">**:ProjectRefresh**</a>
    to ensure that eclipse and eclim are updated with the latest version of
    the files on disk.
  - <a href="vim/common/project.html#ProjectCreate">**:ProjectCreate**</a>
    now supports optional -p argument for specifying the project name to
    use.
  - Created new command
    <a href="vim/common/project.html#ProjectRefreshAll">**:ProjectRefreshAll**</a>
    to support refreshing all projects at once, and modified
    <a href="vim/common/project.html#ProjectRefresh">**:ProjectRefresh**</a>
    to only refresh the current project if no project names are supplied.
  - Added
    <a href="vim/common/project.html#ProjectGrep">**:ProjectGrep**</a>,
    <a href="vim/common/project.html#ProjectGrepAdd">**:ProjectGrepAdd**</a>,
    <a href="vim/common/project.html#ProjectLGrep">**:ProjectLGrep**</a>, and
    <a href="vim/common/project.html#ProjectLGrepAdd">**:ProjectLGrepAdd**</a>.
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
  - | Added <a href="vim/css/complete.html">css code completion</a>.
    | Requires the <a href="site:eclipse_wst">eclipse wst plugin</a>.

Dtd:
  - | Added <a href="vim/dtd/validate.html">dtd validation</a>.
    | Requires the <a href="site:eclipse_wst">eclipse wst plugin</a>.

Html:
  - | Added <a href="vim/html/complete.html">html code completion</a>.
    | Requires the <a href="site:eclipse_wst">eclipse wst plugin</a>.
  - | Added <a href="vim/html/validate.html">html validation</a>.
    | Requires the <a href="site:eclipse_wst">eclipse wst plugin</a>.

Log4j:
  - Added
    <a href="vim/java/log4j/validate.html">log4j xml file validation</a>.

Python:
  - Added support for
    <a href="vim/python/regex.html">testing regular expressions</a>.

Django:
  - Added
    <a href="vim/python/django.html#DjangoManage">**:DjangoManage**</a>,
    <a href="vim/python/django.html#DjangoFind">**:DjangoFind**</a>,
    <a href="vim/python/django.html#DjangoTemplateOpen">**:DjangoTemplateOpen**</a>,
    <a href="vim/python/django.html#DjangoViewOpen">**:DjangoViewOpen**</a>, and
    <a href="vim/python/django.html#DjangoContextOpen">**:DjangoContextOpen**</a>.

WebXml:
  - Added
    <a href="vim/java/webxml/validate.html">web.xml file validation</a>.

Vim:
  - Added
    <a href="vim/common/util.html#ArgsRelative">**:ArgsRelative**</a>,
    <a href="vim/common/util.html#ArgAddRelative">**:ArgAddRelative**</a>,
    <a href="vim/common/util.html#ReadRelative">**:ReadRelative**</a>.
  - Added
    <a href="vim/common/util.html#Sign">**:Sign**</a>,
    <a href="vim/common/util.html#Signs">**:Signs**</a>,
    <a href="vim/common/util.html#SignClearUser">**:SignClearUser**</a>,
    <a href="vim/common/util.html#SignClearAll">**:SignClearAll**</a>.

Vcs:
  - Added
    <a href="vim/common/vcs.html#VcsAnnotate">**:VcsAnnotate**</a> and
    <a href="vim/common/vcs.html#Viewvc">**:Viewvc**</a>.

Wsdl:
  - | Added <a href="vim/wsdl/validate.html">wsdl validation</a>.
    | Requires the <a href="site:eclipse_wst">eclipse wst plugin</a>.

Xsd:
  - | Added <a href="vim/xsd/validate.html">xsd validation</a>.
    | Requires the <a href="site:eclipse_wst">eclipse wst plugin</a>.

Xml:
  - | Added <a href="vim/xml/complete.html">xml code completion</a>.
    | Requires the <a href="site:eclipse_wst">eclipse wst plugin</a>.
