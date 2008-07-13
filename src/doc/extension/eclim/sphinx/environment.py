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


  def get_and_resolve_doctree(self, docname, builder, doctree=None,
                              prune_toctrees=True):
    """
    Copied from BuildEnvironment. Changes Noted.
    """
    if doctree is None:
      doctree = self.get_doctree(docname)

    # resolve all pending cross-references
    self.resolve_references(doctree, docname, builder)

    # now, resolve all toctree nodes
# EV: remove inline site toc
    #for toctreenode in doctree.traverse(addnodes.toctree):
    #  result = self.resolve_toctree(docname, builder, toctreenode,
    #                                prune=prune_toctrees)
    #  if result is None:
    #    toctreenode.replace_self([])
    #  else:
    #    toctreenode.replace_self(result)
# EV: end remove inline site toc

    return doctree


  def build_toc_from(self, docname, document):
    """
    Copied from BuildEnvironment. Changes Noted
    """
    numentries = [0] # nonlocal again...

    try:
      maxdepth = int(self.metadata[docname].get('tocdepth', 0))
    except ValueError:
      maxdepth = 0

# EV: added args 'main' and 'title_visited'
    def build_toc(node, depth=1, main=False, title_visited=False):
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
# EV: added or condition on 'main' and 'title_visited'
        if not isinstance(subnode, nodes.section) or (main and title_visited):
          continue
        title = subnode[0]
# EV: set title_visited = True
#        title_visited = True
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
                                    anchorname=anchorname,
                                    *nodetext)
        para = addnodes.compact_paragraph('', '', reference)
        item = nodes.list_item('', para)
        if maxdepth == 0 or depth < maxdepth:
# EV: set 'main' and 'title_visited' args
          item += build_toc(subnode, depth+1, main=main, title_visited=True)
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
# EV: end main toc

    toc = build_toc(document)
    if toc:
      self.tocs[docname] = toc
    else:
      self.tocs[docname] = nodes.bullet_list('')
    self.toc_num_entries[docname] = numentries[0]
