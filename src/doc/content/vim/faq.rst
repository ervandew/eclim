.. Copyright (C) 2005 - 2008  Eric Van Dewoestine

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

.. _vim/faq:

FAQ
====

.. _eclim_workspace:

- **How to I tell eclim which eclipse workspace to use?**

  If you are running windows, then you can start eclimd like so:

    ::

      $ eclimd -Dosgi.instance.area.default=@user.home/another_workspace

  Note the system property ``osgi.instance.area.default``, which is used to
  specify the location of your workspace.  Also note the variable
  ``@user.home`` which will be replaced with your home directory at runtime.

  If you are running a unix variant (linux, mac osx, bsd, etc.) then you
  can specify the above system property in the .eclimrc file in your home
  directory.

    ::

      $ echo "osgi.instance.area.default=@user.home/another_workspace" >> ~/.eclimrc

.. _eclim_proxy:

- **How can I configure eclim to use a proxy?**

  The occasional eclim feature requires network access to function properly.
  For example, xml validation may require validating the file against a dtd or
  xsd located remotely.  If you are behind a proxy then you may need to provide
  eclim with the necessary proxy settings.

    ::

      $ eclimd -Dhttp.proxyHost=my.proxy -Dhttp.proxyPort=8080

  If you are running a unix variant (linux, mac osx, bsd, etc.) then you
  can specify the above system property in the .eclimrc file in your home
  directory.

    ::

      $ echo -e "http.proxyHost=my.proxy\nhttp.proxyPort=8080" >> ~/.eclimrc
