package siemensinsightshubsinglesignon;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.logging.ILogNode;
import com.mendix.m2ee.api.IMxRuntimeResponse;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import siemensinsightshubsinglesignon.proxies.Tenant;
import siemensinsightshubsinglesignon.proxies.constants.Constants;
import siemensinsightshubsinglesignon.proxies.microflows.Microflows;
import system.proxies.UserRole;

public class AutoRegistration {
	public interface ITenantHelper {		
		public String getFirstTenantName();		
		public boolean hasTenant();
	}

	public class TenantHelper implements ITenantHelper {
		private Tenant firstTenant;
		private IContext context;
		public TenantHelper(IContext context) {
			this.context = context;
			this.firstTenant = this.getFirstTenant(this.context);
		}
		
		private Tenant getFirstTenant(IContext context) {
			return Microflows.dS_FirstTenant(context);
		}		
		public boolean hasTenant() {
			return this.firstTenant != null;
		}
		
		public String getFirstTenantName() {			
			return this.firstTenant.getName();			
		}
	}

	private IContext context;

	public AutoRegistration() {
		this.context = Core.createSystemContext();
	}

	protected static ILogNode LOGGER = Core.getLogger(Constants.getModuleName());

	private String getProjectID() {
		return Microflows.dS_GetAppUUID(context);		
	}

	private String getProjectUserRoles() {
		List<UserRole> appUserRoles;
		try {
			appUserRoles = UserRole.load(context, "");
			return appUserRoles
					.stream()
					.filter(userRole -> userRole.getName().compareToIgnoreCase("Anonymous")!=0)
					.map(UserRole::getName)
					.collect(Collectors.joining(","));
		} catch (CoreException e) {
			LOGGER.error(e.getMessage(), e);
			return "";
		}
	}

	private String getApplicationURL() {
		return Core.getConfiguration().getApplicationRootUrl();
	}

	private String getDevCockpitName(String TenantName) {
		if (TenantName.isEmpty()) {
			return "TENANT_NAME-devcockpit";
		} else {
			return TenantName + "-devcockpit";
		}
	}

	private String getAutoRegistrationURL(String TenantName) {
		return "https://" + this.getDevCockpitName(TenantName) + this.getRegion()
				+ "/assets/html/auto-registration.html" 
				+ "?appUUID=" + this.getProjectID() 
				+ "&projectRoles=" + this.getProjectUserRoles() 
				+ "&appURL=" + this.getApplicationURL();
	}

	private String getBase64AutoRegistrationURL() {
		return Base64.getEncoder().encodeToString(this.getAutoRegistrationURL("").getBytes());
	}

	private String getRegion() {
		String response = "eu1.mindsphere.io";
		int FirstDot = Constants.getGatewayURL().indexOf(".");
		if (FirstDot > 0) {
			response = Constants.getGatewayURL().substring(FirstDot);
		}
		return response;
	}

	private String redirectToAutoRegistration(ITenantHelper tenantHelper) throws CoreException {
		// We have to distinguish between already once called via a InsightsHub Tenant
		// and never called via a InsightsHub Tenant
		// Therefore check if a tenant - entity exists in the db.		
		if (tenantHelper.hasTenant()) {
			return this.getAutoRegistrationURL(tenantHelper.getFirstTenantName());			
		} else {
			// Generate the auto registration-explanation URL
			// https://core-start.<Region>/autoregistration
			return "https://core-start" + this.getRegion() + "/autoregistration"					 
					+ "?forwardingURL=" + this.getBase64AutoRegistrationURL();			
		}
	}
	public void redirectToAutoRegistration(IMxRuntimeResponse response) throws CoreException {
		// Set status = 303
		response.setStatus(IMxRuntimeResponse.SEE_OTHER);
		response.addHeader("Location", this.redirectToAutoRegistration(new TenantHelper(this.context)));
	}
	public String getAutoRegistrationURL() throws CoreException {
		
		return this.redirectToAutoRegistration(new TenantHelper(Core.createSystemContext()));			
	}
	
}
