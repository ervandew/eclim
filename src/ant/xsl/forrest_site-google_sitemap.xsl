<?xml version="1.0" encoding="ISO-8859-1"?>

<!--
  - Stylesheet which processes a forest site.xml file to produce an xml file
  - for use with googles sitemap service.
  -
  - Author: Eric Van Dewoestine
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
