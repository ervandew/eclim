.. Copyright (C) 2005 - 2014  Eric Van Dewoestine

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

==========
Html / Css
==========

.. _html:

Html
======

Code Completion
---------------

Html code completion uses the standard
:doc:`Vim code completion mechanism </vim/code_completion>` like so\:

::

  <ht<Ctrl-X><Ctrl-U>

  <html>
    <he<Ctrl-X><Ctrl-U>

  <html>
    <head>
      <lin<Ctrl-X><Ctrl-U>

  <html>
    <head>
      <link ty<Ctrl-X><Ctrl-U>

  <html>
    <head>
      <link type
  ...

File Validation
---------------

When editing a html file eclim will default to validating the file when it is
written. Any errors will be added to the current window's location list (:help
location-list) and their corresponding line number noted via Vim's sign
functionality.

If you do not want your html files validated automatically when saved, you can
set the :ref:`g:EclimHtmlValidate` variable described in the configuration
section below.

.. _\:Validate_html:

Whether or not auto validation has been enabled, eclim also exposes
the command **:Validate** to manually execute the validation of the
file.

Utils
-----

When editing html files eclim provides some utilility commands for your
convience.

.. _\:BrowserOpen:

**:BrowserOpen** - Opens the current html file in your configured browser.

Configuration
-------------

:doc:`Vim Settings </vim/settings>`

.. _g\:EclimHtmlValidate:

- **g:EclimHtmlValidate** (Default: 1) -
  If set to 0, disables validation when saving the file.

  .. include:: /vim/validation.rst
     :start-after: begin-disable
     :end-before: end-disable

- **g:EclimValidateSortResults** (Default: 'occurrence') -
  If set to 'severity', the validation results will be sorted by severity
  (errors > warnings > info > etc.)

.. _css:

Css
======

Code Completion
---------------

Css code completion uses the standard
:doc:`Vim code completion mechanism </vim/code_completion>` like so\:

::

  bo<Ctrl-X><Ctrl-U>

  body {
    font-<Ctr-X><Ctrl-U>

  body {
    font-family: sa<Ctrl-X><Ctrl-U>

  body {
    font-family: sans-serif;
  ...

Validation
----------

When editing a css file eclim will default to validating the file when it is
written.  Any errors will be added to the current window's location list (:help
location-list) and their corresponding line number noted via Vim's sign
functionality.

If you do not want your css files validated automatically when saved, you can
set the :ref:`g:EclimCssValidate` variable described in the configuration
section below.

.. _\:Validate_css:

Whether or not auto validation has been enabled, eclim also exposes
the command **:Validate** to manually execute the validation of the
file.

Configuration
-------------

:doc:`Vim Settings </vim/settings>`

.. _g\:EclimCssValidate:

- **g:EclimCssValidate** (Default: 1) -
  If set to 0, disables validation when saving the file.

- **g:EclimValidateSortResults** (Default: 'occurrence') -
  If set to 'severity', the validation results will be sorted by severity
  (errors > warnings > info > etc.)
