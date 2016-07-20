/*     */ package com.syn.iib.monitor.v1;
/*     */ 
/*     */ import com.ibm.broker.config.proxy.BrokerProxy;
/*     */ import com.ibm.broker.config.proxy.ConfigManagerProxyException;
/*     */ import com.ibm.mq.headers.MQDataException;
/*     */ import com.ibm.mq.headers.pcf.PCFMessageAgent;
/*     */ import com.sun.net.httpserver.Headers;
/*     */ import com.sun.net.httpserver.HttpContext;
/*     */ import com.sun.net.httpserver.HttpExchange;
/*     */ import com.sun.net.httpserver.HttpHandler;
/*     */ import com.sun.net.httpserver.HttpServer;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.OutputStream;
/*     */ import java.net.InetSocketAddress;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.Map.Entry;
/*     */ import java.util.PropertyResourceBundle;
/*     */ import java.util.Set;
/*     */ import java.util.concurrent.Executor;
/*     */ import java.util.concurrent.Executors;
/*     */ import org.apache.log4j.Logger;
/*     */ 
/*     */ public class Monitor
/*     */ {
/*  28 */   static HashMap<String, BrokerProxy> brokerMap = new HashMap();
/*  29 */   static HashMap<String, PCFMessageAgent> queueManagerMap = new HashMap();
/*  30 */   static Logger log = CommonFunctions.setLogging(Monitor.class);
/*     */   private static HttpServer server;
/*     */   
/*     */   public static void main(String[] args) throws Exception {
/*  34 */     log.info("****************************************************************************");
/*  35 */     log.info(new Date() + " " + Monitor.class + 
/*  36 */       " WebServer Monitor is getting started");
/*     */     try {
/*  38 */       LoadIIBMonitor load = new LoadIIBMonitor();
/*  39 */       load.loadIIBComponents(brokerMap, queueManagerMap);
/*     */       
/*  41 */       FileInputStream wsProp = null;
/*  42 */       PropertyResourceBundle wsPropBundle = null;
/*     */       try {
/*  44 */         log.debug(new Date() + " " + Monitor.class + 
/*  45 */           " Accessing webserver/conf.properties");
/*  46 */         wsProp = new FileInputStream("webserver/conf.properties");
/*  47 */         wsPropBundle = new PropertyResourceBundle(wsProp);
/*  48 */         wsProp.close();
/*  49 */         log.debug(new Date() + " " + Monitor.class + 
/*  50 */           " Closing webserver/conf.properties");
/*     */       }
/*     */       catch (IOException e2)
/*     */       {
/*  54 */         log.error(new Date() + " " + Monitor.class + 
/*  55 */           " WebServer properties cannot be accessed. Error: " + 
/*  56 */           e2);
/*     */       }
/*  58 */       log.debug(new Date() + " " + Monitor.class + 
/*  59 */         " Attaching HTTP server");
/*  60 */       server = HttpServer.create();
/*  61 */       Executor exec = Executors.newCachedThreadPool();
/*  62 */       server.setExecutor(exec);
/*  63 */       server.createContext("/Monitor").setHandler(new MonitorHandler());
/*  64 */       log.info(new Date() + " " + Monitor.class + 
/*  65 */         " WebServer starting up on port " + 
/*  66 */         wsPropBundle.getString("port"));
/*  67 */       log.debug(new Date() + " " + Monitor.class + 
/*  68 */         " Binding HTTP server");
/*  69 */       server.bind(
/*  70 */         new InetSocketAddress(Integer.valueOf(wsPropBundle
/*  71 */         .getString("port")).intValue()), 0);
/*  72 */       server.start();
/*  73 */       log.info(new Date() + " " + Monitor.class + 
/*  74 */         " WebServer Monitor has been started successfully");
/*  75 */       log.info("****************************************************************************");
/*     */     } catch (Exception e) {
/*  77 */       log.error(new Date() + " " + Monitor.class + " " + e.toString() + 
/*  78 */         " WebServer Monitor cannot be started");
/*  79 */       log.info("****************************************************************************");
/*     */     }
/*     */   }
/*     */   
/*     */   static class MonitorHandler implements HttpHandler
/*     */   {
/*     */     public void handle(HttpExchange t) throws IOException
/*     */     {
/*     */       try {
/*  88 */         Monitor.log.info("#################################### START ###################################");
/*     */         
/*  90 */         String test = t.getRequestURI().toString();
/*  91 */         Headers responseHeaders = t.getResponseHeaders();
/*  92 */         Monitor.log.debug(new Date() + " " + Monitor.class + 
/*  93 */           " Constructing Response Headers");
/*  94 */         OutputStream os = t.getResponseBody();
/*  95 */         Monitor.log.debug(new Date() + " " + Monitor.class + 
/*  96 */           " Constructing OutputStream");
/*  97 */         String node = null;
/*  98 */         String data = null;
/*     */         
/* 100 */         Monitor.log.info(new Date() + " " + MonitorHandler.class + 
/* 101 */           " Request received with URI " + test);
/*     */         
/* 103 */         if (test.contains("CHECKMK")) {
/* 104 */           Monitor.log.info(new Date() + " " + toString() + 
/* 105 */             " CHECK_MK request received " + test.toString());
/* 106 */           Monitor.log.debug(new Date() + " " + Monitor.class + 
/* 107 */             " Calling IIBandMQStats class");
/* 108 */           IIBandMQStats iibcon = new IIBandMQStats();
/* 109 */           node = test.split("/")[3];
/* 110 */           responseHeaders.set("Content-Type", "text/plain");
/*     */           try {
/* 112 */             data = iibcon.getIIBStats(node, Monitor.brokerMap, 
/* 113 */               Monitor.queueManagerMap, "CHECK_MK_FEED");
/* 114 */             Monitor.log.debug(new Date() + " " + Monitor.class + 
/* 115 */               " IIBandMQStats has responded");
/*     */           }
/*     */           catch (ConfigManagerProxyException e) {
/* 118 */             e.printStackTrace();
/*     */           }
/* 120 */           Monitor.log.info(new Date() + " " + toString() + 
/* 121 */             " CHECK_MK request was processed successfully ");
/*     */         }
/* 123 */         else if (test.contains("Monitor/Disconnect")) {
/* 124 */           Monitor.log.error(new Date() + " " + toString() + 
/* 125 */             " Monitor DashBoard request receieved " + 
/* 126 */             test.toString());
/*     */           
/* 128 */           responseHeaders.set("Content-Type", "text/xml");
/* 129 */           String brkName = "";String qmName = "";
/* 130 */           Iterator entries = Monitor.brokerMap.entrySet().iterator();
/* 131 */           while (entries.hasNext()) {
/* 132 */             Map.Entry thisEntry = (Map.Entry)entries.next();
/* 133 */             String key = (String)thisEntry.getKey();
/* 134 */             BrokerProxy value = (BrokerProxy)thisEntry.getValue();
/* 135 */             value.disconnect();
/* 136 */             brkName = brkName + "<IIB name=\"" + key + 
/* 137 */               "\" status=\"disconnected\" />";
/*     */           }
/*     */           
/*     */ 
/* 141 */           Iterator entries1 = Monitor.queueManagerMap.entrySet().iterator();
/* 142 */           while (entries1.hasNext()) {
/* 143 */             Map.Entry thisEntry = (Map.Entry)entries1.next();
/*     */             
/* 145 */             PCFMessageAgent agent = 
/* 146 */               (PCFMessageAgent)thisEntry.getValue();
/* 147 */             String key = agent.getQManagerName();
/*     */             try {
/* 149 */               WMQConnection.destroyAgent(agent);
/*     */             }
/*     */             catch (MQDataException e) {
/* 152 */               e.printStackTrace();
/*     */             }
/* 154 */             qmName = 
/* 155 */               qmName + "<IMQ name=\"" + key + "\" status=\"disconnected\" />";
/*     */           }
/*     */           
/* 158 */           data = "<Monitor>" + brkName + qmName + "</Monitor>";
/*     */         }
/*     */         else {
/* 161 */           Monitor.log.error(new Date() + " " + toString() + 
/* 162 */             " Unknown request " + test.toString());
/* 163 */           data = "<Monitor><fault>Unknown Request</fault></Monitor>";
/* 164 */           responseHeaders.set("Content-Type", "text/xml");
/*     */         }
/*     */         
/*     */ 
/* 168 */         t.sendResponseHeaders(200, data.length());
/* 169 */         os.write(data.getBytes());
/* 170 */         Monitor.log.debug(new Date() + " " + Monitor.class + 
/* 171 */           " data is written back to OutputStream");
/* 172 */         data = "";
/* 173 */         Monitor.log.info(new Date() + " " + toString() + 
/* 174 */           " Request was processed successfully ");
/* 175 */         Monitor.log.info("#################################### END ###################################");
/* 176 */         os.flush();
/* 177 */         os.close();
/*     */       } catch (Exception e) {
/* 179 */         Monitor.log.error(new Date() + " " + MonitorHandler.class + " " + e.toString() + 
/* 180 */           " Webserver Monitor Handler issue");
/* 181 */         Monitor.log.info("#################################### END ###################################");
/*     */       }
/*     */     }
/*     */   }
/*     */ }


/* Location:              /Users/sud/Downloads/Monitor.jar!/com/syn/iib/monitor/v1/Monitor.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */