.. Copyright (C) 2005 - 2020  Eric Van Dewoestine

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

================
Xml / Dtd / Xsd
================

.. _xml:

Xml
======

.. note::

  If you have xml files that do not have a .xml extension then eclipse may not
  recognize it as an xml file resulting in validation, completion, etc not
  working. Although the vim side may have the correct file type set, you may
  still need to add the file's extension to the list of xml content types in the
  eclipse gui:

  :menuselection:`Preferences --> General --> Content Types --> Text --> XML`

Code Completion
---------------

Xml code completion uses the standard
:doc:`Vim code completion mechanism </vim/code_completion>` like so\:

::

  <ser<Ctrl-X><Ctrl-U>

  <servlet>
    <ser<Ctrl-X><Ctrl-U>

  <servlet>
    <servlet-name>
  ...


.. note::

  Requires a valid dtd or xsd to determine possible completions.

Definition Lookup
-----------------

When editing xml files, eclim provides a couple commands which allow you to
quickly and easily open the file's data definition and optionally jump to the
definition of a particular element.

.. note::

  When opening urls, these commands rely on netrw (:help netrw).

.. _\:DtdDefinition:

- **:DtdDefinition** [<element>] -
  When invoked, this command will attempt to locate the dtd declaration in the
  current xml file and open the dtd in a new split window.  If you supply an
  element name when invoking the command, it will attempt to locate and jump to
  the definition of that element within the dtd.  If no element name is
  supplied, but the cursor is located on an element name when invoke, that
  element name will be used.

.. _\:XsdDefinition:

- **:XsdDefinition** [<element>] -
  Behaves like **:DtdDefinition** except this command locates and opens the
  corresponding schema definition file.

.. _xml-validation:

Validation
----------

When editing a xml file eclim will default to validating the file when it is
written.  Any errors will be added to the current window's location list (:help
location-list) and their corresponding line number noted via Vim's sign
functionality.

If you don't want xml files validated when saving them, you can set the
g:EclimXmlValidate variable described in the configuration section below.

Regardless of whether you have validation enabled upon saving or not, the
following command is still available for validating xml files on demand.

.. _\:Validate_xml:

**:Validate** -
Validate the the current file.

If eclimd is not currently running, and the xmllint command is available,
eclim will validate the xml file using that.  Eclim will never use xmllint
when saving the file with g:EclimXmlValidate enabled.

Configuration
--------------

:doc:`Vim Settings </vim/settings>`

.. _g\:EclimXmlValidate:

- **g:EclimXmlValidate** (Defualt: 1) -
  If set to 0, disables xml validation when saving the file.

- **g:EclimValidateSortResults** (Default: 'occurrence') -
  If set to 'severity', the validation results will be sorted by severity
  (errors > warnings > info > etc.)

.. _dtd:

Dtd
======

Validation
----------

When editing a dtd file eclim will default to validating the file when it is
written. Any errors will be added to the current window's location list (:help
location-list) and their corresponding line number noted via Vim's sign
functionality.

If you do not want your dtd files validated automatically when saved, you can
set the :ref:`g:EclimDtdValidate` variable described in the configuration
section below.

.. _\:Validate_dtd:

Whether or not auto validation has been enabled, eclim also exposes
the command **:Validate** to manually execute the validation of the
file.

Configuration
-------------

:doc:`Vim Settings </vim/settings>`

.. _g\:EclimDtdValidate:

- **g:EclimDtdValidate** (Default: 1) -
  If set to 0, disables validation when saving the file.

- **g:EclimValidateSortResults** (Default: 'occurrence') -
  If set to 'severity', the validation results will be sorted by severity
  (errors > warnings > info > etc.)

.. _xsd:

Xsd
======

Validation
----------

When editing a xsd file eclim will default to validating the file when it is
written.  Any errors will be added to the current window's location list (:help
location-list) and their corresponding line number noted via Vim's sign
functionality.

If you do not want your xsd files validated automatically when saved, you can
set the :ref:`g:EclimXsdValidate` variable described in the configuration
section below.

.. _\:Validate_xsd:

Whether or not auto validation has been enabled, eclim also exposes
the command **:Validate** to manually execute the validation of the
file.

Configuration
--------------

:doc:`Vim Settings </vim/settings>`

.. _g\:EclimXsdValidate:

- **g:EclimXsdValidate** (Default: 1) -
  If set to 0, disables validation when saving the file.

- **g:EclimValidateSortResults** (Default: 'occurrence') -
  If set to 'severity', the validation results will be sorted by severity
  (errors > warnings > info > etc.)
