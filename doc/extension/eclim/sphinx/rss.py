import cgi
import os

from docutils import nodes, utils
from docutils.frontend import OptionParser
from docutils.parsers.rst import Directive, directives, Parser

from sphinx.builders.html import StandaloneHTMLBuilder
from sphinx.writers.html import HTMLTranslator, HTMLWriter
from sphinx.util.osutil import os_path

class RssTranslator(HTMLTranslator):
    def __init__(self, *args, **kwargs):
        HTMLTranslator.__init__(self, *args, **kwargs)
        self.xref_close = 0

    def visit_pending_xref(self, node):
        env = self.builder.env
        reftarget = node.get('reftarget')
        refdomain = node.get('refdomain')
        docname = None
        labelid = None
        if refdomain in env.domains:
            domain = self.builder.env.domains[refdomain]
            if node['reftype'] == 'ref':
                docname, labelid = domain.data['anonlabels'].get(node['reftarget'], ('',''))
                if docname:
                    docname = docname.replace('\\', '/')

        elif reftarget:
            docname = reftarget

        if docname:
            docname = '/' + docname if not docname [0] == '/' else docname
            baseurl = env.config.rss_baseurl
            url = '%s%s.html' % (baseurl, docname)
            if labelid:
                url = '%s#%s' % (url, labelid)
            self.xref_close += 1
            self.body.append(self.starttag(node, 'a', '', href=url))

    def depart_pending_xref(self, node):
        if self.xref_close:
            self.xref_close -= 1
            self.body.append('</a>')

class RssDirective(Directive):
    final_argument_whitespace = True
    option_spec = {
        'title': directives.unchanged_required,
        'description': directives.unchanged_required,
        'end-before': directives.unchanged_required,
    }

    def run(self):
        env = self.state.document.settings.env
        if not isinstance(env.app.builder, StandaloneHTMLBuilder):
            return[]

        lines =  self.state_machine.input_lines
        source = self.state_machine.input_lines.source(
            self.lineno - self.state_machine.input_offset - 1)

        find_start = True
        find_options = False
        start_after = 0
        end_options = 0
        rss_directive = '.. %s::' % self.name
        for line in lines:
            line = line.strip()
            if find_start and line == '':
                start_after += 1
            elif line == rss_directive:
                start_after += 1
                find_start = False
                find_options = True
            elif find_options and line.startswith(':'):
                end_options += 1
            else:
                break

        options = lines[start_after:start_after + end_options] if end_options else []
        lines = lines[start_after + end_options:]

        end_before = self.options.get('end-before', None)
        if end_before:
            lines = lines[:lines.index('.. %s' % end_before.strip())]

        lines = ['.. rssinsert::'] + [o for o in options] + ['   %s' % l for l in lines]

        self.state_machine.insert_input(lines, source)
        return []

class RssInsertDirective(Directive):
    has_content = True

    option_spec = RssDirective.option_spec

    def run(self):
        env = self.state.document.settings.env
        baseurl = env.config.rss_baseurl
        assert baseurl, 'rss_baseurl must be defined in your config.py'

        source = self.state_machine.input_lines.source(
            self.lineno - self.state_machine.input_offset - 1)

        rss_doc = utils.new_document(b'<rss>', self.state.document.settings)
        Parser().parse('\n'.join(self.content), rss_doc)

        if isinstance(env.config.source_suffix, (list, tuple)):
            rst_suffix = env.config.source_suffix[0]
        else:
            rst_suffix = env.config.source_suffix
        path = os.path.relpath(source, env.srcdir).replace(rst_suffix, '.html')

        builder = env.app.builder
        docwriter = HTMLWriter(self)
        docsettings = OptionParser(
            defaults=env.settings,
            components=(docwriter,)).get_default_values()
        docsettings.compact_lists = bool(env.config.html_compact_lists)

        dest = os.path.join(env.app.outdir, os_path(env.docname) + '.rss')
        pageurl = '%s/%s' % (baseurl, path)
        with open(dest, 'w') as rss:
            title = self.options.get('title', '')
            description = self.options.get('description', None)
            rss.write('<?xml version="1.0" encoding="ISO-8859-1" ?>\n')
            rss.write('<rss version="2.0">\n')
            rss.write('<channel>\n')
            rss.write('<title>%s</title>\n' % cgi.escape(title))
            rss.write('<link>%s</link>\n' % pageurl)
            if description:
                rss.write('<description>%s</description>\n' % cgi.escape(description))

            for child in rss_doc.children:
                if not isinstance(child, nodes.section):
                    continue

                title_index = child.first_child_matching_class(nodes.title)
                if title_index is None:
                    continue

                node = nodes.paragraph()
                node.extend(child.children[title_index + 1:])

                sec_doc = utils.new_document(b'<rss-section>', docsettings)
                sec_doc.append(node)
                visitor = RssTranslator(builder, sec_doc)
                sec_doc.walkabout(visitor)

                title = child.children[title_index].astext()
                sectionurl = '%s#%s' % (pageurl, child.get('ids')[0])
                description = ''.join(visitor.body)

                rss.write('<item>\n')
                rss.write('<title>%s</title>\n' % cgi.escape(title))
                rss.write('<link>%s</link>\n' % sectionurl)
                rss.write('<description><![CDATA[%s]]></description>\n' % description)
                rss.write('</item>\n')
            rss.write('</channel>\n')
            rss.write('</rss>\n')

        return []

def setup(app):
    app.add_directive('rss', RssDirective)
    app.add_directive('rssinsert', RssInsertDirective)
    app.add_config_value('rss_baseurl', None, 'html')
