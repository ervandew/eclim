.. Copyright (C) 2013  Eric Van Dewoestine

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

Plugins
=======

.. note::

  This guide is a work in progress. If in the process of writing a new plugin
  you find anything here that is unclear or missing, please don't hesitate to
  post to the `eclim-dev`_ mailing list with questions, suggestions, etc.

To allow eclim to support different languages, eclim is broken up into eclipse
plugins, each of which depend on a corresponding eclipse feature which provides
support for that language. When you install or build eclim, it will examine your
eclipse install to determine which features are available and will add the
corresponding eclim plugins to expose those features. This page documents the
core aspects of what goes into the creation of a new eclim plugin.

Bootstrapping the plugin artifacts
----------------------------------

Eclim includes a set of templates to help you bootstrap a new plugin. To utilize
them you can run the following ant target:

::

  $ ant plugin.create

You will be prompted to enter a name for this plugin along with some guidelines
on choosing an appropriate name.

Once you've chosen a name, the plugin's directory structure will be created and
populated with bare bones version of the required artifacts. Eclim's
``build.xml`` file will also be updated to include a target to the new plugin's
unit test target.

Updating the initial artifacts
------------------------------

After you've bootstrapped your plugin, you can now start updating the generated
artifacts:

build_<plugin_name>.gant
~~~~~~~~~~~~~~~~~~~~~~~~

The first file you'll need to modify is a gant file for your plugin. The main
eclim build.gant script will load this file during the build process to
determine what the plugin's eclipse dependency is, so it knows whether it can be
built against the target eclipse install. So the first thing we need to do is to
fill in that information by updating the ``feature_<plugin_name>`` variable with
the name of the eclipse feature that this plugin depends on. For example, the
eclim jdt plugin has this set to ``'org.eclipse.jdt'``. The build script will
look in the ``features`` directory of your eclipse install (including the
``dropins`` and your user local eclipse dir if set), to find this feature, so
the value you set, must be found in one of those locations (the version suffixes
will be removed from the features in order to match it against the value you've
set).

You'll also notice that there is a unit test target in the gant file. You can
ignore that for now.

META-INF/MANIFEST.MF
~~~~~~~~~~~~~~~~~~~~

The next file to note is the plugin's ``META-INF/MANIFEST.MF``. This is the file
that eclipse will use to determine how to load the bundle and what to include in
its classpath. The only part of this file that you should edit is the
``Require-Bundle:`` section. This is a comma separated list of bundles (or
plugins) which this bundle depends on. When this bundle is loaded only those
bundles listed here will be available in the classpath. So when you start
running commands you've written later, if you receive a
``ClassNotFoundException``, that is likely due to the bundle containing that
class not being listed in your plugin's ``Require-Bundle:`` list. At this point
you probably don't know yet what bundles you need to add to this list. When you
start writing commands for your plugin, you'll have to find out which bundles
contain the classes imported from the eclipse plugin you are integrating with,
and you'll need to add those bundles accordingly.

It's also worth noting that eclim provides a custom classpath container which
scans the manifest of each eclim plugin and loads the required bundles of each
into the classpath. So when adding new bundles, if you want validation, search,
code completion, etc to work with classes from those new bundles, you'll have to
restart the eclim daemon. While restarting can be annoying, this is generally
better than having to add/remove entries from the ``.classpath`` file or
worrying about one user having different bundle version numbers from another.

PluginResources.java
~~~~~~~~~~~~~~~~~~~~

At this point you'll typically need to start customizing your plugin's
``org.eclim.<name>/java/org/eclim/plugin/<name>/PluginResources.java`` file.
Here is where you will map a short alias to the project nature, or natures, of
the plugin you are integrating with, register a project manager for initializing
project's for this plugin, register any plugin settings that users can
configure, etc. You'll be doing all this inside of the ``initialize`` method
which has been generated for you.

Project Nature
^^^^^^^^^^^^^^

You'll first need to find out where the plugin's nature id is defined. Here are
some examples that should give you an idea of where to look:

- **jdt:** org.eclipse.jdt.core.JavaCore.NATURE_ID
- **cdt:**

  - org.eclipse.cdt.core.CProjectNature.CNATURE_ID
  - org.eclipse.cdt.core.CCProjectNature.CC_NATURE_ID

- **dltkruby:** org.eclipse.dltk.ruby.core.RubyNature.NATURE_ID
- **adt:** com.android.ide.eclipse.adt.AdtConstants.NATURE_DEFAULT

One way to find it is to open up the ``.project`` file in a project containing
the nature, locate the fully qualified name in the ``<natures>`` section, then
grep the plugin's code for that name.

Once you have the reference to the nature id, you can then create a public
static variable called ``NATURE``:

.. code-block:: java

  public static final String NATURE = SomeClass.NATURE_ID;

You'll be using this constant as the key to register features for project
containing this nature, but first we'll register a short alias for this nature
since the actual nature id tends to be long and unstandardized, and we don't
want users to have to type it out when creating projects from eclim:

.. code-block:: java

  ProjectNatureFactory.addNature("shortname", NATURE);

Project Manager
^^^^^^^^^^^^^^^

The next thing you'll probably need to do is to create a project manager for
your project
(``org.eclim.<name>/java/org/eclim/plugin/<name>/project/SomeProjectManager.java``).
The project manager is responsible for performing any post create, update,
delete, or refresh logic required for projects of this nature. This logic can
include things such as creating an initial classpath/buildpath, validate the
classpath/buildpath on update, forcing a full update of the search index on
refresh, or any number of other things.

Overriding the ``create`` method will almost certainly be necessary, but the
logic you'll need here varies widely.  Finding what you'll need is a matter of
digging through the parent plugin's source code, typically looking for the
project creation wizard class, to see what it does to create a project of this
nature and later comparing the created artifacts from your code against those of
a project created from the eclipse gui. This can be a difficult hurdle to get
past for someone doing this the first time, so please don't be shy about asking
for help on the `eclim-dev`_ mailing list.

Eclim does provide a couple ant tasks to at least help you to quickly extract
any docs and source code found in your eclipse install:

.. include:: /development/commands.rst
   :start-after: begin-eclipse-doc-src
   :end-before: end-eclipse-doc-src

Once you've created your project manager, you then map it to your plugin's
nature inside of your ``PluginResources.initialize`` method like so:

.. code-block:: java

  ProjectManagement.addProjectManager(NATURE, new SomeProjectManager());

Project Settings
^^^^^^^^^^^^^^^^^

At this point you should have the minimum of what is needed for a new plugin.
Hopefully you can now create new projects with your plugin's defined nature.
The next step would be to start adding :doc:`commands </development/commands>`
to provide validation, code completion, etc. The remaining items in this list
are not required to continue. They provide you with the ability to setup your
own preferences or to expose the parent plugin's defined preferences inside of
vim. When you've come to the point that you need to work with preferences, then
feel free to come back here and continue reading.

**To Be Continued...**

.. - mesage bundle
   - option handler
   - preferences handler

..
  Updating the eclim installer for your new plugin
  ------------------------------------------------

  install.properties: add the following keys
    - featureList.<plugin>
    - featureList.<plugin>.html
  dependencies.xml: add feature dependencies
  FeatureProvider: add feature name, etc to constants
  EclipsePluginStep: add feature to FeatureNameComparator

.. _eclim-dev: http://groups.google.com/group/eclim-dev
