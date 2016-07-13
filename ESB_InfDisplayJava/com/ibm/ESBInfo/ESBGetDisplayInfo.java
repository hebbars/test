package com.ibm.ESBInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

import com.ibm.broker.config.proxy.BrokerProxy;
import com.ibm.broker.config.proxy.ConfigManagerProxyLoggedException;
import com.ibm.broker.config.proxy.ConfigManagerProxyPropertyNotInitializedException;
import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.*;

/**
 * @author Brian Reddick
 * Inspired by a flow by James Rapley that showed if a given execution group was running or not<br/>
 * Extended by using the API exerciser as a guide
 * 
 * This class is intended to be replaced by a standard set of classes that can be used to drive a variety
 * of tools for the ESB project and produce more robust and standardised XML.
 * 
 * Once this new package is created then this class will be replace by a minimal set of calls to this standard set
 * Most of the 'presentation' has been removed from this routing and placed elsewhere to separate data collection
 * from display
 *
 */
public class ESBGetDisplayInfo extends MbJavaComputeNode {
	// Return message if connection to broker cannot be established
	final private String CONN_FAIL = "<Error>Failed to connect to Broker</Error>";

	// The number of milliseconds that must pass before the status can be refreshed.
	final private long REFRESH_INTERVAL = 5000;

	// The info to be returned as at the last refresh was performed.
	private String currentStatus = null;
	
	private String workPath = null;
	private String DownloadFileName = "";
	private String DownloadFileKey = "";
	private String OriginViaHTTPS = "";
	
	// A unique time stamp is generated here - the time the flow was run after the last restart of the EG
	// this can be used to make hashes etc much harder to hack as it is added to temporary keys
	private String extraPlaintext = BrokerInfo.SDF.format(Calendar.getInstance().getTime());

	// The time at which the status was last refreshed.
	private Date lastRefresh = null;
	private Long lastRefreshMS = null;
	
	/* This is the method that the broker calls and triggers all evaluation
	 * @see com.ibm.broker.javacompute.MbJavaComputeNode#evaluate(com.ibm.broker.plugin.MbMessageAssembly)
	 */
	public void evaluate(MbMessageAssembly inAssembly) throws MbException {
		MbOutputTerminal out = getOutputTerminal("out");
		MbOutputTerminal alt = getOutputTerminal("alternate");
		MbOutputTerminal fail = getOutputTerminal("failure");
		MbMessage inMessage = inAssembly.getMessage();
		@SuppressWarnings("unused")
		MbElement outRoot, outBody;

		// create new message
		MbMessage outMessage = new MbMessage();
		MbMessageAssembly outAssembly = new MbMessageAssembly(inAssembly, outMessage);
		
		boolean fileDownloadRequested = false;

		try {
			// ----------------------------------------------------------
			// Add user code below
			
			// Copy message headers
			copyMessageHeaders(inMessage, outMessage);
			
			this.DownloadFileName = "";
			this.DownloadFileKey = "";
			parseQueryString(inMessage);
			
			this.OriginViaHTTPS = "";
			parseOriginalHTTPCommandString(inMessage);
			
			fileDownloadRequested =  this.DownloadFileName.endsWith(".gz");
			
			// Check to see if the statuses need to be checked again.
			if ( (!fileDownloadRequested) && (lastRefresh == null || !recentRefresh())) {
				// Read all the different types of status
				GetInfo();
			}

			// Build the out message 
			outRoot = outMessage.getRootElement();
			
			//Presentation later may depend on the security of the requestor channel so put that in the environment
			MbMessage env = outAssembly.getGlobalEnvironment();
			env.getRootElement().createElementAsLastChild(MbElement.TYPE_NAME, "ESBGetDisplay", null).
				createElementAsLastChild(MbElement.TYPE_NAME_VALUE , "cmd",  this.OriginViaHTTPS);
			
			// if a file download is requested then check that the file is valid and change http headers to notify
			// the browser to expect a file rather than a web page. The message is propagated through the alt vs the out terminal
			if (fileDownloadRequested){
				
				// Set the HTTPReply headers adding the correct file name
				MbElement httpReply = outRoot.createElementAsLastChild(MbElement.TYPE_NAME , "HTTPReplyHeader", null);
				httpReply.createElementAsLastChild(MbElement.TYPE_NAME_VALUE , "Content-Type", "application/x-gzip-compressed");
				httpReply.createElementAsLastChild(MbElement.TYPE_NAME_VALUE , "Content-disposition", "attachment; filename=" + DownloadFileName);
				
				// Experiment with adding file name and Original HTTP Command to environment
				env.getRootElement().createElementAsLastChild(MbElement.TYPE_NAME, "ESBGetDisplay", null).
					createElementAsLastChild(MbElement.TYPE_NAME, "DownloadFile", null).
					createElementAsLastChild(MbElement.TYPE_NAME_VALUE , "name",  DownloadFileName);
				
				// The file must be readable and the provided Key must match the calculated one - otherwise return text file with 'not known' content
				// This should prevent path spoofing as the key changes each time the flow is refreshed because of the extra plaintext
				File f = new File(workPath + File.separator + "common" + File.separator + "errors" + File.separator + this.DownloadFileName );
				
				Boolean isHashMatch = BrokerInfo.getHash(f.getCanonicalPath() + this.extraPlaintext).equalsIgnoreCase(this.DownloadFileKey );
				if ( f.canRead() & isHashMatch ) {
					outBody = outRoot.createElementAsLastChildFromBitstream(getBytesFromFile(f), MbBLOB.PARSER_NAME, null, null, null, 0, 0, 0);					
				}else{
					httpReply.getLastChild().setValue("attachment; filename=NoSuchFile.txt");
					outBody = outRoot.createElementAsLastChildFromBitstream("The requested file did not exist on the Broker with a matching Key".getBytes(), MbBLOB.PARSER_NAME, null, null, null, 0, 0, 0);
				}
				// output the encoded file through the alt terminal
				alt.propagate(outAssembly);
			}else{
				
				// Set the HTTPReply Headers for a HTML page return (for consistency done here rather than in a later HTTPHeader node)
				MbElement httpReply = outRoot.createElementAsLastChild(MbElement.TYPE_NAME , "HTTPReplyHeader", null);
				httpReply.createElementAsLastChild(MbElement.TYPE_NAME_VALUE , "Content-Type", "text/html; charset=utf-8");

				outBody = outRoot.createElementAsLastChildFromBitstream(currentStatus.getBytes(), MbXMLNSC.PARSER_NAME, null, null, null, 0, 0, 0);
				out.propagate(outAssembly);
			}

			// End of user code
			// ----------------------------------------------------------
			

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail.propagate(inAssembly);
		} finally {
			// clear the outMessage
			outMessage.clearMessage();

		}
	}

