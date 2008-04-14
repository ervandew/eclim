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

.. _vim/java/ant/validate:

Ant File Validation
===================

When editing an ant xml file eclim will default to validating the file when it
is written.  Any errors will be added to the current window's location list
(:help location-list) and their corresponding line number noted via Vim's sign
functionality.

Currently the Eclipse ant file validation isn't as robust as one might hope.  It
doesn't validate that element attributes are correct, that child elements are
valid, etc., but it does perform the following\:

- If a default target is specified, validate that it exists and that the target
  dependencies exist.
- Check for missing dependencies.
- Check for circular dependencies.

Eclim also combines the above validation with <a
href="../../xml/validate.html">xml validation</a> to validate that the ant file
is well formed.

If you do not want your ant files validated automatically when saved, you can
set the <a href="#EclimAntValidate">g:EclimAntValidate</a> variable described in
the configuration section below.

.. _Validate:

Whether or not auto validation has been enabled, eclim also exposes the command
**:Validate** to manually execute the validation of the ant file.


Configuration
-------------

Vim Variables

.. _EclimAntValidate:

- **g:EclimAntValidate** (Default: 1) -
  If set to 0, disables ant xml validation when saving the file.
