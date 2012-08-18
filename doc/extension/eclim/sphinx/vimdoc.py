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

from sphinx.builders.text import TextBuilder
from sphinx.util.nodes import make_refnode
from sphinx.writers.text import TextTranslator, TextWriter

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
  sphinx.add_builder(VimdocBuilder)
  sphinx.connect('missing-reference', missing_reference)
