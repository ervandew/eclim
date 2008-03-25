<?xml version="1.0" encoding="ISO-8859-1"?>
<!--
  Copyright (C) 2005 - 2008  Eric Van Dewoestine

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
-->

<!--
  Stylesheet which processes a forest site.xml file to produce an xml file
  for use with googles sitemap service.
-->
<xsl:stylesheet version="1.0"
    xmlns="http://www.google.com/schemas/sitemap/0.84"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="xml" indent="yes" encoding="UTF-8"/>

  <xsl:variable name="url" select="//@url"/>

  <xsl:template match="/">
    <urlset>
      <xsl:apply-templates select="//*"/>
    </urlset>
  </xsl:template>

  <xsl:template match="*">
    <!-- only process leaf nodes who do not have an ancestor of 'external-refs' -->
    <xsl:if test="count(current()/*) = 0 and count(ancestor::node()[name() = 'external-refs']) = 0 and not(starts-with(@href, 'ext:'))">
      <url>
        <loc>
          <xsl:value-of select="$url"/>
          <xsl:for-each select="ancestor::node()/@href">
            <xsl:value-of select="current()"/>
          </xsl:for-each>
          <xsl:value-of select="@href"/>
        </loc>
      </url>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
