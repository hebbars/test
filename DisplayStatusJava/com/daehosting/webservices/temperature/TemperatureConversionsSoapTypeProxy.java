package com.daehosting.webservices.temperature;

public class TemperatureConversionsSoapTypeProxy implements com.daehosting.webservices.temperature.TemperatureConversionsSoapType {
  private String _endpoint = null;
  private com.daehosting.webservices.temperature.TemperatureConversionsSoapType temperatureConversionsSoapType = null;
  
  public TemperatureConversionsSoapTypeProxy() {
    _initTemperatureConversionsSoapTypeProxy();
  }
  
  public TemperatureConversionsSoapTypeProxy(String endpoint) {
    _endpoint = endpoint;
    _initTemperatureConversionsSoapTypeProxy();
  }
  
  private void _initTemperatureConversionsSoapTypeProxy() {
    try {
      temperatureConversionsSoapType = (new com.daehosting.webservices.temperature.TemperatureConversionsLocator()).getTemperatureConversionsSoap();
      if (temperatureConversionsSoapType != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)temperatureConversionsSoapType)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)temperatureConversionsSoapType)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (temperatureConversionsSoapType != null)
      ((javax.xml.rpc.Stub)temperatureConversionsSoapType)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public com.daehosting.webservices.temperature.TemperatureConversionsSoapType getTemperatureConversionsSoapType() {
    if (temperatureConversionsSoapType == null)
      _initTemperatureConversionsSoapTypeProxy();
    return temperatureConversionsSoapType;
  }
  
  public java.math.BigDecimal celciusToFahrenheit(java.math.BigDecimal nCelcius) throws java.rmi.RemoteException{
    if (temperatureConversionsSoapType == null)
      _initTemperatureConversionsSoapTypeProxy();
    return temperatureConversionsSoapType.celciusToFahrenheit(nCelcius);
  }
  
  public java.math.BigDecimal fahrenheitToCelcius(java.math.BigDecimal nFahrenheit) throws java.rmi.RemoteException{
    if (temperatureConversionsSoapType == null)
      _initTemperatureConversionsSoapTypeProxy();
    return temperatureConversionsSoapType.fahrenheitToCelcius(nFahrenheit);
  }
  
  public java.math.BigDecimal windChillInCelcius(java.math.BigDecimal nCelcius, java.math.BigDecimal nWindSpeed) throws java.rmi.RemoteException{
    if (temperatureConversionsSoapType == null)
      _initTemperatureConversionsSoapTypeProxy();
    return temperatureConversionsSoapType.windChillInCelcius(nCelcius, nWindSpeed);
  }
  
  public java.math.BigDecimal windChillInFahrenheit(java.math.BigDecimal nFahrenheit, java.math.BigDecimal nWindSpeed) throws java.rmi.RemoteException{
    if (temperatureConversionsSoapType == null)
      _initTemperatureConversionsSoapTypeProxy();
    return temperatureConversionsSoapType.windChillInFahrenheit(nFahrenheit, nWindSpeed);
  }
  
  
}