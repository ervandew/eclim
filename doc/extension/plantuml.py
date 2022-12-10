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
import shutil
import subprocess
from contextlib import contextmanager

from docutils import nodes
from docutils.parsers.rst import directives
from docutils.parsers.rst import Directive

from sphinx import util
from sphinx.errors import SphinxError
from sphinx.util import (
    i18n,
    logging,
)
from sphinx.util.nodes import set_source_info
from sphinx.util.osutil import (
    ensuredir,
)

try:
    from PIL import Image
except ImportError:
    Image = None


logger = logging.getLogger(__name__)


if os.name == 'nt':
    def rename(src, dst):
        try:
            os.rename(src, dst)
        except OSError as err:
            if err.errno != errno.EEXIST:
                raise
            os.unlink(dst)
            os.rename(src, dst)
else:
    rename = os.rename


class PlantUmlError(SphinxError):
    pass


class plantuml(nodes.General, nodes.Element):
    pass


def align(argument):
    align_values = ('left', 'center', 'right')
    return directives.choice(argument, align_values)


def html_format(argument):
    format_values = list(_KNOWN_HTML_FORMATS.keys())
    return directives.choice(argument, format_values)


def latex_format(argument):
    format_values = list(_KNOWN_LATEX_FORMATS.keys())
    return directives.choice(argument, format_values)


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
    final_argument_whitespace = True  # allow whitespace in arguments[-1]
    option_spec = {
        'alt': directives.unchanged,
        'align': align,
        'caption': directives.unchanged,
        'height': directives.length_or_unitless,
        'html_format': html_format,
        'latex_format': latex_format,
        'name': directives.unchanged,
        'scale': directives.percentage,
        'width': directives.length_or_percentage_or_unitless,
    }

    def run(self):
        warning = self.state.document.reporter.warning
        env = self.state.document.settings.env
        if self.arguments and self.content:
            return [warning('uml directive cannot have both content and '
                            'a filename argument', line=self.lineno)]
        if self.arguments:
            fn = i18n.search_image_for_language(self.arguments[0], env)
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
            inodes, messages = self.state.inline_text(self.options['caption'],
                                                      self.lineno)
            caption_node = nodes.caption(self.options['caption'], '', *inodes)
            caption_node.extend(messages)
            set_source_info(self, caption_node)
            node += caption_node
        self.add_name(node)
        if 'html_format' in self.options:
            node['html_format'] = self.options['html_format']
        if 'latex_format' in self.options:
            node['latex_format'] = self.options['latex_format']

        return [node]


def _read_utf8(filename):
    fp = codecs.open(filename, 'rb', 'utf-8')
    try:
        return fp.read()
    finally:
        fp.close()


def hash_plantuml_node(node):
    h = hashlib.sha1()
    # may include different file relative to doc
    h.update(node['incdir'].encode('utf-8'))
    h.update(b'\0')
    h.update(node['uml'].encode('utf-8'))
    return h.hexdigest()


def generate_name(self, node, fileformat):
    key = hash_plantuml_node(node)
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
    'latex': ['-tlatex:nopreamble'],
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

    cachefname = self.builder.plantuml_builder.render(node, fileformat)
    ensuredir(os.path.dirname(outfname))
    # TODO: optionally do symlink/link
    shutil.copyfile(cachefname, outfname)
    return refname, outfname


def render_plantuml_inline(self, node, fileformat):
    absincdir = os.path.join(self.builder.srcdir, node['incdir'])
    try:
        p = subprocess.Popen(generate_plantuml_args(self, node, fileformat),
                             stdin=subprocess.PIPE,
                             stdout=subprocess.PIPE,
                             stderr=subprocess.PIPE,
                             cwd=absincdir)
    except OSError as err:
        if err.errno != errno.ENOENT:
            raise
        raise PlantUmlError('plantuml command %r cannot be run'
                            % self.builder.config.plantuml)
    sout, serr = p.communicate(node['uml'].encode('utf-8'))
    if p.returncode != 0:
        raise PlantUmlError('error while running plantuml\n\n%s' % serr)
    return sout.decode('utf-8')


