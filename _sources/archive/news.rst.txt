:orphan:

.. Copyright (C) 2005 - 2018  Eric Van Dewoestine

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

Eclim News Archive
==================

Jul 21, 2016
-------------

A new version of eclim is now available with support for Eclipse 4.6 (Neon).

- :ref:`Eclim 2.6.0 <2.6.0>`

Jul 25, 2015
-------------

A new version of eclim is now available with support for Eclipse 4.5 (Mars).

- :ref:`Eclim 2.5.0 <2.5.0>`

Jan 22, 2015
-------------

The latest version of eclim is now available with scala support re-enabled, new
groovy support, java debugging support, many bug fixes, and more.

- :ref:`Eclim 2.4.1 <2.4.1>`


Aug 24, 2014
-------------

Eclim has **finally** been released with Eclipse Luna support. Please note
however that scala support is disabled in this release. I tried waiting for a
final release of ScalaIDE 4.0.0 for Luna, but I don't want to hold up the rest
of eclim any longer. If you want to use eclim's scala support, you'll need to
install the ScalaIDE 4.0.0 milestone and build eclim from the master git branch.

- :ref:`Eclim 2.4.0 <2.4.0>`

May 07, 2014
-------------

Release of eclim for indigo users.

- :ref:`Eclim 1.7.19 <1.7.19>`

Apr. 12, 2014
-------------

Eclim has been updated to fix an issue on Windows that could prevent many
features from working.

- :ref:`Eclim 2.3.4 <2.3.4>`

Apr. 06, 2014
-------------

This release includes many bug fixes and refinements as well as a rewrite of
eclim's python support to utilize pydev instead of rope.

- :ref:`Eclim 2.3.3 <2.3.3>`

.. warning::

  Any exiting eclim python projects you have should be re-created with the new
  ``python`` nature:

  ::

    :ProjectCreate /path/to/project -n python

Sep. 12, 2013
-------------

This release fixes the extraction of the necessary vim files when installing
scala support.

- :ref:`Eclim 2.3.2 <2.3.2>` for Eclipse 4.3 (Kepler).
- :ref:`Eclim 1.7.18 <1.7.18>` for Eclipse 3.7/3.8 (Indigo).

Jul. 27, 2013
-------------

The previous eclim installer for Kepler was still pointing at the Juno update
site. This release remedies that.

- :ref:`Eclim 2.3.1 <2.3.1>` for Eclipse 4.3 (Kepler).

Jul. 21, 2013
-------------

The focus of this release is to bring eclim fully up to date with Eclipse Kepler
support. The installer for eclim 2.3.0 now requires that you install against
Kepler.

- :ref:`Eclim 2.3.0 <2.3.0>` for Eclipse 4.3 (Kepler).
- :ref:`Eclim 1.7.17 <1.7.17>` for Eclipse 3.7/3.8 (Indigo).

Jul. 14, 2013
-------------

This is primarily a bug fix release with a few new features. Unless some
critical error is found, this will be the last release targeting Juno. The next
release will likely target Kepler though this release should work fine on Kepler
as well, with the exception of scala support which has not been tested. Indigo
support will continue but will likely end with the release of Luna, possibly
sooner.

- :ref:`Eclim 2.2.7 <2.2.7>` for Eclipse 4.2 (Juno).
- :ref:`Eclim 1.7.16 <1.7.16>` for Eclipse 3.7/3.8 (Indigo).

May 18, 2013
-------------

Eclim has been updated to support the Android Development Toolkit version
22.0.0, scala is now supported for both Juno and Indigo, and there are a few
other improvements and many bug fixes.

- :ref:`Eclim 2.2.6 <2.2.6>` for Eclipse 4.2 (Juno).
- :ref:`Eclim 1.7.14 <1.7.14>` for Eclipse 3.7/3.8 (Indigo).

Nov. 25, 2012
-------------

The most notable change in this release is support for Eclipse 3.8 with the
Indigo release (1.7.13). Both releases also include several small bug fixes.

