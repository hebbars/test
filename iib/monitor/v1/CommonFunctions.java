/*     */ package com.syn.iib.monitor.v1;
/*     */ 
/*     */ import java.io.FileInputStream;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.IOException;
/*     */ import java.util.Date;
/*     */ import java.util.Properties;
/*     */ import java.util.PropertyResourceBundle;
/*     */ import java.util.concurrent.TimeUnit;
/*     */ import org.apache.log4j.Logger;
/*     */ import org.apache.log4j.PropertyConfigurator;
/*     */ 
/*     */ public class CommonFunctions
/*     */ {
/*  15 */   static Logger log = Logger.getLogger(CommonFunctions.class);
/*     */   
/*  17 */   public static String getQMName(String node) { String connPath = "connection/" + node + ".properties";
/*     */     try {
/*  19 */       FileInputStream fis1 = new FileInputStream(connPath);
/*  20 */       PropertyResourceBundle properties1 = new PropertyResourceBundle(fis1);
/*  21 */       return properties1.getString("QMGR");
/*     */     } catch (IOException e) {
/*  23 */       log.error(new Date() + " " + "CommonFunctions" + " " + e.toString()); }
/*  24 */     return node + "QM";
/*     */   }
/*     */   
/*     */   public static Logger setLogging(Class C)
/*     */   {
/*  29 */     Logger logging = Logger.getLogger(C);
/*  30 */     String log4JPropertyFile = "log.property/common.properties";
/*  31 */     Properties p = new Properties();
/*     */     try {
/*  33 */       p.load(new FileInputStream(log4JPropertyFile));
/*     */     }
/*     */     catch (FileNotFoundException e1) {
/*  36 */       e1.printStackTrace();
/*     */     }
/*     */     catch (IOException e1) {
/*  39 */       e1.printStackTrace();
/*     */     }
/*  41 */     PropertyConfigurator.configure(p);
/*  42 */     return logging;
/*     */   }
/*     */   
/*  45 */   public static String getAge(long diff) { String age = null;
/*  46 */     long diffSeconds = 0L;
/*  47 */     long diffMinutes = 0L;
/*  48 */     long diffHours = 0L;
/*  49 */     long diffDays = 0L;
/*  50 */     if (diff <= 60000L) {
/*  51 */       diffSeconds = diff / 1000L % 60L;
/*  52 */       age = diffSeconds + " secs";
/*  53 */     } else if ((diff > 60000L) && (diff <= 3600000L)) {
/*  54 */       diffMinutes = diff / 60000L % 60L;
/*  55 */       age = diffMinutes + " mins";
/*  56 */     } else if ((diff > 3600000L) && (diff <= 604800000L)) {
/*  57 */       diffHours = diff / 3600000L % 24L;
/*  58 */       age = diffHours + " hrs";
/*  59 */     } else if (diff > 604800000L)
/*     */     {
/*  61 */       diffDays = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
/*  62 */       age = diffDays + " days";
/*     */     } else {
/*  64 */       age = diff + " ms";
/*     */     }
/*  66 */     return age;
/*     */   }
/*     */   
/*     */   public static String componentStatus(String status, String usrTrace, String svcTrace, String traceNode)
/*     */   {
/*  71 */     if (status.equals("true")) {
/*  72 */       if ((!usrTrace.equals("none")) || (!svcTrace.equals("none")) || (traceNode.equalsIgnoreCase("ON"))) {
/*  73 */         return "2";
/*     */       }
/*     */       
/*  76 */       return "0";
/*     */     }
/*     */     
/*     */ 
/*  80 */     if (status.equals("false")) {
/*  81 */       return "2";
/*     */     }
/*  83 */     return "3";
/*     */   }
/*     */   
/*     */ 
/*     */   public static String componentStringStatus(String status)
/*     */   {
/*  89 */     if (status.equals("true"))
/*  90 */       return "UP";
/*  91 */     if (status.equals("false")) {
/*  92 */       return "DOWN";
/*     */     }
/*  94 */     return "UNKNOWN";
/*     */   }
/*     */   
/*     */ 
/*     */   public static String componentRuningStatus(String status)
/*     */   {
/* 100 */     if (status.equals("true"))
/* 101 */       return "Running";
/* 102 */     if (status.equals("false")) {
/* 103 */       return "Stopped";
/*     */     }
/* 105 */     return "Unknown";
/*     */   }
/*     */   
/*     */   public static String channleStatusCode(String status)
/*     */   {
/* 110 */     String code = null;
/*     */     
/* 112 */     if (status.equals("MQCHS_RUNNING")) {
/* 113 */       code = "0";
/*     */     }
/*     */     else {
/* 116 */       code = "2";
/*     */     }
/*     */     
/* 119 */     return code;
/*     */   }
/*     */   
/*     */   public static String getStatusCode(String status) {
/* 123 */     String code = null;
/* 124 */     if (status.equalsIgnoreCase("true")) {
/* 125 */       code = "0";
/*     */     }
/* 127 */     else if (status.equalsIgnoreCase("false")) {
/* 128 */       code = "2";
/*     */     }
/*     */     else {
/* 131 */       code = "3";
/*     */     }
/*     */     
/* 134 */     return code;
/*     */   }
/*     */   
/*     */   public static String traceOptions(String level)
/*     */   {
/* 139 */     return "";
/*     */   }
/*     */ }


/* Location:              /Users/sud/Downloads/Monitor.jar!/com/syn/iib/monitor/v1/CommonFunctions.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */