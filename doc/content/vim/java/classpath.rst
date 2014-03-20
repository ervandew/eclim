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

Eclipse Classpath Editing
=========================

Source code completion, searching, auto imports, all rely on a properly
configured classpath.  When you first create a project, a ``.classpath`` file is
created in the project's root directory.  If you created the project on an
existing code-base, eclim will attempt to setup the ``.classpath`` file with any
source code directories or libraries in the project.

Regardless of the state of your project you will at some point need to update
the classpath.  The primary method of doing so, is to directly edit the
``.classpath`` to add, update, or remove entries as needed. To help you do this,
eclim provides several commands to ease the creation of new classpath entries
and variables.

.. note::

  All of the commands described below are only available while editing the
  ``.classpath`` file in vim.

  When you write the ``.classpath`` file, Vim will issue a command to the eclim
  server to update the project's classpath, and will report any errors via vim's
  location list (:help location-list).

  In addition to directly editing the ``.classpath`` file, you may also use
  :ref:`maven's support <classpath-maven>` for maintaining the eclipse
  classpath.  For users who use ivy_, eclim also provides a means to :ref:`auto
  update the eclipse classpath <classpath-ivy>` when saving changes to your
  ivy.xml.

.. _\:NewSrcEntry_java:

- **:NewSrcEntry** <dir> -
  Adds a new entry for a source code directory relative to the project's root
  directory.

  .. code-block:: xml

    <classpathentry kind="src" path="src/java"/>

  This command supports command completion of the directory relative to the
  .classpath file.

.. _\:NewProjectEntry_java:

- **:NewProjectEntry** <project> -
  Adds a new entry for a dependency on another project.

  .. code-block:: xml

    <classpathentry exported="true" kind="src" path="/a_project"/>

  This command supports command completion of the project name.

.. _\:NewJarEntry_java:

- **:NewJarEntry** <file> [<src_path> <javadoc_path>] -
  Adds a new entry for a jar file dependency.  If the jar file is not in a
  folder under the project root, you must use an absolute path (apparent
  limitation with Eclipse).

  .. code-block:: xml

    <classpathentry exported="true" kind="lib" path="lib/commons-beanutils-1.8.3.jar"/>

  You may optionally supply the path to the source for this jar and the entry
  created will include the ``sourcepath`` attribute:

  ::

    :NewJarEntry lib/commons-beanutils-1.8.3.jar lib/commons-beanutils-1.8.3-sources.jar

  .. code-block:: xml

    <classpathentry kind="lib" path="lib/commons-beanutils-1.8.3.jar"
        sourcepath="lib/commons-beanutils-1.8.3-sources.jar"/>

  In addition to the source path you can all supply the path to the javadocs:

  ::

    :NewJarEntry lib/commons-beanutils-1.8.3.jar lib/commons-beanutils-1.8.3-sources.jar lib/commons-beanutils-1.8.3-javadoc.jar

  .. code-block:: xml

    <classpathentry kind="lib" path="lib/commons-beanutils-1.8.3.jar"
        sourcepath="lib/commons-beanutils-1.8.3-sources.jar">
      <attributes>
        <attribute name="javadoc_location" value="jar:platform:/resource/my_project/lib/commons-beanutils-1.8.3-javadoc.jar"/>
      </attributes>
    </classpathentry>

.. _\:NewVarEntry_java:

- **:NewVarEntry** <VAR/file> [<src_path> <javadoc_path>] -
  Just like NewJarEntry except an Eclipse "var" entry is created.  When a jar
  entry references an absolute path, you should instead use a var entry.  The
  var entry allows you to define a base dir as a variable (ex. USER_HOME =
  /home/username), and then reference files relative to that variable.

  .. code-block:: xml

    <classpathentry exported="true" kind="var" path="USER_HOME/lib/hibernate-4.0.jar"/>

  This allows you to share .classpath files with other developers without each
  having a local copy with environment specific paths.

  To add new base classpath variables, you can edit
  ``$ECLIPSE_HOME/plugins/org.eclim_version/classpath_variables.properties``

  By default, a USER_HOME variable is created that defaults to the java System
  property "user.home" and you can add more as needed.

  This command supports command completion of Eclipse variable names as well as
  the files and directories beneath the path the variable represents.

  To manage the classpath variables, eclim also provides the following
  commands.

  .. _\:VariableList:

  - **:VariableList** -
    Lists all the currently available classpath variables and their
    corresponding values.

  .. _\:VariableCreate:

  - **:VariableCreate** <name> <path> -
    Creates or updates the variable with the supplied name.

  .. _\:VariableDelete:

  - **:VariableDelete** <name> -
    Deletes the variable with the supplied name.

.. _classpath-src-javadocs:

Source and Javadoc location
---------------------------

For your ``var`` and ``lib`` classpath entries, if you didn't do so when you
created the entry, you can configure the location for that entry's source code
and javadocs, like the example below, allowing you to :ref:`jump to the source
<:JavaSearch>` or :ref:`lookup the docs <:JavaDocSearch>` of classes, etc found
in that library. Note that the javadoc location must be a url, whether it be on
the local file system (file:, jar:file:) or remote (http:).

