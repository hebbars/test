package com.ibm.ESBInfo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.ibm.broker.config.proxy.BrokerProxy;
import com.ibm.broker.config.proxy.ConfigManagerProxyPropertyNotInitializedException;
import com.ibm.broker.config.proxy.ConfigurableService;
import com.ibm.broker.config.proxy.ExecutionGroupProxy;
import com.ibm.broker.config.proxy.MessageFlowProxy;

/**
 * @author Brian Reddick
 *
 * This class creates the standard XML output that can be used in other tools.
 * It is implemented to be non  ESB project specific to enable easier reuse
 * 
 * Business logic would be applied in another layer for colour coding of data etc. This class is for data return only.
 * It is expected that the calling class will manage DOS issues by not calling this class to often 
 * 
 * Each of the main external methods returns an XML formatted string. Internally these call the methods that utilise the SAX parser
 * to generate XML in a way that ensures that it is valid and well formed.
 * 
 */
/**
 * @author Brian Reddick - brianrck
 * 
 */
public class BrokerInfo {

	// Standard formats that are used for display purposes
	final public static DecimalFormat DigitFormat = new DecimalFormat("#,##0");
	final public static DecimalFormat PercentFormat = new DecimalFormat("0.0%");
	final public static SimpleDateFormat SDF = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	final public static String DATA_UNAVAILABLE = "Not Available";

	// Number of bytes in a MB - used to convert bytes to MB
	final public static long MB = 1048576; // One MB in bytes

	// Used in the compression routine
	static final int BUFFER = 2048;
	private boolean isFormattingApplied = true;
	private String workPath = null;
	private String mqsiRegistry = null;
	private String brokerName = null;
	private long zipDeleteDelay = 60000; // default of 60 sec age before a file
											// can be zipped and deleted

	// Unique addition to plaintext pre hashing
	private String extraPlaintext = "";

	BrokerProxy broker;

	// XML creation components
	ByteArrayOutputStream outputStreamXML;
	TransformerHandler th;

	/**
	 * The format of registry and other files in broker depends on the platform.
	 * In particular the extension on AIX vs Windows registryFileExtension =
	 * .dat for windows and blank for Unix
	 */
	public static String registryFileExtension = System.getProperty("os.name")
			.contains("Windows") ? ".dat" : "";
	/**
	 * The format of the seperator in the name of a dbparam is different on AIX
	 * vs Windows registryDBParamSeperator = ## for windows and :: for Unix
	 */
	public static String registryDBParamSeperator = System.getProperty(
			"os.name").contains("Windows") ? "##" : "::";

	public BrokerInfo(BrokerProxy broker)
			throws ConfigManagerProxyPropertyNotInitializedException {

		this.broker = broker;
		this.brokerName = broker.getName();

		// set up the components required to construct the XML output stream
		outputStreamXML = new ByteArrayOutputStream();
		StreamResult streamResult = new StreamResult(outputStreamXML);
		SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory
				.newInstance();
		// SAX2.0 ContentHandler.
		try {
			th = tf.newTransformerHandler();
		} catch (TransformerConfigurationException e) {
			// TODO Work out what to do if the constructor fails here
			e.printStackTrace();
		}
		Transformer serializer = th.getTransformer();
		serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		serializer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
		// TODO Add in a schema definition element to the output stream when it
		// is defined
		// serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,"ESBBroker.dtd");
		// ?? or something like this
		serializer.setOutputProperty(OutputKeys.INDENT, "yes");
		th.setResult(streamResult);

	}

