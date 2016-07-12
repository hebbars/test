<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0" xmlns:xalan="http://xml.apache.org/xslt">
	<xsl:template match="/">
		<head>
			<title>
				(
				<xsl:value-of select="/StatusData/Broker/@ShortDescription" />
				)
				<xsl:value-of select="/StatusData/Broker/@Name" />
			</title>
			<style>
				th {font-size:14px;}
				td {font-size:12px; padding:1px;}
				table
				{border-collapse:collapse;}
				table, th, td, tr {border: 1px solid
				black;}
    		</style>
		</head>
		<html>
			<body>
				<!-- Display a page heading -->
				<h1 align="CENTER">
					Status information for
					<xsl:value-of select="/StatusData/Broker/@Name" />
					(
					<xsl:value-of select="/StatusData/Broker/@ShortDescription" />
					)
				</h1>

				<!--
					Test to see if there was an error. If so, display error message. If
					not, display normally
				-->
				<xsl:choose>
					<xsl:when test="/StatusData/Error">
						<p style="color: red">
							<b>Error: </b>
							<xsl:value-of select="/StatusData/Error" />
						</p>
						Detail:
						<br />
						<xsl:for-each select="/StatusData/ErrorDetail/line">
							<xsl:value-of select="." />
							<br />
						</xsl:for-each>
					</xsl:when>
					<xsl:otherwise>
						<!-- Show the time at which the status was last read -->
						<b>Status Read At: </b>
						<xsl:value-of select="/StatusData/GenerationTime" />

						<!-- Show broker level info -->
						<table>
							<tr>
								<td>
									<b>Broker Name: </b>
									<xsl:value-of select="/StatusData/Broker/@Name" />
									<br />
									<b>Short Description: </b>
									<xsl:value-of select="/StatusData/Broker/@ShortDescription" />
									<br />
									<b>Long Description: </b>
									<xsl:value-of select="/StatusData/Broker/LongDescription" />
									<br />
									<b>QManager: </b>
									<xsl:value-of select="/StatusData/Broker/@QManager" />
								</td>
								<td>
									<b>Workpath: </b>
									<xsl:value-of select="/StatusData/FileSystem/@WorkPath" />
									<br />
									<b>Workpath MB Free: </b>
									<xsl:value-of select="/StatusData/FileSystem/@WorkPathFreeMB" />
									<br />
									<b>Workpath MB Total: </b>
									<xsl:value-of select="/StatusData/FileSystem/@WorkPathTotalMB" />
									<br />
									<b>Workpath Percent Used: </b>
									<xsl:value-of select="/StatusData/FileSystem/@WorkPathPercentUsed" />
									<br />
								</td>
								<td>
									<b>Last Start: </b>
									<xsl:value-of select="/StatusData/FileSystem/LockFile/@Date" />
									<br />
									<b>HTTP/HTTPS Ports: </b>
									<xsl:value-of select="/StatusData/Broker/@HTTPPort" />
									:
									<xsl:value-of select="/StatusData/Broker/@HTTPSPort" />
									<br />
									<b>Default Soap HTTP/HTTPS Port Ranges: </b>
									<xsl:value-of select="/StatusData/Broker/@SoapHTTPPortRange" />
									:
									<xsl:value-of select="/StatusData/Broker/@SoapHTTPSPortRange" />
									<br />
									<b>Version: </b>
									<xsl:value-of select="/StatusData/Broker/@Version" />
									(
									<xsl:value-of select="/StatusData/Broker/@LongVersion" />
									)
									<br />
								</td>
							</tr>
						</table>
						<hr />
						<!--
							Use a table to display any files in the error directory on the
							system if they exist
						-->
						<xsl:choose>
							<xsl:when test="/StatusData/FileSystem/ErrorFiles/ErrorFile">
								<font color="Red">
									<b>Error files exist. These should be investigated and
										removed/archived</b>
								</font>
								<table cellspacing="10">
									<tr align="CENTER">
										<td>
											<b>File Name</b>
										</td>
										<td>
											<b>Date</b>
										</td>
										<td>
											<b>Size</b>
										</td>
									</tr>
									<xsl:for-each select="/StatusData/FileSystem/ErrorFiles/ErrorFile">
										<tr>
											<xsl:choose>
												<xsl:when
													test="@Type='Compressed' and /StatusData/DisplaySensitiveData='True'">
													<td>
														<a>
															<xsl:attribute name="href">BrokerInfo?Download=<xsl:value-of
																select="./@Name" />&amp;Key=<xsl:value-of select="./@FileIDKey"/></xsl:attribute><xsl:value-of select="./@Name"/></a></td>
												</xsl:when>
												<xsl:otherwise>
													<td><xsl:value-of select="./@Name"/></td>
												</xsl:otherwise>
											</xsl:choose>
											<td><xsl:value-of select="./@Date"/></td>
											<td><xsl:value-of select="./@Size"/></td>
										</tr>
									</xsl:for-each>
								</table>
								<hr/>	
							</xsl:when>
							<xsl:otherwise>
								<!--There are no error files detected at this time	 -->
							</xsl:otherwise>
						</xsl:choose>
    					<!-- Use a table to display the status of each execution group and message flow -->
    					<table cellspacing="0">
							<tr>
								<th>Execution Group/<i>Message Flow</i></th>
								<th>Status</th>
								<th>Monitoring<br/>Status</th>	
								<th>Monitoring<br/>Profile</th>	
								<th>Additional<br/>Instances</th>							
								<th>BarFile<br/>Name</th>							
								<th>BAR<br/>Deploy Time</th>
								<th>BAR<br/>ModifyTime</th>
								<th>HTTPPort<br/>(Set/InUse)</th>
								<th>HTTPSPort<br/>(Set/InUse)</th>
								<th>DebugPort<br/>Active</th>
								<th title="This indicates the status of Trace nodes in flows">Trace<br/>Nodes</th>
								<th>Service<br/>TraceLevel</th>
								<th>User<br/>TraceLevel</th>	
								<th>Trace<br/>LogSize</th>							
								<th>Last<br/>CompletionCode</th>
								</tr>
							<xsl:for-each select="/StatusData/ExecutionGroups/ExecutionGroup">								
								<tr align="CENTER">
									<td align="Left"><xsl:attribute name="title"><xsl:value-of select="./@UUID"/></xsl:attribute><b><xsl:value-of select="./@Name"/></b></td>
									<td><font><xsl:attribute name="color"><xsl:value-of select="./Status/@color"/></xsl:attribute><b><xsl:value-of select="./Status"/></b></font></td>
									<td><font><xsl:attribute name="color"><xsl:value-of select="./MonitoringStatus/@color"/></xsl:attribute><b><xsl:value-of select="./@MonitoringStatus"/></b></font></td>
									<td><font><xsl:attribute name="color"><xsl:value-of select="./@MonitoringProfilecolor"/></xsl:attribute><b><xsl:value-of select="./@MonitoringProfile"/></b></font></td>
									<td>-</td>
									<td>-</td>
									<td>-</td>
									<td>-</td>
									<td><b><xsl:value-of select="./@HTTPExplicitPort"/>/<xsl:value-of select="./@HTTPPort"/></b></td>
									<td><b><xsl:value-of select="./@HTTPSExplicitPort"/>/<xsl:value-of select="./@HTTPSPort"/></b></td>
									<td><b><xsl:value-of select="./@DebugPortActive"/></b></td>
									<td><b><xsl:value-of select="./@TraceNodeLevel"/></b></td>
									<td><b><xsl:value-of select="./@ServiceTraceLevel"/></b></td>
									<td><b><xsl:value-of select="./@UserTraceLevel"/></b></td>
									<td><b><xsl:value-of select="./@TraceLogSize"/></b></td>
									<td><b><xsl:value-of select="./@LastCompletionCode"/></b></td>
								</tr>
								<!-- Add information for each message flow in the execution group -->
								<xsl:for-each select="./MessageFlows/MessageFlow">
									<tr align="CENTER">
										<td align="right"><xsl:attribute name="title"><xsl:value-of select="./@Name"/></xsl:attribute><i><xsl:value-of select="./@ShortName"/></i></td>
										<td><font><xsl:attribute name="color"><xsl:value-of select="./Status/@color"/></xsl:attribute><xsl:value-of select="./Status"/></font></td>
										<td><font><xsl:attribute name="color"><xsl:value-of select="./@MonitoringStatuscolor"/></xsl:attribute><xsl:value-of select="./@MonitoringStatus"/></font></td>
										<td><font><xsl:attribute name="color"><xsl:value-of select="./@MonitoringProfilecolor"/></xsl:attribute><xsl:value-of select="./@MonitoringProfile"/></font></td>
										<td><xsl:value-of select="./@AdditionalInstances"/></td>
										<td><xsl:attribute name="title"><xsl:value-of select="./@BARFileName"/></xsl:attribute><xsl:value-of select="./@BARFileShortName"/></td>
										<td nowrap="nowrap"><xsl:value-of select="./@DeployTime"/></td>
										<td nowrap="nowrap"><xsl:value-of select="./@ModifyTime"/></td>
										<td>-</td>
										<td>-</td>
										<td>-</td>
										<td><xsl:value-of select="./@TraceNodeLevel"/></td>
										<td><xsl:value-of select="./@ServiceTraceLevel"/></td>
										<td><xsl:value-of select="./@UserTraceLevel"/></td>
										<td>-</td>
										</tr>
								</xsl:for-each>
							</xsl:for-each>
						</table>
						<!-- Some of the following data may be deemed security sensitive so it is only displayed conditionally based on env and https  -->
						<xsl:choose>
							<xsl:when test="/StatusData/DisplaySensitiveData='True'">
								<!-- List all the Configurable Services -->
								<hr/>
								<b>Configurable services (Template Services not displayed)</b>
								<table cellspacing="0" border="1" >
									<tr align="CENTER">
										<th>Type</th>
										<th>Configurable Service Name</th>
										<th>Properties</th>
									</tr>
									<!-- <xsl:for-each select="/StatusData/ConfigurableServices/ConfigurableService[@Type='IMSConnect' or @Type='MonitoringProfiles' or @Type='JDBCProviders']"> -->
									<xsl:for-each select="/StatusData/ConfigurableServices/ConfigurableService[@Template='false']">
										<tr>
											<td><xsl:value-of select="./@Type"/></td>
											<td><xsl:value-of select="./@Name"/></td>
											<td>
											<table width="100%" >
											<xsl:for-each select="./Property">
												<tr>
													<td width="20%"><xsl:value-of select="./@Key"/></td>
													<td width="80%"><xsl:value-of select="."/>  </td>
												</tr>
											</xsl:for-each>
											</table>
											</td>
										</tr>
									</xsl:for-each>
								</table>
								<hr/>
								<b>DP Parms</b>
		    					<table cellspacing="10">
									<tr align="CENTER">
										<td><b>Security Identifier</b></td>
										<td><b>UserId</b></td>
									</tr>
									<xsl:for-each select="/StatusData/FileSystem/DBparms/DBparm">
										<tr>
											<td><xsl:value-of select="./@Name"/></td>
											<td><xsl:value-of select="./@UserID"/></td>
										</tr>
									</xsl:for-each>
								</table>
								<hr/>
							</xsl:when>	
							<xsl:otherwise>
								<p>Display of additional data or the download of abend files requires a secure connection. Please refer to the system administrator</p>
							</xsl:otherwise>
						</xsl:choose>		
								<b>Shared classes</b>
		    					<table cellspacing="10">
									<tr align="CENTER">
										<td><b>File Name</b></td>
										<td><b>Date</b></td>
										<td><b>Size</b></td>
									</tr>
									<xsl:for-each select="/StatusData/FileSystem/SharedClasses/SharedClass">
										<tr>
											<td><xsl:value-of select="./@Name"/></td>
											<td><xsl:value-of select="./@Date"/></td>
											<td><xsl:value-of select="./@Size"/></td>
										</tr>
									</xsl:for-each>
								</table>
								<hr/>
								<b>XML/external</b>
		    					<table cellspacing="10">
									<tr align="CENTER">
										<td><b>File Name</b></td>
										<td><b>Date</b></td>
										<td><b>Size</b></td>
									</tr>
									<xsl:for-each select="/StatusData/FileSystem/XMLExternal/file">
										<tr>
											<td><xsl:value-of select="./@Name"/></td>
											<td><xsl:value-of select="./@Date"/></td>
											<td><xsl:value-of select="./@Size"/></td>
										</tr>
									</xsl:for-each>
								</table>
								<hr/>
								<b>XSL/external</b>
		    					<table cellspacing="10">
									<tr align="CENTER">
										<td><b>File Name</b></td>
										<td><b>Date</b></td>
										<td><b>Size</b></td>
									</tr>
									<xsl:for-each select="/StatusData/FileSystem/XSLExternal/file">
										<tr>
											<td><xsl:value-of select="./@Name"/></td>
											<td><xsl:value-of select="./@Date"/></td>
											<td><xsl:value-of select="./@Size"/></td>
										</tr>
									</xsl:for-each>
								</table>
								<hr/>
					</xsl:otherwise>
				</xsl:choose>
       		</body>
   		</html>
	</xsl:template>
</xsl:stylesheet>
