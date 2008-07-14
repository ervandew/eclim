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

.. _vim/common/taglist:

Eclim & Taglist
===============

Taglist Enhancement
-------------------

Eclim provides a scripted ctags implementation that sits on top of the original
ctags to provide an alternative approach to adding new languages for use by the
excellent taglist_ plugin for Vim.

The standard ctags, gives you two choices when you wish to add support for a new
language.  First, you can define a new language via regular expression patterns
in your .ctags file.  Or, using the second approach, you can write the C code
necessary to truly integrate the new language into ctags.

The first approach, while fairly simple, is a bit limiting.  The most
frustrating limitation is that the file to be parse is processed one line at a
time, which prevents you from identifying tags that span two or more lines.

For example, given the following web.xml file, you would not be able to
distinguish between the first block which is a servlet definition, and the
second which is a servlet mapping, because you would need to process the parent
tag, not just the servlet-name tag.

.. code-block:: xml

  <servlet>
    <servlet-name>MyServlet</servlet-name>
    <servlet-class>org.foo.MyServlet</servlet-class>
  </servlet>

  <servlet-mapping>
    <servlet-name>MyServlet</servlet-name>
    <servlet-class>/my-servlet</servlet-class>
  </servlet-mapping>

The second approach, is much more flexible, but writing a language processor in
C may not be a feasible solution for various reasons (unfamiliarity with C,
portability, etc.).

Taking into account these concerns, eclim provides a means to add new languages
by writing a groovy_ script which can range from simple multi-line capable
regular expression matching, to a full blown language parser using a scripting
language that borrows many of its convenient language constructs from languages
such as Python, Ruby, and Smalltalk.

Adding a new language involves the following three steps\:

#.  Write a groovy script that conforms to a simple interface defined
    below.
#.  Place the script in the appropriate plugin's ``resources/scripts/taglist``
    dir.

    | For example general purpose taglist scripts should be placed in\:
    | $ECLIPSE_HOME/plugins/org.eclim_version/resources/scripts/taglist
    | While those specific to java programming should be placed in\:
    | $ECLIPSE_HOME/plugins/org.eclim.jdt_version/resources/scripts/taglist
#.  Add a Vim variable that informs the taglist plugin about your new
    language.

    .. code-block:: vim

      let g:tlist_mylang_settings='mylang;t:mytype;f:myfield'

    See the `taglist documentation`_ for more details.

The second step is self explanatory and the third step is well documented by the
`taglist documentation`_ so the rest of this document will
concentrate on step one.

.. note::

  When opening the taglist window, eclim will search for the taglist script in
  the directory noted above by looking for a file named filetype.groovy, where
  filetype is the case sensitive value of Vim's ``&amp;filetype`` option.  If no
  script is found for the file type, then eclim will delegate the call to your
  system's ctags command as configured_ for taglist.vim.


Writing the Groovy Script
-------------------------

Writing the groovy script is a pretty simple process.  The script simply needs
to define one class which implements
``org.eclim.command.taglist.TaglistScript``.  This interface defines a single
method, ``execute(String)`` which takes the name of the file to be processed and
returns an array of ``org.eclim.command.taglist.TagResult``.

Since regular expression matching tends to be the easiest way to add new
language support, eclim provides a helper class,
``org.eclim.command.taglist.RegexTaglist`` which makes the pattern matching
process as painless as possible.

