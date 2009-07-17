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

.. _vim/java/validate:

.. _\:Validate_java:

Java Validation
===============

When saving a java source file that resides in a project, eclim will update that
source file in Eclipse and will report any validation errors found.  Any errors
will be placed in the current window's location list (:help location-list) and
the corresponding lines in the source file will be marked via Vim's :sign
functionality with '>>' markers in the left margin.

Automatic validation of java source files can be disabled via the
**g:EclimJavaSrcValidate** variable (described below).  If you choose to disable
automatic validation, you can still use the **:Validate** command to manually
validate the current file.


Configuration
-------------

Vim Variables

.. _g\:EclimJavaSrcValidate:

- **g:EclimJavaSrcValidate** (Default: 1) -
  If set to 0, disables source code validation.

- **g:EclimValidateSortResults** (Default: 'occurrence') -
  If set to 'severity', the validation results will be sorted by severity
  (errors > warnings > info > etc.)

Eclim settings

.. _org.eclipse.jdt.core.compiler.source:

- **org.eclipse.jdt.core.compiler.source** -
  Determines the target java vm version (1.2, 1.3, 1.4, 1.5).

.. _org.eclim.java.validation.ignore.warnings:

- **org.eclim.java.validation.ignore.warnings** -
  Determines if warnings are suppressed.
