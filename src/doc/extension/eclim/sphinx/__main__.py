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
import getopt
import os.path
import sys

from docutils.utils import SystemMessage

from sphinx.cmdline import usage
from sphinx.errors import SphinxError
from sphinx.util.console import red, nocolor, color_terminal

from eclim.sphinx.application import Sphinx

def main(argv):
  """
  Based mostly on sphinx.cmdline.main
  """
  if not color_terminal():
    # Windows' poor cmd box doesn't understand ANSI sequences
    nocolor()

  try:
    opts, args = getopt.getopt(argv[1:], 'ab:t:d:c:CD:A:ng:NEqQWw:P')
    allopts = set(opt[0] for opt in opts)
    srcdir = confdir = os.path.abspath(args[0])
    if not os.path.isdir(srcdir):
      print >>sys.stderr, 'Error: Cannot find source directory.'
      return 1
    if not os.path.isfile(os.path.join(srcdir, 'conf.py')) and \
           '-c' not in allopts and '-C' not in allopts:
      print >> sys.stderr, 'Error: Source directory doesn\'t contain conf.py file.'
      return 1
    outdir = os.path.abspath(args[1])
    if not os.path.isdir(outdir):
      print >>sys.stderr, 'Making output directory...'
      os.makedirs(outdir)
  except (IndexError, getopt.error):
    usage(argv)
    return 1

  filenames = args[2:]
  err = 0
  for filename in filenames:
    if not os.path.isfile(filename):
       print >> sys.stderr, 'Cannot find file %r.' % filename
       err = 1
  if err:
    return 1

  buildername = None
  force_all = freshenv = warningiserror = False
  status = sys.stdout
  warning = sys.stderr
  confoverrides = {}
  tags = []
  doctreedir = os.path.join(outdir, '.doctrees')
  for opt, val in opts:
    if opt == '-b':
        buildername = val
    elif opt == '-a':
        if filenames:
            usage(argv, 'Cannot combine -a option and filenames.')
            return 1
        force_all = True
    elif opt == '-t':
        tags.append(val)
    elif opt == '-d':
        doctreedir = os.path.abspath(val)
    elif opt == '-c':
        confdir = os.path.abspath(val)
        if not os.path.isfile(os.path.join(confdir, 'conf.py')):
            print >> sys.stderr, \
                'Error: Configuration directory doesn\'t contain conf.py file.'
            return 1
    elif opt == '-C':
        confdir = None
    elif opt == '-D':
        try:
            key, val = val.split('=')
        except ValueError:
            print >> sys.stderr, \
                'Error: -D option argument must be in the form name=value.'
            return 1
        try:
            val = int(val)
        except ValueError:
            pass
        confoverrides[key] = val
    elif opt == '-A':
        try:
            key, val = val.split('=')
        except ValueError:
            print >> sys.stderr, \
                'Error: -A option argument must be in the form name=value.'
            return 1
        try:
            val = int(val)
        except ValueError:
            pass
        confoverrides['html_context.%s' % key] = val
    elif opt == '-n':
        confoverrides['nitpicky'] = True
    elif opt == '-N':
        nocolor()
    elif opt == '-E':
        freshenv = True
    elif opt == '-q':
        status = None
    elif opt == '-Q':
        status = None
        warning = None
    elif opt == '-W':
        warningiserror = True

  try:
    app = Sphinx(srcdir, confdir, outdir, doctreedir, buildername,
                 confoverrides, status, warning, freshenv,
                 warningiserror, tags)
    app.build(force_all, filenames)
    return app.statuscode
  except KeyboardInterrupt:
    return 1
  except Exception as err:
    print >> sys.stderr
    if isinstance(err, SystemMessage):
      print >> sys.stderr, red('reST markup error:')
      print >> sys.stderr, err.args[0].encode('ascii', 'backslashreplace')
    elif isinstance(err, SphinxError):
      print >> sys.stderr, red('%s:' % err.category)
      print >> sys.stderr, err
    else:
      raise
    return 1

if __name__ == '__main__':
  main(sys.argv)