	/**
	 * Extract the X-Query-String from the head and do a rough parse to extract the two expected values
	 * @param inMessage is the input structure
	 * @param DownloadFileName is updated to the Download parameter (if found)
	 * @param DownloadFileKey is updated to the Key parameter (if found)
	 */
	private void parseQueryString(MbMessage inMessage){
		// string will be of the format Download=MB7BROKER.WMB_INFRASTRUCTURE.1752.5376.Abend.gz&Key=a18398938001229745302d986e7ff4f8
		//String httpCommand = inMessage.getRootElement().getFirstElementByPath("/HTTPInputHeader/X-Query-String").getValueAsString();
		try {
			String QueryString = inMessage.getRootElement().getFirstElementByPath("/HTTPInputHeader/X-Query-String").getValueAsString();
			String [] components=QueryString.split("&");
			for (String s: components){
				if (s.startsWith("Download=")){
					this.DownloadFileName= s.substring(9);
				}
				if (s.startsWith("Key=")){
					this.DownloadFileKey= s.substring(4);
				}
			}
			
		} catch (MbException e){
			//the query string was not in the message - allow the values to be unchanged
		} catch (Exception e) {
			//an error occurred - allow the values to be unchanged
			}
	}

	/**
	 * Extract the X-Query-String from the head and do a rough parse to extract the two expected values
	 * @param inMessage is the input structure
	 * @param DownloadFileName is updated to the Download parameter (if found)
	 * @param DownloadFileKey is updated to the Key parameter (if found)
	 */
	private void parseOriginalHTTPCommandString(MbMessage inMessage){
		// string will be of the format X-Original-HTTP-Command:CHARACTER:GET https://localhost:7101/BrokerInfo HTTP/1.1
		//                           or X-Original-HTTP-Command:CHARACTER:GET http://localhost:7100/BrokerInfo HTTP/1.1
		try {
			String QueryString = inMessage.getRootElement().getFirstElementByPath("/HTTPInputHeader/X-Original-HTTP-Command").getValueAsString();
			if (QueryString.toUpperCase().startsWith("GET HTTPS:")){
				this.OriginViaHTTPS = "HTTPS";
				return;
			}
			if (QueryString.toUpperCase().startsWith("GET HTTP:")){
				this.OriginViaHTTPS = "HTTP";
				return;
			}
			this.OriginViaHTTPS = "UNKNOWN:" + QueryString;
			
		} catch (MbException e){
			//the query string was not in the message - allow the values to be unchanged
		} catch (Exception e) {
			//an error occurred - allow the values to be unchanged
			}
	}

	
	public void copyMessageHeaders(MbMessage inMessage, MbMessage outMessage) throws MbException {
		MbElement outRoot = outMessage.getRootElement();

		// Iterate though the headers starting with the first child of the root
		// element
		MbElement header = inMessage.getRootElement().getFirstChild();
		while (header != null && header.getNextSibling() != null) {
			// copy the header and add it to the out message
			outRoot.addAsLastChild(header.copy());
			// move along to next header
			header = header.getNextSibling();
		}
	}

