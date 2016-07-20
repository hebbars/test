/*     */ package com.syn.iib.monitor.v1;
/*     */ 
/*     */ import com.ibm.mq.MQException;
/*     */ import com.ibm.mq.constants.MQConstants;
/*     */ import com.ibm.mq.headers.MQDataException;
/*     */ import com.ibm.mq.headers.pcf.PCFException;
/*     */ import com.ibm.mq.headers.pcf.PCFMessage;
/*     */ import com.ibm.mq.headers.pcf.PCFMessageAgent;
/*     */ import java.io.IOException;
/*     */ import java.util.StringTokenizer;
/*     */ 
/*     */ 
/*     */ public class WMQConnection
/*     */ {
/*     */   public static String getChannelStatus(PCFMessageAgent agent, String ChannelName)
/*     */     throws MQDataException, IOException
/*     */   {
/*  18 */     PCFMessage pcfCmd = new PCFMessage(
/*  19 */       42);
/*  20 */     pcfCmd.addParameter(3501, ChannelName);
/*  21 */     String[] chStatusText = { "", "MQCHS_BINDING", "MQCHS_STARTING", 
/*  22 */       "MQCHS_RUNNING", "MQCHS_STOPPING", "MQCHS_RETRYING", 
/*  23 */       "MQCHS_STOPPED", "MQCHS_REQUESTING", "MQCHS_PAUSED", "", "", 
/*  24 */       "", "", "MQCHS_INITIALIZING" };
/*  25 */     int chStatus = 0;
/*     */     try {
/*  27 */       PCFMessage[] pcfResponse = agent.send(pcfCmd);
/*     */       
/*  29 */       chStatus = ((Integer)pcfResponse[0]
/*  30 */         .getParameterValue(1527))
/*  31 */         .intValue();
/*     */     }
/*     */     catch (PCFException pcfe) {
/*  34 */       if (pcfe.reasonCode == 3065) {
/*  35 */         return "MQRCCF_CHL_STATUS_NOT_FOUND";
/*     */       }
/*  37 */       throw pcfe;
/*     */     }
/*     */     
/*  40 */     return chStatusText[chStatus];
/*     */   }
/*     */   
/*     */   public static PCFMessageAgent createAgent(String host, int port, String channel) throws MQDataException
/*     */   {
/*  45 */     PCFMessageAgent agent = null;
/*     */     try
/*     */     {
/*  48 */       agent = new PCFMessageAgent(host, port, channel);
/*     */     }
/*     */     catch (MQDataException mqde)
/*     */     {
/*  52 */       throw mqde;
/*     */     }
/*  54 */     return agent;
/*     */   }
/*     */   
/*     */   public static String handleWMQException(Exception exception, PCFMessageAgent agent, String ChannelName)
/*     */   {
/*  59 */     String exceptionMessage = null;
/*  60 */     if (exception.getClass().equals(PCFException.class))
/*     */     {
/*  62 */       PCFException pcfe = (PCFException)exception;
/*     */       
/*  64 */       if (pcfe.reasonCode == 4032)
/*     */       {
/*  66 */         exceptionMessage = 
/*  67 */           "Either the queue manager \"" + agent.getQManagerName() + "\"";
/*     */         
/*  69 */         exceptionMessage = exceptionMessage + " or the channel \"" + ChannelName + 
/*  70 */           "\" on the queue manager could not be found.";
/*     */       } else {
/*  72 */         exceptionMessage = 
/*  73 */           pcfe.toString() + ": " + MQConstants.lookupReasonCode(pcfe.reasonCode);
/*     */       }
/*  75 */     } else if (exception.getClass().equals(IOException.class))
/*     */     {
/*  77 */       IOException ioe = (IOException)exception;
/*     */       
/*  79 */       exceptionMessage = ioe.toString();
/*  80 */     } else if (exception.getClass().equals(MQDataException.class))
/*     */     {
/*  82 */       MQDataException de = (MQDataException)exception;
/*     */       
/*  84 */       exceptionMessage = de.toString() + ": " + 
/*  85 */         MQConstants.lookupReasonCode(de.reasonCode);
/*  86 */     } else if (exception.getClass().equals(MQException.class))
/*     */     {
/*  88 */       MQException mqe = (MQException)exception;
/*     */       
/*  90 */       exceptionMessage = mqe.toString() + ": " + 
/*  91 */         MQConstants.lookupReasonCode(mqe.reasonCode);
/*     */     }
/*     */     
/*  94 */     return exceptionMessage;
/*     */   }
/*     */   
/*     */   public static String getLocalQueuesStats(PCFMessageAgent agent, String QueueName) throws PCFException, MQDataException, IOException
/*     */   {
/*  99 */     String QName = null;String QLastGetTime = null;String QLastPutTime = null;
/* 100 */     Integer QOpenOutPutCnt = null;
/* 101 */     Integer QOpenInpCnt = null;
/* 102 */     Integer QOldMsgAge = null;
/* 103 */     Integer QDepth = null;
/* 104 */     Integer QDepth_Trigger = null;
/* 105 */     PCFMessage pcfCmd = new PCFMessage(41);
/*     */     
/* 107 */     pcfCmd.addParameter(2016, QueueName);
/* 108 */     pcfCmd.addParameter(1103, 
/* 109 */       1105);
/*     */     
/* 111 */     pcfCmd.addParameter(1026, new int[] {
/* 112 */       2016, 3, 
/* 113 */       3130, 
/* 114 */       3131, 
/* 115 */       3128, 
/* 116 */       3129, 
/* 117 */       1227, 
/* 118 */       17, 
/* 119 */       18, 
/* 120 */       1027 });
/*     */     
/* 122 */     pcfCmd.addParameter(1002, 
/* 123 */       new int[] { 29 });
/*     */     
/* 125 */     PCFMessage[] pcfResponse = agent.send(pcfCmd);
/*     */     
/* 127 */     for (int index = 0; index < pcfResponse.length; index++) {
/* 128 */       PCFMessage response = pcfResponse[index];
/*     */       
/* 130 */       QName = ((String)response
/* 131 */         .getParameterValue(2016)).trim();
/* 132 */       QDepth = (Integer)response
/* 133 */         .getParameterValue(3);
/* 134 */       QLastGetTime = ((String)response
/* 135 */         .getParameterValue(3130))
/* 136 */         .trim() + 
/* 137 */         " " + 
/* 138 */         ((String)response
/* 139 */         .getParameterValue(3131))
/* 140 */         .trim();
/* 141 */       QLastPutTime = ((String)response
/* 142 */         .getParameterValue(3128))
/* 143 */         .trim() + 
/* 144 */         " " + 
/* 145 */         ((String)response
/* 146 */         .getParameterValue(3129))
/* 147 */         .trim();
/* 148 */       QOldMsgAge = (Integer)response
/* 149 */         .getParameterValue(1227);
/* 150 */       QOpenInpCnt = (Integer)response
/* 151 */         .getParameterValue(17);
/* 152 */       QOpenOutPutCnt = (Integer)response
/* 153 */         .getParameterValue(18);
/* 154 */       QDepth_Trigger = (Integer)response
/* 155 */         .getParameterValue(29);
/*     */     }
/*     */     
/* 158 */     String outBody = "QName:" + QName + ",QDepth:" + QDepth + 
/* 159 */       ",QLastGetTime:" + QLastGetTime + ",QLastPutTime:" + 
/* 160 */       QLastPutTime + ",QOldMsgAge:" + QOldMsgAge + ",QOpenInpCnt:" + 
/* 161 */       QOpenInpCnt + ",QOpenOutPutCnt:" + QOpenOutPutCnt;
/* 162 */     return outBody;
/*     */   }
/*     */   
/*     */   public static String getConnections(PCFMessageAgent agent) throws PCFException, MQDataException, IOException
/*     */   {
/* 167 */     String connectionString = "";
/* 168 */     PCFMessage pcfCmd = new PCFMessage(85);
/*     */     
/* 170 */     pcfCmd.addParameter(7007, 
/* 171 */       new byte[0]);
/*     */     
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/* 177 */     PCFMessage[] pcfResponse = agent.send(pcfCmd);
/*     */     
/* 179 */     for (int index = 0; index < pcfResponse.length; index++)
/*     */     {
/* 181 */       connectionString = String.valueOf(index + 1);
/*     */     }
/*     */     
/*     */ 
/* 185 */     return connectionString;
/*     */   }
/*     */   
/*     */   public static void destroyAgent(PCFMessageAgent agent)
/*     */     throws MQDataException
/*     */   {
/* 191 */     agent.disconnect();
/*     */   }
/*     */   
/*     */ 
/*     */   public static String getQueueManagerName(PCFMessageAgent agent)
/*     */     throws MQDataException
/*     */   {
/* 198 */     return agent.getQManagerName().toString();
/*     */   }
/*     */   
/*     */   public static String getQueueStatsWithKey(String rawStats, String key) throws MQDataException
/*     */   {
/* 203 */     String value = null;
/* 204 */     StringTokenizer st2 = new StringTokenizer(rawStats, ",");
/* 205 */     while (st2.hasMoreElements()) {
/* 206 */       String[] keyValue = st2.nextElement().toString().split(":");
/*     */       
/* 208 */       if (keyValue[0].equals(key)) {
/* 209 */         value = keyValue[1];
/*     */       }
/*     */     }
/*     */     
/*     */ 
/* 214 */     return value;
/*     */   }
/*     */   
/*     */   public static String getLocalQueuesGeneralProp(PCFMessageAgent agent, String QueueName) throws PCFException, MQDataException, IOException
/*     */   {
/* 219 */     String QName = null;
/*     */     
/* 221 */     Integer QDepth_HighLimit = null;
/* 222 */     Integer QDepth_LowLimit = null;
/* 223 */     Integer GetInhibit = null;
/* 224 */     Integer PutInhibit = null;
/* 225 */     Integer QMaxDepth = null;
/* 226 */     PCFMessage pcfCmd = new PCFMessage(13);
/*     */     
/* 228 */     pcfCmd.addParameter(2016, QueueName);
/*     */     
/* 230 */     pcfCmd.addParameter(1002, 
/* 231 */       new int[] { 40, 
/* 232 */       41, 
/* 233 */       9, 
/* 234 */       10, 
/* 235 */       15 });
/*     */     
/* 237 */     PCFMessage[] pcfResponse = agent.send(pcfCmd);
/*     */     
/* 239 */     for (int index = 0; index < pcfResponse.length; index++) {
/* 240 */       PCFMessage response = pcfResponse[index];
/*     */       
/* 242 */       QName = ((String)response
/* 243 */         .getParameterValue(2016)).trim();
/*     */       
/* 245 */       QDepth_HighLimit = (Integer)response
/* 246 */         .getParameterValue(40);
/* 247 */       QDepth_LowLimit = (Integer)response
/* 248 */         .getParameterValue(41);
/*     */       
/* 250 */       GetInhibit = (Integer)response
/* 251 */         .getParameterValue(9);
/* 252 */       PutInhibit = (Integer)response
/* 253 */         .getParameterValue(10);
/* 254 */       QMaxDepth = (Integer)response
/* 255 */         .getParameterValue(15);
/*     */     }
/*     */     
/* 258 */     String outBody = "QDepth_HighLimit:" + QDepth_HighLimit + ",QDepth_LowLimit:" + QDepth_LowLimit + ",GetInhibit:" + GetInhibit + ",PutInhibit:" + PutInhibit + ",QMaxDepth:" + QMaxDepth;
/* 259 */     return String.valueOf(outBody);
/*     */   }
/*     */ }


/* Location:              /Users/sud/Downloads/Monitor.jar!/com/syn/iib/monitor/v1/WMQConnection.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */