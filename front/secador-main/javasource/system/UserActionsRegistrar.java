package system;

import com.mendix.core.actionmanagement.IActionRegistrator;

public class UserActionsRegistrar
{
  public void registerActions(IActionRegistrator registrator)
  {
    registrator.bundleComponentLoaded();
    registrator.registerUserAction(databaseconnector.actions.ExecuteCallableStatement.class);
    registrator.registerUserAction(databaseconnector.actions.ExecuteParameterizedQuery.class);
    registrator.registerUserAction(databaseconnector.actions.ExecuteParameterizedStatement.class);
    registrator.registerUserAction(databaseconnector.actions.ExecuteQuery.class);
    registrator.registerUserAction(databaseconnector.actions.ExecuteStatement.class);
    registrator.registerUserAction(siemensinsightshubosbarconnector.actions.Java_getModelVersion.class);
    registrator.registerUserAction(siemensinsightshubosbarconnector.actions.Java_getRuntimeVersion.class);
    registrator.registerUserAction(siemensinsightshubsinglesignon.actions.AccessTokenConnector.class);
    registrator.registerUserAction(siemensinsightshubsinglesignon.actions.AuthenticateRestAPI.class);
    registrator.registerUserAction(siemensinsightshubsinglesignon.actions.CalcBase64Credentials.class);
    registrator.registerUserAction(siemensinsightshubsinglesignon.actions.generateUUID.class);
    registrator.registerUserAction(siemensinsightshubsinglesignon.actions.Get_MDSP_ENV.class);
    registrator.registerUserAction(siemensinsightshubsinglesignon.actions.GetAccessToken.class);
    registrator.registerUserAction(siemensinsightshubsinglesignon.actions.getRandomString.class);
    registrator.registerUserAction(siemensinsightshubsinglesignon.actions.GetUserAgent.class);
    registrator.registerUserAction(siemensinsightshubsinglesignon.actions.PutAccessToken.class);
    registrator.registerUserAction(siemensinsightshubsinglesignon.actions.RunningLocal.class);
    registrator.registerUserAction(siemensinsightshubsinglesignon.actions.ScheduledAccessTokenConnector.class);
    registrator.registerUserAction(siemensinsightshubsinglesignon.actions.SingleSignOn.class);
    registrator.registerUserAction(siemensinsightshubsinglesignon.actions.SingleSignOnMobile.class);
    registrator.registerUserAction(system.actions.VerifyPassword.class);
  }
}
