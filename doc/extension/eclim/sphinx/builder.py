"""
Copyright (C) 2005 - 2012  Eric Van Dewoestine

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

from eclim.pygments import GroovyLexer

from sphinx import addnodes, highlighting
from sphinx.builders.html import StandaloneHTMLBuilder
from sphinx.builders.text import TextBuilder
from sphinx.util import url_re
from sphinx.util.nodes import clean_astext, make_refnode
from sphinx.writers.text import TextTranslator, TextWriter

class EclimBuilder(StandaloneHTMLBuilder):
  name = 'eclim'

  def __init__(self, *args, **kwargs):
    StandaloneHTMLBuilder.__init__(self, *args, **kwargs)
    self.toctrees = {}
    self.index_node = nodes.list_item('', addnodes.compact_paragraph(''))

  def write(self, build_docnames, updated_docnames, method='update'):
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

  def get_doc_context(self, docname, body, metatags):
    """ Copied from StandaloneHTMLBuilder, changes noted. """
    # find out relations
    prev = next = None
    parents = []
    rellinks = self.globalcontext['rellinks'][:]
    related = self.relations.get(docname)
    titles = self.env.titles
# EV: don't include next/prev rellinks
#    if related and related[2]:
#      try:
#        next = {
#            'link': self.get_relative_uri(docname, related[2]),
#            'title': self.render_partial(titles[related[2]])['title']
#        }
#        rellinks.append((related[2], next['title'], 'N', _('next')))
#      except KeyError:
#        next = None
#    if related and related[1]:
#      try:
#        prev = {
#            'link': self.get_relative_uri(docname, related[1]),
#            'title': self.render_partial(titles[related[1]])['title']
#        }
#        rellinks.append((related[1], prev['title'], 'P', _('previous')))
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
    title = self.env.longtitles.get(docname)
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

    # local TOC and global TOC tree
    self_toc = self.env.get_toc_for(docname, self)
    toc = self.render_partial(self_toc)['fragment']

    return dict(
      parents = parents,
      prev = prev,
      next = next,
      title = title,
      meta = meta,
      body = body,
      metatags = metatags,
      rellinks = rellinks,
      sourcename = sourcename,
# EV: new main_toc
      main_tocs = main_tocs,
      toc = toc,
      # only display a TOC if there's more than one item to show
      display_toc = (self.env.toc_num_entries[docname] > 1),
    )

  def _entries_from_toctree(self, docname, toctreenode, separate=False, subtree=True):
    """
    Copied from sphinx.environment.  Modified to utilize list items instead of
    the old version which had an independent bullet_list for each entry.
    """
    refs = [(e[0], str(e[1])) for e in toctreenode['entries']]
# EV: instead of a [], use a bullet_list
    entries = nodes.bullet_list()
    for (title, ref) in refs:
      try:
        if url_re.match(ref):
          reference = nodes.reference('', '', internal=False,
                                      refuri=ref, anchorname='',
                                      *[nodes.Text(title)])
          para = addnodes.compact_paragraph('', '', reference)
          item = nodes.list_item('', para)
          toc = nodes.bullet_list('', item)
        elif ref == 'self':
          # 'self' refers to the document from which this
          # toctree originates
          ref = toctreenode['parent']
          if not title:
            title = clean_astext(self.titles[ref])
          reference = nodes.reference('', '', internal=True,
                                      refuri=ref,
                                      anchorname='',
                                      *[nodes.Text(title)])
          para = addnodes.compact_paragraph('', '', reference)
          item = nodes.list_item('', para)
          # don't show subitems
          toc = nodes.bullet_list('', item)
        else:
          # EV: get the tocs reference using self.env.main_tocs instead of just
          # self.tocs
          #toc = self.tocs[ref].deepcopy()
          toc = self.env.main_tocs[ref].deepcopy()
          if title and toc.children and len(toc.children) == 1:
            child = toc.children[0]
            for refnode in child.traverse(nodes.reference):
              if refnode['refuri'] == ref and not refnode['anchorname']:
                refnode.children = [nodes.Text(title)]
        if not toc.children:
          # empty toc means: no titles will show up in the toctree
          self.warn(docname,
                    'toctree contains reference to document '
                    '%r that doesn\'t have a title: no link '
                    'will be generated' % ref)
      except KeyError:
        # this is raised if the included file does not exist
        self.warn(docname, 'toctree contains reference to '
                  'nonexisting document %r' % ref)
      else:
# EV: copied over from 0.6.3, but outside of Environment, we don't have the
# titles_only var.
#        # if titles_only is given, only keep the main title and
#        # sub-toctrees
#        if titles_only:
#          # delete everything but the toplevel title(s)
#          # and toctrees
#          for toplevel in toc:
#            # nodes with length 1 don't have any children anyway
#            if len(toplevel) > 1:
#              subtrees = toplevel.traverse(addnodes.toctree)
#              toplevel[1][:] = subtrees

        # resolve all sub-toctrees
        for toctreenode in toc.traverse(addnodes.toctree):
          #i = toctreenode.parent.index(toctreenode) + 1
          #for item in self._entries_from_toctree(toctreenode, subtree=True):
          #  toctreenode.parent.insert(i, item)
          #  i += 1
          #toctreenode.parent.remove(toctreenode)
          toctreenode.parent.replace_self(
              self._entries_from_toctree(docname, toctreenode, subtree=True))

# EV: append each child as a list item in the bullet_list.
        #if separate:
        #  entries.append(toc)
        #else:
        #  entries.extend(toc.children)
        for child in toc.children:
          entries.append(child)

# EV: pass the entries in as a single element instead of a list of elements.
#    if not subtree and not separate:
#        ret = nodes.bullet_list()
#        ret += entries
#        return [ret]
#    return entries
    return addnodes.compact_paragraph('', '', entries)

class VimdocBuilder(TextBuilder):
  name = 'vimdoc'

  def prepare_writing(self, docnames):
    """
    Straight copy from TextBuilder, just using VimdocWriter instead of
    TextWriter.
    """
    self.writer = VimdocWriter(self)

class VimdocWriter(TextWriter):

  def translate(self):
    """
    Straight copy from TextWriter, just using VimdocTranslator instead of
    TextTranslator.
    """
    visitor = VimdocTranslator(self.document, self.builder)
    self.document.walkabout(visitor)
    self.output = visitor.body

# EV: add vim modline
    self.output = self.output.strip() + '\n\nvim:ft=eclimhelp'


# EV: don't wrap on '-' so we don't break some vimdoc links
import textwrap
new_wordsep_re = re.compile(
        r'(\s+|'                                  # any whitespace
        r'(?<=\s)(?::[a-z-]+:)?`\S+|'             # interpreted text start
#        r'[^\s\w]*\w+[a-zA-Z]-(?=\w+[a-zA-Z])|'   # hyphenated words
        r'(?<=[\w\!\"\'\&\.\,\?])-{2,}(?=\w))')   # em-dash
textwrap.TextWrapper.wordsep_re = new_wordsep_re


class VimdocTranslator(TextTranslator):

  TARGET = re.compile(r'(^\.\.\s+_|\\|:$)')

  def _toRefUri(self, value):
    """
    EV: Helper function which emulates the docutils conversion of a string to a
    refuri.
    """
    value = value.lower()
    value = value.replace(':', '')
    return value

  def depart_image(self, node):
    """
    Missing from sphinx's text translator and results in errors when ommitted.
    """
    pass

  def depart_line(self, node):
    self.add_text('\n')

  def depart_list_item(self, node):
    """
    Straight copy, just change the leading '*' to a '-'.
    """
    if self.list_counter[-1] == -1:
      self.end_state(first='- ', end=None)
    elif self.list_counter[-1] == -2:
      pass
    else:
      self.end_state(first='%s. ' % self.list_counter[-1], end=None)

  def visit_reference(self, node):
    if node.children and isinstance(node.children[0], nodes.emphasis):
      em = node.children[0]
      value = unicode(em.children[0])
      refuri = node.attributes.get('refuri')
      if refuri and refuri.startswith('#'):
        refuri = refuri[1:]
      if (
        not refuri or
        refuri == self._toRefUri(value) or
        re.search(r'id\d+', refuri)
      ) and (
        value.startswith(':') or
        value.startswith('g:') or
        value.startswith('org.')
      ):
        self.add_text('|')

  def depart_reference(self, node):
    # internal references
    if node.children and isinstance(node.children[0], nodes.emphasis):
      em = node.children[0]
      value = unicode(em.children[0])
      refuri = node.attributes.get('refuri')
      if refuri and refuri.startswith('#'):
        refuri = refuri[1:]
      if (
        not refuri or
        refuri == self._toRefUri(value) or
        re.search(r'id\d+', refuri)
      ) and (
        value.startswith(':') or
        value.startswith('g:') or
        value.startswith('org.')
      ):
        # lame edge case
        if value.startswith(':Validate'):
          self.add_text('_' + node.attributes.get('refuri').rsplit('-', 1)[-1])
        self.add_text('|')
      elif refuri:
        self.add_text(' (|%s|)' % refuri)

    # external references
    elif 'refuri' in node:
      self.add_text(' (%s)' % node.attributes.get('refuri'))

  def visit_target(self, node):
    refid = node.attributes.get('refid')
    if refid:
      value = VimdocTranslator.TARGET.sub('', node.rawsource)
      value = value.replace('/', '-')
      self.add_text('*%s' % value)

  def depart_target(self, node):
    refid = node.attributes.get('refid')
    if refid:
      self.add_text('* ')

  #def visit_index(self, node):
  #  raise nodes.SkipNode

  def visit_literal_block(self, node):
    self.add_text('>')
    self.new_state()

  def depart_literal_block(self, node):
    self.add_text('\n')
    self.end_state(wrap=False)
    self.add_text('<')
    # hack to ensure a newline after ending '<'
    self.new_state()
    self.add_text('')
    self.end_state()

  def visit_emphasis(self, node):
    pass

  def depart_emphasis(self, node):
    pass

  def visit_literal_emphasis(self, node):
    pass

  def depart_literal_emphasis(self, node):
    pass

  def visit_strong(self, node):
    pass

  def depart_strong(self, node):
    pass

  def visit_title_reference(self, node):
    pass

  def depart_title_reference(self, node):
    pass

  def visit_literal(self, node):
    pass

  def depart_literal(self, node):
    pass


# EV: Custom missing_reference event listener to handle:
#     - references like> :ref:`:ProjectCreate`
def missing_reference(app, env, node, contnode):
  if 'refdomain' in node and node['refdomain']:
    domain = env.domains[node['refdomain']]
    if node['reftype'] == 'ref':
        docname, labelid = domain.data['anonlabels'].get(node['reftarget'], ('',''))
        if docname:
          return make_refnode(
            app.builder, node['refdoc'], docname, labelid, contnode)
#    print (domain, node['reftype'], node['refdoc'], node['reftarget'])


def setup(sphinx):
  highlighting.lexers['groovy'] = GroovyLexer()
  sphinx.add_builder(EclimBuilder)
  sphinx.add_builder(VimdocBuilder)
  sphinx.connect('missing-reference', missing_reference)
