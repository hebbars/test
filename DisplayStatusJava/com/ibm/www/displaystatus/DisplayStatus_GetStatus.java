package com.ibm.www.displaystatus;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import com.ibm.broker.config.proxy.ConfigManagerProxyLoggedException;
import com.ibm.broker.config.proxy.ConfigManagerProxyPropertyNotInitializedException;
import com.ibm.broker.config.proxy.BrokerProxy;
import com.ibm.broker.config.proxy.ExecutionGroupProxy;
import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbElement;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbMessage;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbOutputTerminal;
import com.ibm.broker.plugin.MbXMLNSC;

/**
 * This class is used to query broker and execution group statuses so that they
 * can be displayed via a web interface. One of two interfaces can be displayed.
 * The regular version contains basic information about the broker and it's 
 * execution groups. The 'Plus' version displays additional information which may
 * be of interest to an ESB developer.
 * 
 * @author James Rapley
 */
public class DisplayStatus_GetStatus extends MbJavaComputeNode 
{	
	// The name of the broker (to be displayed by the web interface).
	private String brokerName;
	
	// The type of web interface to be displayed (standard, plus or none)
	private final int NONE = 0;
	private final int STANDARD = 1;
	private final int PLUS = 2;
	
	// The valid url suffixes for accessing the web interface
	private final CharSequence PLUS_URL = "/BrokerStatus/Plus ";
	private final CharSequence STANDARD_URL = "/BrokerStatus/Standard ";
	
	// The error message when an invalid URL is used
	private final String invalidURL = "<StatusData><Error>Invalid URL used for Broker Status Web Interface</Error></StatusData>";
	
	// The current status, at the time of the last refresh.
	private String currentStatus = null;
	private String currentStatusPlus = null;
	
	// The time at which the status was last refreshed.
	private Date lastRefresh = null;
	private Long lastRefreshMS = null;
	private Date lastRefreshPlus = null;
	private Long lastRefreshMSPlus = null;
	
	// The number of milliseconds that must pass before the status can be refreshed.
	// If the page is refreshed within this interval the status will be retrieved from memory.
	final private long REFRESH_INTERVAL = 180000;
	
	// The format used to display refresh time data as a String.
	final private SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	// The various statuses that an execution group can have
	final private String RUNNING = "<Status>RUNNING</Status>";
	final private String NOT_RUNNING = "<Status>DOWN</Status>";
	final private String CONN_FAIL = "<Error>Failed to connect to Broker</Error>";
	
	// Colours used to display various statuses
	final private String RUNNING_COLOUR = "<Colour>00CC00</Colour>";
	final private String NOT_RUNNING_COLOUR = "<Colour>FF0000</Colour>";
	
	// Executed when the flow is called.
	public void evaluate(MbMessageAssembly inAssembly) throws MbException 
	{
		MbOutputTerminal out = getOutputTerminal("out");
		@SuppressWarnings("unused")
		MbOutputTerminal alt = getOutputTerminal("alternate");
		MbMessage inMessage = inAssembly.getMessage();
		@SuppressWarnings("unused")
		MbElement outRoot, outBody;
		
		// Create the output message
		MbMessage outMessage = new MbMessage();
		MbMessageAssembly outAssembly = new MbMessageAssembly(inAssembly, outMessage);
		
		// Indicates which version of the web interface is to be displayed. The plus version contains
		// extra information which is not intended for the SIP. If true, the plus version is displayed.
		int webInterfaceType;

		try 
		{
			// Copy message headers
			copyMessageHeaders(inMessage, outMessage);
			
			// Check to see which version of the web interface is to be displayed.
			webInterfaceType = getWebInterfaceType(inMessage);
			
			// Check to see if the status need to be checked again.
			if(webInterfaceType != NONE && !recentRefresh(webInterfaceType))
				readExecutionGroupStatuses(webInterfaceType);
			
			// Build the out message and put the existing status data in the output message.
	        outRoot = outMessage.getRootElement();
			outBody = outRoot.createElementAsLastChildFromBitstream(displayStatuses(webInterfaceType), MbXMLNSC.PARSER_NAME, null, null, null, 0,0,0);
			
			// Propagate message to the out terminal.
			out.propagate(outAssembly);
		} 
		finally 
		{
			// clear the outMessage even if there's an exception
			outMessage.clearMessage();
		}
	}

	public void copyMessageHeaders(MbMessage inMessage, MbMessage outMessage) throws MbException 
	{
		MbElement outRoot = outMessage.getRootElement();

		// Iterate though the headers starting with the first child of the root element.
		MbElement header = inMessage.getRootElement().getFirstChild();
		
		while (header != null && header.getNextSibling() != null)
		{
			// Copy the header and add it to the out message.
			outRoot.addAsLastChild(header.copy());
			
			// Move along to next header.
			header = header.getNextSibling();
		}
	}
	
	// Read the broker and execution group statuses
	private void readExecutionGroupStatuses(int webInterfaceType)
	{
		// Holds a string description of the status of each execution group to be checked.
		StringBuffer status = new StringBuffer();
		
		// An execution group proxy for iterating through the list of execution groups.
		ExecutionGroupProxy executionGroup;
		
		try
		{
			// The Broker proxy for the broker this flow is deployed on
			BrokerProxy broker = BrokerProxy.getLocalInstance();
			
			// Get the broker name.
			brokerName = broker.getName();

			// Get all the execution groups for this broker.
			Enumeration<ExecutionGroupProxy> executionGroups = broker.getExecutionGroups(null);
		
			status.append("<ExecutionGroups>");
		
			// Create an XML representation of the status of each execution group.
			while(executionGroups.hasMoreElements())
			{
				// Get the next execution group
				executionGroup = executionGroups.nextElement();
			
				// Add the execution group name
				status.append("<ExecutionGroup><Name>" + executionGroup.getName() + "</Name>");				
				
				// Add code here in order to display information for each execution group
				// in the standard Broker Status web interface.
				// =================================================
				
				// Check to see if the execution group is running and set the status and display colour.
				if(executionGroup.isRunning())
					status.append(RUNNING + RUNNING_COLOUR);
				else
					status.append(NOT_RUNNING + NOT_RUNNING_COLOUR);
				
				// =================================================
				
				if(webInterfaceType == PLUS)
				{
					// Add code here in order to display information for each execution group 
					// in the Broker Status Plus web interface.
					// =================================================
					
					
					
					// =================================================
				}

				status.append("</ExecutionGroup>");
			}
			
			// Close the list of all execution groups
			status.append("</ExecutionGroups>");
			
			// Set the status based on the data read from the broker and which version of the 
			// web interface is to be displayed.
			if(webInterfaceType == PLUS)
			{
				currentStatusPlus = status.toString();
				
				// Get the current time
				lastRefreshPlus = Calendar.getInstance().getTime();
				lastRefreshMSPlus = Calendar.getInstance().getTimeInMillis();
			}
			else
			{
				currentStatus = status.toString();
				
				// Get the current time
				lastRefresh = Calendar.getInstance().getTime();
				lastRefreshMS = Calendar.getInstance().getTimeInMillis();
			}

		}
		catch(ConfigManagerProxyLoggedException e)
		{
			// An error message if connection failed.
			if(webInterfaceType == PLUS)
				currentStatusPlus = CONN_FAIL;
			else
				currentStatus = CONN_FAIL;
		}
		catch(ConfigManagerProxyPropertyNotInitializedException e) 
		{
			// An error message if connection failed.
			if(webInterfaceType == PLUS)
				currentStatusPlus = CONN_FAIL;
			else
				currentStatus = CONN_FAIL;
		}
	}
	
	// Put the status data into a byte array so that it may be output as XML.
	private byte[] displayStatuses(int webInterfaceType)
	{
		// The string to be returned.
		StringBuffer status = new StringBuffer();
		
		// If no valid url was used, return an error message
		if(webInterfaceType == NONE)
		{
			status.append(invalidURL);
			return status.toString().getBytes();
		}
		
		// Add the broker name and refresh time data to the output data.
		status.append("<StatusData><BrokerName>" + brokerName + "</BrokerName>");
		
		// Add the current status and refresh time based on which version of the web interface
		// is being displayed.
		if(webInterfaceType == PLUS)
		{
			// Add the last refresh time, if one was able to be performed.
			if(lastRefresh != null)
				status.append("<Time>" + SDF.format(lastRefreshPlus) + "</Time>");
			
			status.append(currentStatusPlus + "</StatusData>");
		}
		else if(webInterfaceType == STANDARD)
		{
			// Add the last refresh time, if one was able to be performed.
			if(lastRefresh != null)
				status.append("<Time>" + SDF.format(lastRefresh) + "</Time>");
			
			status.append(currentStatus + "</StatusData>");
		}
		
		// Return this data as a byte array.
		return status.toString().getBytes();
	}
	
	// Check to see if the last refresh was recent enough to avoid having to check the status again.
	private boolean recentRefresh(int webInterfaceType)
	{
		// Get the current time.
		Long currentTime = Calendar.getInstance().getTimeInMillis();
		
		// Check to see if a recent refresh has been performed based on the version of the
		// web interface to be displayed.
		if(webInterfaceType == PLUS)
		{
			if(lastRefreshPlus == null)
				return false;
			
			if(currentTime < lastRefreshMSPlus + REFRESH_INTERVAL)
				return true;
		}
		else if(webInterfaceType == STANDARD)
		{
			if(lastRefresh == null)
				return false;
			
			if(currentTime < lastRefreshMS + REFRESH_INTERVAL)
				return true;
		}
		
		return false;
	}
	
	// Determine if the 'Plus' version of the web interface is to be displayed based on the URL
	// used to invoke this flow.
	private int getWebInterfaceType(MbMessage inMessage)
	{
		// The original http command from the http input node
		String httpCommand;
		
		try
		{
			// Get the original http command
			httpCommand = inMessage.getRootElement().getFirstElementByPath("/HTTPInputHeader/X-Original-HTTP-Command").getValueAsString();
		}
		catch(MbException e)
		{
			return NONE;
		}
		
		// Look at the URL to determine which version of the web interface is to be displayed.
		if(httpCommand.contains(STANDARD_URL))
			return STANDARD;
		else if(httpCommand.contains(PLUS_URL))
			return PLUS;
		else
			return NONE;
	}
}