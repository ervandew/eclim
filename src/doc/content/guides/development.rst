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

.. _guides/development:

Eclim Developers Guide
======================

This guide is intended mostly for those who wish to contribute to eclim by
fixing bugs or adding new functionality, but the first section is also useful
for users who would like to use the latest development version of eclim.

.. _development-build:

Checking out the code and building it.
--------------------------------------

1. Check out the code:

  ::

    $ svn co https://eclim.svn.sourceforge.net/svnroot/eclim/trunk eclim

  If your planning on contributing code beyond small bug fixes, then it is
  highly recommend to check the code out using git_, specifically using
  git-svn_:

  ::

    $ git svn clone https://eclim.svn.sourceforge.net/svnroot/eclim/trunk eclim

  Once you have a local git repository you can utilize the extensive local git
  functionality allowing you to commit code locally, create local branches,
  etc.  For guidelines on managing patches and submitting them, please see the
  :ref:`patch guide <development-patches>` below.

2. Build eclim:

  ::

    $ cd eclim
    $ ant -Declipse.home=<your eclipse home dir>

  This will build and deploy eclim to your eclipse and vim directories.  If you
  don't want to supply the eclipse home directory every time, you can set the
  environment variable ECLIM_ECLIPSE_HOME which the build script will then
  utilize.

  By default the above ant call will build all the eclim plugins, requiring you
  to have all the related dependencies already installed in your eclipse
  distribution.  However, if you only want a subset of the eclim plugins to be
  built, you can specify so using the 'plugins' system property:

  ::

    # build only ant and jdt (java) support
    $ ant -Dplugins=ant,jdt

    # build only pdt (php) support (requires wst)
    $ ant -Dplugins=wst,pdt

  .. note::

    On windows you will need to quote the plugins argument if you are building
    more than one plugin:

      > ant "-Dplugins=ant,jdt"


.. _development-patches:

Managing / Submitting Patches
-----------------------------

Before you start writing any code you should first familiarize yourself with
the preferred means of submitting patches, and if you plan on contributing
anything non-trivial, the preferred means of managing those patches.

.. _development-patches-submitting:

**Submitting Patches**

Any patches you submit should be in the form of an svn diff (if you chose to
use svn) or as a git formatted patch (for those using git).  For svn users,
simply redirecting ``svn diff`` to a file will suffice.  For git users, the
preferred method is to use git-format-patch:

  ::

    $ git format-patch --stdout origin > myfeature.patch

Or using StGit (:ref:`described below <development-patches-managing>`):

  ::

    $ stg show myfeature > myfeature.patch

If you use git-format-patch please be aware that it will generate a patch entry
for every commit you've made. So unless each commit represents a change that
you are willing to submit independently of the others, please consolidate all
your commits for a given patch into a single commit prior to generating the
patch file.  If you don't do so then the patch file will contain all your trial
and errors, dead ends, etc. and evaluating a patch with an entire history like
that can be very difficult.

.. _development-patches-managing:

**Managing Patch Development**

Although you can manage patches and format "clean" patch files by manually
rewriting git's history or juggling some local branches, the recommended tool
for managing patches is `Stacked Git`_ (StGit).  StGit makes creating,
managing, and submitting patches a lot easier than doing so manually.  If you
decide to not use StGit, then you might want to read the section in the git
manual regarding `managing of patch series`_.

.. _development-patch-example:

**Example Patch Workflow**

To fully illustrate creating and submitting a patch, let's walk through making
a small change, from start to finish.  In this example we will modify the
:PingEclim command to print "Pong" along with the version numbers normally
returned.

This example will use the recommend tools, git_ and stgit_.

1. First clone the eclim repository:

  ::

    $ git svn clone https://eclim.svn.sourceforge.net/svnroot/eclim/trunk eclim

2. Initialize stgit for the eclim repository:

  ::

    $ cd eclim
    $ stg init

3. Start a new stgit patch:

  ::

    $ stg new -m "Alter :PingEclim to print 'Pong'" pong


4. Edit the file PingCommand.java:

  ::

    $ vim src/java/org/eclim/command/admin/PingCommand.java
    ...

    $ git diff
    diff --git a/src/java/org/eclim/command/admin/PingCommand.java b/src/java/org/eclim/command/admin/PingCommand.java
    index bb5c569..b2f2ebc 100644
    --- a/src/java/org/eclim/command/admin/PingCommand.java
    +++ b/src/java/org/eclim/command/admin/PingCommand.java
    @@ -65,7 +65,7 @@ public class PingCommand
           version = eclim_version + '\n' + eclipse_version;
         }

    -    return version;
    +    return "Pong!\n" + version;
       }

       private String getVersion()


5. Test the change:

  ::

    $ ant
    ...

    $ $ECLIPSE_HOME/eclimd
    ...

    $ vim -c ":PingEclim"
    ...
    Pong!
    eclim   1.4.4
    eclipse 3.4.2
    Press ENTER or type command to continue


6. Commit the change to the patch:

  ::

    $ stg refresh
    Checking for changes in the working directory ... done
    Refreshing patch "pong" ... done

7. Create a patch file:

  ::

    $ stg show > pong.patch

At this point all that is left is submitting the patch to the
`eclim development group`_.


**Pulling Updates**

  As some point you'll need to pull updates from the remote svn repository.
  For svn users it's a simple ``svn up``, but for git/stgit users the process
  is not as obvious.

  If you're using just git-svn, without stgit, then you can pull updates like
  so:

    ::

      $ git svn rebase

  If you're using stgit on top of git, then the preferred method is to first
  run the following commands:

    ::

      $ git config stgit.pull-policy rebase
      $ git config stgit.rebasecmd "git svn rebase"
      $ git config branch.master.stgit.parentbranch remotes/trunk

  Once you've got that part setup you can then use stgit to pull the latest
  changes from the remote repository:

    ::

      $ stg pull -m


What's Next
------------

Now that you're familiar with the basics of building and patching eclim, the
next step is to familiarize yourself with the eclim architecture and to review
the detailed docs on how new features are added.

All of that and more can be found in the
:ref:`eclim development docs <development/index>`.


.. _git: http://git-scm.com/
.. _git-svn: http://www.kernel.org/pub/software/scm/git/docs/git-svn.html
.. _git-format-patch: http://www.kernel.org/pub/software/scm/git/docs/git-format-patch.html
.. _managing of patch series: http://www.kernel.org/pub/software/scm/git/docs/user-manual.html#cleaning-up-history
.. _Stacked Git: http://procode.org/stgit/
.. _stgit: http://procode.org/stgit/
.. _eclim development group: http://groups.google.com/group/eclim-dev
