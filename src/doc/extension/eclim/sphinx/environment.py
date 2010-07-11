"""
Copyright (C) 2005 - 2010  Eric Van Dewoestine

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
"""
import re

from docutils import nodes

from sphinx import addnodes
from sphinx.directives import additional_xref_types
from sphinx.environment import BuildEnvironment, NoUri, SphinxContentsFilter
from sphinx.util import docname_join

class EclimBuildEnvironment(BuildEnvironment):

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
    for toctreenode in doctree.traverse(addnodes.toctree):
    #  result = self.resolve_toctree(docname, builder, toctreenode,
    #                                prune=prune_toctrees)
    #  if result is None:
    #    toctreenode.replace_self([])
    #  else:
    #    toctreenode.replace_self(result)
       toctreenode.replace_self([])
# EV: end remove inline site toc

    return doctree

  def resolve_references(self, doctree, fromdocname, builder):
    """
    Straight copy from BuildEnvironment. Changes noted.
    """
    reftarget_roles = set(('token', 'term', 'citation'))
    # add all custom xref types too
    reftarget_roles.update(i[0] for i in additional_xref_types.values())

    for node in doctree.traverse(addnodes.pending_xref):
      contnode = node[0].deepcopy()
      newnode = None

      typ = node['reftype']
      target = node['reftarget']

      try:
        if typ == 'ref':
          if node['refcaption']:
            # reference to anonymous label; the reference uses the supplied
            # link caption
            docname, labelid = self.anonlabels.get(target, ('',''))
            sectname = node.astext()

            # EV: hack to support g:MyVar refuri in vim docs
            if not isinstance(self, EclimHtmlBuildEnvironment) and\
               labelid.startswith('g-'):
              labelid = re.sub(r'.*<(.*)>.*', r'\1', node.rawsource)

            if not docname:
              self.warn(fromdocname, 'undefined label: %s' % target,
                        node.line)
          else:
            # reference to the named label; the final node will contain the
            # section name after the label
            docname, labelid, sectname = self.labels.get(target, ('','',''))

