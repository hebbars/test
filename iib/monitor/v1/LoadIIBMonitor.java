/*     */ package com.syn.iib.monitor.v1;
/*     */ 
/*     */ import com.ibm.broker.config.proxy.BrokerConnectionParameters;
/*     */ import com.ibm.broker.config.proxy.BrokerProxy;
/*     */ import com.ibm.broker.config.proxy.ConfigManagerProxyException;
/*     */ import com.ibm.broker.config.proxy.MQBrokerConnectionParameters;
/*     */ import com.ibm.mq.headers.pcf.PCFMessageAgent;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.IOException;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.PropertyResourceBundle;
/*     */ import org.apache.log4j.Logger;
/*     */ 
/*     */ 
/*     */ 
/*     */ public class LoadIIBMonitor
/*     */ {
/*     */   private static final String QMGR = "QMGR";
/*     */   private static final String HOST = "HOST";
/*     */   private static final String PORT = "PORT";
/*     */   private static final String CHANNEL = "CHANNEL";
/*     */   private static final String IIB_QM = "IIB_QM";
/*     */   
/*     */   public void loadIIBComponent(String node, HashMap<String, BrokerProxy> brokerMap, HashMap<String, PCFMessageAgent> queueManagerMap)
/*     */   {
/*  28 */     Logger log = CommonFunctions.setLogging(getClass());
/*     */     
/*  30 */     log.info(new Date() + " " + toString() + " Load IIB and WMQ component");
/*     */     
/*  32 */     FileInputStream fis1 = null;
/*  33 */     PropertyResourceBundle properties1 = null;
/*     */     try {
/*  35 */       log.debug(new Date() + " " + toString() + 
/*  36 */         " Accessing " + "connection/" + node + ".properties");
/*  37 */       fis1 = new FileInputStream("connection/" + node + ".properties");
/*  38 */       properties1 = new PropertyResourceBundle(fis1);
/*  39 */       fis1.close();
/*  40 */       log.debug(new Date() + " " + toString() + 
/*  41 */         " Closing " + "connection/" + node + ".properties");
/*     */     }
/*     */     catch (IOException e) {
/*  44 */       e.printStackTrace();
/*     */     }
/*     */     
/*  47 */     boolean isBrokerQMGR = Boolean.valueOf(properties1.getString("IIB_QM")).booleanValue();
/*  48 */     if (isBrokerQMGR) {
/*  49 */       log.info(new Date() + " " + toString() + " " + node + " is loaded to map");
/*  50 */       BrokerProxy b = null;
/*  51 */       BrokerConnectionParameters bcp = new MQBrokerConnectionParameters(
/*  52 */         properties1.getString("HOST"), Integer.parseInt(properties1
/*  53 */         .getString("PORT")), properties1.getString("QMGR"));
/*  54 */       ((MQBrokerConnectionParameters)bcp).setAdvancedConnectionParameters(properties1.getString("CHANNEL"), "", "", -1, -1, null);
/*     */       try {
/*  56 */         b = BrokerProxy.getInstance(bcp);
/*  57 */         log.info(new Date() + " " + toString() + 
/*  58 */           " Connected to Broker proxy " + b.toVerboseString());
/*     */       }
/*     */       catch (ConfigManagerProxyException e2) {
/*  61 */         log.error(new Date() + " " + toString() + 
/*  62 */           " Broker Proxy issue " + e2.toString());
/*     */       }
/*     */       
/*  65 */       brokerMap.remove(node);
/*  66 */       brokerMap.put(node, b);
/*  67 */       log.info(new Date() + " " + toString() + " " + node + " has been loaded successfully to map");
/*     */     }
/*  69 */     PCFMessageAgent agent = null;
/*     */     try
/*     */     {
/*  72 */       log.info(new Date() + " " + toString() + " " + properties1.getString("QMGR") + " is being loaded to Map");
/*  73 */       agent = WMQConnection.createAgent(properties1.getString("HOST"), Integer.valueOf(properties1.getString("PORT")).intValue(), properties1.getString("CHANNEL"));
/*  74 */       log.info(new Date() + " " + toString() + " " + properties1.getString("QMGR") + " has been loaded to Map successfully");
/*     */     } catch (Exception e) {
/*  76 */       String exceptionMessage = WMQConnection.handleWMQException(e, agent, properties1.getString("CHANNEL"));
/*  77 */       log.error(new Date() + " " + toString() + 
/*  78 */         " Queue Manager Issue " + exceptionMessage);
/*     */     }
/*  80 */     queueManagerMap.remove(node);
/*  81 */     queueManagerMap.put(node, agent);
/*     */   }
/*     */   
/*     */ 
/*     */   public void loadIIBComponents(HashMap<String, BrokerProxy> brokerMap, HashMap<String, PCFMessageAgent> queueManagerMap)
/*     */   {
/*  87 */     Logger log = CommonFunctions.setLogging(getClass());
/*  88 */     File f = new File("connection");
/*  89 */     File[] files = f.listFiles();
/*  90 */     if (files != null) {
/*  91 */       for (int i = 0; i < files.length; i++) {
/*  92 */         File file = files[i];
/*  93 */         String[] temp = file.getName().split("/");
/*  94 */         log.debug(new Date() + " " + toString() + 
/*  95 */           "Accessing property file " + temp[0]);
/*  96 */         if (temp[0].contains(".properties")) {
/*  97 */           int temp1 = temp[0].indexOf(".properties");
/*     */           
/*  99 */           String FileName = temp[0].substring(0, temp1);
/* 100 */           log.debug(new Date() + " " + toString() + 
/* 101 */             " Storing " + "broker and queue manager details in map for /" + FileName);
/* 102 */           loadIIBComponent(FileName, brokerMap, queueManagerMap);
/* 103 */           log.debug(new Date() + " " + toString() + 
/* 104 */             FileName + "has been inserted to map successfully");
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ }


/* Location:              /Users/sud/Downloads/Monitor.jar!/com/syn/iib/monitor/v1/LoadIIBMonitor.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */