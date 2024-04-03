package siemensinsightshubsinglesignon;

import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.mendix.core.Core;
import com.mendix.externalinterface.connector.RequestHandler;
import com.mendix.m2ee.api.IMxRuntimeRequest;
import com.mendix.m2ee.api.IMxRuntimeResponse;


import siemensinsightshubsinglesignon.proxies.constants.Constants;

public class AppCredentialsPage extends RequestHandler {
	
	@Override
	protected void processRequest(IMxRuntimeRequest request, IMxRuntimeResponse response, String path) throws Exception {
		String queryString = request.getHttpServletRequest().getQueryString();
		this.WriteAppCredPageToResponse(response, false, 0, queryString,"",""); 
	}		
	
	/**
	 * DeploymentID = cachebust. Read it from Database
	 * @return
	 */
	private String getDeploymentID() {
		String sql = "SELECT \"value\" FROM \"PUBLIC\".\"mendixsystem$properties\" WHERE \"key\" LIKE 'Mendix.Runtime.DeploymentID'";
		return Core.dataStorage().executeWithConnection(connection -> {
			PreparedStatement stmt;
			try {
				stmt = connection.prepareStatement(sql);
				ResultSet rset = stmt.executeQuery();
				while (rset.next()) {
					return rset.getString("value");
				}	
				return "";
			} catch (SQLException e) {
				e.printStackTrace();
				return "";
			}		
		});			
	}
	public void WriteAppCredPageToResponse(IMxRuntimeResponse response, boolean showErrorHint, int statusCode, String queryString, String errorMessage, String errorType) throws Exception {		
		 
		String content = this.getPage();	
		SecretFromEnvironment secretHandler = new SecretFromEnvironment();
		String clientSecret = showErrorHint ? "" : secretHandler.getClientSecret();
		String clientId = (clientSecret.length() > 0) ? secretHandler.getClientId() : "";
		
		content = content.replaceAll("\\{\\{appName\\}\\}", Constants.getCockpitApplicationName());
		content = content.replaceAll("\\{\\{appVersion\\}\\}", Constants.getCockpitApplicationVersion());
		content = content.replaceAll("\\{\\{hostTenant\\}\\}", Constants.getHostTenant());
		content = content.replaceAll("\\{\\{userTenant\\}\\}", Constants.getUserTenant());
		content = content.replaceAll("\\{\\{clientId\\}\\}", clientId);
		content = content.replaceAll("\\{\\{clientSecret\\}\\}", clientSecret);
		String cachebust = getDeploymentID();
		content = content.replaceAll("\\{\\{cachebust\\}\\}",cachebust);
		if (queryString == null) {
			queryString = "";
		}
		content = content.replaceAll("\\{\\{queryString\\}\\}",queryString);
		if (showErrorHint) {
			content = content.replaceAll("\\{\\{visibility\\}\\}", "visible");
		} else {
			content = content.replaceAll("\\{\\{visibility\\}\\}", "hidden");
		}
		content = content.replaceAll("\\{\\{spinnerVisibility\\}\\}", "hidden");
		content = content.replaceAll("\\{\\{responseCode\\}\\}", Integer.toString(statusCode));
		content = content.replaceAll("\\{\\{errorMessage\\}\\}", errorMessage);
		content = content.replaceAll("\\{\\{errorType\\}\\}", errorType);
		content = content.replaceAll("\\{AUTO_APPLY_SCRIPT\\}", this.getAutoApplyScript(clientSecret.length() > 0));
		
		OutputStream out = response.getOutputStream();		
		out.write(content.getBytes());			
		response.setContentType("text/html");
		response.addHeader("Cache-Control", "no-cache");		
		response.setStatus(200);		
	}
	
	private String getAutoApplyScript(boolean applyAutoApply) {
		if (applyAutoApply) {
			return "\r\n" +
					"<script>\r\n" +
					"   document.getElementById(\\\"ok_button\\\").click();\r\n" +
					"</script>\r\n";
		}
		
		return "";
		
	}
	
