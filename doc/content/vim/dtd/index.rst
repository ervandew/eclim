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