- :ref:`Eclim 2.2.5 <2.2.5>` for Eclipse 4.2 (Juno).
- :ref:`Eclim 1.7.13 <1.7.13>` for Eclipse 3.7/3.8 (Indigo).

Nov. 18, 2012
-------------

This is another bug fix release which includes support for the latest Android
development toolkit (21.0.0).

- :ref:`Eclim 2.2.4 <2.2.4>` for Eclipse 4.2 (Juno).
- :ref:`Eclim 1.7.12 <1.7.12>` for Eclipse 3.7 (Indigo).

Oct. 19, 2012
-------------

This is a bug fix release for Windows users which fixes executing of eclim
commands from vim:

- :ref:`Eclim 2.2.3 <2.2.3>` for Eclipse 4.2 (Juno).
- :ref:`Eclim 1.7.11 <1.7.11>` for Eclipse 3.7 (Indigo).

Oct. 07, 2012
-------------

Two new eclim updates are once again available with several bug fixes and
improvements.

- :ref:`Eclim 2.2.2 <2.2.2>` for Eclipse 4.2 (Juno).
- :ref:`Eclim 1.7.10 <1.7.10>` for Eclipse 3.7 (Indigo).

Sep. 09, 2012
-------------

| :ref:`Eclim 1.7.9 <1.7.9>` for Eclipse 3.7 (Indigo) is now available.
| This release adds initial support for :doc:`scala </vim/scala/index>`.

Please note that the `Scala IDE <http://scala-ide.org>`_ , which eclim uses to
provide scala support, is not yet available for Eclipse 4.2 (Juno), so eclim's
scala support will not be available for the eclim 2.2.x releases until sometime
after the Scala IDE has been updated for Juno.

Sep. 01, 2012
-------------

Another set of releases are now available for both Juno and Indigo. These both
include several bug fixes along with new support for creating android projects.

- :ref:`Eclim 2.2.1 <2.2.1>` for Eclipse 4.2 (Juno).
- :ref:`Eclim 1.7.8 <1.7.8>` for Eclipse 3.7 (Indigo).

Eclim also has a newly redesigned site using the
`sphinx bootstrap theme <https://github.com/ervandew/sphinx-bootstrap-theme>`_.

Aug. 07, 2012
-------------

Two new versions of eclim have been released, one for the latest Eclipse
version, Juno, the other a bug fix release for the previous version of Eclipse,
Indigo.

- :ref:`Eclim 2.2.0 <2.2.0>` for Eclipse 4.2 (Juno).
- :ref:`Eclim 1.7.7 <1.7.7>` for Eclipse 3.7 (Indigo).

Jun. 07, 2012
-------------

| :ref:`Eclim 1.7.6 <1.7.6>` is now available.
| This is a minor bug fix release.

Jun. 03, 2012
-------------

| :ref:`Eclim 1.7.5 <1.7.5>` is now available.
| This is a minor release with an improved installer, some bug fixes, and a few
  minor enhancements.

Apr. 22, 2012
-------------

| :ref:`Eclim 1.7.4 <1.7.4>` is now available.
| This is a bug fix release.

Mar. 18, 2012
-------------

| :ref:`Eclim 1.7.3 <1.7.3>` is now available.
| This version fixes numerious small bugs and adds a handful of small features.

.. warning::

   Non vim users (emacs-eclim, subclim, etc.): The underlying command response
   format for eclim has changed, which means that any project relying on the
   old format isn't going to work. So if you are installing eclim for use with
   a client other than vim, then be sure to check with the client project to
   see if it has been updated for eclim 1.7.3 or later.

2011-09-10
-----------

| :ref:`Eclim 1.7.2 <1.7.2>` is now available.
| This version fixes running the installer with java 7 as well as several other
  small bug fixes and improvements.

I'd also like to announce the #eclim channel on freenode.

2011-07-02
-----------