	/**
	 * Directly accessible services would come in at this location but there are
	 * none implemented at this time
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		System.out
				.println("No directly callable functions are implemented at this time");
		System.out.println("version @ESBAuomatedBuild@");
	}

	/**
	 * Public interface for obtaining Broker level information in an XML format
	 * 
	 * @return A XML formatted string representing all broker level information
	 * @throws ConfigManagerProxyPropertyNotInitializedException
	 */
	public String getBrokerInfo()
			throws ConfigManagerProxyPropertyNotInitializedException {
		// Reset the XML output stream and then call the method that embeds the
		// required
		// attributes into the stream and return the result as a string
		outputStreamXML.reset();
		try {
			th.startDocument();
			AttributeWrapper atts = new AttributeWrapper();

			getBrokerDetails(atts);

			th.endDocument();

		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return outputStreamXML.toString();

	}

	/**
	 * Public interface for obtaining ExecutionGroup level information in an XML
	 * format
	 * 
	 * @return A XML formatted string representing all ExecutionGroup level
	 *         information
	 * @throws ConfigManagerProxyPropertyNotInitializedException
	 */
	public String getExecutionGroupInfo()
			throws ConfigManagerProxyPropertyNotInitializedException {
		// Reset the XML output stream and then call the method that embeds the
		// required
		// attributes into the stream and return the result as a string
		outputStreamXML.reset();
		try {

			th.startDocument();
			AttributeWrapper atts = new AttributeWrapper();

			getExecutionGroupDetails(atts);

			th.endDocument();

		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return outputStreamXML.toString();

	}

	/**
	 * Public interface for obtaining ConfigurableService level information in
	 * an XML format
	 * 
	 * @return A XML formatted string representing all ConfigurableService level
	 *         information
	 * @throws ConfigManagerProxyPropertyNotInitializedException
	 */
	public String getConfigurableServiceInfo()
			throws ConfigManagerProxyPropertyNotInitializedException {
		// Reset the XML output stream and then call the method that embeds the
		// required
		// attributes into the stream and return the result as a string
		outputStreamXML.reset();
		try {
			th.startDocument();
			AttributeWrapper atts = new AttributeWrapper();

			getConfigurableServiceDetails(atts);

			th.endDocument();

		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return outputStreamXML.toString();

	}

	/**
	 * Public interface for obtaining FileSystem level information in an XML
	 * format
	 * 
	 * @return A XML formatted string representing all FileSystem level
	 *         information
	 * @throws ConfigManagerProxyPropertyNotInitializedException
	 */
	public String getFileSystemInfo()
			throws ConfigManagerProxyPropertyNotInitializedException {
		// Reset the XML output stream and then call the method that embeds the
		// required
		// attributes into the stream and return the result as a string
		outputStreamXML.reset();
		try {
			th.startDocument();
			AttributeWrapper atts = new AttributeWrapper();

			getFileSystemDetails(atts);

			th.endDocument();

		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return outputStreamXML.toString();
	}

	/**
	 * Create an XML representation of information about this broker assuming
	 * that this function is adding to an existing XML structure and not
	 * creating a new one from scratch
	 * 
	 * @return XML formatted string
	 * @throws ConfigManagerProxyPropertyNotInitializedException
	 */
	private void getBrokerDetails(AttributeWrapper atts)
			throws ConfigManagerProxyPropertyNotInitializedException {

		// hd is the TransformHandler that is used to create all output
		try {
			// Top level tag
			atts.add("Name", broker.getName());
			atts.add("ShortDescription", broker.getShortDescription());
			atts.add("QManager", broker.getQueueManagerName());
			atts.add("HTTPPort", broker
					.getHTTPListenerProperty("HTTPConnector/port"));
			atts.add("HTTPSPort", broker
					.getHTTPListenerProperty("HTTPSConnector/port"));
			atts
					.add(
							"SoapHTTPPortRange",
							broker
									.getRegistryProperty("BrokerRegistry/HTTPConnectorPortRange"));
			atts
					.add(
							"SoapHTTPSPortRange",
							broker
									.getRegistryProperty("BrokerRegistry/HTTPSConnectorPortRange"));
			atts.add("UUID", broker.getUUID());
			atts.add("Version", broker.getBrokerVersion());
			atts.add("LongVersion", broker.getBrokerLongVersion());

			th.startElement("", "", "Broker", atts);
			atts.clear();
			th.startElement("", "", "LongDescription", atts);
			th.characters(broker.getLongDescription().toCharArray(), 0, broker
					.getLongDescription().length());
			th.endElement("", "", "LongDescription");
			th.endElement("", "", "Broker");

		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Create an XML representation of information about each Execution Group on
	 * the specified broker
	 * 
	 * @param broker
	 * @return XML formatted string
	 * @throws ConfigManagerProxyPropertyNotInitializedException
	 */
	private void getExecutionGroupDetails(AttributeWrapper atts)
			throws ConfigManagerProxyPropertyNotInitializedException {

		// Get all the execution groups for this broker and sort them according
		// to the comparator (alphabetically)
		List<ExecutionGroupProxy> executionGroups = Collections.list(broker
				.getExecutionGroups(null));
		Collections.sort(executionGroups, new EGComparator());

		try {
			th.startElement("", "", "ExecutionGroups", atts);

			for (ExecutionGroupProxy executionGroup : executionGroups) {
				// Top level tag
				atts.clear();
				atts.add("Name", executionGroup.getName());
				atts.add("isRunning", executionGroup.isRunning());
				atts
						.add(
								"HTTPExplicitPort",
								executionGroup
										.getRuntimeProperty("HTTPConnector/explicitlySetPortNumber"));
				atts.add("HTTPPort", executionGroup
						.getRuntimeProperty("HTTPConnector/port"));
				atts
						.add(
								"HTTPSExplicitPort",
								executionGroup
										.getRuntimeProperty("HTTPSConnector/explicitlySetPortNumber"));
				atts.add("HTTPSPort", executionGroup
						.getRuntimeProperty("HTTPSConnector/port"));
				atts.add("DebugPortActive", Boolean.toString(executionGroup
						.isDebugPortActive()));
				atts.add("TimeOfLastUpdate", executionGroup
						.getTimeOfLastUpdate());
				atts.add("LastCompletionTime", executionGroup
						.getTimeOfLastCompletionCode());
				atts.add("LastCompletionCode", executionGroup
						.getLastCompletionCode().toString());
				atts.add("LastCompletionCode", executionGroup
						.getLastCompletionCode().toString());
				atts.add("TraceNodeLevel", executionGroup
						.getRuntimeProperty("This/traceNodeLevel"));
				atts.add("ServiceTraceLevel", executionGroup
						.getRuntimeProperty("This/traceLevel"));
				atts.add("UserTraceLevel", executionGroup
						.getRuntimeProperty("This/userTraceLevel"));
				atts.add("TraceLogSize", Long.parseLong(executionGroup
						.getRuntimeProperty("TraceLog/fileSize")));
				atts.add("MonitoringStatus", executionGroup
						.getRuntimeProperty("This/monitoring"));
				atts.add("MonitoringProfile", executionGroup
						.getRuntimeProperty("This/monitoringProfile"));
				atts.add("UUID", executionGroup.getUUID());
				th.startElement("", "", "ExecutionGroup", atts);
				atts.clear();
				getMessageFlowDetails(executionGroup, atts);
				th.endElement("", "", "ExecutionGroup");
			}
			th.endElement("", "", "ExecutionGroups");

		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Create an XML representation of information about each Configurable
	 * Service on the specified broker
	 * 
	 * @param broker
	 * @return XML formatted string
	 * @throws ConfigManagerProxyPropertyNotInitializedException
	 */
	private void getConfigurableServiceDetails(AttributeWrapper atts)
			throws ConfigManagerProxyPropertyNotInitializedException {

		// The broker has a number of configurable services - get the array of
		// all services available
		// since some of them are IBM template services that cannot be removed,
		// add an attribute to identify them
		ConfigurableService configurableService[] = broker
				.getConfigurableServices(null);

		try {
			th.startElement("", "", "ConfigurableServices", atts);

			for (ConfigurableService cs : configurableService) {
				atts.clear();
				atts.add("Name", cs.getName());
				atts.add("Type", cs.getType());
				atts.add("Template", isTemplateConfigurableService(
						cs.getType(), cs.getName()));
				th.startElement("", "", "ConfigurableService", atts);

				Properties csProperties = cs.getProperties();
				Enumeration<?> propertiesEnum = csProperties.keys();
				while (propertiesEnum.hasMoreElements()) {
					String propertiesKey = "" + propertiesEnum.nextElement();
					String propertiesValue = csProperties
							.getProperty(propertiesKey);
					atts.clear();
					atts.add("Key", propertiesKey);
					th.startElement("", "", "Property", atts);
					th.startCDATA();
					th.characters(propertiesValue.toCharArray(), 0,
							propertiesValue.length());
					th.endCDATA();
					th.endElement("", "", "Property");

				}

				th.endElement("", "", "ConfigurableService");

			}

			th.endElement("", "", "ConfigurableServices");

		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Create an XML representation of information about each Message Flow in a
	 * specified execution Group
	 * 
	 * @param executionGroup
	 * @return XML formatted string
	 * @throws ConfigManagerProxyPropertyNotInitializedException
	 */
	private void getMessageFlowDetails(ExecutionGroupProxy executionGroup,
			AttributeWrapper atts)
			throws ConfigManagerProxyPropertyNotInitializedException {

		// Get all the message flows in this execution group in order
		List<MessageFlowProxy> MFList = Collections.list(executionGroup
				.getMessageFlows(null));
		Collections.sort(MFList, new MFComparator());

		try {
			th.startElement("", "", "MessageFlows", atts);

			for (MessageFlowProxy messageFlow : MFList) {
				atts.clear();
				atts.add("Name", messageFlow.getName());
				atts.add("isRunning", messageFlow.isRunning());

				// if the flow has a schema then only show the last part of the
				// message flow name
				String shortMsgFlowName[] = messageFlow.getName().split("\\.");
				if (shortMsgFlowName.length > 1) {
					atts.add("ShortName",
							shortMsgFlowName[shortMsgFlowName.length - 1]);
				} else {
					atts.add("ShortName", messageFlow.getName());
				}
				atts.add("TraceNodeLevel", messageFlow
						.getRuntimeProperty("This/traceNodeLevel"));
				atts.add("ServiceTraceLevel", messageFlow
						.getRuntimeProperty("This/traceLevel"));
				atts.add("UserTraceLevel", messageFlow
						.getRuntimeProperty("This/userTraceLevel"));
				atts.add("MonitoringStatus", messageFlow
						.getRuntimeProperty("This/monitoring"));
				atts.add("MonitoringProfile", messageFlow
						.getRuntimeProperty("This/monitoringProfile"));
				atts.add("AdditionalInstances", Integer.toString(messageFlow
						.getAdditionalInstances()));
				atts.add("UUID", messageFlow.getUUID());
				atts.add("TimeOfLastCompletionCode", messageFlow
						.getTimeOfLastCompletionCode());
				atts.add("TimeOfLastUpdate", messageFlow.getTimeOfLastUpdate());
				atts.add("BARFileName", messageFlow.getBARFileName().replace(
						"&", "&amp;"));
				// Determine the direction of path separators in the bar file
				// name so we can remove the path from the name
				if (messageFlow.getBARFileName().contains("/")) {
					String shortBarfileName[] = messageFlow.getBARFileName()
							.split("/");
					atts.add("BARFileShortName",
							shortBarfileName[shortBarfileName.length - 1]);
				} else {
					String shortBarfileName[] = messageFlow.getBARFileName()
							.split("\\\\");
					atts.add("BARFileShortName",
							shortBarfileName[shortBarfileName.length - 1]);
				}
				atts.add("DeployTime", messageFlow.getDeployTime());
				atts.add("ModifyTime", messageFlow.getModifyTime());

				th.startElement("", "", "MessageFlow", atts);
				th.endElement("", "", "MessageFlow");
			}

			th.endElement("", "", "MessageFlows");

		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Create an XML representation of information about the file system
	 * relevant to this broker. This includes the presence of abend and dump
	 * files as well as other information that is not available through the
	 * standard java interface
	 * 
	 * @param brokerName
	 * @return XML formatted string
	 * @throws ConfigManagerProxyPropertyNotInitializedException
	 */
	private void getFileSystemDetails(AttributeWrapper atts)
			throws ConfigManagerProxyPropertyNotInitializedException {
		determineMQSIRegistry();
		determineWorkPath();

		File fileWP = new File(this.workPath);
		File fileBR = new File(this.mqsiRegistry);

		// Record the registry and workpath locations and the filesystem space
		// available
		// Also display some information from the registry about dbparms info

		try {
			atts.add("Registry", mqsiRegistry);
			atts.add("WorkPath", workPath);
			atts.add("RegistryFreeMB", fileBR.getUsableSpace() / MB);
			atts.add("WorkPathFreeMB", fileWP.getUsableSpace() / MB);
			atts.add("RegistryTotalMB", fileBR.getTotalSpace() / MB);
			atts.add("WorkPathTotalMB", DigitFormat.format(fileWP
					.getTotalSpace()
					/ MB));
			atts.add("WorkPathPercentUsed", PercentFormat
					.format((double) (fileWP.getTotalSpace() - fileWP
							.getUsableSpace())
							/ (fileWP.getTotalSpace())));
			th.startElement("", "", "FileSystem", atts);

			// Look for .abend files so they can be reported and actioned
			String errPath = this.workPath + File.separator + "common"
					+ File.separator + "errors";

			File errDir = new File(errPath);
			// Zip all the abend files and delete the originals
			for (File f : errDir.listFiles(new FileFilterEnd("abend"))) {// only
																			// include
																			// files
																			// ending
																			// in
																			// "abend"
				ZipFileAndDelete(f);
			}
			// Zip all the dump files and delete the originals
			for (File f : errDir.listFiles(new FileFilterEnd("dump"))) {// only
																		// include
																		// files
																		// ending
																		// in
																		// "dump"
				ZipFileAndDelete(f);
			}
			// Create an ErrorFiles section and add everything
			// This will included abend and dump files that were not zipped this
			// time
			atts.clear();
			th.startElement("", "", "ErrorFiles", atts);
			for (File f : errDir.listFiles()) {
				atts.clear();
				atts.add("Name", f.getName());
				atts.add("Type", f.getName().endsWith(".gz") ? "Compressed"
						: "unknown");
				atts.add("Date", new Date(f.lastModified()));
				atts.add("Size", f.length());
				atts.add("FileIDKey", getHash(errPath + File.separator
						+ f.getName() + this.extraPlaintext));
				th.startElement("", "", "ErrorFile", atts);
				th.endElement("", "", "ErrorFile");
			}
			th.endElement("", "", "ErrorFiles");

			// List all files in the shared-classes directory
			File classesDir = new File(this.workPath + File.separator
					+ "shared-classes");
			if (classesDir.listFiles().length > 0) {
				atts.clear();
				th.startElement("", "", "SharedClasses", atts);
				for (File f : classesDir.listFiles()) {
					atts.clear();
					atts.add("Name", f.getName());
					atts.add("Date", new Date(f.lastModified()));
					atts.add("Size", f.length());
					th.startElement("", "", "SharedClass", atts);
					th.endElement("", "", "SharedClass");
				}
				th.endElement("", "", "SharedClasses");
			}

			// List all files in the XSL and XML external directories
			File externalXSLDir = new File(this.workPath + File.separator
					+ "XSL" + File.separator + "external");
			int externalXSLDirPathLength = (this.workPath + File.separator
					+ "XSL" + File.separator + "external").length() + 1;
			
			if (externalXSLDir.exists()) {
				
				try {
					// get a list of all of the files from the directory down
					List<File> files = getFileListing(externalXSLDir);
				
					atts.clear();

					th.startElement("", "", "XSLExternal", atts);
					for (File f : files) {
					//for (File f : externalXSLDir.listFiles()) {
						atts.clear();
						if (externalXSLDirPathLength < f.getPath().length()){
							atts.add("Name", f.getPath().substring(externalXSLDirPathLength));
						} 
						else 
						{
							atts.add("Name", f.getName());
						}
						atts.add("Date", new Date(f.lastModified()));
						atts.add("Size", f.length());
						th.startElement("", "", "file", atts);
						th.endElement("", "", "file");
					}
					th.endElement("", "", "XSLExternal");

				} catch (FileNotFoundException fnf) {
					// ignore
				}
			}
			
			// create the XML directory file path
			File externalXMLDir = new File(this.workPath + File.separator
					+ "XML" + File.separator + "external");
			int externalXMLDirPathLength = (this.workPath + File.separator
					+ "XSL" + File.separator + "external").length() + 1;
			
			if (externalXMLDir.exists()) {

				try {
					// get a list of all of the files from the directory down
					List<File> files = getFileListing(externalXMLDir);

					atts.clear();
					th.startElement("", "", "XMLExternal", atts);
					for (File f : files) {
						// for (File f: externalXMLDir.listFiles()){
						atts.clear();
						if (externalXSLDirPathLength < f.getPath().length()){
							atts.add("Name", f.getPath().substring(externalXMLDirPathLength));
						} 
						else 
						{
							atts.add("Name", f.getName());
						}
						atts.add("Date", new Date(f.lastModified()));
						atts.add("Size", f.length());
						th.startElement("", "", "file", atts);
						th.endElement("", "", "file");
					}
					th.endElement("", "", "XMLExternal");
				} catch (FileNotFoundException fnf) {
					// ignore
				}
			}

			// Find any dbparms entries and return only the UserID component
			File DBParmsDir = new File(this.mqsiRegistry + File.separator
					+ "registry" + File.separator + this.brokerName
					+ File.separator + "CurrentVersion" + File.separator
					+ "DSN");
			if (DBParmsDir.exists()) {
				atts.clear();
				th.startElement("", "", "DBparms", atts);
				for (File f : DBParmsDir.listFiles(new FileFilterContains(
						BrokerInfo.registryDBParamSeperator))) {
					atts.clear();
					atts.add("Name", f.getName());
					// Obtain the UserID in the sub-directory (not password)
					File userIdFile = new File(f.getAbsolutePath()
							+ File.separator + "UserId"
							+ BrokerInfo.registryFileExtension);
					try {
						BufferedReader input = new BufferedReader(
								new FileReader(userIdFile));
						atts.add("UserID", input.readLine());
					} catch (FileNotFoundException e1) {
						// The file does not exist so report no data available
						atts.add("UserID", DATA_UNAVAILABLE);
					} catch (IOException e) {
						// The file can not be read so report no data available
						atts.add("UserID", DATA_UNAVAILABLE);
					}
					th.startElement("", "", "DBparm", atts);
					th.endElement("", "", "DBparm");
				}
				th.endElement("", "", "DBparms");

			}

			// on AIX you can tell how long the broker has been up from the lock
			// file configStore.brokername
			// TODO: work out how to determine how long a broker has been up in
			// Win
			File lockFile = new File(this.mqsiRegistry + File.separator
					+ "common" + File.separator + "locks" + File.separator
					+ "configStore." + this.brokerName);
			atts.clear();
			if (lockFile.canRead()) {
				atts.add("Date", new Date(lockFile.lastModified()));
			} else {
				atts.add("Date", "Unavailable on Windows Platform");
			}
			th.startElement("", "", "LockFile", atts);
			th.endElement("", "", "LockFile");

			th.endElement("", "", "FileSystem");

		} catch (SAXException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/**
	 * Setup the mqsiRegistry variable so if can be used internally
	 */
	private void determineMQSIRegistry() {
		if (this.mqsiRegistry == null) {
			this.mqsiRegistry = System.getenv("MQSI_REGISTRY");
		}
		return;
	}

	/**
	 * Setup the workPath variable so it can be used internally
	 */
	private void determineWorkPath() {

		// if the workPath has already been calculated then just return
		if (this.workPath != null) {
			return;
		}

		// The file we use to determine workpath is different on Unix vs Windows
		// and based on the MQSIRegistry
		determineMQSIRegistry();
		try {
			String workPathFileName = "WorkPath"
					+ BrokerInfo.registryFileExtension;
			BufferedReader input = new BufferedReader(new FileReader(
					this.mqsiRegistry + File.separator + "registry"
							+ File.separator + this.brokerName + File.separator
							+ "CurrentVersion" + File.separator
							+ workPathFileName));
			this.workPath = input.readLine();

		} catch (FileNotFoundException e) {
			// File does not exist so it is the default workpath
			this.workPath = this.mqsiRegistry;
			// e.printStackTrace();
		} catch (IOException e) {
			// File does not exist so it is the default workpath
			this.workPath = this.mqsiRegistry;
			// e.printStackTrace();
		}

		return;
	}

	/**
	 * Zip up the file and delete the original if it is older than a given delay <br>
	 * The delay is so that we do not attempt to zip and delete a file that is
	 * still being written. <br/>
	 * The delay is controlled by setZipDeleteDelay()
	 * 
	 * @param filename
	 *            to zip and delete
	 * 
	 */
	private boolean ZipFileAndDelete(File filename) {

		// if the file is not old enough then don't do anything
		if (filename.lastModified() + zipDeleteDelay > Calendar.getInstance()
				.getTimeInMillis()) {
			return false;
		}
		try {
			FileOutputStream dest = new FileOutputStream(filename
					.getCanonicalPath()
					+ ".gz");
			// CheckedOutputStream checksum = new CheckedOutputStream(dest, new
			// Adler32());
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
					dest));
			// out.setMethod(ZipOutputStream.DEFLATED);
			byte data[] = new byte[BUFFER];
			// get a list of files from current directory
			FileInputStream fi = new FileInputStream(filename);
			BufferedInputStream origin = new BufferedInputStream(fi, BUFFER);
			ZipEntry entry = new ZipEntry(filename.getName());
			out.putNextEntry(entry);
			int count;
			while ((count = origin.read(data, 0, BUFFER)) != -1) {
				out.write(data, 0, count);
			}
			origin.close();
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		filename.delete();
		return true;
	}

	/**
	 * A comparator function that allows the execution groups to be sorted in
	 * alphabetical order by the standard Collection functions
	 */
	private class EGComparator implements Comparator<ExecutionGroupProxy> {

		public int compare(ExecutionGroupProxy eg1, ExecutionGroupProxy eg2) {
			try {
				return eg1.getName().compareToIgnoreCase(eg2.getName());
			} catch (ConfigManagerProxyPropertyNotInitializedException e) {
				return 0;
			}
		}
	}

	/**
	 * A comparator function that allows the message flows to be sorted in
	 * alphabetical order by the standard Collection functions
	 */
	private class MFComparator implements Comparator<MessageFlowProxy> {

		public int compare(MessageFlowProxy mf1, MessageFlowProxy mf2) {
			try {
				return mf1.getName().compareToIgnoreCase(mf2.getName());
			} catch (ConfigManagerProxyPropertyNotInitializedException e) {
				return 0;
			}
		}
	}

	/**
	 * An implementation of the FilenameFilter interface to return files ending
	 * with a given string
	 */
	private class FileFilterEnd implements FilenameFilter {
		String filterString;

		private FileFilterEnd(String endString) {
			this.filterString = endString.toLowerCase();
		}

		public boolean accept(File file) {
			return file.getName().toLowerCase().endsWith(filterString);
		}

		public boolean accept(File dir, String name) {
			File f = new File(dir, name);
			return accept(f);
		}
	}

	/**
	 * An implementation of the FilenameFilter interface to return files ending
	 * with a given string
	 */
	private class FileFilterContains implements FilenameFilter {
		String filterString;

		private FileFilterContains(String midString) {
			this.filterString = midString.toLowerCase();
		}

		public boolean accept(File file) {
			return file.getName().toLowerCase().contains(filterString);
		}

		public boolean accept(File dir, String name) {
			File f = new File(dir, name);
			return accept(f);
		}
	}

	/**
	 * @author Brian Reddick </br> This class wraps
	 *         org.xml.sax.helpers.AttributesImpl <br/>
	 *         The purpose is to reduce coding for highly repetitive
	 *         addAttribute calls and to simplify the generation of XML and
	 *         minimise typos <br/>
	 *         The method is overloaded so that different return types can be
	 *         handled automatically.<br/>
	 *         Some include specific error handling as the type returned is
	 *         sometimes a null (possible bug in broker API)<br/>
	 *         Formatting may also be applied to longs depending on the setting
	 *         of isFormattingApplied
	 */
	private class AttributeWrapper extends AttributesImpl {

		/**
		 * Wrap the standard SAX AttributesImpl class to simplify the coding by
		 * adding default values for many of the .addAttibute() method
		 * parameters and formatting inputs from native format to strings
		 */
		public AttributeWrapper() {
			super();
		}

		/**
		 * Wrapper for the standard addAttribute that optionally adds formatting
		 * and defaults to <br/>
		 * super.addAttribute("","",qName,"CDATA",DigitFormat.format(value)); or <br/>
		 * super.addAttribute("","",qName,"CDATA",Long.toString(value)); <br/>
		 * Depending on the setting of isFormattingApplied
		 * 
		 * @param qName
		 * @param value
		 */
		public void add(String qName, long value) {
			if (isFormattingApplied) {
				super.addAttribute("", "", qName, "CDATA", DigitFormat
						.format(value));
			} else {
				super
						.addAttribute("", "", qName, "CDATA", Long
								.toString(value));
			}
		}

		/**
		 * Wrapper for the standard addAttribute that defaults to <br/>
		 * super.addAttribute("","",qName,"CDATA",SDF.format(value)); <br/>
		 * If the date is not valid then the default is <br/>
		 * super.addAttribute("","",qName,"CDATA",DATE_UNAVAILABLE);
		 * 
		 * @param qName
		 * @param value
		 */
		public void add(String qName, Date value) {
			try {
				super.addAttribute("", "", qName, "CDATA", SDF.format(value));
			} catch (Exception e) {
				super.addAttribute("", "", qName, "CDATA", DATA_UNAVAILABLE);
			}
		}

		/**
		 * Wrapper for the standard addAttribute that defaults to <br/>
		 * super.addAttribute("","",qName,"CDATA",SDF.format(value.getTime())); <br/>
		 * If the date is not valid then the default is <br/>
		 * super.addAttribute("","",qName,"CDATA",DATE_UNAVAILABLE);
		 * 
		 * @param qName
		 * @param value
		 */
		public void add(String qName, GregorianCalendar value) {
			System.out.print(qName + " :");
			System.out.println(value);
			try {
				super.addAttribute("", "", qName, "CDATA", SDF.format(value
						.getTime()));
			} catch (Exception e) {
				super.addAttribute("", "", qName, "CDATA", DATA_UNAVAILABLE);
			}
		}

		/**
		 * Wrapper for the standard addAttribute that defaults to <br/>
		 * super.addAttribute("","",qName,"CDATA",String.valueOf(value));
		 * 
		 * @param qName
		 * @param value
		 */
		public void add(String qName, boolean value) {
			System.out.print(qName + " :");
			System.out.println(value);
			super.addAttribute("", "", qName, "CDATA", String.valueOf(value));
		}

		/**
		 * Wrapper for the standard addAttribute that defaults to <br/>
		 * super.addAttribute("","",qName,"CDATA",value);
		 * 
		 * @param qName
		 * @param value
		 */
		public void add(String qName, String value) {
			System.out.print(qName + " :");
			System.out.println(value);
			if (value == null) {
				super.addAttribute("", "", qName, "CDATA", "null");
			} else {
				super.addAttribute("", "", qName, "CDATA", value);
			}
		}
	}

	/**
	 * @return if formatting is applied to decimals to make it more human
	 *         readable
	 */
	public boolean isFormattingApplied() {
		return isFormattingApplied;
	}

	/**
	 * Formatting of numerics can be turned off (it is on by default) If this
	 * property is set to false then the local formatting will not be applied to
	 * numerics Dates will continue to be formatted as set as per SDF
	 * 
	 * @param isFormattingApplied
	 *            the isFormattingApplied to set
	 */
	public void setFormattingApplied(boolean isFormattingApplied) {
		this.isFormattingApplied = isFormattingApplied;
	}

	/**
	 * @return the workPath
	 */
	public String getWorkPath() {
		// return a copy of the workpath directory
		determineWorkPath();
		return new String(this.workPath);
	}

	/**
	 * Return the delay in milliseconds before a file will be zipped and the
	 * original deleted
	 * 
	 * @return the zipDeleteDelay
	 */
	public long getZipDeleteDelay() {
		return zipDeleteDelay;
	}

	/**
	 * The delay in milliseconds before a file can be zipped and the original
	 * deleted
	 * 
	 * @param zipDeleteDelay
	 *            the zipDeleteDelay to set
	 */
	public void setZipDeleteDelay(long zipDeleteDelay) {
		if (zipDeleteDelay >= 0) {
			this.zipDeleteDelay = zipDeleteDelay;
		}
		// TODO throw an error here if the value can't be set
	}

	/**
	 * Generate a hash of the supplied text string
	 * 
	 * @param plaintext
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static String getHash(String plaintext) {
		MessageDigest m;
		try {
			m = MessageDigest.getInstance("MD5");
			m.reset();
			m.update(plaintext.getBytes()); // NB encoding is the platform
											// default
			byte[] digest = m.digest();
			BigInteger bigInt = new BigInteger(1, digest);
			return bigInt.toString(16);

		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * Additional plaintext can be added to the hash function to give more
	 * uniqueness
	 * 
	 * @param extraPlaintext
	 *            the extraPlaintext to set
	 */
	public void setExtraPlaintext(String extraPlaintext) {
		this.extraPlaintext = extraPlaintext;
	}

	/**
	 * There are a set of template broker configurable services that can be
	 * updated but not deleted. This method identifies if a configurable service
	 * is a template service
	 */
	public static boolean isTemplateConfigurableService(String Type, String Name) {

		String paddedName = " " + Name + " ";

		if (Type.equalsIgnoreCase("Aggregation")) {
			return " Template ".contains(paddedName);
		}
		if (Type.equalsIgnoreCase("Collector")) {
			return " Template ".contains(paddedName);
		}
		if (Type.equalsIgnoreCase("EISProviders")) {
			return " PeopleSoft SAP Siebel Twineball ".contains(paddedName);
		}
		if (Type.equalsIgnoreCase("JDBCProviders")) {
			return " DB2 Informix Informix_With_Date_Format Microsoft_SQL_Server Oracle Sybase_JConnect6_05 "
					.contains(paddedName);
		}
		if (Type.equalsIgnoreCase("JMSProviders")) {
			return " ActiveMQ BEA_Weblogic FioranoMQ Generic_File Generic_LDAP JBoss JOnAS Joram OpenJMS Oracle_OEMS SeeBeyond SonicMQ SwiftMQ Tibco_EMS WebSphere_MQ WebSphere_WAS_Client "
					.contains(paddedName);
		}
		if (Type.equalsIgnoreCase("JavaClassLoader")) {
			return " Template ".contains(paddedName);
		}
		if (Type.equalsIgnoreCase("MonitoringProfiles")) {
			return " DefaultMonitoringProfile ".contains(paddedName);
		}
		if (Type.equalsIgnoreCase("PolicySetBindings")) {
			return " WSS10Default ".contains(paddedName);
		}
		if (Type.equalsIgnoreCase("PolicySets")) {
			return " WSS10Default ".contains(paddedName);
		}
		if (Type.equalsIgnoreCase("Resequence")) {
			return " Template ".contains(paddedName);
		}
		if (Type.equalsIgnoreCase("SecurityProfiles")) {
			return " Default_Propagation ".contains(paddedName);
		}
		if (Type.equalsIgnoreCase("ServiceRegistries")) {
			return " DefaultWSRR ".contains(paddedName);
		}
		if (Type.equalsIgnoreCase("TCPIPClient")) {
			return " Default ".contains(paddedName);
		}
		if (Type.equalsIgnoreCase("TCPIPServer")) {
			return " Default ".contains(paddedName);
		}
		if (Type.equalsIgnoreCase("Timer")) {
			return " Template ".contains(paddedName);
		}

		return false;

	}

	/**
	 * Recursively walk a directory tree and return a List of all Files found;
	 * the List is sorted using File.compareTo().
	 * 
	 * @param aStartingDir
	 *            is a valid directory, which can be read.
	 */
	static public List<File> getFileListing(File aStartingDir)
			throws FileNotFoundException {
		validateDirectory(aStartingDir);
		List<File> result = getFileListingNoSort(aStartingDir);
		Collections.sort(result);
		return result;
	}

	// PRIVATE //
	static private List<File> getFileListingNoSort(File aStartingDir)
			throws FileNotFoundException {
		List<File> result = new ArrayList<File>();
		File[] filesAndDirs = aStartingDir.listFiles();
		List<File> filesDirs = Arrays.asList(filesAndDirs);
		for (File file : filesDirs) {
			result.add(file); // always add, even if directory
			if (!file.isFile()) {
				// must be a directory
				// recursive call!
				List<File> deeperList = getFileListingNoSort(file);
				result.addAll(deeperList);
			}
		}
		return result;
	}

	/**
	 * Directory is valid if it exists, does not represent a file, and can be
	 * read.
	 */
	static private void validateDirectory(File aDirectory)
			throws FileNotFoundException {
		if (aDirectory == null) {
			throw new IllegalArgumentException("Directory should not be null.");
		}
		if (!aDirectory.exists()) {
			throw new FileNotFoundException("Directory does not exist: "
					+ aDirectory);
		}
		if (!aDirectory.isDirectory()) {
			throw new IllegalArgumentException("Is not a directory: "
					+ aDirectory);
		}
		if (!aDirectory.canRead()) {
			throw new IllegalArgumentException("Directory cannot be read: "
					+ aDirectory);
		}
	}
}
