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

Java Search
===========

.. _\:JavaSearch:

Pattern Search
--------------

Pattern searching provides a means to widen a search beyond a single element.  A
pattern search can be executed using the command

**:JavaSearch** -p <pattern> [-t <type> -x <context> -s <scope> -i -a <action>]

All of the results will be placed into the current window's location list (:help
location-list) so that you can easily navigate the results.

Vim command completion is supported through out the command with the excption
of the pattern to search for.

.. code-block:: vim

  :JavaSearch <Tab>
  :JavaSearch -p MyClass* <Tab>
  :JavaSearch -p MyClass* -t <Tab>
  :JavaSearch -p MyClass* -t all <Tab>
  :JavaSearch -p MyClass* -t all -x <Tab>
  :JavaSearch -p MyClass* -t all -x declarations


- -p <pattern>: The pattern to search for.

  Ex.

  ::

    MyClass*
    MyClass.someMethod*

- -t <type> (Default: type): The type of element to search for where possible
  types include

  - annotation
  - class
  - classOrEnum
  - classOrInterface
  - constructor
  - enum
  - field
  - interface
  - method
  - package
  - type

- -x <context> (Default: declarations): The context of the search, where
  possible context values include

  - all - All occurrences.
  - declarations - Declarations matching the pattern or element.
  - implementors - Implementors of the pattern or element.
  - references - References of the pattern or element.

- -s <scope> (Default: all): The scope of the search where possible values
  include

  - all - Search the whole workspace.
  - project - Search the current project, dependent projects, and libraries.

- -i: Ignore case when searching.

- -a: The vim command to use to open the result (edit, split, vsplit, etc).

Eclim also provides a shortcut when issuing a pattern search for a type.  You
may simply invoke **:JavaSearch** supplying only the pattern.

.. code-block:: vim

  :JavaSearch SomeType

To shorten things even more, there is support for camel case searching as well.

.. code-block:: vim

  :JavaSearch NPE

However, please note that camel case searching does not permit wild card
characters ('*', '?').

Element Search
--------------

Element searching allows you to place the cursor over just about any element in
a source file (method call, class name, field) and perform a search for that
element.  Performing an element search is the same as performing a pattern
search with the exception that you do not specify the -p option since the
element under the cursor will be searched for instead.

If only one result is found and that result is in the current source file, the
cursor will be moved to the element found.

.. _\:JavaSearchContext:

As a convenience eclim also provides the command **:JavaSearchContext**.  This
command accepts only the optional ``-a`` argument described above, and will
perform the appropriate search depending on the context of the element under the
cursor.

- If the cursor is on a class or interface declaration, the command will search
  for all classes / interfaces that implement / extend the element.
- If the cursor is on a method or field declaration, the command will search for
  all references to the element.
- Otherwise, it will search for the declaration of the element.

Alternate Searching
-------------------

For those occasions that you find yourself browsing a third party source
distribution that you want to be able to search without going through the steps
of setting up a project, eclim provides an alternate searching mechanism. To
utilize the alternate searching requires no change in behavior or commands, but
to achieve the best results, you should know how it works.

The first thing worth noting is that the alternate search is currently a bit
limited.  It only supports searches involving types (classes, interfaces,
annotations, and enums).  It doesn't currently have any support for methods or
fields.

Secondly, it can only search for and locate types within the current source
tree.  Searching across the jdk source or other third party source files without
setting up an Eclipse or similar classpath, is difficult at worst, and slow at
best.

With that said, I've found that when I'm walking through a third party source
tree, my main focus is on finding referenced classes / interfaces quickly and
easily, and the eclim alternate searching does just that.

Invoking the search is the same as the standard search mechanism.  You simply
use the same **:JavaSearch** command as you normally would.  The only difference
is that the alternate search doesn't support the -t option and will notify you
of such if supplied.

When invoked, the alternate search will perform the following\:

- It will grab the full path of the current source file, strip off the package
  and search from the resulting directory.

  Ex.  When editing a file /usr/local/java/foo/src/org/foo/bar/Baz.java, the
  alternate search will first search the directory /usr/local/java/foo/src.

- If no files are found in that directory, then it will proceed to search Vim's
  'path' option (:h 'path' for more info on this option).

  As an example, I have my 'path' set to '/usr/local/java/java-src' and in that
  directory is where I store all my third party source distributions (hibernate,
  spring, axis, etc.).

- Once one or more files are found, the search will stop if the requested search
  was for declarations.  For all searches, eclim will first try to find the
  declarations and if the user requested a search for implementors, references,
  or all, then the eclim will proceed to the next step.

- For non-declaration searches, if multiple declaring source files are found,
  eclim will prompt you to narrow the results down to the type you would like
  results for.

- Once eclim has narrowed the search down to the specific type to proceed with,
  it will then attempt to narrow the search down to a specific source
  distribution directory.  To do this it locates the relevant entry from the
  'path' option, tacks on one more level of the path from the resulting file,
  and commences its search from there.

  Ex.  When searching for all implementors of MyType, if eclim finds
  a file /usr/local/java/java-src/myproject/core/src/org/my/MyType.java
  and a 'path' entry of /usr/local/java/java-src exists, then eclim
  will deduce that that search must continue in the directory
  /usr/local/java/java-src/myproject.

This may seem a bit complicated for a simple search, but in practice it's
actually quite simple, and as usual, I'm open to any and all comments and
suggestions.

.. note::

  Alternate searching is bound to the performance of the file system and as
  such, the response time on Windows can be significantly slower than Linux.
  This is most noticable when searching for 'implementors', 'references', and
  'all'.  The number of and depth of the directories in your Vim 'path' option
  may also impact performance.

Configuration
-------------

:doc:`Vim Settings </vim/settings>`

.. _g\:EclimJavaSearchSingleResult:

- **g:EclimJavaSearchSingleResult** (Default: 'split') -
  Determines what action to take when a only a single result is found.

  Possible values include\:

  - 'split' - open the result in a new window via "split".
  - 'edit' - open the result in the current window.
  - 'tabnew' - open the result in a new tab.

  This setting overrides the global default for all supported language types
  which can be set using the **g:EclimDefaultFileOpenAction** setting which
  accepts the same possible values.

- **g:EclimQuickfixHeight** (Default: 10) -
  Sets the height in lines of the quickfix window when eclim opens it to display
  search results.

.. _g\:EclimJavaSearchMapping:

- **g:EclimJavaSearchMapping** (Default: 1) -
  When set to 1, <enter> will be mapped to the java search functionality for the
  various java related xml files (spring, hibernate, web.xml, and
  struts-config.xml).  By default this is enabled.
