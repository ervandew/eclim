"""
Copyright (C) 2005 - 2008  Eric Van Dewoestine

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

@version: $Revision$
"""
from os import path

from docutils import nodes
from docutils.io import StringOutput

from eclim.sphinx.environment import EclimBuildEnvironment

from sphinx import addnodes
from sphinx.builder import StandaloneHTMLBuilder, ENV_PICKLE_FILENAME
from sphinx.util import relative_uri
from sphinx.util.console import bold

class EclimBuilder (StandaloneHTMLBuilder):
  name = 'eclim'

  def __init__ (self, *args, **kwargs):
    StandaloneHTMLBuilder.__init__(self, *args, **kwargs)
    self.toctrees = {}

  def load_env(self):
    """
    Copied from sphinx.builder.Builder and just replaces 'BuildEnvironment' w/
    'EclimBuildEnvironment'.
    """
    if self.env:
      return
    if not self.freshenv:
      try:
          self.info(bold('trying to load pickled env... '), nonl=True)
          self.env = EclimBuildEnvironment.frompickle(
              path.join(self.doctreedir, ENV_PICKLE_FILENAME))
          self.info('done')
      except Exception, err:
          if type(err) is IOError and err.errno == 2:
              self.info('not found')
          else:
              self.info('failed: %s' % err)
          self.env = EclimBuildEnvironment(self.srcdir, self.doctreedir, self.config)
          self.env.find_files(self.config)
    else:
      self.env = EclimBuildEnvironment(self.srcdir, self.doctreedir, self.config)
      self.env.find_files(self.config)
    self.env.set_warnfunc(self.warn)

  def write (self, build_docnames, updated_docnames, method='update'):
    names = build_docnames and updated_docnames and \
        build_docnames + updated_docnames or \
        build_docnames or updated_docnames

    if 'index' not in names:
      names.append('index')

    for docname in names:
      doctree = self.env.get_doctree(docname)
      nodes = doctree.traverse(addnodes.toctree)
      if nodes:
        self.toctrees[docname] = nodes

    StandaloneHTMLBuilder.write(
      self, build_docnames, updated_docnames, method=method
    )

  def write_doc(self, docname, doctree):
    """
    Direct copy from StandaloneHTMLBuilder.  Changes noted.
    """
    destination = StringOutput(encoding='utf-8')
    doctree.settings = self.docsettings

    self.imgpath = relative_uri(self.get_target_uri(docname), '_images')
    self.docwriter.write(doctree, destination)
    self.docwriter.assemble_parts()

    prev = next = None
    parents = []
    related = self.env.toctree_relations.get(docname)
    titles = self.env.titles
    if related:
      try:
        prev = {'link': self.get_relative_uri(docname, related[1]),
                'title': self.render_partial(titles[related[1]])['title']}
      except KeyError:
        # the relation is (somehow) not in the TOC tree, handle that gracefully
        prev = None
      try:
        next = {'link': self.get_relative_uri(docname, related[2]),
                'title': self.render_partial(titles[related[2]])['title']}
      except KeyError:
        next = None
    while related:
        try:
          parents.append(
              {'link': self.get_relative_uri(docname, related[0]),
               'title': self.render_partial(titles[related[0]])['title']})
        except KeyError:
          pass
        related = self.env.toctree_relations.get(related[0])
    if parents:
      parents.pop() # remove link to the master file; we have a generic
                    # "back to index" link already
    parents.reverse()

    title = titles.get(docname)
    if title:
      title = self.render_partial(title)['title']
    else:
      title = ''
    self.globalcontext['titles'][docname] = title
    sourcename = self.config.html_copy_source and docname + '.txt' or ''

# EV: genterate a 'main' toc for the current page.
    main_tocs = []
    for toc in self.toctrees.get('index'):
      entries = self._entries_from_toctree(docname, toc)
      for node in entries.traverse(nodes.reference):
        if node.hasattr('anchorname'):
          # a TOC reference
          node['refuri'] = self.get_relative_uri(
              docname, node['refuri']) + node['anchorname']
      main_tocs.append(self.render_partial(entries)['fragment'])
# EV: end main toc

    ctx = dict(
      title = title,
      sourcename = sourcename,
      body = self.docwriter.parts['fragment'],
# EV: new main_toc
      main_tocs = main_tocs,
# EV: change 'toc' to 'page_toc'
      page_toc = self.render_partial(self.env.get_toc_for(docname))['fragment'],
      # only display a TOC if there's more than one item to show
# EV: change 'display_toc' to 'display_page_toc'
      display_page_toc = (self.env.toc_num_entries[docname] > 1),
      parents = parents,
      prev = prev,
      next = next,
    )

    self.index_page(docname, doctree, title)
    self.handle_page(docname, ctx)

  def _entries_from_toctree (self, docname, toctreenode):
    """
    Copied from sphinx.environment.  Modified to utilize list items instead of
    the old version which had an independent bullet_list for each entry.
    """
    includefiles = map(str, toctreenode['includefiles'])

# EV: instead of a [], use a bullet_list
    entries = nodes.bullet_list()
    for includefile in includefiles:
      try:
        toc = self.env.main_tocs[includefile].deepcopy()
      except KeyError:
        # this is raised if the included file does not exist
        self.env.warn(docname, 'toctree contains ref to nonexisting '
                  'file %r' % includefile)
      else:
        for toctreenode in toc.traverse(addnodes.toctree):
          toctreenode.parent.replace_self(
              self._entries_from_toctree(docname, toctreenode))
# EV: append each child as a list item in the bullet_list.
        for child in toc.children:
          entries.append(child)
    if entries:
# EV: pass the entries in as a single element instead of a list of elements.
      return addnodes.compact_paragraph('', '', entries)
    return None


def setup (sphinx):
  sphinx.add_builder(EclimBuilder)
