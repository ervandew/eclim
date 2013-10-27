.. Copyright (C) 2005 - 2013  Eric Van Dewoestine

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

Python
======

Features
--------

.. toctree::
   :maxdepth: 1

   validate
   complete
   search
   django

.. _python-rope:

Rope
----

Contrary to other languages supported by eclim, python support is not provided
by an eclipse plugin.  When evaluating the currently available eclipse plugins
for python, namely pydev and dltk, we found that they had downsides making them
less than ideal candidates for inclusion in eclim.

After evaluating the eclipse plugins, attention turned to pure python
solutions, which then lead to the eventual choice: rope_

.. note::

  Eclim comes bundled with a version of rope, so there is nothing extra for you
  to install once you have met the minimum requirements below.

**Minimum Requirements**:
  - python 2.5 or greater
  - vim compiled with python support

    .. warning::

      **Windows Users**: gvim for windows does have python support, but at the
      time of this writing, it is compiled against python 2.4.  So if you want
      to leverage eclim's rope support, you will need to recompile gvim against
      python 2.5 or higher or find a site which provides a pre-compiled version
      for you.  Here are is one such site providing binaries for python 2.5 and
      2.6: gooli.org_

**Functionality Utilizing Rope**
  - :doc:`code completion </vim/python/complete>`
  - :doc:`find element definition </vim/python/search>`
  - :ref:`:PyLint`

**Creating A New Python Project**

Since python support is not provided by eclipse, you can create your project
with the ``none`` nature:

.. code-block:: vim

  :ProjectCreate my_project -n none

**Configuration**

When using functionality that in turn utilizes rope, eclim attempt to make the
usage of rope as transparent as possible.  Eclim will automatically create the
rope project in the same directory as your eclim/eclipse project, resulting in a
new directory and file (.ropeproject/config.py).  Once that file has been
created you can then modify it to suit your environment.

For example, lets say you have another python project which is not on your
python path, but you wish to have **:PyLint**, code completion, etc. recognize
that project.  To do so you can open the .ropeproject/config.py file and inside
the set_prefs method you will see a commented example of how you can add paths
to the rope 'python_path'.  You can then add your project like so:

.. code-block:: python

  prefs.add('python_path', '~/myotherproject')

Suggested Mappings
------------------

Here are some mappings for the python funtionality provided by eclim.  To make
use of these mappings, simply create a ftplugin file for python and place your
mappings there (:help ftplugin-name).

- The following mapping allows you to simply hit <enter> on an element to
  perform a search to find its definition or occurrences depending on the
  context.

  .. code-block:: vim

    nnoremap <silent> <buffer> <cr> :PythonSearchContext<cr>

- If you are doing django development you may want to use the following mapping
  which will execute **:DjangoViewOpen**, or **:DjangoTemplateOpen** depending
  on the context of the text under the cursor and if no results were found from
  either of those, it will issue **:PythonFindDefinition**.

  .. code-block:: vim

    function! s:MyFind ()
      let found = eclim#python#django#find#ContextFind()
      if !found
        PythonFindDefinition
      endif
    endfunction
    nnoremap <silent> <buffer> <cr> :call <SID>MyFind()<cr>

.. _rope: http://rope.sourceforge.net/
.. _gooli.org: http://www.gooli.org/blog/gvim-72-with-python-2526-support-windows-binaries/
