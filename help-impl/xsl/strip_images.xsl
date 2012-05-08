<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
    xmlns="http://www.cs.rpi.edu/HTML"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:ns1="http://www.w3.org/1999/xlink"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    >
<xsl:output method='text' version='1.0' encoding='UTF-8' indent='yes'/> 





<xsl:template match="/">
<xsl:text>&lt;?xml version="1.0"?&gt;
&lt;project name="ImageStrip" default="strip"&gt;
&lt;target name="strip"&gt;
</xsl:text>
	<xsl:apply-templates select="//inlinemediaobject/imageobject/imagedata"/>
<xsl:text>
&lt;/target&gt;
&lt;/project&gt;
</xsl:text>
</xsl:template>

<xsl:template match="inlinemediaobject/imageobject/imagedata">
	&lt;get src="<xsl:value-of select="@fileref"/>" dest="<xsl:value-of select="@fileref"/>" ignoreerrors="yes" usetimestamp="yes"/&gt;
<xsl:text>
</xsl:text>
</xsl:template>

</xsl:stylesheet>


