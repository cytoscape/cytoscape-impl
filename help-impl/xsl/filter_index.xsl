<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:import href="copy.xsl"/>
   
<xsl:output method="xml" version="1.0" encoding="UTF-8"/>

<!-- filter out the index view since we don't create index information -->
<xsl:template match="view[name='Index']"/>

</xsl:stylesheet>
