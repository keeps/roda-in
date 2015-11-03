<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ap="http://www.w3.org/1999/xhtml">

    <xsl:output method='text'/>

    <xsl:template match="/">
        <xsl:apply-templates select="//ap:meta"/>
    </xsl:template>

    <xsl:template match="ap:meta">
        <xsl:value-of select="@name"/>:<xsl:value-of select="@content"/>
        <xsl:text>&#10;</xsl:text>
    </xsl:template>

</xsl:stylesheet>