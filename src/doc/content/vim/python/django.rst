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

.. _vim/python/django:

Django
======

.. _DjangoManage:

Django manage.py
----------------

For each project you create with the django framework, django provides you with
a manage.py which can be used to perform various tasks.  To make the invocation
of the manage.py script even easier, eclim provides the command
**:DjangoManage** which can be invoked from any file in the same directory as
your manage.py or in any of the child directories.

**:DjangoManage** supports all the same commands as manage.py and supports
command line completion of command names and app names where supported.

Several of the manage.py commands simply perform an action without generating
much if any output.  However there is also a set of commands which generate sql
statements.  For all of these commands, instead of just running the command in a
shell, **:DjangoManage** will run the command and populate a new buffer with the
resulting output and set the proper file type.


Configuration
-------------

Vim Variables

.. _EclimPythonInterpreter:

- **g:EclimPythonInterpreter** = 'python'

.. _EclimDjangoAdmin:

- **g:EclimDjangoAdmin** = 'django-admin.py'


Django python support
---------------------

.. _DjangoTemplateOpen:

**Locating templates**

The command **:DjangoTemplateOpen** supports finding and opening a template
referenced under the cursor.

Ex.

.. code-block:: python

  # w/ cursor on 'mytemplates/mytemplate.html'
  return render_to_response('mytemplates/mytemplate.html', ...)


.. _DjangoViewOpen:

**Locating views**

The command **:DjangoViewOpen** supports finding and opening a view referenced
under the cursor.

Ex.

.. code-block:: python

    # w/ cursor on 'myproject.myapp.views' or 'my_view' on the second line.
    urlpatterns = patterns('myproject.myapp.views',
        (r'^$', 'my_view'),
    )

.. _DjangoContextOpen:

**Contextually locate file**

The command **:DjangoContextOpen** supports executing **:DjangoViewOpen**,
**:DjangoTemplateOpen**, or **:PythonFindDefinition** depending on the context
of the text under the cursor.

.. _htmldjango:

Django html template support
----------------------------

**Syntax**

Vim ships with a syntax file for django html template files, but eclim builds on
that base to support highlighting of user defined tags and filters (see the
<a href="#configuration">configuration section</a> below.

**Indent**

Using the same settings as the enhanced syntax file, eclim also ships with an
indent script which provides indentation support all of the default django tags
and any user defined tags that have been configured.

**Match It**

Again, using the same set of variables, eclim set the necessary variables to
allow proper matchit.vim support for django default and user defined tags.

**End Tag Completion**

Using the :ref:`g:HtmlDjangoUserBodyElements <htmldjangouserbodyelements>`
setting along with the pre-configured default list of body elements, eclim
includes support for auto completion of ending template tags when you type an
'{%e' or '{% e'.

.. _DjangoFind:

**Contextual Find**

While editing django html templates, the command **:DjangoFind** which will
attempt to locate the relevant resource depending on what is under the cursor.

- If on a user defined tag, attempt to find the tag definition within the python
  tag definition file.

  Ex.

  ::

    {# w/ cursor on 'mytag' #}
    {% mytag somearg %}

- If on a user defined filter, attempt to find the filter definition within the
  python filter definition file.

  Ex.

  ::

    {# w/ cursor on 'myfilter' #}
    {{ somevalue|myfilter }}

- If on the tag/filter definition portion of of a 'load' tag, attempt to
  find the definition file.

  Ex.

  ::

    {# w/ cursor on 'mytags' #}
    {% load mytags %}

- If on a reference to a template for ethier an 'extends' or 'include' tag,
  attempt to find that template file.

  Ex.

  ::

    {# w/ cursor on 'include/mytemplate.html' #}
    {% include "include/mytemplate.html" %}

- If on static file reference, as defined in a 'src' or 'href' attribute
  of an element, attempt to find that static file.

  Ex.

  ::

    {# w/ cursor on '/css/my.css' #}
    <link rel="stylesheet" href="/css/my.css" type="text/css" />

  Note: this functionality requires that
  **g:EclimDjangoStaticPaths** is set to a list of absolute
  or django project relative (relative to directory containing manage.py
  and settings.py) directories.

  Ex.

  .. code-block:: vim

    let g:EclimDjangoStaticPaths = ["../static/"]


Configuration
-------------

Vim Variables

.. _HtmlDjangoUserBodyElements:

- **g:HtmlDjangoUserBodyElements** -
  List of lists, where each list contains the name of the start and end
  tag, as well as any intermediary tags of any custom tags which have a
  body.

  Ex.

  .. code-block:: vim

    let g:HtmlDjangoUserBodyElements = [
        \ ['repeat', 'endrepeat'],
        \ ['try', 'except', 'finally', 'endtry'],
      \ ]

  This setting is used for indentation of the custom tag's body, as well
  as arguments for proper matchit support, end tag completion, and
  syntax highlighting.

.. _HtmlDjangoUserTags:

- **g:HtmlDjangoUserTags** -
  This setting is a list of any non-body tags which don't require indentation or
  matchit support.  The items configured here will be used for syntax
  highlighting.

.. _HtmlDjangoUserFilters:

- **g:HtmlDjangoUserFilters** -
  This settings contains a list of any user defined django filters.  It is
  currently used for syntax highlighting.

.. _HtmlDjangoCompleteEndTag:

- **g:HtmlDjangoCompleteEndTag** (Default: 1) -
  When set to 0, disables the auto completion of end tags.

.. _EclimDjangoStaticPaths:

- **g:EclimDjangoStaticPaths** -
  Used as a list of directories to search when looking for static files (js,
  css, etc). Expected to be a list of absolute or django project relative
  (relative to directory containing manage.py and settings.py) directories.

  Ex.

  .. code-block:: vim

    let g:EclimDjangoStaticPaths = ["../static/"]

.. _EclimDjangoFindAction:

- **g:EclimDjangoFindAction** (Default: "split") -
  For **:DjangoFind** and **:DjangoTemplateOpen**, used as the action to perform
  on the file found.
