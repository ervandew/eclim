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

Javascript
==========

Validation
----------

When editing a javascript file eclim will default to validating the file when it
is written.  Any errors will be added to the current window's location list
(:help location-list) and their corresponding line number noted via Vim's sign
functionality.

Javascript validation currently uses `JavaScript Lint`_ to
perform the validation.  To use it you will need to first install JavaScript
Lint and put it in your path.

Installing on windows and the mac should be very straight forward since
pre-compiled version for each are available for download on the
`JavaScript Lint`_ site.  For other unix based systems (linux, bsd, etc.) the
installation procedure is not so obvious.  Here are the steps used to compile
and install it on a linux machine (your paths may vary)\:

.. code-block:: bash

  $ cd jsl-<version>/src
  $ make -f Makefile.ref

  # this path will undoubtedly vary on non-linux machines, so watch the
  # make output for the real destination.
  $ sudo cp Linux_All_DBG.OBJ/jsl /usr/local/bin

If you don't want javascript files validated when saving them, you can set the
g:EclimJavascriptValidate variable described in the configuration section below.

.. _\:Validate_javascript:

Regardless of whether you have validation enabled upon saving or not, the
command **:Validate** is available to manually execute the validation.

Configuration
-------------

:doc:`Vim Settings </vim/settings>`

.. _g\:EclimJavascriptValidate:

- **g:EclimJavascriptValidate** (Default: 1) -
  If set to 0, disables javascript validation when saving the file.

- **g:EclimValidateSortResults** (Default: 'occurrence') -
  If set to 'severity', the validation results will be sorted by severity
  (errors > warnings > info > etc.)

.. _g\:EclimJavascriptLintConf:

- **g:EclimJavascriptLintConf** (Default: '~/.jslrc') -
  Used to set the location of your jsl config file.

.. _javascript lint: http://www.javascriptlint.com/
