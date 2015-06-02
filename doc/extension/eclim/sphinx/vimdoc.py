"""
Copyright (C) 2005 - 2015  Eric Van Dewoestine

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
import os
import re
import sys

from docutils import nodes

from sphinx.builders.text import TextBuilder
from sphinx.roles import XRefRole
from sphinx.util.nodes import make_refnode
from sphinx.writers import text

class VimdocBuilder(TextBuilder):
  name = 'vimdoc'

  def prepare_writing(self, docnames):
    """
    Straight copy from TextBuilder, just using VimdocWriter instead of
    TextWriter.
    """
    self.writer = VimdocWriter(self)

  def get_target_uri(self, docname, typ=None):
    path = os.path.normpath(os.path.join(self.env.srcdir, docname))
    relpath = os.path.relpath(path, self.env.srcdir)
    return relpath.replace(os.path.sep, '-')

class VimdocWriter(text.TextWriter):

  def translate(self):
    """
    Straight copy from TextWriter, just using VimdocTranslator instead of
    TextTranslator.
    """
    visitor = VimdocTranslator(self.document, self.builder)
    self.document.walkabout(visitor)
    self.output = visitor.body

    page = os.path.relpath(
      self.document.settings._source,
      self.builder.env.srcdir
    )

    suffixes = self.builder.config.source_suffix
    # retain backwards compatibility with sphinx < 1.3
    if isinstance(suffixes, basestring):
      suffixes = [suffixes]

    for suffix in suffixes:
      if page.endswith(suffix):
        page = '%s.html' % page[:-len(suffix)]
        break

    # add page tag and vim modline
    self.output = '*%s*\n\n%s\n\nvim:ft=eclimhelp' % (
      page.replace(os.path.sep, '-'),
      self.output.strip(),
    )

# only wrap on whitespace and don't break long words since it can break long
# links
text.TextWrapper.wordsep_re = re.compile(r'(\s+)')
def my_wrap(txt, width=text.MAXWIDTH, **kwargs):
    w = text.TextWrapper(width=width, break_long_words=False, **kwargs)
    return w.wrap(txt)
text.my_wrap = my_wrap


class VimdocTranslator(text.TextTranslator):

  TARGET = re.compile(r'(^\.\.\s+_|\\|:$)')
  RELEASE = re.compile(r'^Eclim (\d+\.\d+\.\d+)$')

  def _idToRefUri(self, value):
    """
    Translate some auto generate #id\d uris to the original target.
    """

    # The only real eclim specific bit, to deal with release links from the
    # index page.
    match = VimdocTranslator.RELEASE.match(value)
    if match:
      return match.group(1)
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
      try:
        value = unicode(em.children[0])
      except NameError:
        # python 3
        value = str(em.children[0])
      refuri = node.get('refuri')

      # attempt to translate #id\d+ into the original names
      if refuri and re.search(r'#id\d+', refuri):
        refuri = self._idToRefUri(value)

      # handle anchors into the document (don't break vim autoload function
      # references!)
      elif refuri and re.search(r'\b(?<!eclim)#', refuri):
        refuri = refuri.split('#', 1)[1]

      # the link target and text are the same, link the text
      if (not refuri or refuri == value) and \
         re.search(r'^(:|g:|org\.)', value):
        self.add_text('|')

  def depart_reference(self, node):
    # internal references
    if node.children and isinstance(node.children[0], nodes.emphasis):
      em = node.children[0]
      try:
        value = unicode(em.children[0])
      except NameError:
        # python 3
        value = str(em.children[0])
      refuri = node.get('refuri')

      # attempt to translate #id\d+ into the original names
      if refuri and re.search(r'id\d+', refuri):
        refuri = self._idToRefUri(value)

      # handle anchors into the document (don't break vim autoload function
      # references!)
      elif refuri and re.search(r'\b(?<!eclim)#', refuri):
        refuri = refuri.split('#', 1)[1]

      # the link target and text are the same, so link the text
      if (not refuri or refuri == value) and \
         re.search(r'^(:|g:|org\.)', value):
        # lame edge case
        if value.startswith(':Validate'):
          self.add_text('_' + node.get('refuri').rsplit('-', 1)[-1])
        self.add_text('|')

      # the link target and text differ
      elif refuri:
        self.add_text(' (|%s|)' % refuri)

    # external references
    elif 'refuri' in node:
      self.add_text(' (%s)' % node.get('refuri'))

  def visit_target(self, node):
    refid = node.get('refid')
    if refid:
      value = VimdocTranslator.TARGET.sub('', node.rawsource)
      value = value.replace('/', '-')
      self.add_text('*%s' % value)

  def depart_target(self, node):
    refid = node.get('refid')
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

  def visit_plantuml(self, node):
    self.add_text('[diagram]')

  def depart_plantuml(self, node):
    pass

# Custom missing_reference event listeners to handle:
#   - references like> :ref:`:ProjectCreate`
#   - not tranlateing references from> g:EclimMakeLCD to g-eclimmakelcd

# for html docs
def missing_reference_html(app, env, node, contnode):
  if 'refdomain' in node and node['refdomain']:
    domain = env.domains[node['refdomain']]
    if node['reftype'] == 'ref':
        docname, labelid = domain.data['anonlabels'].get(node['reftarget'], ('',''))
        if docname:
          return make_refnode(
            app.builder, node['refdoc'], docname, labelid, contnode)

# for vimdocs
def missing_reference_vimdoc(app, env, node, contnode):
  if 'refdomain' in node and node['refdomain']:
    domain = env.domains[node['refdomain']]
    if node['reftype'] == 'ref':
        docname, _ = domain.data['anonlabels'].get(node['reftarget'].lower(), ('',''))
        if docname:
          return make_refnode(
            app.builder, node['refdoc'], docname, node['reftarget'], contnode)

def setup(sphinx):
  if VimdocBuilder.name in sys.argv:
    sphinx.add_builder(VimdocBuilder)
    sphinx.connect('missing-reference', missing_reference_vimdoc)
    # prevent sphinx from lower casing the references so we can display then as
    # intended with some help from missing_reference above.
    sphinx.add_role_to_domain(
      'std', 'ref', XRefRole(innernodeclass=nodes.emphasis, warn_dangling=True))

  else:
    sphinx.connect('missing-reference', missing_reference_html)

# python 2 and 3 compatibility
try:
    basestring
except:
    basestring = str
