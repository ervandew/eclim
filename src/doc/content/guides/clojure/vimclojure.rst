.. Copyright (C) 2005 - 2010  Eric Van Dewoestine

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

.. _guides/clojure/vimclojure:

Running VimClojure in eclimd
============================

`VimClojure`_ is set of plugins for vim which provide various features for
working with `clojure`_.  When running the bundled nailgun server it can
provide interactive features like code completion, repl interaction, etc.
While VimClojure can be used independently of eclim, you can also embed
VimClojure's nailgun server inside of eclim via eclim's
:ref:`ext dir <eclimd_extdir>` support.  By doing so, you only have to run a
single nailgun server, as opposed to running one for eclim and another for
VimClojure.

To install VimClojure and embed it into eclim, simply follow these steps:

.. note::

  In this guide we keep VimClojure as self contained as possible instead of
  copying all of its vim files into your vimfiles directory.  While this isn't
  required, it can make upgrading or removing VimClojure easier.

#. First download the `VimClojure distribution`_.

#. Next, unpack the distribution into your vim files directory:

   Linux, BSD, OSX:
   ::

     $ cd ~/.vim
     $ unzip ~/vimclojure-2.1.2.zip

   Windows:
   ::

     > cd %HOME%\vimfiles
     > jar -xvf %HOME%\Desktop\vimclojure-2.1.2.zip

#. Then build vimclojure:

   ::

     $ cd vimclojure-2.1.2
     $ ant

#. Then copy the VimClojure jars into eclim's ext dir:

   Linux, BSD, OSX:
   ::

     $ mkdir ~/.eclim/resources/ext/vimclojure
     $ cp {build/vimclojure.jar,lib/clojure*.jar} ~/.eclim/resources/ext/vimclojure

   Windows users, your can just create the
   ``%HOME%\.eclim\resources\ext\vimclojure`` directory and copy the
   jar files from the build and lib dirs via Windows Explorer.

#. Lastely, configure vim:

   Linux, BSD, OSX:
   ::

     $ cp ftdetect/clojure.vim ~/.vim/ftdetect/

   Windows:
   ::

     > cp ftdetect\clojure.vim %HOME%\vimfiles\ftdetect

   Add the following to your vimrc file:

   Linux, BSD, OSX:

   .. code-block:: vim

       set rtp+=~/.vim/vimclojure-2.1.2

   Windows:

   .. code-block:: vim

       set rtp+=~/vimfiles/vimclojure-2.1.2

   Create a new clojure ftplugin file to configure vimclojure:

   Linux, BSD, OSX:
   ::

     $ mkdir ~/.vim/ftplugin/clojure
     $ vim ~/.vim/ftplugin/clojure/clojure.vim

   Windows:
   ::

     > mkdir %HOME%\vimfiles\ftplugin\clojure
     > gvim %HOME%\vimfiles\ftplugin\clojure\clojure.vim


   ftplugin/clojure/clojure.vim contents:

   .. code-block:: vim

     let g:clj_want_gorilla = 1
     let vimclojure#NailgunClient =
       \ eclim#client#nailgun#GetNgCommand() .
       \ ' --nailgun-port ' . eclim#client#nailgun#GetNgPort()

Once you have completed the above steps, then you just need to restart eclimd
and then all of VimClojure's features should be available to you while editing
a clojure source file in vim.


.. _VimClojure: http://kotka.de/projects/clojure/vimclojure.html
.. _VimClojure distribution: http://www.vim.org/scripts/script.php?script_id=2501
.. _clojure: http://clojure.org