.. code-block:: xml

  <classpathentry exported="true" kind="lib" path="lib/hibernate-4.0.jar"
      sourcepath="<path>">
    <attributes>
      <attribute name="javadoc_location" value="file:<javadoc_dir>"/>
    </attributes>
  </classpathentry>

.. note::

  If your javadoc location is a jar in your workspace (in the curent project or
  another project), then the url must be in the form (where ``<project_name>``
  is replaced with your project's name):

  ::

    jar:platform:/resource/<project_name>/path/to/javadoc.jar!/

  If the jar file is outside of your workspace, then it would be in the form:

  ::

    jar:file:/your/absolute/path/to/javadoc.jar!/

.. _classpath-maven:

Maven
-----

Maven_ comes bundled with an Eclipse plugin that allows you to easily maintain
your .classpath file based on your pom.xml (or project.xml for maven 1.x
users).

.. note::

  For additional information on the Eclipse plugin from maven, you may visit
  their online documentation for `maven 1.x`_ or `maven 2.x`_.


.. _\:MvnRepo:
.. _\:MavenRepo:

* Initial Setup

  To initialize maven's support for updating the eclipse classpath you first need
  to set the ``M2_REPO`` (or ``MAVEN_REPO`` for 1.x) classpath variable in the
  Eclipse workspace by executing the following command in vim:

  maven 2.x:

  .. code-block:: vim

    :MvnRepo

  maven 1.x:

  .. code-block:: vim

    :MavenRepo

* Updating your .classpath

  Once you have performed the initial setup, updating the Eclipse
  ``.classpath`` file is as easy as executing the following at a command line:

  maven 2.x:

  ::

    mvn eclipse:eclipse

  maven 1.x:

  ::

    maven eclipse

  or in Vim:

  maven 2.x:

  .. code-block:: vim

    :Mvn eclipse:eclipse

  maven 1.x:

  .. code-block:: vim

    :Maven eclipse

  .. _classpath-maven-pom:

  For maven 2.x users, eclim also provides support for auto updating the
  ``.classpath`` for your project every time you save your pom.xml file.  Any
  entries found in the pom.xml that are not in the ``.classpath`` will be added,
  any entries that differ in version will be updated, and any stale entries
  deleted.
  
  .. note::

    This behavior can be disabled by adding the following setting to your
    vimrc:

    .. code-block:: vim

      let g:EclimMavenPomClasspathUpdate = 0

  .. note::

    This feature simply updates the entries in your project's ``.classpath``
    file, it does not download any newly added jars. When you'd like maven to
    download those new jars, you can run the following from the command line:

    ::

      mvn dependency:resolve

    or from within Vim:

    .. code-block:: vim

      :Mvn dependency:resolve

.. _classpath-ivy:

Ivy
---

For users of ivy_, eclim provides support for auto updating the ``.classpath``
for your project every time you save your ivy.xml file.  Any entries found in
the ivy.xml that are not in the ``.classpath`` will be added, any entries that
differ in version will be updated, and any stale entries deleted.


.. _\:IvyRepo:

* Initial Setup

  Before you can start utilizing the auto updating support, you must first set
  the location of your ivy repository (ivy cache).  This is the directory where
  ivy will download the dependencies to and where eclipse will then pick them
  up to be added to your project's classpath.

  To set the repository location you can use the **:IvyRepo** command which is
  made available when editing an ivy.xml file.

  .. code-block:: vim

    :IvyRepo ~/.ivy2/cache/

  If you fail to set this prior to writing the ivy.xml file, eclim will emit an
  error notifying you that you first need to set the IVY_REPO variable via this
  command.


* Updating your .classpath

  Once you have performed the initial setup, updating the Eclipse ``.classpath``
  file is as easy as saving your ivy.xml file (``:w``) and letting eclim do the
  rest.
  
  .. note::

    This behavior can be disabled by adding the following setting to your
    vimrc:

    .. code-block:: vim

      let g:EclimIvyClasspathUpdate = 0

  .. note::

    This feature will update your project's ``.classpath`` file accordingly, but
    it will not download any newly added jars. For that you'll need to have a
    target in your ant build file that will force ivy to download dependencies.
    Something like the example from the ivy docs:

    .. code-block:: xml

      <target name="resolve" description="--> retrieve dependencies with ivy">
        <ivy:retrieve/>
      </target>

    You can then run this target from the command line:

    ::

      ant resolve

    or from within Vim

    .. code-block:: vim

      :Ant resolve

* Preserving manually added entries

  When utilizing the ivy support, eclim will attempt to remove any stale
  entries from your .classpath file.  If you have some manually added entries,
  these may be removed as well.  To prevent this you can add a classpath entry
  attribute notifying eclim that the entry should be preserved.

  Ex.

  .. code-block:: xml

    <classpathentry kind="lib" path="lib/j2ee-1.4.jar">
      <attributes>
        <attribute name="eclim.preserve" value="true"/>
      </attributes>
    </classpathentry>

.. _ivy: http://jayasoft.org/ivy
.. _maven: http://maven.apache.org
.. _maven 1.x: http://maven.apache.org/maven-1.x/plugins/eclipse/
.. _maven 2.x: http://maven.apache.org/guides/mini/guide-ide-eclipse.html
