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
  - Added :ref:`:Only <only>` as
    a configurable alternative to vim's :only command.

Django:
  - Added :ref:`end tag completion <htmldjango>` support for django templates.

Vcs:
  - Removed **:VcsAnnotateOff** in favor of invoking
    **:VcsAnnotate** again to remove the annotations.
  - Added `vcs editor <vcseditor>` plugin which allows you to view diff of a
    file by hitting <enter> on a file name in the cvs, svn, or hg commit
    editor.

.. _1.3.5:

1.3.5 (Mar. 11, 2008)
---------------------

Bug Fixes:
  - Fixed exclusion of plugins not chosen by the user for installation.
  - Various bug fixes.

Eclim:
  - Added an :ref:`archive <vim/common/archive>` (jar, tar, etc.)
    viewer.

Html:
  - Updated html validator to validate <style> and <script> tag contents.

Vcs:
  - Added support for limiting the number of log entries returned by
    :ref:`:VcsLog <vcslog>` (limits to 50 entries by default).
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
  - Added :ref:`:ProjectInfo <projectinfo>`.
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
    :ref:`:ProjectNatures <projectnatures>`,
    :ref:`:ProjectNatureAdd <projectnatureadd>`, and
    :ref:`:ProjectNatureRemove <projectnatureremove>`.

Css:
  - Added :ref:`css validation <vim/css/validate>`.

Html:
  - Added :ref:`:BrowserOpen <browseropen>`

Html / Xml:
  - Added auto completion of end tags when typing '</'.
    This can be disabled by setting
    **g:EclimSgmlCompleteEndTag** to 0.

Java / Python:
  - :ref:`**:JavaRegex** <vim/java/regex>` and
    :ref:`**:PythonRegex** <vim/python/regex>`
    now support **b:eclim_regex_type** to determine if the
    regex should be applied to the whole sample text at once, or to each
    line individually.

Java:
  - Updated the :ref:`java logger <vim/java/logging>` functionality to support
    a custom logger template.

Javascript:
  - Added :ref:`javascript validation <vim/javascript/validate>` using
    <a href="site:jsl">jsl</a>.

Python:
  - Added basic :ref:`python validation <vim/python/validate>` using
    <a href="site:pyflakes">pyflakes</a> and the python compiler.
  - Added support for <a href="site:pylint">pylint</a> using new
    :ref:`:PyLint <pylint>` command.

Vcs:
  - Added
    :ref:`:VcsInfo <vcsinfo>`,
    :ViewvcAnnotate, :ViewvcChangeSet, and :ViewvcDiff.

Vcs (subversion):
  - Added
    :ref:`:VcsLog <vcslog>`,
    :ref:`:VcsChangeSet <vcschangeset>`,
    :ref:`:VcsDiff <vcsdiff>`, and
    :ref:`:VcsCat <vcscat>`.

Vim:
  - Added vim :ref:`window maximize and minimize <vim/common/maximize>`
    support.
  - Added an alternate implementation of
    <a href="vim/common/taglist.html#taglisttoo">taglist</a>.
  - Added command :ref:`:Buffers <buffers>`.
  - Added
    :ref:`VimgrepRelative <vimgreprelative>`,
    :ref:`VimgrepAddRelative <vimgrepaddrelative>`,
    :ref:`LvimgrepRelative <lvimgreprelative>`,
    :ref:`LvimgrepAddRelative <lvimgrepaddrelative>`,
    :ref:`CdRelative <cdrelative>`, and
    :ref:`LcdRelative <lcdrelative>`.

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
    :ref:`:ProjectRefresh <projectrefresh>` to ensure that eclipse and eclim
    are updated with the latest version of the files on disk.
  - :ref:`:ProjectCreate <projectcreate>` now supports optional -p argument for
    specifying the project name to use.
  - Created new command :ref:`:ProjectRefreshAll <projectrefreshall>` to
    support refreshing all projects at once, and modified
    :ref:`:ProjectRefresh <projectrefresh>` to only refresh the current project
    if no project names are supplied.
  - Added
    :ref:`:ProjectGrep <projectgrep>`,
    :ref:`:ProjectGrepAdd <projectgrepadd>`,
    :ref:`:ProjectLGrep <projectlgrep>`, and
    :ref:`:ProjectLGrepAdd <projectlgrepadd>`.
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
  - | Added :ref:`css code completion <vim/css/complete>`.
    | Requires the <a href="site:eclipse_wst">eclipse wst plugin</a>.

Dtd:
  - | Added :ref:`dtd validation <vim/dtd/validate>`.
    | Requires the <a href="site:eclipse_wst">eclipse wst plugin</a>.

Html:
  - | Added :ref:`html code completion <vim/html/complete>`.
    | Requires the <a href="site:eclipse_wst">eclipse wst plugin</a>.
  - | Added :ref:`html validation <vim/html/validate>`.
    | Requires the <a href="site:eclipse_wst">eclipse wst plugin</a>.

Log4j:
  - Added :ref:`log4j xml file validation <vim/java/log4j/validate>`.

Python:
  - Added support for :ref:`testing regular expressions <vim/python/regex>`.

Django:
  - Added
    :ref:`:DjangoManage <djangomanage>`,
    :ref:`:DjangoFind <djangofind>`,
    :ref:`:DjangoTemplateOpen <djangotemplateopen>`,
    :ref:`:DjangoViewOpen <djangoviewopen>`, and
    :ref:`:DjangoContextOpen <djangocontextopen>`.

WebXml:
  - Added :ref:`web.xml file validation <vim/java/webxml/validate>`.

Vim:
  - Added
    :ref:`:ArgsRelative <argsrelative>`,
    :ref:`:ArgAddRelative <argaddrelative>`,
    :ref:`:ReadRelative <readrelative>`.
  - Added
    :ref:`:Sign <sign>`,
    :ref:`:Signs <signs>`,
    :ref:`:SignClearUser <signclearuser>`,
    :ref:`:SignClearAll <signclearall>`.

Vcs:
  - Added
    :ref:`:VcsAnnotate <vcsannotate>` and :Viewvc.

Wsdl:
  - | Added :ref:`wsdl validation <vim/wsdl/validate>`.
    | Requires the <a href="site:eclipse_wst">eclipse wst plugin</a>.

Xsd:
  - | Added :ref:`xsd validation <vim/xsd/validate>`.
    | Requires the <a href="site:eclipse_wst">eclipse wst plugin</a>.

Xml:
  - | Added :ref:`xml code completion <vim/xml/complete>`.
    | Requires the <a href="site:eclipse_wst">eclipse wst plugin</a>.