| :ref:`Eclim 1.7.1 <1.7.1>` is now available.
| This is a bug fix release.

2011-06-26
-----------

| :ref:`Eclim 1.7.0 <1.7.0>` is now available.
| The most notable changes are:

* Eclim has been upgraded to support Eclipse 3.7 (Indigo).

  .. note::

    Eclim now **requires** Eclipse 3.7.

2011-04-16
-----------

| :ref:`Eclim 1.6.3 <1.6.3>` is now available.
| This is primarily a bug fix release.

2011-02-26
-----------

| :ref:`Eclim 1.6.2 <1.6.2>` is now available.
| This is mostly a bug fix release, but please note that a handful of vim
  features have been broken out into separate projects and are no longer shipped
  with eclim.

2010-10-23
-----------

| :ref:`Eclim 1.6.1 <1.6.1>` is now available.
| This is mostly a bug fix release with a few minor features tossed in.

2010-08-01
-----------

| :ref:`Eclim 1.6.0 <1.6.0>` is now available.
| The most notable changes are:

* Eclim has been upgraded to support Eclipse 3.6 (Helios).

  .. note::

    Eclim now **requires** Eclipse 3.6.

2010-06-26
-----------

| :ref:`Eclim 1.5.8 <1.5.8>` is now available.
| This is a bug fix release for the installer as well as some php and ruby
  features.

2010-06-20
-----------

| :ref:`Eclim 1.5.7 <1.5.7>` is now available.
| The main focus of this release is bug fixes and improving the installer.

2010-03-06
-----------

| :ref:`Eclim 1.5.6 <1.5.6>` is now available.

2010-02-22
-----------

| :ref:`Eclim 1.5.5 <1.5.5>` is now available.
| This is a bug fix release for the eclim installer.

2009-12-18
-----------

| :ref:`Eclim 1.5.4 <1.5.4>` is now available.
| This is primarily a bug fix release for OSX users.

2009-12-12
-----------

| :ref:`Eclim 1.5.3 <1.5.3>` is now available.

2009-08-30
-----------

| :ref:`Eclim 1.5.2 <1.5.2>` is now available.

2009-07-18
-----------

| :ref:`Eclim 1.5.1 <1.5.1>` is now available.
| This is primarily a bug fix release

2009-07-12
-----------

| :ref:`Eclim 1.5.0 <1.5.0>` is now available.
| The most notable changes are:

* Eclim has been upgraded to support Eclipse 3.5 (Galileo).

  .. note::

    Eclim now **requires** Eclipse 3.5.

* Ruby support has been added using the `eclipse dltk`_.

2009-06-14
-----------

| :ref:`Eclim 1.4.9 <1.4.9>` is now available.
| This is primarily a bug fix release, with a few refinements.

2009-05-30
-----------

| :ref:`Eclim 1.4.8 <1.4.8>` is now available.
| This is primarily a bug fix release with a few enhancements.

2009-05-02
-----------

| :ref:`Eclim 1.4.7 <1.4.7>` is now available.
| This is a bug fix release which resolves an installation on unix based
  operating systems.

2009-05-02
-----------

| :ref:`Eclim 1.4.6 <1.4.6>` is now available.
| The major highlight of this release is support for c/c++ using the
  `eclipse cdt`_ plugin.

2009-04-04
-----------

| :ref:`Eclim 1.4.5 <1.4.5>` is now available.
| This is primarily a bug fix release.

2009-01-10
-----------

| :ref:`Eclim 1.4.4 <1.4.4>` is now available.
| Highlights of this release include:

- re-enabled php support
- added ability to run eclimd inside of eclipse gui
- added support for embedding gvim in eclipse

2008-11-15
-----------

| :ref:`Eclim 1.4.3 <1.4.3>` is now available.
| This release focuses on updating the installer to support ganymede's p2 for
  upgrading / installing external dependencies and adding additional python
  support.

2008-09-30
-----------

