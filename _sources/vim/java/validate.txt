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

.. _\:Validate_java:

Java Validation / Correction
============================

Validation
----------

When saving a java source file that resides in a project, eclim will update that
source file in Eclipse and will report any validation errors found.  Any errors
will be placed in the current window's location list (:help location-list) and
the corresponding lines in the source file will be marked via Vim's :sign
functionality with '>>' markers in the left margin.

Automatic validation of java source files can be disabled via the
**g:EclimJavaValidate** variable (described below).  If you choose to disable
automatic validation, you can still use the **:Validate** command to manually
validate the current file.

Configuration
^^^^^^^^^^^^^

:doc:`Vim Settings </vim/settings>`

.. _g\:EclimJavaValidate:

- **g:EclimJavaValidate** (Default: 1) -
  If set to 0, disables source code validation.

  .. include:: /vim/validation.rst
     :start-after: begin-disable
     :end-before: end-disable

- **g:EclimValidateSortResults** (Default: 'occurrence') -
  If set to 'severity', the validation results will be sorted by severity
  (errors > warnings > info > etc.)

Eclim settings

.. _org.eclipse.jdt.core.compiler.source:

- **org.eclipse.jdt.core.compiler.source** -
  Determines the target java vm version (1.2, 1.3, 1.4, 1.5).

.. _\:JavaCorrect:

Code Correction
---------------

Code correction in eclim is equivalent to the quick fix functionality of
Eclipse. When you save a java source file, eclim
:doc:`validates </vim/java/validate>` the file and notes which lines contain
errors. To have eclim suggest possible corrections for an error, you simply
place the cursor on the error line and issue **:JavaCorrect**.

The result will be a small window opened at the bottom of Vim where any
correction proposals will be noted. To apply a suggested change, simply move the
cursor to the line describing the modification and hit <enter>. Upon doing so,
the change will be applied to the source file.

Example output of **:JavaCorrect**.

::

  The serializable class Foo does not declare a static final serialVersionUID field of type long
  0.1227:  Add @SuppressWarnings 'serial' to 'Foo'
    ...
    @SuppressWarnings("serial")
    public class Foo
    implements Serializable
  ...

To apply the above change you would hit <enter> on the line\:

::

  0.1227:  Add @SuppressWarnings 'serial' to 'Foo'

.. note::

   Java code corrections are handled just like a :doc:`refactoring
   </vim/java/refactor>` so the :ref:`RefactorUndo <:RefactorUndo>` and
   :ref:`RefactorRedo <:RefactorRedo>` commands can be used to undo/redo
   corrections that can't be handled by vim's undo (like file moves).

.. _\:Checkstyle:

Checkstyle
----------

When editing a java source file, eclim provides the command **:Checkstyle**
which will invoke `checkstyle`_ on the current file.

Additionally, you can configure eclim to execute checkstyle automatically when
you save a java source file by setting the eclim project settings
**org.eclim.java.checkstyle.onvalidate** to true.

Please note that both methods of invoking checkstyle require that you first
configure the location of your checkstyle config file using the eclim setting
**org.eclim.java.checkstyle.config**, described in the configuration section
below.

Configuration
^^^^^^^^^^^^^

:doc:`Eclim Settings </vim/settings>`

.. _org.eclim.java.checkstyle.config:

- **org.eclim.java.checkstyle.config** -
  Defines the location (project relative or absolute) or your checkstyle config
  file.

.. _org.eclim.java.checkstyle.properties:

- **org.eclim.java.checkstyle.properties** -
  Defines the location (project relative or absolute) or your checkstyle
  properties file.

.. _org.eclim.java.checkstyle.onvalidate:

- **org.eclim.java.checkstyle.onvalidate** -
  When set to true, checkstyle will be run on the file along with the regular
  java validation upon writing the file.

.. _checkstyle: http://checkstyle.sourceforge.net/
