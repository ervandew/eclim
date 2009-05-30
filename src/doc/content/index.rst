.. Copyright (C) 2005 - 2009  Eric Van Dewoestine

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

   features

.. toctree::

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
but support for various other languages (c/c++, php, python, css, html, xml,
etc.) have been added and several more are planned.

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
2009-05-30
-----------

| **Eclim 1.4.8** is now available.
| This is primarily a bug fix release with a few enhancements.

You can view the :ref:`release notes <1.4.8>` for more info.

-----------
2009-05-02
-----------

| **Eclim 1.4.7** is now available.
| This is a bug fix release which resolves an installation on unix based
  operating systems.

-----------
2009-05-02
-----------

| **Eclim 1.4.6** is now available.
| The major highlight of this release is support for c/c++ using the
  `eclipse cdt`_ plugin.

You can view the :ref:`release notes <1.4.6>` for more info.

-----------
2009-04-04
-----------

| **Eclim 1.4.5** is now available.
| This is primarily a bug fix release.

You can view the :ref:`release notes <1.4.5>` for more info.

-----------
2009-01-10
-----------

| **Eclim 1.4.4** is now available.
| Highlights of this release include:

- re-enabled php support
- added ability to run eclimd inside of eclipse gui
- added support for embedding gvim in eclipse

You can view the :ref:`release notes <1.4.4>` for more info.

-----------
2008-11-15
-----------

| **Eclim 1.4.3** is now available.
| This release focuses on updating the installer to support ganymede's p2 for
  upgrading / installing external dependencies and adding additional python
  support.
| You can view the :ref:`release notes <1.4.3>` for more info.

-----------
2008-09-30
-----------

| **Eclim 1.4.2** is now available.
| This is primary a bug fix release.
| You can view the :ref:`release notes <1.4.2>` for more info.

-----------
2008-08-24
-----------

| **Eclim 1.4.1** is now available.
| This is primary a bug fix release, but there are some new features included
  as well.
| You can view the :ref:`release notes <1.4.1>` for more info.

-----------
2008-07-27
-----------

| **Eclim 1.4.0** is now available.
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

You can view the :ref:`release notes <1.4.0>` for more info.

-----------
2008-03-11
-----------

| **Eclim 1.3.5** is now available.
| You can view the :ref:`release notes <1.3.5>` for
  more info.

-----------
2008-02-05
-----------

| **Eclim 1.3.4** is now available.
| This release fixes a few minor bugs, improves the installer to account for
  eclipse installs with per user plugin locations, and adds php support.
| You can view the :ref:`release notes <1.3.4>` for more info.

-----------
2007-12-15
-----------

| **Eclim 1.3.3** is now available.
| This release fixes some installer issues.  If you have already installed
  1.3.2, then there is no need to upgrade to 1.3.3.

-----------
2007-12-04
-----------

| **Eclim 1.3.2** is now available.
| You can view the :ref:`release notes <1.3.2>` for more info.

-----------
2007-07-13
-----------

| **Eclim 1.3.1** is now available.
| This is only a bug fix release.
| You can view the :ref:`release notes <1.3.1>` for more info.

-----------
2007-07-01
-----------

| **Eclim 1.3.0** is now available.
| The most notable changes are:

* Eclim has been upgraded to support Eclipse 3.3.

  .. note::

    Eclim now **requires** Eclipse 3.3 and JDK 1.5.

* A new :ref:`graphical installer <installer>` built on the formic_ installer
  framework.

* New functionality based on and requiring the `eclipse wst`_.

* Many more :ref:`changes <1.3.0>`.

View the :ref:`release notes <1.3.0>` for more info.

.. _eclipse: http://eclipse.org
.. _vim: http://vim.org
.. _nailgun: http://www.martiansoftware.com/nailgun/
.. _formic: http://sourceforge.net/projects/formic/
.. _gplv3: http://www.gnu.org/licenses/gpl-3.0-standalone.html

.. _eclipse cdt: http://eclipse.org/cdt/
.. _eclipse wst: http://eclipse.org/webtools/

.. _downloads: http://sourceforge.net/project/showfiles.php?group_id=145869
.. _eclim_vim_1.2.3.jar: http://sourceforge.net/project/showfiles.php?group_id=145869&package_id=160492&release_id=453910
.. _eclim_vim_1.2.0.jar: http://sourceforge.net/project/showfiles.php?group_id=145869&package_id=160492&release_id=432538
