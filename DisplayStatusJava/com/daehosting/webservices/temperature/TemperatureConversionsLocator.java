/**
 * TemperatureConversionsLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.daehosting.webservices.temperature;

public class TemperatureConversionsLocator extends org.apache.axis.client.Service implements com.daehosting.webservices.temperature.TemperatureConversions {

/**
 * Visual DataFlex Web Service to convert temperature values between
 * Celcius and Fahrenheit
 */

    public TemperatureConversionsLocator() {
    }


    public TemperatureConversionsLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public TemperatureConversionsLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for TemperatureConversionsSoap
    private java.lang.String TemperatureConversionsSoap_address = "http://webservices.daehosting.com/services/TemperatureConversions.wso";

    public java.lang.String getTemperatureConversionsSoapAddress() {
        return TemperatureConversionsSoap_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String TemperatureConversionsSoapWSDDServiceName = "TemperatureConversionsSoap";

    public java.lang.String getTemperatureConversionsSoapWSDDServiceName() {
        return TemperatureConversionsSoapWSDDServiceName;
    }

    public void setTemperatureConversionsSoapWSDDServiceName(java.lang.String name) {
        TemperatureConversionsSoapWSDDServiceName = name;
    }

    public com.daehosting.webservices.temperature.TemperatureConversionsSoapType getTemperatureConversionsSoap() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(TemperatureConversionsSoap_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getTemperatureConversionsSoap(endpoint);
    }

    public com.daehosting.webservices.temperature.TemperatureConversionsSoapType getTemperatureConversionsSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.daehosting.webservices.temperature.TemperatureConversionsSoapBindingStub _stub = new com.daehosting.webservices.temperature.TemperatureConversionsSoapBindingStub(portAddress, this);
            _stub.setPortName(getTemperatureConversionsSoapWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setTemperatureConversionsSoapEndpointAddress(java.lang.String address) {
        TemperatureConversionsSoap_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.daehosting.webservices.temperature.TemperatureConversionsSoapType.class.isAssignableFrom(serviceEndpointInterface)) {
                com.daehosting.webservices.temperature.TemperatureConversionsSoapBindingStub _stub = new com.daehosting.webservices.temperature.TemperatureConversionsSoapBindingStub(new java.net.URL(TemperatureConversionsSoap_address), this);
                _stub.setPortName(getTemperatureConversionsSoapWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("TemperatureConversionsSoap".equals(inputPortName)) {
            return getTemperatureConversionsSoap();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://webservices.daehosting.com/temperature", "TemperatureConversions");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://webservices.daehosting.com/temperature", "TemperatureConversionsSoap"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("TemperatureConversionsSoap".equals(portName)) {
            setTemperatureConversionsSoapEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