	private String getPage() {
		return "<!DOCTYPE html>\r\n" + 
				"<html>\r\n" + 
				"<head>\r\n" +				
				"   <link rel=\"stylesheet\" href=\"theme.compiled.css?cachebust\">\r\n" +
				"	\r\n" + 
				"</head>\r\n" + 
				"<title>Application Credentials</title>\r\n" + 
				"<script>\r\n" + 
				"	function onSubmit() {\r\n" + 
				"		document.getElementById(\"mxui_widget_Progress\").style.visibility = 'visible';\r\n" + 
				"	}\r\n" + 
				"\r\n" + 
				"	function onCloseDialog() {\r\n" + 
				"		document.getElementById(\"modal_dialog\").style.visibility = 'hidden';\r\n" + 
				"	}\r\n" + 
				"</script>\r\n" + 
				"\r\n" + 
				"<body>\r\n" + 
				"	<div class=\"mx-layoutcontainer-middle mx-scrollcontainer-middle region-content\">\r\n" + 
				"		<div class=\"mx-layoutcontainer-wrapper mx-scrollcontainer-wrapper\">\r\n" + 
				"			<div class=\"mx-placeholder\">\r\n" + 
				"				<div class=\"mx-layoutgrid mx-layoutgrid-fluid container-fluid\">\r\n" + 
				"					<div class=\"row\">\r\n" + 
				"						<div class=\"col-md-12\">\r\n" + 
				"							<div class=\"form-group no-columns\">\r\n" +
				"								<h1 class=\"mx-text\">Request Insights Hub access token</h1>\r\n" + 
				"								<div class=\"form-control mx-textarea-input mx-textarea mx-textarea-input-noresize\" readonly=\"\" disabled=\"\">\r\n" + 
				"									You could obtain Application Credentials in the Developer Cockpit.<br>For further information see the Insights Hub documentation.\r\n" + 
				"								</div>\r\n" + 
				"							</div>\r\n" + 
				"						</div>\r\n" + 
				"					</div>\r\n" + 
				"					\r\n" + 
				"				\r\n" + 
				"					<div class=\"row\">\r\n" + 
				"						<div class=\"col-md-12\">\r\n" + 
				"							<div class=\"mx-dataview form-vertical\">\r\n" +
				"								<div class=\"mx-textbox form-group no-columns\">\r\n" +
				"									<label for=\"appname\" class=\"control-label\">Cockpit application name</label>\r\n" + 
				"									<input type=text id=\"appname\" value=\"{{appName}}\" class=\"form-control\" readonly=\"\" disabled=\"\"></input>\r\n" + 
				"								</div>\r\n" + 
				"								<div class=\"form-group no-columns\">\r\n" +
				"									<label for=\"appversion\" class=\"control-label\">Cockpit application version</label>\r\n" + 
				"									<input type=text id=\"appversion\" value=\"{{appVersion}}\" class=\"form-control\" readonly=\"\" disabled=\"\"></input>\r\n" + 
				"								</div>\r\n" + 
				"								<div class=\"form-group no-columns\">\r\n" +
				"									<label for=\"hosttenant\" class=\"control-label\">Host tenant</label>\r\n" + 
				"									<input type=text id=\"hosttenant\" value=\"{{hostTenant}}\" class=\"form-control\" readonly=\"\" disabled=\"\"></input>\r\n" + 
				"								</div>\r\n" + 
				"								<div class=\"form-group no-columns\">\r\n" +
				"									<label for=\"usertenant\" class=\"control-label\">User tenant</label>\r\n" + 
				"									<input type=text id=\"usertenant\" value=\"{{userTenant}}\" class=\"form-control\" readonly=\"\" disabled=\"\"></input>\r\n" + 
				"								</div>\r\n" + 
				"							</div>\r\n" + 
				"						</div>\r\n" + 
				"					</div>\r\n" + 
				"					<form action=\"/credentials\" method=\"post\">\r\n" + 
				"						<div class=\"mx-dataview\">\r\n" + 
				"							<div class=\"mx-dataview-content\">\r\n" + 
				"								<div class=\"form-group no-columns\">\r\n" +
				"									<label for=\"username\" class=\"control-label\">Client Id</label>\r\n" + 
				"									<input type=\"text\" class=\"form-control\" id=\"username\" name=\"clientId\" value=\"{{clientId}}\" autofocus>\r\n" + 
				"								</div>\r\n" + 
				"								<div class=\"form-group no-columns\">\r\n" +
				"									<label for=\"pass\" style=\"display: block\" class=\"control-label\">Client secret</label>\r\n" +
				"									<input id=\"pass\" type=\"password\" class=\"form-control\" name=\"clientSecret\" value=\"{{clientSecret}}\">\r\n" + 
				"								</div>\r\n" + 
				"							</div>\r\n" + 
				"							<div class=\"mx-dataview-controls\">\r\n" + 
				"								<input id=\"ok_button\" type=\"submit\" value=\"Submit\" class=\"btn mx-button btn-primary\" onclick=\"onSubmit();\">\r\n" + 				
				"								<input type=\"hidden\" id=\"queryString\" name=\"queryString\" value=\"{{queryString}}\"/>\r\n" +
				"								<button type=\"button\" class=\"btn mx-button mx-name-actionButton2 btn-default\" onclick=\"location.href = '/'\">Cancel</button>\r\n" + 
				"							</div>\r\n" + 
				"						</div>\r\n" + 
				"					</form>\r\n" + 
				"				</div>\r\n" + 
				"			</div>\r\n" + 
				"		</div>\r\n" + 
				"	</div>\r\n" + 
				"\r\n" + 
				"	<div id=\"mxui_widget_Progress\" style=\"visibility: {{spinnerVisibility}}\">\r\n" + 
				"		<div class=\"mx-progress\">\r\n" + 
				"			<div style=\"opacity: 1; z-index: 1002;\">\r\n" + 
				"				<div class=\"mx-progress-message\">Trying to get access token from\r\n" + 
				"					provided Application Credentials</div>\r\n" + 
				"				<div class=\"mx-progress-indicator\"></div>\r\n" + 
				"			</div>\r\n" + 
				"		</div>\r\n" + 
				"		<div class=\"mx-underlay\" id=\"mxui_widget_Underlay_0\" style=\"z-index: 101;\"></div>\r\n" + 
				"	</div>\r\n" + 
				"\r\n" + 
				"	<div id=\"modal_dialog\" style=\"visibility: {{visibility}}\">\r\n" + 
				"		<div class=\"modal-dialog mx-dialog mx-dialog-error\" tabindex=\"-1\"\r\n" + 
				"			id=\"mxui_widget_DialogMessage_0\"\r\n" + 
				"			style=\"opacity: 1; z-index: 1002; left: calc(50% - 250px); top: calc(50% - 233px);\"\r\n" + 
				"			data-focus-capturing=\"modal\">\r\n" + 
				"			<div class=\"modal-content mx-dialog-content\">\r\n" + 
				"				<div focusindex=\"-1\" class=\"modal-header mx-dialog-header\"\r\n" + 
				"					style=\"user-select: none;\">\r\n" + 
				"					<button type=\"button\" onclick=\"onCloseDialog()\" class=\"close mx-dialog-close\">x</button>\r\n" + 
				"					<h4 class=\"caption mx-dialog-caption\">Error</h4>\r\n" + 
				"				</div>\r\n" + 
				"				<div class=\"modal-body mx-dialog-body\">\r\n" + 
				"					<p>\r\n" + 
				"						Unable to fetch Application Credentials.<br>Response Code: {{responseCode}}. <br>{{errorType}}<br>{{errorMessage}}\r\n" + 
				"					</p>\r\n" + 
				"				</div>\r\n" + 
				"				<div focusindex=\"0\" class=\"modal-footer mx-dialog-footer\" style=\"\">\r\n" + 
				"					<button class=\"btn btn-primary\" onclick=\"onCloseDialog()\">OK</button>\r\n" + 
				"				</div>\r\n" + 
				"			</div>\r\n" + 
				"			<div class=\"mx-resizer mx-resizer-n\" data-resize-dir=\"n\"\r\n" + 
				"				style=\"user-select: none;\"></div>\r\n" + 
				"			<div class=\"mx-resizer mx-resizer-e\" data-resize-dir=\"e\"\r\n" + 
				"				style=\"user-select: none;\"></div>\r\n" + 
				"			<div class=\"mx-resizer mx-resizer-s\" data-resize-dir=\"s\"\r\n" + 
				"				style=\"user-select: none;\"></div>\r\n" + 
				"			<div class=\"mx-resizer mx-resizer-w\" data-resize-dir=\"w\"\r\n" + 
				"				style=\"user-select: none;\"></div>\r\n" + 
				"			<div class=\"mx-resizer mx-resizer-ne\" data-resize-dir=\"ne\"\r\n" + 
				"				style=\"user-select: none;\"></div>\r\n" + 
				"			<div class=\"mx-resizer mx-resizer-se\" data-resize-dir=\"se\"\r\n" + 
				"				style=\"user-select: none;\"></div>\r\n" + 
				"			<div class=\"mx-resizer mx-resizer-sw\" data-resize-dir=\"sw\"\r\n" + 
				"				style=\"user-select: none;\"></div>\r\n" + 
				"			<div class=\"mx-resizer mx-resizer-nw\" data-resize-dir=\"nw\"\r\n" + 
				"				style=\"user-select: none;\"></div>\r\n" + 
				"		</div>\r\n" + 
				"		<div class=\"mx-underlay\" id=\"mxui_widget_Underlay_1\" style=\"z-index: 101;\"></div>\r\n" + 
				"	</div>	\r\n" + 
				"</body>\r\n" + 
				"\r\n" + 
				"{AUTO_APPLY_SCRIPT}" +
				"</html>";
	}
}
