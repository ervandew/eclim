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

Multiple Workspace Guide
========================

Running eclim against more than one eclipse workspace can be accomplished by
running multiple eclimd instances.  To accomplish this you must configure each
instance to run nailgun on a unique port and supply the path to the workspace
you which that instance to use.  Once your eclimd instances are up and running
the vim client will automatically determine which server to send requests to
based on your context.  In some cases you may be prompted for which workspace
to use if one cannot be determined for you.

Below are some different ways in which you can configure your eclimd instances:

1. All Users: Supply the nailgun port and eclipse workspace path when starting
   eclimd:

   ::

     $ eclimd -Dosgi.instance.area.default=@user.home/workspace1 -Dnailgun.server.port=9091
     $ eclimd -Dosgi.instance.area.default=@user.home/workspace2 -Dnailgun.server.port=9092

   If you are using the eclimd view in the eclipse gui, then you can start the
   eclipse gui with the desired nailgun server port (note that you must place
   the -vmargs option before the list of jvm arguments):

   ::

     $ eclipse -vmargs -Dnailgun.server.port=9092

2. Linux, OSX, BSD Users: Specify the port and workspace in eclimrc files and
   start eclimd with the -f argument:

   ::

     $ vim ~/.eclimrc1
     osgi.instance.area.default=@user.home/workspace1
     nailgun.server.port=9091

     $ vim ~/.eclimrc2
     osgi.instance.area.default=@user.home/workspace2
     nailgun.server.port=9092

     $ eclimd -f ~/.eclimrc1
     $ eclimd -f ~/.eclimrc2

   .. note::

     The -f argument is not supported by eclipse so the above option is only
     available when using a headless eclimd instance.

3. Windows Users: Create Windows shortcuts:

   - In Windows Explorer, open your eclipse folder.
   - Hold down the right mouse button and drag the eclimd.bat file to where
     you want the shortcut to exist (like your desktop) and release the
     right mouse button.
   - Choose "Create Shortcut(s) Here"
   - Right click the shortcut and choose "Properties"
   - | On the "Shortcut" tab edit the "Target:" field and append:
     | -Dosgi.instance.area.default=\@user.home/workspace1 -Dnailgun.server.port=9091
   - Repeat this process for your other workspaces.
