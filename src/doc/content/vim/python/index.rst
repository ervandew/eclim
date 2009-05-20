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

.. _vim/python/index:

Python
======

.. toctree::

   validate
   complete
   search
   regex
   django

- :ref:`vim/python/validate`
- :ref:`vim/python/complete`
- :ref:`vim/python/search`
- :ref:`vim/python/regex`
- :ref:`vim/python/django`

.. _vim/python/rope:

Rope
----

Contrary to other languages supported by eclim, python support is not provided
by an eclipse plugin.  When evaluating the currently available eclipse plugins
for python, namely pydev and dltk, we found that they had downsides making them
less than ideal candidates for inclusion in eclim.

After evaluating the eclipse plugins, attention turned to pure python
solutions, which then lead to the eventual choice: rope_

**Minimum Requirements**:
  - python 2.5 or greater
  - vim compiled with python support

    .. warning::

      **Windows Users**: gvim for windows does have python support, but at the
      time of this writing, it is compiled against python 2.4.  So if you want
      to leverage eclim's rope support, you will need to recompile gvim against
      python 2.5 or higher.

**Functionality Utilizing Rope**
  - :ref:`code completion <vim/python/complete>`
  - :ref:`find element definition <vim/python/search>`
  - :ref:`:PyLint`

**Configuration**

When using functionality that in turn utilizes rope, eclim attempt to make the
usage of rope as transparent as possible.  Eclim will automatically create the
rope project in the same directory as your rope project, resulting in a new
directory and file (.ropeproject/config.py).  Once that file has been created
you can then modify it to suit your environment.

For example, lets say you have another python project which is not on your
python path, but you wish to have **:PyLint**, code completion, etc. recognize
that project.  To do so you can open the .ropeproject/config.py file and inside
the set_prefs method you will see a commented example of how you can add paths
to the rope 'python_path'.  You can then add your project like so:

.. code-block:: python

  prefs.add('python_path', '~/myotherproject')

.. _rope: http://rope.sourceforge.net/
