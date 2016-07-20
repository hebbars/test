/*     */ package com.syn.iib.monitor.v1;
/*     */ 
/*     */ import com.ibm.broker.config.proxy.ApplicationProxy;
/*     */ import com.ibm.broker.config.proxy.BrokerProxy;
/*     */ import com.ibm.broker.config.proxy.CompletionCodeType;
/*     */ import com.ibm.broker.config.proxy.ConfigManagerProxyException;
/*     */ import com.ibm.broker.config.proxy.ExecutionGroupProxy;
/*     */ import com.ibm.broker.config.proxy.LibraryProxy;
/*     */ import com.ibm.broker.config.proxy.MessageFlowProxy;
/*     */ import com.ibm.mq.headers.pcf.PCFMessageAgent;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.IOException;
/*     */ import java.util.Date;
/*     */ import java.util.Enumeration;
/*     */ import java.util.HashMap;
/*     */ import java.util.Properties;
/*     */ import java.util.PropertyResourceBundle;
/*     */ import java.util.StringTokenizer;
/*     */ import org.apache.log4j.Logger;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class IIBandMQStats
/*     */ {
/*     */   private static final String IIB_QM = "IIB_QM";
/*     */   
/*     */   public String getIIBStats(String node, HashMap<String, BrokerProxy> brokerMap, HashMap<String, PCFMessageAgent> queueManagerMap, String feedType)
/*     */     throws ConfigManagerProxyException
/*     */   {
/*  32 */     String checkMK = "";
/*  33 */     Logger log = CommonFunctions.setLogging(getClass());
/*     */     
/*  35 */     log.info(new Date() + " " + toString() + " CHECK MK feed started");
/*     */     
/*  37 */     FileInputStream fis1 = null;
/*  38 */     PropertyResourceBundle properties1 = null;
/*     */     try {
/*  40 */       log.debug(new Date() + " " + toString() + 
/*  41 */         "Accessing connection/" + node + ".properties");
/*  42 */       fis1 = new FileInputStream("connection/" + node + ".properties");
/*  43 */       properties1 = new PropertyResourceBundle(fis1);
/*  44 */       fis1.close();
/*  45 */       log.debug(new Date() + " " + toString() + 
/*  46 */         "Closing connection/" + node + ".properties");
/*     */     }
/*     */     catch (IOException e) {
/*  49 */       e.printStackTrace();
/*     */     }
/*     */     
/*  52 */     boolean isBrokerQMGR = Boolean.valueOf(properties1.getString("IIB_QM")).booleanValue();
/*  53 */     log.debug(new Date() + " " + toString() + 
/*  54 */       "Broker QueueManager is " + isBrokerQMGR);
/*     */     
/*  56 */     if (isBrokerQMGR) {
/*  57 */       log.info(new Date() + " " + toString() + 
/*  58 */         " Start of IIB details");
/*  59 */       BrokerProxy b = (BrokerProxy)brokerMap.get(node);
/*  60 */       log.debug(new Date() + " " + toString() + 
/*  61 */         "Broker Details Retrieved ");
/*     */       
/*  63 */       if (b == null) {
/*  64 */         log.debug(new Date() + " " + toString() + 
/*  65 */           "Broker Details is null ");
/*  66 */         LoadIIBMonitor load = new LoadIIBMonitor();
/*  67 */         load.loadIIBComponent(node, brokerMap, queueManagerMap);
/*     */       }
/*  69 */       else if (b.toString().contains("Broker")) {
/*  70 */         log.debug(new Date() + " " + toString() + 
/*  71 */           "Broker Details could not be receieved ");
/*  72 */         LoadIIBMonitor load = new LoadIIBMonitor();
/*  73 */         load.loadIIBComponent(node, brokerMap, queueManagerMap);
/*     */       }
/*  75 */       else if (!b.getLastCompletionCode().toString().equals("success")) {
/*  76 */         log.debug(new Date() + " " + toString() + 
/*  77 */           "Broker LastCompletion code is " + b.getLastCompletionCode().toString());
/*  78 */         LoadIIBMonitor load = new LoadIIBMonitor();
/*  79 */         load.loadIIBComponent(node, brokerMap, queueManagerMap);
/*     */ 
/*     */       }
/*  82 */       else if (!b.isRunning()) {
/*  83 */         log.debug(new Date() + " " + toString() + 
/*  84 */           "Broker is not running ");
/*  85 */         LoadIIBMonitor load = new LoadIIBMonitor();
/*  86 */         load.loadIIBComponent(node, brokerMap, queueManagerMap);
/*     */       }
/*     */       
/*     */ 
/*     */ 
/*     */ 
/*  92 */       boolean isBrokerPopulated = true;boolean isIIBRunning = true;
/*  93 */       int counter = 0;
/*  94 */       while (isBrokerPopulated) {
/*  95 */         b = (BrokerProxy)brokerMap.get(node);
/*  96 */         if (b == null) {
/*  97 */           counter++;
/*     */         }
/*  99 */         else if (b.hasBeenPopulatedByBroker()) {
/* 100 */           isBrokerPopulated = false;
/*     */         }
/*     */         
/*     */ 
/* 104 */         if (counter > 50) {
/* 105 */           isBrokerPopulated = false;
/* 106 */           isIIBRunning = false;
/*     */         }
/*     */       }
/*     */       
/* 110 */       String brokerName = null;
/* 111 */       if (isIIBRunning) {
/* 112 */         if (b.getLastCompletionCode().toString().equals("success")) {
/* 113 */           if (b.isRunning()) {
/* 114 */             brokerName = b.getName();
/*     */             
/* 116 */             checkMK = "0 " + brokerName + " Status=UP " + brokerName + 
/* 117 */               " is Running \n";
/* 118 */             log.debug(new Date() + " " + toString() + 
/* 119 */               "Broker is running ");
/* 120 */             Enumeration allEGsInThisBroker = b
/* 121 */               .getExecutionGroups(null);
/* 122 */             Enumeration allLibraryInThisApp; for (; allEGsInThisBroker.hasMoreElements(); 
/*     */                 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/* 299 */                 allLibraryInThisApp.hasMoreElements())
/*     */             {
/* 124 */               ExecutionGroupProxy thisEG = 
/* 125 */                 (ExecutionGroupProxy)allEGsInThisBroker.nextElement();
/* 126 */               log.debug(new Date() + " " + toString() + 
/* 127 */                 "EG details received " + thisEG.getName());
/* 128 */               String status = String.valueOf(thisEG.isRunning());
/* 129 */               Properties advancedProp = thisEG
/* 130 */                 .getAdvancedProperties();
/* 131 */               String EG_SVC_TRACE = advancedProp
/* 132 */                 .getProperty("traceLevel");
/* 133 */               String EG_USR_TRACE = advancedProp
/* 134 */                 .getProperty("userTraceLevel");
/*     */               
/* 136 */               checkMK = checkMK + 
/* 137 */                 CommonFunctions.componentStatus(status, 
/* 138 */                 EG_USR_TRACE, EG_SVC_TRACE, 
/* 139 */                 "off") + " " + brokerName + "-" + 
/* 140 */                 thisEG.getName() + " Status=" + 
/* 141 */                 CommonFunctions.componentStringStatus(status) + 
/* 142 */                 "|EG_SVC_TRACE=" + EG_SVC_TRACE + 
/* 143 */                 "|EG_USR_TRACE=" + EG_USR_TRACE + 
/* 144 */                 " " + 
/* 145 */                 thisEG.getName() + " is " + 
/* 146 */                 CommonFunctions.componentRuningStatus(status) + 
/* 147 */                 "\n";
/*     */               
/*     */ 
/* 150 */               Enumeration allFlowsInThisEG = thisEG
/* 151 */                 .getMessageFlows(null);
/* 152 */               while (allFlowsInThisEG.hasMoreElements()) {
/* 153 */                 MessageFlowProxy thisFlow = 
/* 154 */                   (MessageFlowProxy)allFlowsInThisEG.nextElement();
/* 155 */                 log.debug(new Date() + " " + toString() + 
/* 156 */                   "MF details received " + thisFlow.getName());
/* 157 */                 String FlowStatus = String.valueOf(thisEG
/* 158 */                   .isRunning());
/* 159 */                 advancedProp.clear();
/* 160 */                 advancedProp = thisFlow.getAdvancedProperties();
/* 161 */                 String MF_USR_TRACE = advancedProp
/* 162 */                   .getProperty("userTraceLevel");
/*     */                 
/* 164 */                 checkMK = checkMK + 
/* 165 */                   CommonFunctions.componentStatus(
/* 166 */                   FlowStatus, MF_USR_TRACE, "none", 
/* 167 */                   "none") + 
/* 168 */                   " " + 
/* 169 */                   brokerName + 
/* 170 */                   "-" + 
/* 171 */                   thisEG + 
/* 172 */                   "-" + 
/* 173 */                   thisFlow.getName() + 
/* 174 */                   " Status=" + 
/*     */                   
/* 176 */                   CommonFunctions.componentStringStatus(FlowStatus) + 
/* 177 */                   "|MF_USR_TRACE=" + 
/* 178 */                   MF_USR_TRACE + 
/* 179 */                   " " + 
/* 180 */                   thisFlow.getName() + 
/* 181 */                   " is " + 
/*     */                   
/* 183 */                   CommonFunctions.componentRuningStatus(FlowStatus) + 
/* 184 */                   "\n";
/*     */               }
/*     */               
/*     */ 
/* 188 */               Enumeration allAppInThisEG = thisEG
/* 189 */                 .getApplications(null);
/* 190 */               while (allAppInThisEG.hasMoreElements()) {
/* 191 */                 ApplicationProxy thisApp = 
/* 192 */                   (ApplicationProxy)allAppInThisEG.nextElement();
/* 193 */                 log.debug(new Date() + " " + toString() + 
/* 194 */                   "APP details received " + thisApp.getName());
/* 195 */                 String AppStatus = String.valueOf(thisEG
/* 196 */                   .isRunning());
/* 197 */                 advancedProp.clear();
/* 198 */                 advancedProp = thisApp.getAdvancedProperties();
/* 199 */                 String APP_SVC_TRACE = advancedProp
/* 200 */                   .getProperty("traceLevel");
/* 201 */                 String APP_USR_TRACE = advancedProp
/* 202 */                   .getProperty("userTraceLevel");
/*     */                 
/*     */ 
/*     */ 
/*     */ 
/* 207 */                 String AppFlow = "";
/* 208 */                 Enumeration allFlowsInThisApp = thisApp
/* 209 */                   .getMessageFlows(null);
/* 210 */                 while (allFlowsInThisApp.hasMoreElements()) {
/* 211 */                   MessageFlowProxy thisAppFlow = (MessageFlowProxy)allFlowsInThisApp.nextElement();
/* 212 */                   log.debug(new Date() + " " + toString() + 
/* 213 */                     "APP Flow details received " + thisAppFlow.getName());
/* 214 */                   String AppFlowStatus = String.valueOf(thisAppFlow
/* 215 */                     .isRunning());
/* 216 */                   AppFlow = "|" + thisAppFlow.getName() + "=" + 
/* 217 */                     CommonFunctions.componentStringStatus(AppFlowStatus);
/*     */                 }
/*     */                 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/* 264 */                 String checkMkStatusCode = CommonFunctions.componentStatus(
/* 265 */                   AppStatus, APP_USR_TRACE, 
/* 266 */                   APP_SVC_TRACE, "off");
/* 267 */                 String checkMkStatusString = 
/* 268 */                   CommonFunctions.componentStringStatus(AppStatus);
/* 269 */                 if (AppFlow.contains("DOWN")) {
/* 270 */                   checkMkStatusCode = "2";
/* 271 */                   checkMkStatusString = "DOWN";
/*     */                 }
/* 273 */                 checkMK = 
/*     */                 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/* 293 */                   checkMK + checkMkStatusCode + " " + brokerName + "-" + thisEG + "-" + thisApp.getName() + " Status=" + checkMkStatusString + "|APP_SVC_TRACE=" + APP_SVC_TRACE + "|APP_USR_TRACE=" + APP_USR_TRACE + AppFlow + " " + thisApp.getName() + " is " + CommonFunctions.componentRuningStatus(AppStatus) + "\n";
/*     */               }
/*     */               
/*     */ 
/* 297 */               allLibraryInThisApp = 
/* 298 */                 thisEG.getLibraries(null);continue;
/*     */               
/* 300 */               LibraryProxy thisLib = (LibraryProxy)allLibraryInThisApp.nextElement();
/* 301 */               log.debug(new Date() + " " + toString() + 
/* 302 */                 "APP Library details received " + thisLib.getName());
/*     */               
/*     */ 
/*     */ 
/* 306 */               Enumeration allFlowsInThisAppLib = thisLib
/* 307 */                 .getMessageFlows(null);
/* 308 */               while (allFlowsInThisAppLib.hasMoreElements()) {
/* 309 */                 MessageFlowProxy thisAppLibFlow = (MessageFlowProxy)allFlowsInThisAppLib.nextElement();
/* 310 */                 log.debug(new Date() + " " + toString() + 
/* 311 */                   "APP Library Flow details received " + thisAppLibFlow.getName());
/* 312 */                 String AppLibFlowStatus = String.valueOf(thisAppLibFlow
/* 313 */                   .isRunning());
/* 314 */                 checkMK = checkMK + 
/* 315 */                   CommonFunctions.getStatusCode(AppLibFlowStatus) + 
/* 316 */                   " " + 
/* 317 */                   brokerName + 
/* 318 */                   "-" + 
/* 319 */                   thisEG + 
/* 320 */                   "_" + 
/* 321 */                   thisLib.getName() + 
/* 322 */                   "_" + 
/* 323 */                   thisAppLibFlow.getName() + 
/* 324 */                   " Status=" + 
/* 325 */                   AppLibFlowStatus + 
/* 326 */                   " " + 
/* 327 */                   thisAppLibFlow.getName() + 
/* 328 */                   " is " + 
/*     */                   
/* 330 */                   CommonFunctions.componentRuningStatus(AppLibFlowStatus) + 
/* 331 */                   "\n";
/*     */               }
/*     */             }
/*     */             
/*     */ 
/*     */ 
/*     */ 
/* 338 */             log.info(new Date() + " " + toString() + 
/* 339 */               " End of IIB details");
/*     */           } else {
/* 341 */             checkMK = 
/* 342 */               "1 " + node + " Status=DOWN " + node + " is Stopped \n";
/* 343 */             log.error(new Date() + " " + toString() + 
/* 344 */               " Broker is down " + node);
/*     */           }
/*     */         } else {
/* 347 */           checkMK = 
/* 348 */             "1 " + node + " Status=DOWN " + node + " is Stopped \n";
/* 349 */           log.error(new Date() + " " + toString() + 
/* 350 */             " Broker is down " + node);
/*     */         }
/*     */       }
/*     */       else {
/* 354 */         checkMK = 
/* 355 */           "1 " + node + " Status=DOWN " + node + " is Stopped \n";
/* 356 */         log.error(new Date() + " " + toString() + 
/* 357 */           " Broker is down " + node + " Number of attempts made to connect " + counter);
/*     */       }
/*     */     }
/*     */     
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/* 371 */     PCFMessageAgent agent = null;
/*     */     try {
/* 373 */       agent = (PCFMessageAgent)queueManagerMap.get(node);
/* 374 */       log.debug(new Date() + " " + toString() + 
/* 375 */         "QM details receieved " + agent.getQManagerName());
/* 376 */       String connections = null;
/*     */       try {
/* 378 */         connections = WMQConnection.getConnections(agent);
/*     */       } catch (Exception e) {
/* 380 */         log.error(new Date() + " " + toString() + " " + 
/* 381 */           e.toString() + " WMQ connection cannot be retrieved " + 
/* 382 */           node);
/*     */       }
/*     */       
/* 385 */       log.info(new Date() + " " + toString() + 
/* 386 */         " WMQ connection details");
/*     */       
/* 388 */       String QMName = WMQConnection.getQueueManagerName(agent);
/*     */       
/* 390 */       checkMK = checkMK + "0 " + QMName + 
/* 391 */         "-CONNECTION Status=UP Number of connections " + 
/* 392 */         connections + "\n";
/*     */       try
/*     */       {
/* 395 */         FileInputStream fisQueue = new FileInputStream(
/* 396 */           "monitor/queues/" + node + ".properties");
/* 397 */         PropertyResourceBundle propertiesQueue = new PropertyResourceBundle(
/* 398 */           fisQueue);
/* 399 */         Enumeration queueEnum = propertiesQueue.getKeys();
/* 400 */         fisQueue.close();
/*     */         
/* 402 */         while (queueEnum.hasMoreElements())
/*     */         {
/* 404 */           String QueueName = (String)queueEnum.nextElement();
/* 405 */           log.debug(new Date() + " " + toString() + 
/* 406 */             "Queue details receieved " + QueueName);
/*     */           
/*     */ 
/*     */ 
/*     */ 
/* 411 */           String rawQDepthTrigger = WMQConnection.getLocalQueuesGeneralProp(agent, QueueName);
/* 412 */           String QDepth_HighLimit = WMQConnection.getQueueStatsWithKey(rawQDepthTrigger, "QDepth_HighLimit");
/* 413 */           String QDepth_LowLimit = WMQConnection.getQueueStatsWithKey(rawQDepthTrigger, "QDepth_LowLimit");
/* 414 */           String GetInhibit = WMQConnection.getQueueStatsWithKey(rawQDepthTrigger, "GetInhibit");
/* 415 */           String PutInhibit = WMQConnection.getQueueStatsWithKey(rawQDepthTrigger, "PutInhibit");
/* 416 */           String QMaxDepth = WMQConnection.getQueueStatsWithKey(
/* 417 */             rawQDepthTrigger, "QMaxDepth");
/*     */           
/* 419 */           String rawStats = WMQConnection.getLocalQueuesStats(agent, 
/* 420 */             QueueName);
/* 421 */           String qDepth = WMQConnection.getQueueStatsWithKey(
/* 422 */             rawStats, "QDepth");
/* 423 */           String QOldMsgAge = WMQConnection.getQueueStatsWithKey(
/* 424 */             rawStats, "QOldMsgAge");
/* 425 */           String QOpenInpCnt = WMQConnection.getQueueStatsWithKey(
/* 426 */             rawStats, "QOpenInpCnt");
/* 427 */           String QOpenOutPutCnt = WMQConnection.getQueueStatsWithKey(
/* 428 */             rawStats, "QOpenOutPutCnt");
/* 429 */           int QDepth_HighLimit_int = Integer.valueOf(QMaxDepth).intValue() / Integer.valueOf(QDepth_HighLimit).intValue();
/* 430 */           int QDepth_LowLimit_int = Integer.valueOf(QMaxDepth).intValue() / Integer.valueOf(QDepth_LowLimit).intValue();
/*     */           String status;
/*     */           String statustxt;
/* 433 */           String status; if ((Integer.valueOf(qDepth).intValue() > Integer.valueOf(QDepth_LowLimit_int).intValue()) && 
/* 434 */             (Integer.valueOf(qDepth).intValue() < Integer.valueOf(QDepth_HighLimit_int).intValue())) { String status;
/* 435 */             if (Integer.valueOf(QOldMsgAge).intValue() < 1800) {
/* 436 */               String statustxt = "WARN";
/* 437 */               status = "1";
/*     */             } else { String status;
/* 439 */               if (Integer.valueOf(QOldMsgAge).intValue() > 3600) {
/* 440 */                 String statustxt = "CRITICAL";
/* 441 */                 status = "2";
/*     */               }
/*     */               else {
/* 444 */                 String statustxt = "WARN";
/* 445 */                 status = "1";
/*     */               }
/*     */             }
/*     */           }
/*     */           else {
/*     */             String status;
/* 451 */             if (Integer.valueOf(qDepth).intValue() > Integer.valueOf(QDepth_HighLimit_int).intValue()) {
/* 452 */               String statustxt = "CRITICAL";
/* 453 */               status = "2";
/*     */             }
/*     */             else {
/*     */               String status;
/* 457 */               if (Integer.valueOf(QOldMsgAge).intValue() < 1800) {
/* 458 */                 String statustxt = "OK";
/* 459 */                 status = "0";
/*     */               } else { String status;
/* 461 */                 if (Integer.valueOf(QOldMsgAge).intValue() > 3600) {
/* 462 */                   String statustxt = "CRITICAL";
/* 463 */                   status = "2";
/*     */                 } else {
/*     */                   String status;
/* 466 */                   if ((Integer.valueOf(GetInhibit).intValue() > 0) || (Integer.valueOf(PutInhibit).intValue() > 0)) {
/* 467 */                     String statustxt = "CRITICAL";
/* 468 */                     status = "2";
/*     */                   }
/*     */                   else {
/* 471 */                     statustxt = "OK";
/* 472 */                     status = "0";
/*     */                   }
/*     */                 }
/*     */               }
/*     */             }
/*     */           }
/* 478 */           checkMK = 
/*     */           
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/* 486 */             checkMK + status + " " + QMName + "-" + QueueName + " " + "Q_DEPTH=" + qDepth + "|Q_OLD_MSG_AGE=" + QOldMsgAge + "|Q_OPEN_INPUT_COUNT=" + QOpenInpCnt + "|Q_OPEN_OUTPUT_COUNT=" + QOpenOutPutCnt + "|Q_INHIBIT_GET=" + GetInhibit + "|Q_INHIBIT_PUT=" + PutInhibit + " " + statustxt + " QDepth is " + qDepth + "\n";
/*     */         }
/*     */       }
/*     */       catch (Exception e) {
/* 490 */         log.error(new Date() + " " + toString() + " " + 
/* 491 */           e.toString() + " WMQ Queue's cannot be retrieved " + 
/* 492 */           node);
/*     */       }
/*     */       
/* 495 */       log.info(new Date() + " " + toString() + " WMQ Queue details");
/*     */       try
/*     */       {
/* 498 */         FileInputStream fisChannel = new FileInputStream(
/* 499 */           "monitor/channels/" + node + ".properties");
/* 500 */         PropertyResourceBundle propertiesChannel = new PropertyResourceBundle(
/* 501 */           fisChannel);
/* 502 */         Enumeration channelEnum = propertiesChannel.getKeys();
/* 503 */         fisChannel.close();
/*     */         StringTokenizer st2;
/* 505 */         for (; channelEnum.hasMoreElements(); 
/*     */             
/*     */ 
/*     */ 
/* 509 */             st2.hasMoreElements())
/*     */         {
/* 506 */           String raw = propertiesChannel.getString(channelEnum
/* 507 */             .nextElement().toString());
/* 508 */           st2 = new StringTokenizer(raw, ",");
/* 509 */           continue;
/* 510 */           String channelName = st2.nextElement().toString();
/* 511 */           log.debug(new Date() + " " + toString() + 
/* 512 */             "Channel details receieved " + channelName);
/* 513 */           String channelStatus = WMQConnection.getChannelStatus(
/* 514 */             agent, channelName);
/* 515 */           String channelStatusCode = 
/* 516 */             CommonFunctions.channleStatusCode(channelStatus);
/* 517 */           String qMgrName = 
/* 518 */             WMQConnection.getQueueManagerName(agent);
/*     */           
/* 520 */           checkMK = checkMK + channelStatusCode + " " + qMgrName + 
/* 521 */             "-" + channelName + " " + "CHANNEL_STATUS=" + 
/* 522 */             channelStatus + " Channel : " + channelName + 
/* 523 */             " is " + channelStatus + "\n";
/*     */         }
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 528 */         log.error(new Date() + " " + toString() + " " + 
/* 529 */           e.toString() + " WMQ Channel's cannot be retrieved " + 
/* 530 */           node);
/*     */       }
/*     */       
/* 533 */       log.info(new Date() + " " + toString() + 
/* 534 */         " WMQ Channel details");
/*     */     }
/*     */     catch (Exception e) {
/* 537 */       log.error(new Date() + " " + toString() + " " + e.toString() + 
/* 538 */         " QueueManager is down " + node + "QM");
/* 539 */       checkMK = checkMK + "1 " + node + " Status=DOWN " + node + 
/* 540 */         " Queue Manager is Stopped \n";
/*     */     }
/*     */     
/*     */ 
/* 544 */     log.info(new Date() + " " + toString() + " CHECK MK feed Ended");
/* 545 */     return checkMK;
/*     */   }
/*     */ }


/* Location:              /Users/sud/Downloads/Monitor.jar!/com/syn/iib/monitor/v1/IIBandMQStats.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */