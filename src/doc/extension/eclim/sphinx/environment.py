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
from docutils import nodes

from sphinx import addnodes
from sphinx.environment import BuildEnvironment, SphinxContentsFilter

class EclimBuildEnvironment (BuildEnvironment):

  def __init__ (self, *args, **kwargs):
    self.main_tocs = {}
    BuildEnvironment.__init__(self, *args, **kwargs)

  def get_and_resolve_doctree(self, docname, builder, doctree=None):
    """
    Copied from BuildEnvironment. Changes Noted.
    """
    if doctree is None:
      doctree = self.get_doctree(docname)

    # resolve all pending cross-references
    self.resolve_references(doctree, docname, builder)

    # now, resolve all toctree nodes
    def _entries_from_toctree(toctreenode, separate=False):
      """Return TOC entries for a toctree node."""
      includefiles = map(str, toctreenode['includefiles'])

      entries = []
      for includefile in includefiles:
        try:
          toc = self.tocs[includefile].deepcopy()
        except KeyError:
          # this is raised if the included file does not exist
          self.warn(docname, 'toctree contains ref to nonexisting '
                    'file %r' % includefile)
        else:
          # resolve all sub-toctrees
          for toctreenode in toc.traverse(addnodes.toctree):
            i = toctreenode.parent.index(toctreenode) + 1
            for item in _entries_from_toctree(toctreenode):
              toctreenode.parent.insert(i, item)
              i += 1
            toctreenode.parent.remove(toctreenode)
          if separate:
            entries.append(toc)
          else:
            entries.extend(toc.children)
      return entries

    for toctreenode in doctree.traverse(addnodes.toctree):
# EV: remove inline site toc
      #maxdepth = toctreenode.get('maxdepth', -1)
      #titleoverrides = toctreenode.get('includetitles', {})
      #tocentries = _entries_from_toctree(toctreenode, separate=True)
      #if tocentries:
      #  newnode = addnodes.compact_paragraph('', '', *tocentries)
      #  # prune the tree to maxdepth and replace titles
      #  if maxdepth > 0:
      #    _walk_depth(newnode, 1, maxdepth, titleoverrides)
      #  # replace titles, if needed
      #  if titleoverrides:
      #    for refnode in newnode.traverse(nodes.reference):
      #      if refnode.get('anchorname', None):
      #        continue
      #      if refnode['refuri'] in titleoverrides:
      #        newtitle = titleoverrides[refnode['refuri']]
      #        refnode.children = [nodes.Text(newtitle)]
      #  toctreenode.replace_self(newnode)
      #else:
      toctreenode.replace_self([])

    # set the target paths in the toctrees (they are not known
    # at TOC generation time)
    for node in doctree.traverse(nodes.reference):
      if node.hasattr('anchorname'):
        # a TOC reference
        node['refuri'] = builder.get_relative_uri(
            docname, node['refuri']) + node['anchorname']

    return doctree


  def build_toc_from(self, docname, document):
    """Build a TOC from the doctree and store it in the inventory."""
    numentries = [0] # nonlocal again...

    def build_toc (node, main=False, title_visited=False):
      entries = []
      for subnode in node:
        if isinstance(subnode, addnodes.toctree):
          # just copy the toctree node which is then resolved
          # in self.get_and_resolve_doctree
          item = subnode.copy()
          entries.append(item)
          # do the inventory stuff
          self.note_toctree(docname, subnode)
          continue
        if not isinstance(subnode, nodes.section) or (title_visited and main):
          continue
        title = subnode[0]
        title_visited = True
        # copy the contents of the section title, but without references
        # and unnecessary stuff
        visitor = SphinxContentsFilter(document)
        title.walkabout(visitor)
        nodetext = visitor.get_entry_text()
        if not numentries[0]:
          # for the very first toc entry, don't add an anchor
          # as it is the file's title anyway
          anchorname = ''
        else:
          anchorname = '#' + subnode['ids'][0]
        numentries[0] += 1
        reference = nodes.reference('', '', refuri=docname,
            anchorname=anchorname, *nodetext)
        para = addnodes.compact_paragraph('', '', reference)
        item = nodes.list_item('', para)
        item += build_toc(subnode, main=main, title_visited=True)
        entries.append(item)
      if entries:
        return nodes.bullet_list('', *entries)
      return []

    # EV: main toc
    main_toc = build_toc(document, main=True)
    if main_toc:
      self.main_tocs[docname] = main_toc
    else:
      self.main_tocs[docname] = nodes.bullet_list('')

    # page toc
    toc = build_toc(document)
    if toc:
      self.tocs[docname] = toc
    else:
      self.tocs[docname] = nodes.bullet_list('')
    self.toc_num_entries[docname] = numentries[0]