| :ref:`Eclim 1.4.2 <1.4.2>` is now available.
| This is primary a bug fix release.

2008-08-24
-----------

| :ref:`Eclim 1.4.1 <1.4.1>` is now available.
| This is primary a bug fix release, but there are some new features included
  as well.

2008-07-27
-----------

| :ref:`Eclim 1.4.0 <1.4.0>` is now available.
| Please note that eclim now requires the latest version of `eclipse`_
  (Ganymede, 3.4.x).

Also note that the eclipse pdt plugin which serves as the base for eclim's php
support has not yet been released for the latest version of eclipse.  For this
reason php support has been temporarily removed from this release and will
hopefully return soon after the pdt team release a Ganymede (3.4) compatible
version.

Another major change worth noting, is that eclim is now licensed under the
GPLv3.  This was done to give eclim the freedom to integrate with other GPL
projects in the future.

2008-03-11
-----------

| :ref:`Eclim 1.3.5 <1.3.5>` is now available.
| You can view the :ref:`release notes <1.3.5>` for
  more info.

2008-02-05
-----------

| :ref:`Eclim 1.3.4 <1.3.4>` is now available.
| This release fixes a few minor bugs, improves the installer to account for
  eclipse installs with per user plugin locations, and adds php support.

2007-12-15
-----------

| :ref:`Eclim 1.3.3 <1.3.3>` is now available.
| This release fixes some installer issues.  If you have already installed
  1.3.2, then there is no need to upgrade to 1.3.3.

2007-12-04
-----------

| :ref:`Eclim 1.3.2 <1.3.2>` is now available.

2007-07-13
-----------

| :ref:`Eclim 1.3.1 <1.3.1>` is now available.
| This is only a bug fix release.

2007-07-01
-----------

| :ref:`Eclim 1.3.0 <1.3.0>` is now available.
| The most notable changes are:

* Eclim has been upgraded to support Eclipse 3.3.

  .. note::

    Eclim now **requires** Eclipse 3.3 and JDK 1.5.

* A new :ref:`graphical installer <installer>` built on the formic_ installer
  framework.

* New functionality based on and requiring the `eclipse wst`_.

* Many more :ref:`changes <1.3.0>`.

2006-10-09
-----------

**All Users**:  A bug made its way into the initial
1.2.3 release which prevents you from adding methods via **:JavaImpl**.

An updated eclim_vim_1.2.3.jar is now available to resolve this issue.  If
you downloaded this file on October 8th or 9th you can either download the
updated version or execute the following within vim:

.. code-block:: vim

  :PatchEclim eclim/autoload/eclim/util.vim 1.27

2006-10-08
-----------

| **Eclim 1.2.3** is now available.
| This is primarily a bug fix release.

Please view the :ref:`release notes <1.2.3>` for more info.

2006-09-08
-----------

| **Eclim 1.2.2** is now available.
| The previous release introduced two new bugs that managed to slip through the
  cracks.  These have now been fixed including a third that had been around for
  some time but went previously unnoticed.

To see a list of fixes you may view the :ref:`release notes <1.2.2>`.

2006-09-07
-----------

| **Eclim 1.2.1** is now available.
| This is primarily a bug fix release, but some new functionality has been
  added as well. This release should resolve all known issues.

To see a list of what's new / changed, be sure to take a look at the
:ref:`release notes <1.2.1>`.

2006-07-17
-----------

**Windows Users**:  Eclim 1.2.0 contained a couple issues that can potentially
prevent eclim from functioning.  A new version of eclim_vim_1.2.0.jar is now
available, which fixes these issues.

Simply download the new file and extract it as explained in the
<a href="guides/install.html#step3">installation guide</a>.  There is
no need to download or re-install the Eclipse plugins.

If any other issues are encountered please report them.

2006-07-16
-----------

| **Eclim 1.2.0** is now available.
| This release requires `Eclipse 3.2 <http://eclipse.org/downloads>`_.

