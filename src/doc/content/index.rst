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

Welcome to Eclim
==================

.. toctree::

   download
   features

.. toctree::

   gettingstarted
   changes
   contribute

.. toctree::

   documentation
   guides/index
   translations/index
   development/index

.. _overview:

==================
Overview
==================

The primary goal of eclim is to bring Eclipse_ functionality to the Vim_
editor.  The initial goal was to provide Eclipse's java functionality in vim,
but support for various other languages (c/c++, php, python, ruby, css, html,
xml, etc.) have been added and several more are planned.

Eclim is less of an application and more of an integration of two great
projects.
The first, Vim, is `arguably <http://en.wikipedia.org/wiki/Editor_wars>`_
one of the best text editors in existence.  The second, Eclipse, provides many
great tools for development in various languages.  Each provides many features
that can increase developer productivity, but both still leave something to be
desired.  Vim lacks native Java support and many of the advanced features
available in Eclipse. Eclipse, on the other hand, still requires the use of the
mouse for many things, and when compared to Vim, provides a less than ideal
interface for editing text.

That is where eclim comes into play.  Instead of trying to write an IDE in Vim
or a Vim editor in Eclipse, eclim provides an Eclipse plug-in that exposes
Eclipse features through a server interface, and a set of Vim plug-ins that
communicate with Eclipse over that interface.  This functionality can be
leveraged in three primary ways, as illustrated below.

.. image:: images/diagrams/use_cases.png

#. The :ref:`first scenario <eclimd_headless>` is for those for which vim is
   their primary editing interface.  In this scenario you run a headless
   instance of eclipse which all vim instances can then communicate with to
   provide the various eclipse features.

#. The :ref:`second scenario <eclimd_headed>` is for those who prefer using vim
   as their main interface, but frequently end up jumping back to eclipse for
   any features not provided by eclim.  In this case you can run the eclim
   server inside of the eclipse gui and then interact with it via external vim
   instances just like the first scenario.

#. The :ref:`last scenario <gvim_embedded>` is for those who wish to use the
   eclipse interface full time, but want to use gvim as an embedded eclipse
   editor. Just like the previous use case, the eclim server is run inside of
   the eclipse gui and the embedded gvim will interact with it just like
   external vim instances would.

==================
Emacs Users
==================

Development of an `emacs client`_ for eclim has been started over on github.

.. _license:

==================
License
==================

Eclim is released under the GPLv3_.

.. _news:

==================
News
==================

-----------
2011-06-26
-----------

| :ref:`Eclim 1.7.0 <1.7.0>` is now available.
| The most notable changes are:

* Eclim has been upgraded to support Eclipse 3.7 (Indigo).

  .. note::

    Eclim now **requires** Eclipse 3.7.

-----------
2011-04-16
-----------

| :ref:`Eclim 1.6.3 <1.6.3>` is now available.
| This is primarily a bug fix release.

-----------
2011-02-26
-----------

| :ref:`Eclim 1.6.2 <1.6.2>` is now available.
| This is mostly a bug fix release, but please note that a handful of vim
  features have been broken out into separate projects and are no longer shipped
  with eclim.

-----------
2010-10-23
-----------

| :ref:`Eclim 1.6.1 <1.6.1>` is now available.
| This is mostly a bug fix release with a few minor features tossed in.

-----------
2010-08-01
-----------

| :ref:`Eclim 1.6.0 <1.6.0>` is now available.
| The most notable changes are:

* Eclim has been upgraded to support Eclipse 3.6 (Helios).

  .. note::

    Eclim now **requires** Eclipse 3.6.

-----------
2010-06-26
-----------

| :ref:`Eclim 1.5.8 <1.5.8>` is now available.
| This is a bug fix release for the installer as well as some php and ruby
  features.

-----------
2010-06-20
-----------

| :ref:`Eclim 1.5.7 <1.5.7>` is now available.
| The main focus of this release is bug fixes and improving the installer.

-----------
2010-03-06
-----------

| :ref:`Eclim 1.5.6 <1.5.6>` is now available.

-----------
2010-02-22
-----------

| :ref:`Eclim 1.5.5 <1.5.5>` is now available.
| This is a bug fix release for the eclim installer.

-----------
2009-12-18
-----------

