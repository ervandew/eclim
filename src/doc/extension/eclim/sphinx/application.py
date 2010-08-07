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
from os import path
import sys

from sphinx.application import Sphinx as SphinxBase, ENV_PICKLE_FILENAME
from sphinx.util.console import bold
from sphinx.util.osutil import ENOENT

from eclim.sphinx.environment import EclimBuildEnvironment, EclimHtmlBuildEnvironment

class Sphinx(SphinxBase):
  ENV_CLASSES = {
    'eclim': EclimHtmlBuildEnvironment,
    'vimdoc': EclimBuildEnvironment,
  }

  def __init__(
    self, srcdir, confdir, outdir, doctreedir, buildername,
    confoverrides=None, status=sys.stdout, warning=sys.stderr,
    freshenv=False, warningiserror=False, tags=None
  ):
    self.buildername = buildername
    super(Sphinx, self).__init__(
      srcdir, confdir, outdir, doctreedir, buildername, confoverrides,
      status, warning, freshenv, warningiserror, tags)

  def _init_env(self, freshenv):
    """
    Copied from sphinx.application.Sphinx with BuildEnvironment replaced with
    the appropriate eclim build environment class.
    """
    env_class = self.ENV_CLASSES[self.buildername]

    if freshenv:
      #self.env = BuildEnvironment(self.srcdir, self.doctreedir, self.config)
      self.env = env_class(self.srcdir, self.doctreedir, self.config)
      self.env.find_files(self.config)
      for domain in self.domains.keys():
        self.env.domains[domain] = self.domains[domain](self.env)
    else:
      try:
        self.info(bold('loading pickled environment... '), nonl=True)
        #self.env = BuildEnvironment.frompickle(self.config,
        #    path.join(self.doctreedir, ENV_PICKLE_FILENAME))
        self.env = env_class.frompickle(self.config,
            path.join(self.doctreedir, ENV_PICKLE_FILENAME))
        self.env.domains = {}
        for domain in self.domains.keys():
          # this can raise if the data version doesn't fit
          self.env.domains[domain] = self.domains[domain](self.env)
        self.info('done')
      except Exception, err:
        if type(err) is IOError and err.errno == ENOENT:
          self.info('not yet created')
        else:
          self.info('failed: %s' % err)
        return self._init_env(freshenv=True)

    self.env.set_warnfunc(self.warn)