To see a list of what's new / changed, be sure to take a look at the
:ref:`release notes <1.2.0>`.

.. warning::
  The layout of eclim plugins within the Vim runtimepath has changed.  Please
  read the <a href="changes.html#upgrade_1.2.0">details</a> in the release
  notes and take the appropriate action prior to upgrading.

2006-05-07
-----------

| **Eclim 1.1.2** is now available.
| Before upgrading, you should upgrade your Vim installation to the stable
  release of Vim 7.0 or greater.

To see a list of what's new / changed, be sure to take a look at the
:ref:`release notes <1.1.2>`.

2006-02-19
-----------

| New version of eclim (1.1.1) is now available.
| This is mostly a bug fix release will a few new additions.
| Please note, that this latest version requires Eclipse version 3.1.2 for some
  bug fixes and improvements.

To see a list of what's new / changed, be sure to take a look at the
:ref:`release notes <1.1.1>`.

2005-12-26
-----------

| New version of eclim (1.1.0) is now available.
| All questions, issues, suggestions are welcome and encouraged.

To see a list of what's new / changed, be sure to take a look at the
:ref:`release notes <1.1.0>`.

2005-10-16
-----------

The first eclim release (1.0.0) is now available.
All questions, issues, suggestions are welcome and encouraged.

Be sure to read the docs to see what features are currently available,
and take a look at the <a href="todo.html">todo</a> to see what's
coming in future releases.

2005-09-11
-----------

Several new additions over the past couple weeks:

* Java code completion: Integrated into Vim via Vim 7's new "User Defined
  Completion".

* Added eclim command line support for creating and updating projects,
  including Vim support for editing Eclipse .classpath files and updating
  Eclipse upon writing of those files.

* Integrated nailgun_ to greatly improve the command line client performance.

* Started documenting eclim and its features.

With the addition of these features I'm going to stop adding new
functionality for the time being and focus on testing and ensuring that
everything works as expected on Windows.

2005-08-21
-----------

Code navigation / searching is done!  Most of the Vim integration for
searching is done as well.  The only thing missing is viewing code for
results that are found in a jar file that have no corresponding source
attachment.  I may end up doing what Eclipse appears to do, which is
to use javap to display the class and method signatures.  That or I'll
use jad to decompile the whole source.  My only issue with jad, is
that it is not up to date with the 1.5 byte code.

I also have automated importing done as well.  The eclim server
request just returns a list of possible results to import for a given
request and the editor (Vim in this case) handles prompting the user
and updating the code.

.. note::
  The Vim integration now requires Vim 7.  Even though Vim 7 is still
  alpha, I haven't had any major issues with it and the new additions to
  the Vim scripting language are just too good to pass up.

My next step is to start documenting everything and testing on a
Windows environment to ensure there aren't any compatibility issues.

After that I should be ready to put out a preliminary release.
I'm trying to be very careful about releasing anything too soon.  The
last thing I want it to scare anyone off with a broken project that
doesn't seem to work properly.

2005-08-11
-----------

Sourceforge site is up!  Now it's just a matter of getting the ball rolling
again.

I'm hoping to have source code navigation working by the end of next week.
This includes the ability to simply hit <enter> on a class name, method
name, method call, etc. to jump to its declaration.  Basically I want to
replace my previous
`Vim plug-in <http://www.vim.org/scripts/script.php?script_id=1106>`_ with the
new Eclipse one.

Before I put out any releases though, I want to have a comprehensive
set of documentation.  For the first few releases, setup will probably
be pretty manual, with most of it occurring through the Eclipse
interface.  Going forward, I want to move more of that functionality
into Vim.

.. _eclipse: http://eclipse.org
.. _eclipse cdt: http://eclipse.org/cdt/
.. _eclipse dltk: http://eclipse.org/dltk/
.. _eclipse wst: http://eclipse.org/webtools/
.. _formic: http://github.com/ervandew/formic/
.. _nailgun: http://www.martiansoftware.com/nailgun/
