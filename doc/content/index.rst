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

Welcome to Eclim
==================

.. rst-class:: lead

The power of Eclipse in your favorite editor.

.. toctree::
   :hidden:

   download
   gettingstarted
   gettinghelp
   guides/index
   documentation
   development/index
   features
   changes
   contribute
   relatedprojects

==================
What is it?
==================

Eclim provides the ability to access Eclipse_ features (code completion,
searching, code validation, and :doc:`many more </features>`) via the command
line or a local network connection, allowing those features to be integrated
with your favorite editor. Eclim provides an integration with Vim_, but
:doc:`third party clients </relatedprojects>` have been created to add eclim
support to other editors as well (emacs, sublime text 2, textmate).

There are three primary usage scenarios in which eclim is designed to be used:

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
   external vim instances would. This feature is only support on Windows and
   Unix systems (where gvim is compiled with the gtk gui).

Eclim is released under the GPLv3_.

========================
How do I get/install it?
========================

.. include:: /download.rst
   :start-after: begin-installer
   :end-before: end-installer

Installing
----------

Once you've downloaded the installer, you can follow the :doc:`eclim install
guide </guides/install>`.

================
How do I use it?
================

After you've installed eclim, you can refer to the :doc:`getting started
</gettingstarted>` page which will walk you through creating your first
project.

=====================
Where can I get help?
=====================

.. include:: /gettinghelp.rst
   :start-after: begin-help
   :end-before: end-help

======================
How do I report a bug?
======================

.. include:: /gettinghelp.rst
   :start-after: begin-report-bug
   :end-before: end-report-bug

===========
What's New?
===========

2012-08-07
-----------

| :ref:`Eclim 1.7.7 <1.7.7>` is now available.
| This is a bug fix release.

2012-06-07
-----------

| :ref:`Eclim 1.7.6 <1.7.6>` is now available.
| This is a minor bug fix release.

2012-06-03
-----------

| :ref:`Eclim 1.7.5 <1.7.5>` is now available.
| This is a minor release with an improved installer, some bug fixes, and a few
  minor enhancements.

2012-04-22
-----------

| :ref:`Eclim 1.7.4 <1.7.4>` is now available.
| This is a bug fix release.

2012-03-18
-----------

| :ref:`Eclim 1.7.3 <1.7.3>` is now available.
| This version fixes numerious small bugs and adds a handful of small features.

.. warning::

   Non vim users (emacs-eclim, subclim, etc.): The underlying command response
   format for eclim has changed, which means that any project relying on the
   old format isn't going to work. So if you are installing eclim for use with
   a client other than vim, then be sure to check with the client project to
   see if it has been updated for eclim 1.7.3 or later.

:doc:`/archive/news`

.. _eclipse: http://eclipse.org
.. _vim: http://www.vim.org
.. _gplv3: http://www.gnu.org/licenses/gpl-3.0-standalone.html
