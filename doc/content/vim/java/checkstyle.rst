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

Checkstyle
==========

.. _\:Checkstyle:

When editing a java source file, eclim provides the command **:Checkstyle**
which will invoke `checkstyle`_ on the current file.

Additionally, you can configure vim to execute checkstyle automatically when
you save a java source file.  Simply set the vim variable
**g:EclimJavaCheckstyleOnSave** to 1 in your vimrc or java ftplugin.

.. code-block:: vim

  let g:EclimJavaCheckstyleOnSave = 1

Please note that both methods of invoking checkstyle require that you first
configure the location of your checkstyle config file using the eclim setting
**org.eclim.java.checkstyle.config**, described in the configuration section
below.

Configuration
-------------

Vim Settings

.. _g\:EclimJavaCheckstyleOnSave:

- **g:EclimJavaCheckstyleOnSave** (Default: 0) -
  When non-zero, enables running of checkstyle automatially upon saving of a
  java source file.

Eclim Settings

.. _org.eclim.java.checkstyle.config:

- **org.eclim.java.checkstyle.config** -
  Defines the location (project relative or absolute) or your checkstyle config
  file.

.. _org.eclim.java.checkstyle.properties:

- **org.eclim.java.checkstyle.properties** -
  Defines the location (project relative or absolute) or your checkstyle
  properties file.

.. _checkstyle: http://checkstyle.sourceforge.net/
