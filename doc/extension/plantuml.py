# -*- coding: utf-8 -*-
"""
    sphinxcontrib.plantuml
    ~~~~~~~~~~~~~~~~~~~~~~

    Embed PlantUML diagrams on your documentation.

    :copyright: Copyright 2010 by Yuya Nishihara <yuya@tcha.org>.
    :license: BSD, see LICENSE for details.
"""

import codecs
import errno
import hashlib
import os
import re
import shlex
import subprocess

from docutils import nodes
from docutils.parsers.rst import directives
from docutils.parsers.rst import Directive
from sphinx.errors import SphinxError
from sphinx.util.osutil import (
    ensuredir,
    ENOENT,
)

try:
    from PIL import Image
except ImportError:
    Image = None

try:
    from sphinx.util.i18n import search_image_for_language
except ImportError:  # Sphinx < 1.4
    def search_image_for_language(filename, env):
        return filename

class PlantUmlError(SphinxError):
    pass

class plantuml(nodes.General, nodes.Element):
    pass

def align(argument):
    align_values = ('left', 'center', 'right')
    return directives.choice(argument, align_values)

class UmlDirective(Directive):
    """Directive to insert PlantUML markup

    Example::

        .. uml::
           :alt: Alice and Bob

           Alice -> Bob: Hello
           Alice <- Bob: Hi
    """
    has_content = True
    required_arguments = 0
    optional_arguments = 1
    option_spec = {'alt': directives.unchanged,
                   'caption': directives.unchanged,
                   'height': directives.length_or_unitless,
                   'width': directives.length_or_percentage_or_unitless,
                   'scale': directives.percentage,
                   'align': align,
                   }

    def run(self):
        warning = self.state.document.reporter.warning
        env = self.state.document.settings.env
        if self.arguments and self.content:
            return [warning('uml directive cannot have both content and '
                            'a filename argument', line=self.lineno)]
        if self.arguments:
            fn = search_image_for_language(self.arguments[0], env)
            relfn, absfn = env.relfn2path(fn)
            env.note_dependency(relfn)
            try:
                umlcode = _read_utf8(absfn)
            except (IOError, UnicodeDecodeError) as err:
                return [warning('PlantUML file "%s" cannot be read: %s'
                                % (fn, err), line=self.lineno)]
        else:
            relfn = env.doc2path(env.docname, base=None)
            umlcode = '\n'.join(self.content)

        node = plantuml(self.block_text, **self.options)
        node['uml'] = umlcode
        node['incdir'] = os.path.dirname(relfn)
        node['filename'] = os.path.split(relfn)[1]

        # XXX maybe this should be moved to _visit_plantuml functions. it
        # seems wrong to insert "figure" node by "plantuml" directive.
        if 'caption' in self.options or 'align' in self.options:
            node = nodes.figure('', node)
            if 'align' in self.options:
                node['align'] = self.options['align']
        if 'caption' in self.options:
            import docutils.statemachine
            cnode = nodes.Element()  # anonymous container for parsing
            sl = docutils.statemachine.StringList([self.options['caption']],
                                                  source='')
            self.state.nested_parse(sl, self.content_offset, cnode)
            caption = nodes.caption(self.options['caption'], '', *cnode)
            node += caption

        return [node]

def _read_utf8(filename):
    fp = codecs.open(filename, 'rb', 'utf-8')
    try:
        return fp.read()
    finally:
        fp.close()

def generate_name(self, node, fileformat):
    h = hashlib.sha1()
    # may include different file relative to doc
    h.update(node['incdir'].encode('utf-8'))
    h.update(b'\0')
    h.update(node['uml'].encode('utf-8'))
    key = h.hexdigest()
    fname = 'plantuml-%s.%s' % (key, fileformat)
    imgpath = getattr(self.builder, 'imgpath', None)
    if imgpath:
        return ('/'.join((self.builder.imgpath, fname)),
                os.path.join(self.builder.outdir, '_images', fname))
    else:
        return fname, os.path.join(self.builder.outdir, fname)

def _ntunquote(s):
    if s.startswith('"') and s.endswith('"'):
        return s[1:-1]
    return s

def _split_cmdargs(args):
    if isinstance(args, (tuple, list)):
        return list(args)
    if os.name == 'nt':
        return list(map(_ntunquote, shlex.split(args, posix=False)))
    else:
        return shlex.split(args, posix=True)

_ARGS_BY_FILEFORMAT = {
    'eps': ['-teps'],
    'png': [],
    'svg': ['-tsvg'],
    'txt': ['-ttxt'],
}