To see this all in action, lets look at the code for the ``ant.groovy`` script
(found in the org.eclim.ant plugin's resources directory), which provides
taglist support for ant build files.

.. code-block:: groovy
   :linenos:

   import org.eclim.command.taglist.RegexTaglist;
   import org.eclim.command.taglist.TaglistScript;
   import org.eclim.command.taglist.TagResult;

   /**
    * Processes tags for ant files.
    */
   class AntTags implements TaglistScript
   {
     public TagResult[] execute (String file)
     {
       def regex = null;
       try{
         regex = new RegexTaglist(file);
         regex.addPattern('p', ~/(s?)<project\s+[^>]*?name=['"](.*?)['"]/, "\$2");
         regex.addPattern('i', ~/(s?)<import\s+[^>]*?file=['"](.*?)['"]/, "\$2");
         regex.addPattern('t', ~/(s?)<target\s+[^>]*?name=['"](.*?)['"]/, "\$2");
         regex.addPattern('r', ~/(s?)<property\s+[^>]*?name=['"](.*?)['"]/, "\$2");

         return regex.execute();
       }finally{
         if (regex != null) regex.close();
       }
     }
   }


Lines 14 - 18 are the real meat of the script.  Here we create a new
``RegexTaglist`` instance for our file and add the necessary patterns to match
our tags (project, import, target, and property).  There are a few things to
note about the regular expressions here.  The first is the use of '(s?)' at the
beginning of each.  This tells the java regex matcher to include line breaks
when dealing with the \s operator.  Another thing to note, is that we do not use
^ or $.  Ctags processes the file one line at a time so it's common to use ^ and
$ in your regex, but here we are processing the whole file, so ^ and $ would
denote the start and end of the file, not a line.

The third argument to ``addPattern`` also deserves some explanation.  This value
is the substitution to be made on the matched segment of the file, which denotes
the tag name.  In this instance we use $2 (groovy requires that the $ be
escaped), which means that the tag name should be taken from the second group of
the matched text.  Note, that the first group is taken by (s?).

So, to summarize the meaning of line 15: Match the 'project' element and use the
value of the 'name' attribute of that element as the tag name.

If you decide that you'd rather perform the ctags standard line by line regex
matching, that can be accomplished just as easily.  To show an example of just
that, let's look at the ``jproperties.groovy`` script (found in the
org.eclim.jdt plugin's resources directory) which is used to process java
property files.

.. code-block:: groovy
   :linenos:

   import java.io.File;

   import org.eclim.command.taglist.TaglistScript;
   import org.eclim.command.taglist.TagResult;

   /**
    * Processes tags for java property files.
    */
   class PropertiesTags implements TaglistScript
   {
     public TagResult[] execute (String file)
     {
       def results = [];
       def lineNumber = 0;
       new File(file).eachLine {
         line -> processTag(line, ++lineNumber, file, results)
       };

       return (TagResult[])results.toArray(new TagResult[results.size()]);
     }

     void processTag (line, lineNumber, file, results)
     {
       def matcher = line =~ /^\s*([^#]+)\s*=.*/;
       if(matcher.matches()){
         def name = matcher[0][1];
         def tag = new TagResult(
           file:file, pattern:line, line:lineNumber, kind:'p', name:name);

         results.add(tag);
       }
     }
   }

In this script you can see that on lines 15 - 17, it makes use of the groovy
added ``eachLine`` method of ``File`` to process each line individually.


Configuration
--------------

Vim Variables

.. _g\:EclimTaglistEnabled:

- **g:EclimTaglistEnabled** (Default: 1) -
  If set to 0, disables usage of eclim's ctags implementation.

.. _taglisttoo:

TaglistToo: Alternate taglist implementation
--------------------------------------------

The taglist_ plugin written by Yegappan Lakshmanan is an excellent enhancement
to vim which provides an outline of the current file.  However, in my usage
I've found that there were enhancements that I wanted to make to it, but since
the plugin was written to support vim prior to 7.0, the data structures used to
hold and display the tags are very difficult to modify.  So, I decide to
implement an alternate version targeting vim 7.x which would be easier to
enhance.  Please note though, that this new version does not duplicate **all**
of the functionality provided by the original (although I may add more of those
features if user feedback warrants it).

.. note::

  The eclim taglist will not be activated if it detects that you have the
  original taglist plugin installed.  So if you want to try the eclim version
  out, please rename your taglist.vim to taglist.vim.bak or move it out of your
  plugins directory.

Here is a list of enhancements vs unimplemented features:

**Enhancements**

- Supports an extension mechanism allowing the taglist display to be customized
  by file type.
- Provides a custom display for java, javascript, and python files which groups
  methods and variables by object/class for easier viewing and navigation.
- Supports denoting tags based on their visibiltiy (+public, -private, \*static,
  #protected).

**Unimplemented features:**

- Drop down list in gvim with the list of tags.
- Tag re-sorting
- Vim session support
- Support for tags for more than one file in the taglist window.
- ... possibly others.

Other than the feature differences the behavior of the eclim taglist is very
similar to the original taglist. To open / close the taglist simply execute the
command **:TlistToo**.

In an attempt to make your transition from the original taglist to the eclim
taglist as easy as possible, the eclim taglist also supports some of the same
configuration variables\:

- **g:Tlist_Ctags_Cmd** - Sets the location or your ctags executable (if not
  configured it tries exuberant-ctags, ctags, ctags.exe, and tags on your path).
- **g:Tlist_Auto_Open** (Defaults to 0, disabled) - When non-zero, the taglist
  will auto open at vim startup for files that have taglist support.
- **g:tlist_{ft}_settings** - Supports file type specific configuration, but
  unlike the original taglist, uses a dictionary of taglist key to tag title.

  Ex\:

  .. code-block:: vim

    let g:tlist_ant_settings = {
        \ 'p': 'project',
        \ 'i': 'import',
        \ 'r': 'property',
        \ 't': 'target'
      \ }


.. _taglist: http://www.vim.org/scripts/script.php?script_id=273
.. _taglist documentation: http://vim-taglist.sourceforge.net/manual.html#taglist-extend
.. _configured: http://vim-taglist.sourceforge.net/manual.html#%27Tlist_Ctags_Cmd%27
.. _groovy: http://groovy.codehaus.org
