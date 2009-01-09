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

.. _vim/java/logging:

Logging
=======

While editing a java source file, if you start to create a logging statement
(``log.`` or ``logger.``), the logging plugin will attempt to perform the
appropriate initialization (imports, static variable) for the configured logging
implementation.

Eclim provides a handful of templates for the most widely used implementations
(commons-logging, slf4j, log4j, and jdk).  However, if you happen to use an
alternate logging framework, or perhaps a home grown framework, eclim also
provides the means to supply a custom template.  To utilize it, simply set the
**org.eclim.java.logging.impl** setting to "custom" and add your template to
your vim files directory under ``eclim/resources/jdt/templates/logger.gst``.
Two variables will be supplied to your template: ``var``, which is the logger
instance variable, and ``class``, which is the class name of the current class
you are implementing.

Here is an example for the new eclim logger implementation\:

.. code-block:: java

  import org.eclim.logging.Logger;
  private static final Logger ${var} = Logger.getLogger(${class}.class);

After performing the necessary variable substitution, eclim will take any
imports and insert them amongst your existing import statements.  The remaining
code will be inserted after your class definition.


Configuration
-------------

Vim Variables

.. _g\:EclimLoggingDisabled:

- **g:EclimLoggingDisabled** (Default: 0) -
  If set to a value greater than 0, then this plugin will be disabled.

Eclim Settings

.. _org.eclim.java.logging.impl:

- **org.eclim.java.logging.impl** (Default: "commons-logging") -
  Determines which logging implementation to use.

  Possible values include "commons-logging", "slf4j", "log4j", "jdk", and
  "custom".

.. _org.eclim.java.logging.template:

- **org.eclim.java.logging.template** (Default: 'logger.gst') -
  Determines the name of the template to use for the custom logger.  The name
  must be a file name relative to eclim/resources/jdt/templates/.
