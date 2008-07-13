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

from eclim.pygments import GroovyLexer
from eclim.sphinx.environment import EclimBuildEnvironment

from sphinx import addnodes, highlighting
from sphinx.builder import StandaloneHTMLBuilder, ENV_PICKLE_FILENAME
from sphinx.util.console import bold

class EclimBuilder (StandaloneHTMLBuilder):
  name = 'eclim'

  def __init__ (self, *args, **kwargs):
    StandaloneHTMLBuilder.__init__(self, *args, **kwargs)
    self.toctrees = {}
    self.index_node = nodes.list_item('', addnodes.compact_paragraph(''))

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
        self.env = EclimBuildEnvironment.frompickle(self.config,
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

  def get_doc_context(self, docname, body):
    """Collect items for the template context of a page."""
    # find out relations
    prev = next = None
    parents = []
    rellinks = self.globalcontext['rellinks'][:]
    related = self.relations.get(docname)
    titles = self.env.titles
# EV: don't include next/prev rellinks
#    if related and related[2]:
#      try:
#        next = {'link': self.get_relative_uri(docname, related[2]),
#                'title': self.render_partial(titles[related[2]])['title']}
#        rellinks.append((related[2], next['title'], 'N', 'next'))
#      except KeyError:
#        next = None
#    if related and related[1]:
#      try:
#        prev = {'link': self.get_relative_uri(docname, related[1]),
#                'title': self.render_partial(titles[related[1]])['title']}
#        rellinks.append((related[1], prev['title'], 'P', 'previous'))
#      except KeyError:
#        # the relation is (somehow) not in the TOC tree, handle that gracefully
#        prev = None
    while related and related[0]:
      try:
        parents.append(
            {'link': self.get_relative_uri(docname, related[0]),
             'title': self.render_partial(titles[related[0]])['title']})
      except KeyError:
        pass
      related = self.relations.get(related[0])
    if parents:
      parents.pop() # remove link to the master file; we have a generic
                      # "back to index" link already
    parents.reverse()

    # title rendered as HTML
    title = titles.get(docname)
    title = title and self.render_partial(title)['title'] or ''
    # the name for the copied source
    sourcename = self.config.html_copy_source and docname + '.txt' or ''

    # metadata for the document
    meta = self.env.metadata.get(docname)

# EV: genterate a 'main' toc for the current page.
    main_tocs = []
    for ii, toc in enumerate(self.toctrees.get('index')):
      entries = self._entries_from_toctree(docname, toc)

      # add link to the root.
      if ii == 0:
        index_node = self.index_node.deepcopy()
        index_node[0].append(
          nodes.reference(text='About / News', anchorname='', refuri='index')
        )
        entries[0].insert(0, index_node)

      for node in entries.traverse(nodes.reference):
        if node.hasattr('anchorname'):
          # a TOC reference
          node['refuri'] = self.get_relative_uri(
              docname, node['refuri']) + node['anchorname']
      main_tocs.append(self.render_partial(entries)['fragment'])
# EV: end main toc

    return dict(
      parents = parents,
      prev = prev,
      next = next,
      title = title,
      meta = meta,
      body = body,
      rellinks = rellinks,
      sourcename = sourcename,
# EV: new main_toc
      main_tocs = main_tocs,
      toc = self.render_partial(self.env.get_toc_for(docname))['fragment'],
      # only display a TOC if there's more than one item to show
      display_toc = (self.env.toc_num_entries[docname] > 1),
    )

  def _entries_from_toctree (self, docname, toctreenode, top=True):
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
              self._entries_from_toctree(docname, toctreenode, top=False))
# EV: append each child as a list item in the bullet_list.
        for child in toc.children:
          entries.append(child)
    if entries:
# EV: pass the entries in as a single element instead of a list of elements.
      return addnodes.compact_paragraph('', '', entries)
    return None

def setup (sphinx):
  highlighting.lexers['groovy'] = GroovyLexer()
  sphinx.add_builder(EclimBuilder)