def generate_plantuml_args(self, node, fileformat):
    args = _split_cmdargs(self.builder.config.plantuml)
    args.extend(['-pipe', '-charset', 'utf-8'])
    args.extend(['-filename', node['filename']])
    args.extend(_ARGS_BY_FILEFORMAT[fileformat])
    return args

def render_plantuml(self, node, fileformat):
    refname, outfname = generate_name(self, node, fileformat)
    if os.path.exists(outfname):
        return refname, outfname  # don't regenerate
    absincdir = os.path.join(self.builder.srcdir, node['incdir'])
    ensuredir(os.path.dirname(outfname))
    f = open(outfname, 'wb')
    try:
        try:
            p = subprocess.Popen(generate_plantuml_args(self, node, fileformat),
                                 stdout=f, stdin=subprocess.PIPE,
                                 stderr=subprocess.PIPE,
                                 cwd=absincdir)
        except OSError as err:
            if err.errno != ENOENT:
                raise
            raise PlantUmlError('plantuml command %r cannot be run'
                                % self.builder.config.plantuml)
        serr = p.communicate(node['uml'].encode('utf-8'))[1]
        if p.returncode != 0:
            raise PlantUmlError('error while running plantuml\n\n%s' % serr)
        return refname, outfname
    finally:
        f.close()

def render_plantuml_inline(self, node, fileformat):
    absincdir = os.path.join(self.builder.srcdir, node['incdir'])
    try:
        p = subprocess.Popen(generate_plantuml_args(self, node, fileformat),
                             stdin=subprocess.PIPE,
                             stdout=subprocess.PIPE,
                             stderr=subprocess.PIPE,
                             cwd=absincdir)
    except OSError as err:
        if err.errno != ENOENT:
            raise
        raise PlantUmlError('plantuml command %r cannot be run'
                            % self.builder.config.plantuml)
    sout, serr = p.communicate(node['uml'].encode('utf-8'))
    if p.returncode != 0:
        raise PlantUmlError('error while running plantuml\n\n%s' % serr)
    return sout.decode('utf-8')

def _get_png_tag(self, fnames, node):
    refname, outfname = fnames['png']
    alt = node.get('alt', node['uml'])

    # mimic StandaloneHTMLBuilder.post_process_images(). maybe we should
    # process images prior to html_vist.
    scale_keys = ('scale', 'width', 'height')
    if all(key not in node for key in scale_keys) or Image is None:
        return ('<img src="%s" alt="%s" />\n'
                % (self.encode(refname), self.encode(alt)))

    scale = node.get('scale', 100)
    styles = []

    # Width/Height
    vu = re.compile(r"(?P<value>\d+)\s*(?P<units>[a-zA-Z%]+)?")
    for a in ['width', 'height']:
        if a not in node:
            continue
        m = vu.match(node[a])
        if not m:
            raise PlantUmlError('Invalid %s' % a)
        m = m.groupdict()
        w = int(m['value'])
        wu = m['units'] if m['units'] else 'px'
        styles.append('%s: %s%s' % (a, w * scale / 100, wu))

    # Add physical size to assist rendering (defaults)
    if not styles:
        im = Image.open(outfname)
        im.load()
        styles.extend('%s: %s%s' % (a, w * scale / 100, 'px')
                      for a, w in zip(['width', 'height'], im.size))

    return ('<a href="%s"><img src="%s" alt="%s" style="%s"/>'
            '</a>\n'
            % (self.encode(refname),
               self.encode(refname),
               self.encode(alt),
               self.encode('; '.join(styles))))

def _get_svg_style(fname):
    f = open(fname)
    try:
        for l in f:
            m = re.search(r'<svg\b([^<>]+)', l)
            if m:
                attrs = m.group(1)
                break
        else:
            return
    finally:
        f.close()

    m = re.search(r'\bstyle=[\'"]([^\'"]+)', attrs)
    if not m:
        return
    return m.group(1)

def _get_svg_tag(self, fnames, node):
    refname, outfname = fnames['svg']
    return '\n'.join([
        # copy width/height style from <svg> tag, so that <object> area
        # has enough space.
        '<object data="%s" type="image/svg+xml" style="%s">' % (
            self.encode(refname), _get_svg_style(outfname) or ''),
        _get_png_tag(self, fnames, node),
        '</object>'])

_KNOWN_HTML_FORMATS = {
    'png': (('png',), _get_png_tag),
    'svg': (('png', 'svg'), _get_svg_tag),
    }

def html_visit_plantuml(self, node):
    fmt = self.builder.config.plantuml_output_format
    if fmt == 'none':
        raise nodes.SkipNode
    try:
        try:
            fileformats, gettag = _KNOWN_HTML_FORMATS[fmt]
        except KeyError:
            raise PlantUmlError(
                'plantuml_output_format must be one of %s, but is %r'
                % (', '.join(map(repr, _KNOWN_HTML_FORMATS)), fmt))
        # fnames: {fileformat: (refname, outfname), ...}
        fnames = dict((e, render_plantuml(self, node, e))
                      for e in fileformats)
    except PlantUmlError as err:
        self.builder.warn(str(err))
        raise nodes.SkipNode

    self.body.append(self.starttag(node, 'p', CLASS='plantuml'))
    self.body.append(gettag(self, fnames, node))
    self.body.append('</p>\n')
    raise nodes.SkipNode

def _convert_eps_to_pdf(self, refname, fname):
    args = _split_cmdargs(self.builder.config.plantuml_epstopdf)
    args.append(fname)
    try:
        try:
            p = subprocess.Popen(args, stdout=subprocess.PIPE,
                                 stderr=subprocess.PIPE)
        except OSError as err:
            # workaround for missing shebang of epstopdf script
            if err.errno != getattr(errno, 'ENOEXEC', 0):
                raise
            p = subprocess.Popen(['bash'] + args, stdout=subprocess.PIPE,
                                 stderr=subprocess.PIPE)
    except OSError as err:
        if err.errno != ENOENT:
            raise
        raise PlantUmlError('epstopdf command %r cannot be run'
                            % self.builder.config.plantuml_epstopdf)
    serr = p.communicate()[1]
    if p.returncode != 0:
        raise PlantUmlError('error while running epstopdf\n\n' + serr)
    return refname[:-4] + '.pdf', fname[:-4] + '.pdf'

_KNOWN_LATEX_FORMATS = {
    'eps': ('eps', lambda self, refname, fname: (refname, fname)),
    'pdf': ('eps', _convert_eps_to_pdf),
    'png': ('png', lambda self, refname, fname: (refname, fname)),
    }

def latex_visit_plantuml(self, node):
    fmt = self.builder.config.plantuml_latex_output_format
    if fmt == 'none':
        raise nodes.SkipNode
    try:
        try:
            fileformat, postproc = _KNOWN_LATEX_FORMATS[fmt]
        except KeyError:
            raise PlantUmlError(
                'plantuml_latex_output_format must be one of %s, but is %r'
                % (', '.join(map(repr, _KNOWN_LATEX_FORMATS)), fmt))
        refname, outfname = render_plantuml(self, node, fileformat)
        refname, outfname = postproc(self, refname, outfname)
    except PlantUmlError as err:
        self.builder.warn(str(err))
        raise nodes.SkipNode

    # put node representing rendered image
    img_node = nodes.image(uri=refname, **node.attributes)
    img_node.delattr('uml')
    if not img_node.hasattr('alt'):
        img_node['alt'] = node['uml']
    node.append(img_node)

def latex_depart_plantuml(self, node):
    pass

def text_visit_plantuml(self, node):
    try:
        text = render_plantuml_inline(self, node, 'txt')
    except PlantUmlError as err:
        self.builder.warn(str(err))
        text = node['uml']  # fall back to uml text, which is still readable

    self.new_state()
    self.add_text(text)
    self.end_state()
    raise nodes.SkipNode

def pdf_visit_plantuml(self, node):
    try:
        refname, outfname = render_plantuml(self, node, 'eps')
        refname, outfname = _convert_eps_to_pdf(self, refname, outfname)
    except PlantUmlError as err:
        self.builder.warn(str(err))
        raise nodes.SkipNode
    rep = nodes.image(uri=outfname, alt=node.get('alt', node['uml']))
    node.parent.replace(node, rep)

def unsupported_visit_plantuml(self, node):
    self.builder.warn('plantuml: unsupported output format (node skipped)')
    raise nodes.SkipNode

_NODE_VISITORS = {
    'html': (html_visit_plantuml, None),
    'latex': (latex_visit_plantuml, latex_depart_plantuml),
    'man': (unsupported_visit_plantuml, None),  # TODO
    'texinfo': (unsupported_visit_plantuml, None),  # TODO
    'text': (text_visit_plantuml, None),
}

def setup(app):
    app.add_node(plantuml, **_NODE_VISITORS)
    app.add_directive('uml', UmlDirective)
    app.add_config_value('plantuml', 'plantuml', 'html')
    app.add_config_value('plantuml_output_format', 'png', 'html')
    app.add_config_value('plantuml_epstopdf', 'epstopdf', '')
    app.add_config_value('plantuml_latex_output_format', 'png', '')

    # imitate what app.add_node() does
    if 'rst2pdf.pdfbuilder' in app.config.extensions:
        from rst2pdf.pdfbuilder import PDFTranslator as translator
        setattr(translator, 'visit_' + plantuml.__name__, pdf_visit_plantuml)

    return {'parallel_read_safe': True}
