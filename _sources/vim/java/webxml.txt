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

WEB-INF/web.xml
===============

Validation
----------

When editing a web.xml file eclim will default to validating the file when it is
written.  Any errors will be added to the current window's location list (:help
location-list) and their corresponding line number noted via Vim's sign
functionality.

Eclim also combines the above validation with :ref:`xml validation
<xml-validation>` to validate that the file is well formed.

If you do not want your web.xml files validated automatically when saved, you
can set the :ref:`g:EclimWebXmlValidate` variable described in the
configuration section below.

.. _\:Validate_webxml:

Whether or not auto validation has been enabled, eclim also exposes the command
**:Validate** to manually execute the validation of the file.

Configuration
-------------

:doc:`Vim Settings </vim/settings>`

.. _g\:EclimWebXmlValidate:

- **g:EclimWebXmlValidate** (Default: 1) -
  If set to 0, disables validation when saving the file.
