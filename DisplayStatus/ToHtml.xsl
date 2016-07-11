<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0"
    xmlns:xalan="http://xml.apache.org/xslt">
    <xsl:template match="/">
  		<html>
  			<body>
   				
   				<!-- Test to see if there was an error. If so, display error message. If not, display normally -->
   				<xsl:choose>
   					<xsl:when test="/StatusData/Error">
						<p style="color: red"><b>Error: </b><xsl:value-of select="/StatusData/Error"/></p>
					</xsl:when>
					<xsl:otherwise>
					
						<!-- Display a page heading -->
   						<h1 align="CENTER">Execution Group Status</h1>
   				
   						<br/>
   				
   						<!-- Show the name of the broker -->
   						<h3 align="LEFT">Broker: <xsl:value-of select="/StatusData/BrokerName"/></h3>
   				
   						<br/>
						
						<!-- Show the time at which the status was last read -->
   						<b>Status Read At: </b><xsl:value-of select="/StatusData/Time"/>
   				
   						<hr/>
    
    					<!-- Use a table to display the status of each execution group -->
    					<!-- Add code here to display additional execution group information in this table -->
    					<table align="left" cellspacing="10">
							<tr>
								<td><b>Execution Group</b></td>
								<td><b>Status</b></td>
							</tr>
							<xsl:for-each select="/StatusData/ExecutionGroups/ExecutionGroup">
								<xsl:variable name="StatusColour"><xsl:value-of select="./Colour"/></xsl:variable>
								<tr>
									<td><xsl:value-of select="./Name"/></td>
									<td><font color="#{$StatusColour}"><b><xsl:value-of select="./Status"/></b></font></td>
								</tr>
							</xsl:for-each>
						</table>
					</xsl:otherwise>
				</xsl:choose>
       		</body>
   		</html>
	</xsl:template>
</xsl:stylesheet>