# EV: check anonlabels as well so i can do this> :ref:`:VcsLog`
            if not docname:
              docname, labelid = self.anonlabels.get(target, ('',''))
              sectname = node.astext()

            if not docname:
              self.warn(
                  fromdocname,
                  'undefined label: %s' % target + ' -- if you '
                  'don\'t give a link caption the label must '
                  'precede a section header.', node.line)
          if docname:
            newnode = nodes.reference('', '')
            innernode = nodes.emphasis(sectname, sectname)
            if docname == fromdocname:
              newnode['refid'] = labelid
            else:
              # set more info in contnode in case the get_relative_uri call
              # raises NoUri, the builder will then have to resolve these
              contnode = addnodes.pending_xref('')
              contnode['refdocname'] = docname
              contnode['refsectname'] = sectname
              newnode['refuri'] = builder.get_relative_uri(
                  fromdocname, docname)
              if labelid:
                  newnode['refuri'] += '#' + labelid
            newnode.append(innernode)
          else:
            newnode = contnode
        elif typ == 'doc':
          # directly reference to document by source name;
          # can be absolute or relative
          docname = docname_join(fromdocname, target)
          if docname not in self.all_docs:
            self.warn(fromdocname, 'unknown document: %s' % docname, node.line)
            newnode = contnode
          else:
            if node['refcaption']:
              # reference with explicit title
              caption = node.astext()
            else:
              caption = self.titles[docname].astext()
            innernode = nodes.emphasis(caption, caption)
            newnode = nodes.reference('', '')
            newnode['refuri'] = builder.get_relative_uri(fromdocname, docname)
            newnode.append(innernode)
        elif typ == 'keyword':
          # keywords are referenced by named labels
          docname, labelid, _ = self.labels.get(target, ('','',''))
          if not docname:
            #self.warn(fromdocname, 'unknown keyword: %s' % target)
            newnode = contnode
          else:
            newnode = nodes.reference('', '')
            if docname == fromdocname:
              newnode['refid'] = labelid
            else:
              newnode['refuri'] = builder.get_relative_uri(
                  fromdocname, docname) + '#' + labelid
              newnode.append(contnode)
        elif typ == 'option':
          progname = node['refprogram']
          docname, labelid = self.progoptions.get((progname, target), ('', ''))
          if not docname:
            newnode = contnode
          else:
            newnode = nodes.reference('', '')
            if docname == fromdocname:
              newnode['refid'] = labelid
            else:
              newnode['refuri'] = builder.get_relative_uri(
                fromdocname, docname) + '#' + labelid
            newnode.append(contnode)
        elif typ in reftarget_roles:
          docname, labelid = self.reftargets.get((typ, target), ('', ''))
          if not docname:
            if typ == 'term':
              self.warn(node['refdoc'],
                        'term not in glossary: %s' % target,
                        node.line)
            elif typ == 'citation':
              self.warn(node['refdoc'],
                        'citation not found: %s' % target,
                        node.line)
            newnode = contnode
          else:
            newnode = nodes.reference('', '')
            if docname == fromdocname:
              newnode['refid'] = labelid
            else:
              newnode['refuri'] = builder.get_relative_uri(
                  fromdocname, docname, typ) + '#' + labelid
            newnode.append(contnode)
        elif typ == 'mod' or typ == 'obj' and target in self.modules:
          docname, synopsis, platform, deprecated = \
              self.modules.get(target, ('','','', ''))
          if not docname:
            newnode = builder.app.emit_firstresult(
              'missing-reference', self, node, contnode)
            if not newnode:
              newnode = contnode
          elif docname == fromdocname:
            # don't link to self
            newnode = contnode
          else:
            newnode = nodes.reference('', '')
            newnode['refuri'] = builder.get_relative_uri(
                fromdocname, docname) + '#module-' + target
            newnode['reftitle'] = '%s%s%s' % (
                (platform and '(%s) ' % platform),
                synopsis, (deprecated and ' (deprecated)' or ''))
            newnode.append(contnode)
        elif typ in self.descroles:
          # "descrefs"
          modname = node['modname']
          clsname = node['classname']
          searchorder = node.hasattr('refspecific') and 1 or 0
          name, desc = self.find_desc(modname, clsname,
                                      target, typ, searchorder)
          if not desc:
            newnode = builder.app.emit_firstresult(
              'missing-reference', self, node, contnode)
            if not newnode:
              newnode = contnode
          else:
            newnode = nodes.reference('', '')
            if desc[0] == fromdocname:
              newnode['refid'] = name
            else:
              newnode['refuri'] = (
                  builder.get_relative_uri(fromdocname, desc[0])
                  + '#' + name)
            newnode['reftitle'] = name
            newnode.append(contnode)
        else:
          raise RuntimeError('unknown xfileref node encountered: %s' % node)
      except NoUri:
        newnode = contnode
      if newnode:
        node.replace_self(newnode)

    for node in doctree.traverse(addnodes.only):
      try:
        ret = builder.tags.eval_condition(node['expr'])
      except Exception, err:
        self.warn(fromdocname, 'exception while evaluating only '
                  'directive expression: %s' % err, node.line)
        node.replace_self(node.children)
      else:
        if ret:
          node.replace_self(node.children)
        else:
          node.replace_self([])

    # allow custom references to be resolved
    builder.app.emit('doctree-resolved', doctree, fromdocname)


class EclimHtmlBuildEnvironment(EclimBuildEnvironment):

  def __init__(self, *args, **kwargs):
    self.main_tocs = {}
    BuildEnvironment.__init__(self, *args, **kwargs)

  def build_toc_from(self, docname, document):
    """
    Copied from BuildEnvironment. Changes Noted
    """
    numentries = [0] # nonlocal again...

    try:
      maxdepth = int(self.metadata[docname].get('tocdepth', 0))
    except ValueError:
      maxdepth = 0

    def traverse_in_section(node, cls):
      """Like traverse(), but stay within the same section."""
      result = []
      if isinstance(node, cls):
        result.append(node)
      for child in node.children:
        if isinstance(child, nodes.section):
          continue
        result.extend(traverse_in_section(child, cls))
      return result

# EV: added args 'main' and 'title_visited'
    def build_toc(node, depth=1, main=False, title_visited=False):
      entries = []
      for sectionnode in node:
# EV: added or condition on 'main' and 'title_visited'
        # find all toctree nodes in this section and add them
        # to the toc (just copying the toctree node which is then
        # resolved in self.get_and_resolve_doctree)
        if not isinstance(sectionnode, nodes.section) or (main and title_visited):
          for toctreenode in traverse_in_section(sectionnode,
                                                 addnodes.toctree):
            item = toctreenode.copy()
            entries.append(item)
            # important: do the inventory stuff
            self.note_toctree(docname, toctreenode)
          continue

        title = sectionnode[0]
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
          anchorname = '#' + sectionnode['ids'][0]
        numentries[0] += 1
        reference = nodes.reference('', '', refuri=docname,
                                    anchorname=anchorname,
                                    *nodetext)
        para = addnodes.compact_paragraph('', '', reference)
        item = nodes.list_item('', para)
        if maxdepth == 0 or depth < maxdepth:
# EV: set 'main' and 'title_visited' args
          item += build_toc(sectionnode, depth+1, main=main, title_visited=True)
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
