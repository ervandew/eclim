import re
from pygments.lexer import RegexLexer, bygroups, this, using
from pygments.token import Comment, Keyword, Name, Number, Operator, String, Text

class GroovyLexer (RegexLexer):
  name = 'Groovy'
  aliases = ['java']
  filenames = ['*.java']
  mimetypes = ['text/x-java']

  flags = re.MULTILINE | re.DOTALL

  #: optional Comment or Whitespace
  _ws = r'(?:\s|//.*?\n|/[*].*?[*]/)+'

  tokens = {
    'root': [
      # method names
      (r'^(\s*(?:[a-zA-Z_][a-zA-Z0-9_\.]*\s+)+?)'  # return arguments
          r'([a-zA-Z_][a-zA-Z0-9_]*)'                 # method name
          r'(\s*)(\()',                               # signature start
          bygroups(using(this), Name.Function, Text, Operator)),
      (r'[^\S\n]+', Text),
      (r'//.*?\n', Comment),
      (r'/\*.*?\*/', Comment),
      (r'@[a-zA-Z_][a-zA-Z0-9_\.]*', Name.Decorator),
      (r'(abstract|assert|break|case|catch|'
          r'const|continue|default|do|else|enum|extends|final|'
          r'finally|for|if|goto|implements|instanceof|'
          r'interface|native|new|package|private|protected|public|'
          r'return|static|strictfp|super|switch|synchronized|this|'
          r'throw|throws|transient|try|volatile|while)\b', Keyword),
      (r'(boolean|byte|char|double|float|int|long|short|void)\b',
          Keyword.Type),
      (r'(true|false|null)\b', Keyword.Constant),
      (r'(class)(\s+)', bygroups(Keyword, Text), 'class'),
      (r'(import)(\s+)', bygroups(Keyword, Text), 'import'),
      (r'~?/(\\/|[^/])*/', String),
      (r'"(\\\\|\\"|[^"])*"', String),
      (r"'\\.'|'[^\\]'|'\\u[0-9a-f]{4}'", String.Char),
      (r'(\.)([a-zA-Z_][a-zA-Z0-9_]*)', bygroups(Operator, Name.Attribute)),
      (r'[a-zA-Z_][a-zA-Z0-9_]*:', Name.Label),
      (r'[a-zA-Z_\$][a-zA-Z0-9_]*', Name),
      (r'[~\^\*!%&\[\]\(\)\{\}<>\|+=:;,./?-]', Operator),
      (r'[0-9][0-9]*\.[0-9]+([eE][0-9]+)?[fd]?', Number.Float),
      (r'0x[0-9a-f]+', Number.Hex),
      (r'[0-9]+L?', Number.Integer),
      (r'\n', Text),
    ],
    'class': [
      (r'[a-zA-Z_][a-zA-Z0-9_]*', Name.Class, '#pop')
    ],
    'import': [
      (r'[a-zA-Z0-9_.]+\*?', Name.Namespace, '#pop')
    ],
  }
