import os
import re

from docutils import io, nodes, statemachine, utils
from docutils.parsers.rst import directives
from docutils.parsers.rst.directives.body import CodeBlock, NumberLines
from docutils.parsers.rst.directives.misc import Include
from docutils.utils.error_reporting import SafeString, ErrorString

from eclim.sphinx.vimdoc import VimdocBuilder

class IncludeDirective(Include):
  """
  Override the docutils Include directive to allow the addition of suffixes to
  all references encounterd in the included nodes. This is needed for generating
  vim help files where a tag can only occur in one file, but when including the
  same nodes in multiple documents, duplicate tags can occur unless we append
  suffixes.
  """
  option_spec = Include.option_spec
  option_spec['ref-suffix'] = directives.unchanged

  REF = re.compile(r'(\s*\.\. _(?:\w|\\).*)(:.*)')

  def run(self):
    """Include a file as part of the content of this reST file."""

    # Direct copy from Include with changes noted.

    if not self.state.document.settings.file_insertion_enabled:
      raise self.warning('"%s" directive disabled.' % self.name)
    source = self.state_machine.input_lines.source(
      self.lineno - self.state_machine.input_offset - 1)
    source_dir = os.path.dirname(os.path.abspath(source))

    # CHANGE: for some reason the arg to the include directive is expanded to
    # the full path for the docutils Include directive, but for ours, it
    # remains unchanged from the value supplied in the source, so we have to
    # expand it ourselves
    env = self.state.document.settings.env
    if not os.path.exists(self.arguments[0]):
      self.arguments[0] = env.srcdir + self.arguments[0]

    path = directives.path(self.arguments[0])
    if path.startswith('<') and path.endswith('>'):
      path = os.path.join(self.standard_include_path, path[1:-1])

    path = os.path.normpath(os.path.join(source_dir, path))
    path = utils.relative_path(None, path)
    path = nodes.reprunicode(path)
    encoding = self.options.get(
      'encoding', self.state.document.settings.input_encoding)
    e_handler=self.state.document.settings.input_encoding_error_handler
    tab_width = self.options.get(
      'tab-width', self.state.document.settings.tab_width)
    try:
      self.state.document.settings.record_dependencies.add(path)
      include_file = io.FileInput(source_path=path,
                                  encoding=encoding,
                                  error_handler=e_handler)
    except UnicodeEncodeError as error:
      raise self.severe('Problems with "%s" directive path:\n'
                        'Cannot encode input file path "%s" '
                        '(wrong locale?).' %
                        (self.name, SafeString(path)))
    except IOError as error:
      raise self.severe('Problems with "%s" directive path:\n%s.' %
          (self.name, ErrorString(error)))
    startline = self.options.get('start-line', None)
    endline = self.options.get('end-line', None)
    try:
      if startline or (endline is not None):
        lines = include_file.readlines()
        rawtext = ''.join(lines[startline:endline])
      else:
        rawtext = include_file.read()
    except UnicodeError as error:
      raise self.severe('Problem with "%s" directive:\n%s' %
                        (self.name, ErrorString(error)))
    # start-after/end-before: no restrictions on newlines in match-text,
    # and no restrictions on matching inside lines vs. line boundaries
    after_text = self.options.get('start-after', None)
    if after_text:
      # skip content in rawtext before *and incl.* a matching text
      after_index = rawtext.find(after_text)
      if after_index < 0:
        raise self.severe('Problem with "start-after" option of "%s" '
                          'directive:\nText not found.' % self.name)
      rawtext = rawtext[after_index + len(after_text):]
    before_text = self.options.get('end-before', None)
    if before_text:
      # skip content in rawtext after *and incl.* a matching text
      before_index = rawtext.find(before_text)
      if before_index < 0:
        raise self.severe('Problem with "end-before" option of "%s" '
                          'directive:\nText not found.' % self.name)
      rawtext = rawtext[:before_index]

    include_lines = statemachine.string2lines(rawtext, tab_width,
                                              convert_whitespace=True)
    if 'literal' in self.options:
      # Convert tabs to spaces, if `tab_width` is positive.
      if tab_width >= 0:
        text = rawtext.expandtabs(tab_width)
      else:
        text = rawtext
      literal_block = nodes.literal_block(rawtext, source=path,
                              classes=self.options.get('class', []))
      literal_block.line = 1
      self.add_name(literal_block)
      if 'number-lines' in self.options:
        try:
          startline = int(self.options['number-lines'] or 1)
        except ValueError:
          raise self.error(':number-lines: with non-integer '
                           'start value')
        endline = startline + len(include_lines)
        if text.endswith('\n'):
          text = text[:-1]
        tokens = NumberLines([([], text)], startline, endline)
        for classes, value in tokens:
          if classes:
            literal_block += nodes.inline(value, value,
                                          classes=classes)
          else:
            literal_block += nodes.Text(value, value)
      else:
        literal_block += nodes.Text(text, text)
      return [literal_block]

    if 'code' in self.options:
      self.options['source'] = path
      codeblock = CodeBlock(self.name,
                            [self.options.pop('code')], # arguments
                            self.options,
                            include_lines, # content
                            self.lineno,
                            self.content_offset,
                            self.block_text,
                            self.state,
                            self.state_machine)
      return codeblock.run()

    # CHANGE: add the suffixes to all the references (only do it for vimdocs)
    if isinstance(env.app.builder, VimdocBuilder) and 'ref-suffix' in self.options:
      suffix = self.options['ref-suffix']
      for i, line in enumerate(include_lines):
        # relying on a regex is gross, but it's easy and we just have to worry
        # about the eclim docs, so it'll do.
        match = self.REF.match(line)
        if match:
          include_lines[i] = '%s_%s%s' % (match.group(1), suffix, match.group(2))

    self.state_machine.insert_input(include_lines, path)
    return []

def setup(app):
  app.add_directive('include', IncludeDirective)