| :ref:`Eclim 1.5.4 <1.5.4>` is now available.
| This is primarily a bug fix release for OSX users.

-----------
2009-12-12
-----------

| :ref:`Eclim 1.5.3 <1.5.3>` is now available.

-----------
2009-08-30
-----------

| :ref:`Eclim 1.5.2 <1.5.2>` is now available.

-----------
2009-07-18
-----------

| :ref:`Eclim 1.5.1 <1.5.1>` is now available.
| This is primarily a bug fix release

-----------
2009-07-12
-----------

| :ref:`Eclim 1.5.0 <1.5.0>` is now available.
| The most notable changes are:

* Eclim has been upgraded to support Eclipse 3.5 (Galileo).

  .. note::

    Eclim now **requires** Eclipse 3.5.

* Ruby support has been added using the `eclipse dltk`_.

-----------
2009-06-14
-----------

| :ref:`Eclim 1.4.9 <1.4.9>` is now available.
| This is primarily a bug fix release, with a few refinements.

-----------
2009-05-30
-----------

| :ref:`Eclim 1.4.8 <1.4.8>` is now available.
| This is primarily a bug fix release with a few enhancements.

-----------
2009-05-02
-----------

| :ref:`Eclim 1.4.7 <1.4.7>` is now available.
| This is a bug fix release which resolves an installation on unix based
  operating systems.

-----------
2009-05-02
-----------

| :ref:`Eclim 1.4.6 <1.4.6>` is now available.
| The major highlight of this release is support for c/c++ using the
  `eclipse cdt`_ plugin.

-----------
2009-04-04
-----------

| :ref:`Eclim 1.4.5 <1.4.5>` is now available.
| This is primarily a bug fix release.

-----------
2009-01-10
-----------

| :ref:`Eclim 1.4.4 <1.4.4>` is now available.
| Highlights of this release include:

- re-enabled php support
- added ability to run eclimd inside of eclipse gui
- added support for embedding gvim in eclipse

-----------
2008-11-15
-----------

| :ref:`Eclim 1.4.3 <1.4.3>` is now available.
| This release focuses on updating the installer to support ganymede's p2 for
  upgrading / installing external dependencies and adding additional python
  support.

-----------
2008-09-30
-----------

| :ref:`Eclim 1.4.2 <1.4.2>` is now available.
| This is primary a bug fix release.

-----------
2008-08-24
-----------

| :ref:`Eclim 1.4.1 <1.4.1>` is now available.
| This is primary a bug fix release, but there are some new features included
  as well.

-----------
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

-----------
2008-03-11
-----------

| :ref:`Eclim 1.3.5 <1.3.5>` is now available.
| You can view the :ref:`release notes <1.3.5>` for
  more info.

-----------
2008-02-05
-----------

| :ref:`Eclim 1.3.4 <1.3.4>` is now available.
| This release fixes a few minor bugs, improves the installer to account for
  eclipse installs with per user plugin locations, and adds php support.

-----------
2007-12-15
-----------

| :ref:`Eclim 1.3.3 <1.3.3>` is now available.
| This release fixes some installer issues.  If you have already installed
  1.3.2, then there is no need to upgrade to 1.3.3.

-----------
2007-12-04
-----------

| :ref:`Eclim 1.3.2 <1.3.2>` is now available.

-----------
2007-07-13
-----------

| :ref:`Eclim 1.3.1 <1.3.1>` is now available.
| This is only a bug fix release.

-----------
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


.. _eclipse: http://eclipse.org
.. _vim: http://vim.org
.. _nailgun: http://www.martiansoftware.com/nailgun/
.. _formic: http://github.com/ervandew/formic/
.. _gplv3: http://www.gnu.org/licenses/gpl-3.0-standalone.html

.. _emacs client: http://github.com/senny/emacs-eclim

.. _eclipse cdt: http://eclipse.org/cdt/
.. _eclipse dltk: http://eclipse.org/dltk/
.. _eclipse wst: http://eclipse.org/webtools/

.. _eclim_vim_1.2.3.jar: http://sourceforge.net/project/showfiles.php?group_id=145869&package_id=160492&release_id=453910
.. _eclim_vim_1.2.0.jar: http://sourceforge.net/project/showfiles.php?group_id=145869&package_id=160492&release_id=432538
