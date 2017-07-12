.. Copyright (C) 2014  Eric Van Dewoestine

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

Android
=======

Creating a project
------------------

.. begin-project

Creating an android project is the same as creating a regular java project, but
you use the ``android`` nature instead:

::

  :ProjectCreate /path/to/my_project -n android

This will result in a series of prompts for you to input your project's information:

**Note:** at any point in this process you can use Ctrl+C to cancel the project
creation.

1. First you will be asked to choose the target android platform. If you have
   only one platform installed on your system, this prompt will be skipped and
   that platform will be used when creating the project. If you have no
   platforms installed then you will receive an error directing you to install
   a platform using the Android SDK Manager. If you install a new platform you
   will need to either restart eclipse/eclimd or run the eclim supplied
   :ref:`:AndroidReload <:AndroidReload>` command.
2. Next you will be asked to supply a package name (Ex: `com.mycompany.myapp`).
3. Then you will need to supply a name for your application.
4. The next prompt will ask you if you are creating a library project or not.
   Most likely you are not, so type 'n' here to proceed.
5. Lastly, if you are not creating a library project, you will be asked whether
   or not you want to have a new android activity created for you and if so,
   you will be asked for the name of that activity.

Once you've finished supplying the necessary information, your android project
will be created. An android project is simply a specialized java project, so
you can now leverage all the eclim provided :doc:`java functionality
</vim/java/index>` while developing your app.

.. end-project

Commands
--------

.. _\:AndroidReload:

**:AndroidReload** - Reloads the Android SDK environment in the running
eclimd/eclipse instance. Useful if you've made changes to the SDK outside of
eclipse (installed a new target platform, etc).

Configuration
-------------

:doc:`Eclim Settings </vim/settings>`

.. _com.android.ide.eclipse.adt.sdk:

- **com.android.ide.eclipse.adt.sdk** -
  Sets the path to your system's android sdk install.
