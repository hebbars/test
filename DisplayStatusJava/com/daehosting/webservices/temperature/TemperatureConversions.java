/**
 * TemperatureConversions.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.daehosting.webservices.temperature;

public interface TemperatureConversions extends javax.xml.rpc.Service {

/**
 * Visual DataFlex Web Service to convert temperature values between
 * Celcius and Fahrenheit
 */
    public java.lang.String getTemperatureConversionsSoapAddress();

    public com.daehosting.webservices.temperature.TemperatureConversionsSoapType getTemperatureConversionsSoap() throws javax.xml.rpc.ServiceException;

    public com.daehosting.webservices.temperature.TemperatureConversionsSoapType getTemperatureConversionsSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