	/**
	 *  GetInfo is the driver function that calls all the components that make up broker and orders the output
	 *  by populating 'currentstatus' which is then reused if the flow is called again within the refresh time
	 */
	private void GetInfo() {

		currentStatus = "";

		// Holds a string description of each type of information as it is built up
		StringBuffer status = new StringBuffer();

		try {
			// The Broker proxy for the broker this flow is deployed on
			BrokerProxy broker = BrokerProxy.getLocalInstance();
			BrokerInfo brokerinfo = new BrokerInfo(broker);
			
			brokerinfo.setFormattingApplied(true);
			brokerinfo.setExtraPlaintext(this.extraPlaintext); // add something into the File level security key that is returned

			status.append("<StatusData>");
				// Get the broker level information that is important then add each
				// of the groups of information required for display
				String s = brokerinfo.getBrokerInfo();
				status.append(s);
	
				//Get all the execution group information for this broker
				s = brokerinfo.getExecutionGroupInfo();
				status.append(s);
	
				//Get specific file system information
				
				status.append(brokerinfo.getFileSystemInfo());
				workPath = brokerinfo.getWorkPath();
	
				//Get the configurable services defined on the broker
				s = brokerinfo.getConfigurableServiceInfo();
				status.append(s);
				
				// Get the current time as the time this data was generated
				lastRefresh = Calendar.getInstance().getTime();
				lastRefreshMS = Calendar.getInstance().getTimeInMillis();
							
				
				status.append("<GenerationTime>" + BrokerInfo.SDF.format(lastRefresh) + "</GenerationTime>");
				
			status.append("</StatusData>");
			
			currentStatus = status.toString();

		} catch (ConfigManagerProxyLoggedException e) {
			// An error message if connection failed.
			currentStatus = CONN_FAIL;
		} catch (ConfigManagerProxyPropertyNotInitializedException e) {
			// An error message if connection failed.
			currentStatus = CONN_FAIL;
		} catch (Exception e) {
			// some other error has occurred so output something to assist debugging
			// this info will be displayed by the webpage
			StringBuffer tempErr = new StringBuffer();
			tempErr.append("<line>" + e.getMessage() + "</line>");
			StackTraceElement st[] = e.getStackTrace();
			for (StackTraceElement stackTraceElement : st) {
				tempErr.append("<line>" + stackTraceElement.toString()
						+ "</line>");
			}
			currentStatus = "<Error>Non connection related error - if this problem persists please advise the deployment team</Error><ErrorDetail>"
					+ tempErr.toString() + "</ErrorDetail>";
		}

	}
	/**
	 * To prevent DOS effects we only gather the information if the request is outside the 'refresh' period.
	 * this method return true if there has been a recent refresh so the previous data can be returned instead
	 * of gathering data again for this request
	 * @return
	 */
	public boolean recentRefresh() {
		// Get the current time.
		Long currentTime = Calendar.getInstance().getTimeInMillis();

		// If the current time is within 'REFRESH_INTERVAL' of the last refresh return  true, else false.
		if (currentTime < lastRefreshMS + REFRESH_INTERVAL)
			return true;
		else
			return false;
	}
	
	/**
	 * In order to return a file via the broker webpage it needs to be converted to a byte array
	 * this sample was downloaded from the URL below
	 * http://www.java-tips.org/java-se-tips/java.io/reading-a-file-into-a-byte-array.html
	 * @param file
	 * @return
	 * @throws IOException
	 */
    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
    
        // Get the size of the file
        long length = file.length();
    
        if (length > Integer.MAX_VALUE) {
            // File is too large (>2147483647)
        }
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];
    
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
    
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }
    
        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

}
