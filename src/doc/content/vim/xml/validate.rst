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

.. _vim/xml/validate:

Xml Validation
==============

When editing a xml file eclim will default to validating the file when it is
written.  Any errors will be added to the current window's location list (:help
location-list) and their corresponding line number noted via Vim's sign
functionality.

If you don't want xml files validated when saving them, you can set the
g:EclimXmlValidate variable described in the configuration section below.

Regardless of whether you have validation enabled upon saving or not, the
following command is still available for validating xml files on demand.

.. _Validate:

**:Validate** [<file>] -
Validate the supplied file or the current file if no file name provided.

If eclimd is not currently running, and the xmllint command is available,
eclim will validate the xml file using that.  Eclim will never use xmllint
when saving the file with g:EclimXmlValidate enabled.


Configuration
--------------

Vim Variables

.. _EclimXmlValidate:

- **g:EclimXmlValidate** (Defualt: 1) -
  If set to 0, disables xml validation when saving the file.
