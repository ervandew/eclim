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

Xml
======

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

**:Validate** [<file>] -
Validate the supplied file or the current file if no file name provided.

If eclimd is not currently running, and the xmllint command is available,
eclim will validate the xml file using that.  Eclim will never use xmllint
when saving the file with g:EclimXmlValidate enabled.

.. _\:XmlFormat:

Format
------

On occasion you may encounter some xml content that is unformatted (like raw
content from a web service).

.. code-block:: xml

  <blah><foo>one</foo><bar>two</bar></blah>

Executing **:XmlFormat** will reformat the current xml file like so\:

.. code-block:: xml

  <blah>
    <foo>one</foo>
    <bar>two</bar>
  </blah>

Configuration
--------------

:doc:`Vim Settings </vim/settings>`

.. _g\:EclimXmlValidate:

- **g:EclimXmlValidate** (Defualt: 1) -
  If set to 0, disables xml validation when saving the file.

- **g:EclimValidateSortResults** (Default: 'occurrence') -
  If set to 'severity', the validation results will be sorted by severity
  (errors > warnings > info > etc.)
