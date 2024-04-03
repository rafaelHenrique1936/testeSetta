package siemensinsightshubsinglesignon;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.mendix.core.Core;
import com.mendix.externalinterface.connector.RequestHandler;
import com.mendix.http.Http;
import com.mendix.http.HttpHeader;
import com.mendix.http.HttpMethod;
import com.mendix.http.HttpResponse;
import com.mendix.logging.ILogNode;
import com.mendix.m2ee.api.IMxRuntimeRequest;
import com.mendix.m2ee.api.IMxRuntimeResponse;
import com.mendix.thirdparty.org.json.JSONArray;
import com.mendix.thirdparty.org.json.JSONObject;

import siemensinsightshubsinglesignon.proxies.CredentialsResponse;
import siemensinsightshubsinglesignon.proxies.constants.Constants;

public class ApiReverseProxy extends RequestHandler {

	protected static ILogNode LOGGER = Core.getLogger(Constants.getModuleName());

	private Http Http;
	List<ApiReverseProxyConfiguration> configList = new ArrayList<ApiReverseProxyConfiguration>();

	public ApiReverseProxy(Http http) {
		Http = http;
		readConfiguration();
	}

	@Override
	protected void processRequest(IMxRuntimeRequest request, IMxRuntimeResponse response, String path)
			throws Exception {

		String QueryString = request.getHttpServletRequest().getQueryString();
		String URL = getForwardToURL(request.getResourcePath());

		if (QueryString != null) {
			URL = URL + "?" + QueryString;
		}
		URI uri = URI.create(URL);
		String token = getAccessTokenFromSession(request);

		HttpHeader[] Headers = getRequestHeaders(request, token);
		HttpMethod method = HttpMethod.valueOf(request.getHttpServletRequest().getMethod());
		HttpResponse remoteresponse = Http.executeHttpRequest(uri, method, Headers, request.getInputStream());
		// Set the status before sending any data
		response.setStatus(remoteresponse.getStatusCode());
		// Copy Headers to response
		for (HttpHeader header : remoteresponse.getAllHeaders()) {
			response.addHeader(header.getName(), header.getValue());
		}

		InputStream in = remoteresponse.getContent();
		OutputStream out = response.getOutputStream();

		// Copy Content
		if (in != null) {
			int d;
			while ((d = in.read()) != -1) {
				out.write(d);
			}
			in.close();
		}
		out.close();
		remoteresponse.close();
	}

	private String getForwardToURL(String resourcePath) {
		for (ApiReverseProxyConfiguration config : configList) {
			if (resourcePath.matches(config.hostPattern)) { // forward to configured Host, with modified resourcePath
				String modifiedResourcePath = resourcePath.replaceAll(config.resourcePathPattern,
						config.resourcePathPatternReplacement);
				String URL = config.forwardToHost + modifiedResourcePath;
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("forward: " + resourcePath + " to: " + URL);
				}
				return URL;
			}
		}
		return Constants.getGatewayURL() + resourcePath;
	}

	private void readConfiguration() {
		configList.clear();
		try { // Try to parse the JSON and fill the Configuration List
			if (Constants.getApiReverseProxyConfig().isBlank()) {
				return;
			}
			JSONArray array = new JSONArray(Constants.getApiReverseProxyConfig());
			for (int i = 0; i < array.length(); i++) {
				JSONObject obj = array.getJSONObject(i);
				String host = obj.getString("host");
				String pattern = obj.getString("pattern");
				String resourcePattern = obj.getString("resourcePattern");
				String resourcePatternReplacement = obj.getString("resourcePatternReplacement");
				LOGGER.info("Add ApiReverseProxy configuration: {\"host\":\"" + host + "\", \"pattern\":\"" + pattern + "\", \"resourcePattern\":\"" + resourcePattern + "\", \"resourcePatternReplacement\":\""+ resourcePatternReplacement + "\"}");
				ApiReverseProxyConfiguration config = new ApiReverseProxyConfiguration(host, pattern, resourcePattern,
						resourcePatternReplacement);
				configList.add(config);
			}
		} catch( Exception e) {
			LOGGER.error("ApiReverseProxy, try to parse configuration: " + Constants.getApiReverseProxyConfig() + System.lineSeparator() + e.getMessage(), e);
		} 
	}

	private String getAccessTokenFromSession(IMxRuntimeRequest request) throws Exception {
		CredentialsResponse ACResponse = CredentialsHandler.retrievCredentialsResponsFromSession(request);
		if (ACResponse != null) { //
			return ACResponse.getToken_type() + " " + ACResponse.getAccess_token();
		}
		return "";
	}

	private HttpHeader[] getRequestHeaders(IMxRuntimeRequest request, String token) {
		List<HttpHeader> aList = new ArrayList<HttpHeader>();
		Enumeration<String> headersNames = request.getHttpServletRequest().getHeaderNames();
		while (headersNames.hasMoreElements()) {
			String key = headersNames.nextElement();
			if (key.compareToIgnoreCase("Cookie") != 0 && key.compareToIgnoreCase("Host") != 0
					&& key.compareToIgnoreCase("Content-Length") != 0) {
				aList.add(new HttpHeader(key, request.getHeader(key)));
			}
		}

		aList.add(new HttpHeader("Authorization", token));
		String Host = request.getHttpServletRequest().getServerName();
		String Port = Integer.toString(request.getHttpServletRequest().getLocalPort());
		String Scheme = request.getHttpServletRequest().getScheme();
		if (Host.compareToIgnoreCase("localhost") == 0) {
			aList.add(new HttpHeader("Forwarded", "host=" + Host + ":" + Port + ";proto=" + Scheme));
		} else {
			aList.add(new HttpHeader("Forwarded", "host=" + Host + ";proto=" + Scheme));
		}
		HttpHeader[] headers = new HttpHeader[aList.size()];
		return aList.toArray(headers);
	}

	public class ApiReverseProxyConfiguration {
		private String forwardToHost;
		private String hostPattern;
		private String resourcePathPattern;
		private String resourcePathPatternReplacement;

		public ApiReverseProxyConfiguration(String ForwaredToHost, String Pattern, String ResourcePathPattern,
				String ResourcePathPatternReplacement) {
			forwardToHost = ForwaredToHost;
			hostPattern = Pattern;
			resourcePathPattern = ResourcePathPattern;
			resourcePathPatternReplacement = ResourcePathPatternReplacement;
		}

		public String Host() {
			return forwardToHost;
		}

		public String pattern() {
			return hostPattern;
		}

		public String replacePattern() {
			return resourcePathPattern;
		}

		public String replacePatternWith() {
			return resourcePathPatternReplacement;
		}
	}
}