/**
 * TemperatureConversionsSoapType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.daehosting.webservices.temperature;

public interface TemperatureConversionsSoapType extends java.rmi.Remote {

    /**
     * Converts a Celcius Temperature to a Fahrenheit value
     */
    public java.math.BigDecimal celciusToFahrenheit(java.math.BigDecimal nCelcius) throws java.rmi.RemoteException;

    /**
     * Converts a Fahrenheit Temperature to a Celcius value
     */
    public java.math.BigDecimal fahrenheitToCelcius(java.math.BigDecimal nFahrenheit) throws java.rmi.RemoteException;

    /**
     * Windchill temperature calculated with the formula of Steadman
     */
    public java.math.BigDecimal windChillInCelcius(java.math.BigDecimal nCelcius, java.math.BigDecimal nWindSpeed) throws java.rmi.RemoteException;

    /**
     * Windchill temperature calculated with the formula of Steadman
     */
    public java.math.BigDecimal windChillInFahrenheit(java.math.BigDecimal nFahrenheit, java.math.BigDecimal nWindSpeed) throws java.rmi.RemoteException;
}