class PlantumlBuilder(object):
    def __init__(self, builder):
        # for compatibility with existing functions which expect self.builder
        # TODO: remove self.builder
        self.builder = builder

        self.batch_size = builder.config.plantuml_batch_size
        self.cache_dir = os.path.join(builder.outdir,
                                      builder.config.plantuml_cache_path)

        self._base_cmdargs = _split_cmdargs(builder.config.plantuml)
        self._base_cmdargs.extend(['-charset', 'utf-8'])

        self.image_formats = []
        if builder.format == 'html':
            fmt = builder.config.plantuml_output_format
            if fmt != 'none':
                fileformats, _gettag = _lookup_html_format(fmt)
                self.image_formats = list(fileformats)
        elif builder.format == 'latex':
            fmt = builder.config.plantuml_latex_output_format
            if fmt != 'none':
                fileformat, _postproc = _lookup_latex_format(fmt)
                self.image_formats = [fileformat]

        self._known_keys = set()
        self._pending_keys = []

    def collect_nodes(self, doctree):
        for node in doctree.traverse(plantuml):
            key = hash_plantuml_node(node)
            if key in self._known_keys:
                continue
            self._known_keys.add(key)

            doc = node['uml'].encode('utf-8')
            if b'!include' in doc or b'%filename' in doc:
                # Heuristic to work around the path/filename issue. There's no
                # easy way to specify the cwd of the doc without using -pipe.
                continue

            outdir = os.path.join(self.cache_dir, key[:2])
            outfbase = os.path.join(outdir, key)
            if all(os.path.exists('%s.%s' % (outfbase, sfx))
                   for sfx in ['puml'] + self.image_formats):
                continue

            ensuredir(outdir)
            with open(outfbase + '.puml', 'wb') as f:
                # @startuml/enduml is mandatory in non-pipe mode. For backward
                # compatibility, we do wrap the document only if @startXYZ is
                # not specified.
                started = doc.lstrip().startswith(b'@start')
                if not started:
                    f.write(b'@startuml\n')
                f.write(doc)
                if not started:
                    f.write(b'\n@enduml\n')

            self._pending_keys.append(key)

    def render_batches(self):
        pending_keys = sorted(self._pending_keys)
        for fileformat in self.image_formats:
            for i in range(0, len(pending_keys), self.batch_size):
                keys = pending_keys[i:i + self.batch_size]
                with util.progress_message(
                        'rendering plantuml diagrams [%d..%d/%d]'
                        % (i, i + len(keys), len(pending_keys))):
                    self._render_files(keys, fileformat)

        del self._pending_keys[:]

    def _render_files(self, keys, fileformat):
        cmdargs = self._base_cmdargs[:]
        cmdargs.extend(_ARGS_BY_FILEFORMAT[fileformat])
        cmdargs.extend(os.path.join(k[:2], '%s.puml' % k) for k in keys)
        try:
            p = subprocess.Popen(cmdargs, stderr=subprocess.PIPE,
                                 cwd=self.cache_dir)
        except OSError as err:
            if err.errno != errno.ENOENT:
                raise
            raise PlantUmlError('plantuml command %r cannot be run'
                                % self.builder.config.plantuml)
        serr = p.communicate()[1]
        if p.returncode != 0:
            if self.builder.config.plantuml_syntax_error_image:
                logger.warning('error while running plantuml\n\n%s' % serr)
            else:
                raise PlantUmlError('error while running plantuml\n\n%s' % serr)

    def render(self, node, fileformat):
        key = hash_plantuml_node(node)
        outdir = os.path.join(self.cache_dir, key[:2])
        outfname = os.path.join(outdir, '%s.%s' % (key, fileformat))
        if os.path.exists(outfname):
            return outfname

        ensuredir(outdir)
        absincdir = os.path.join(self.builder.srcdir, node['incdir'])
        with open(outfname + '.new', 'wb') as f:
            try:
                p = subprocess.Popen(generate_plantuml_args(self, node,
                                                            fileformat),
                                     stdout=f, stdin=subprocess.PIPE,
                                     stderr=subprocess.PIPE,
                                     cwd=absincdir)
            except OSError as err:
                if err.errno != errno.ENOENT:
                    raise
                raise PlantUmlError('plantuml command %r cannot be run'
                                    % self.builder.config.plantuml)
            serr = p.communicate(node['uml'].encode('utf-8'))[1]
            if p.returncode != 0:
                if self.builder.config.plantuml_syntax_error_image:
                    logger.warning('error while running plantuml\n\n%s' % serr)
                else:
                    raise PlantUmlError('error while running plantuml\n\n%s'
                                        % serr)

        rename(outfname + '.new', outfname)
        return outfname


def _render_batches_on_vist(self):
    self.builder.plantuml_builder.render_batches()


def _get_png_tag(self, fnames, node):
    refname, outfname = fnames['png']
    alt = node.get('alt', node['uml'])

    # mimic StandaloneHTMLBuilder.post_process_images(). maybe we should
    # process images prior to html_vist.
    scale_attrs = [k for k in ('scale', 'width', 'height') if k in node]
    if scale_attrs and Image is None:
        logger.warning(('plantuml: unsupported scaling attributes: %s '
                        '(install PIL or Pillow)'
                        % ', '.join(scale_attrs)))
    if not scale_attrs or Image is None:
        return ('<img src="%s" alt="%s"/>\n'
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
        # the image may be corrupted if platuml isn't configured correctly,
        # which isn't a hard error.
        try:
            im = Image.open(outfname)
            im.load()
            styles.extend('%s: %s%s' % (a, w * scale / 100, 'px')
                          for a, w in zip(['width', 'height'], im.size))
        except (IOError, OSError) as err:
            logger.warning('plantuml: failed to get image size: %s' % err)

    return ('<a href="%s"><img src="%s" alt="%s" style="%s"/>'
            '</a>\n'
            % (self.encode(refname),
               self.encode(refname),
               self.encode(alt),
               self.encode('; '.join(styles))))


def _get_svg_style(fname):
    f = codecs.open(fname, 'r', 'utf-8')
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


def _get_svg_img_tag(self, fnames, node):
    refname, outfname = fnames['svg']
    alt = node.get('alt', node['uml'])
    return ('<img src="%s" alt="%s"/>'
            % (self.encode(refname), self.encode(alt)))


def _get_svg_obj_tag(self, fnames, node):
    refname, outfname = fnames['svg']
    # copy width/height style from <svg> tag, so that <object> area
    # has enough space.
    return ('<object data="%s" type="image/svg+xml" style="%s"></object>'
            % (self.encode(refname), _get_svg_style(outfname) or ''))


_KNOWN_HTML_FORMATS = {
    'png': (('png',), _get_png_tag),
    'svg': (('png', 'svg'), _get_svg_tag),
    'svg_img': (('svg',), _get_svg_img_tag),
    'svg_obj': (('svg',), _get_svg_obj_tag),
}


def _lookup_html_format(fmt):
    try:
        return _KNOWN_HTML_FORMATS[fmt]
    except KeyError:
        raise PlantUmlError(
            'plantuml_output_format must be one of %s, but is %r'
            % (', '.join(map(repr, _KNOWN_HTML_FORMATS)), fmt))


@contextmanager
def _prepare_html_render(self, fmt):
    if fmt == 'none':
        raise nodes.SkipNode

    try:
        yield _lookup_html_format(fmt)
    except PlantUmlError as err:
        logger.warning(str(err))
        raise nodes.SkipNode


def html_visit_plantuml(self, node):
    _render_batches_on_vist(self)
    if 'html_format' in node:
        fmt = node['html_format']
    else:
        fmt = self.builder.config.plantuml_output_format

    with _prepare_html_render(self, fmt) as (fileformats, gettag):
        # fnames: {fileformat: (refname, outfname), ...}
        fnames = dict((e, render_plantuml(self, node, e))
                      for e in fileformats)

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
        if err.errno != errno.ENOENT:
            raise
        raise PlantUmlError('epstopdf command %r cannot be run'
                            % self.builder.config.plantuml_epstopdf)
    serr = p.communicate()[1]
    if p.returncode != 0:
        raise PlantUmlError('error while running epstopdf\n\n%s' % serr)
    return refname[:-4] + '.pdf', fname[:-4] + '.pdf'


_KNOWN_LATEX_FORMATS = {
    'eps': ('eps', lambda self, refname, fname: (refname, fname)),
    'pdf': ('eps', _convert_eps_to_pdf),
    'png': ('png', lambda self, refname, fname: (refname, fname)),
    'tikz': ('latex', lambda self, refname, fname: (refname, fname)),
}


def _lookup_latex_format(fmt):
    try:
        return _KNOWN_LATEX_FORMATS[fmt]
    except KeyError:
        raise PlantUmlError(
            'plantuml_latex_output_format must be one of %s, but is %r'
            % (', '.join(map(repr, _KNOWN_LATEX_FORMATS)), fmt))


def _latex_adjustbox_options(self, node):
    adjustbox_options = []
    if 'width' in node:
        if 'scale' in node:
            w = self.latex_image_length(node['width'], node['scale'])
        else:
            w = self.latex_image_length(node['width'])
        if w:
            adjustbox_options.append('width=%s' % w)
    if 'height' in node:
        if 'scale' in node:
            h = self.latex_image_length(node['height'], node['scale'])
        else:
            h = self.latex_image_length(node['height'])
        if h:
            adjustbox_options.append('height=%s' % h)
    if 'scale' in node:
        if not adjustbox_options:
            adjustbox_options.append('scale=%s'
                                     % (float(node['scale']) / 100.0))
    return adjustbox_options


def _latex_add_package(self, package):
    # TODO: Currently modifying the preamble to add a package, there may be
    # a cleaner solution
    package = '\\usepackage{%s}' % (package,)
    if package not in self.elements['preamble']:
        self.elements['preamble'] += package + '\n'


def latex_visit_plantuml(self, node):
    _render_batches_on_vist(self)
    if 'latex_format' in node:
        fmt = node['latex_format']
    else:
        fmt = self.builder.config.plantuml_latex_output_format
    if fmt == 'none':
        raise nodes.SkipNode
    try:
        fileformat, postproc = _lookup_latex_format(fmt)
        refname, outfname = render_plantuml(self, node, fileformat)
        refname, outfname = postproc(self, refname, outfname)
    except PlantUmlError as err:
        logger.warning(str(err))
        raise nodes.SkipNode

    if fmt == 'tikz':
        _latex_add_package(self, 'tikz')

        base, ext = os.path.splitext(refname)
        input_macro = '\\input{{%s}%s}' % (base, ext)

        adjustbox_options = _latex_adjustbox_options(self, node)
        if adjustbox_options:
            _latex_add_package(self, 'adjustbox')
            options = ','.join(adjustbox_options)
            self.body.append('\\adjustbox{%s}{%s}' % (options, input_macro))
        else:
            self.body.append(input_macro)
    else:
        # put node representing rendered image
        img_node = nodes.image(uri=refname, **node.attributes)
        img_node.delattr('uml')
        if not img_node.hasattr('alt'):
            img_node['alt'] = node['uml']
        node.append(img_node)


def latex_depart_plantuml(self, node):
    pass


_KNOWN_CONFLUENCE_FORMATS = [
    'png',
    'svg',
]


def confluence_visit_plantuml(self, node):
    _render_batches_on_vist(self)
    fmt = self.builder.config.plantuml_output_format
    if fmt == 'none':
        raise nodes.SkipNode

    if fmt not in _KNOWN_CONFLUENCE_FORMATS:
        raise PlantUmlError(
            'plantuml_output_format must be one of %s, but is %r'
            % (', '.join(map(repr, _KNOWN_CONFLUENCE_FORMATS)), fmt))

    _, outfname = render_plantuml(self, node, fmt)

    # put node representing rendered image
    img_node = nodes.image(uri=outfname, alt=node.get('alt', node['uml']))
    node.replace_self(img_node)
    self.visit_image(img_node)


def confluence_depart_plantuml(self, node):
    pass


def text_visit_plantuml(self, node):
    _render_batches_on_vist(self)
    try:
        text = render_plantuml_inline(self, node, 'txt')
    except PlantUmlError as err:
        logger.warning(str(err))
        text = node['uml']  # fall back to uml text, which is still readable

    self.new_state()
    self.add_text(text)
    self.end_state()
    raise nodes.SkipNode


def pdf_visit_plantuml(self, node):
    _render_batches_on_vist(self)
    try:
        refname, outfname = render_plantuml(self, node, 'eps')
        refname, outfname = _convert_eps_to_pdf(self, refname, outfname)
    except PlantUmlError as err:
        logger.warning(str(err))
        raise nodes.SkipNode
    rep = nodes.image(uri=outfname, alt=node.get('alt', node['uml']))
    node.parent.replace(node, rep)


def unsupported_visit_plantuml(self, node):
    logger.warning('plantuml: unsupported output format (node skipped)')
    raise nodes.SkipNode


_NODE_VISITORS = {
    'html': (html_visit_plantuml, None),
    'latex': (latex_visit_plantuml, latex_depart_plantuml),
    'man': (unsupported_visit_plantuml, None),  # TODO
    'texinfo': (unsupported_visit_plantuml, None),  # TODO
    'text': (text_visit_plantuml, None),
    'confluence': (confluence_visit_plantuml, confluence_depart_plantuml),
    'singleconfluence': (confluence_visit_plantuml, confluence_depart_plantuml),
}


def _on_builder_inited(app):
    app.builder.plantuml_builder = PlantumlBuilder(app.builder)


def _on_doctree_read(app, doctree):
    # Collect as many static nodes as possible prior to start building.
    if app.builder.plantuml_builder.batch_size > 1:
        app.builder.plantuml_builder.collect_nodes(doctree)


def _on_doctree_resolved(app, doctree, docname):
    # Dynamically generated nodes will be collected here, which will be
    # batched at node visitor. Since 'doctree-resolved' and node visits
    # can be intermixed, there's no way to batch rendering of dynamic nodes
    # at once.
    if app.builder.plantuml_builder.batch_size > 1:
        app.builder.plantuml_builder.collect_nodes(doctree)


def setup(app):
    app.add_node(plantuml, **_NODE_VISITORS)
    app.add_directive('uml', UmlDirective)
    try:
        app.add_config_value('plantuml', 'plantuml', 'html',
                             types=(str, tuple, list))
    except TypeError:
        # Sphinx < 1.4?
        app.add_config_value('plantuml', 'plantuml', 'html')
    app.add_config_value('plantuml_output_format', 'png', 'html')
    app.add_config_value('plantuml_epstopdf', 'epstopdf', '')
    app.add_config_value('plantuml_latex_output_format', 'png', '')
    app.add_config_value('plantuml_syntax_error_image', False, '')
    app.add_config_value('plantuml_cache_path', '_plantuml', '')
    app.add_config_value('plantuml_batch_size', 1, '')
    app.connect('builder-inited', _on_builder_inited)
    app.connect('doctree-read', _on_doctree_read)
    app.connect('doctree-resolved', _on_doctree_resolved)

    # imitate what app.add_node() does
    if 'rst2pdf.pdfbuilder' in app.config.extensions:
        from rst2pdf.pdfbuilder import PDFTranslator as translator
        setattr(translator, 'visit_' + plantuml.__name__, pdf_visit_plantuml)

    return {'parallel_read_safe': True}
