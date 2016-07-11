package au.com.westpac.www.gn.arrangementManagement.services.maintainStatementPreferences.v1.SVC0273;

public class PtMaintainStatementPreferencesProxy implements au.com.westpac.www.gn.arrangementManagement.services.maintainStatementPreferences.v1.SVC0273.PtMaintainStatementPreferences {
  private String _endpoint = null;
  private au.com.westpac.www.gn.arrangementManagement.services.maintainStatementPreferences.v1.SVC0273.PtMaintainStatementPreferences ptMaintainStatementPreferences = null;
  
  public PtMaintainStatementPreferencesProxy() {
    _initPtMaintainStatementPreferencesProxy();
  }
  
  public PtMaintainStatementPreferencesProxy(String endpoint) {
    _endpoint = endpoint;
    _initPtMaintainStatementPreferencesProxy();
  }
  
  private void _initPtMaintainStatementPreferencesProxy() {
    try {
      ptMaintainStatementPreferences = (new au.com.westpac.www.gn.arrangementManagement.services.maintainStatementPreferences.v1.SVC0273.MaintainStatementPreferencesLocator()).getprMaintainStatementPreferencesSoapHttp();
      if (ptMaintainStatementPreferences != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)ptMaintainStatementPreferences)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)ptMaintainStatementPreferences)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (ptMaintainStatementPreferences != null)
      ((javax.xml.rpc.Stub)ptMaintainStatementPreferences)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public au.com.westpac.www.gn.arrangementManagement.services.maintainStatementPreferences.v1.SVC0273.PtMaintainStatementPreferences getPtMaintainStatementPreferences() {
    if (ptMaintainStatementPreferences == null)
      _initPtMaintainStatementPreferencesProxy();
    return ptMaintainStatementPreferences;
  }
  
  
}