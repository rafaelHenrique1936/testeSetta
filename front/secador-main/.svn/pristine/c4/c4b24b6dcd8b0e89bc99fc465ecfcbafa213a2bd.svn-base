package siemensinsightshubsinglesignon;

import com.mendix.core.Core;
import com.mendix.externalinterface.connector.RequestHandler;
import com.mendix.m2ee.api.IMxRuntimeRequest;
import com.mendix.m2ee.api.IMxRuntimeResponse;
import com.mendix.systemwideinterfaces.core.ISession;

public class LogoutHandler extends RequestHandler {
	
	@Override
	protected void processRequest(IMxRuntimeRequest request, IMxRuntimeResponse response, String path)
			throws Exception {
		ISession session = this.getSessionFromRequest(request);	
		logout(session, request);			
		response.setStatus(200);
		response.setContentType("text/html");
	}
	public static void logout(ISession session, IMxRuntimeRequest request) {				
		if (session != null) {
			Core.logout(session);			
		}
		CredentialsHandler.removeCredentialsFromSession(request);		
	}
}
