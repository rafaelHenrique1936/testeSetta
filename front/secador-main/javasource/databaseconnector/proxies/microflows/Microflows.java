// This file was generated by Mendix Studio Pro.
//
// WARNING: Code you write here will be lost the next time you deploy the project.

package databaseconnector.proxies.microflows;

import java.util.HashMap;
import java.util.Map;
import com.mendix.core.Core;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;

public class Microflows
{
	/**
	 * @deprecated
	 * The default constructor of the Microflows class should not be used.
	 * Use the static microflow invocation methods instead.
	 */
	@java.lang.Deprecated(since = "9.12", forRemoval = true)
	public Microflows() {}

	// These are the microflows for the DatabaseConnector module
	public static java.util.List<databaseconnector.proxies.objDadosTelemetria> dadosTelemetria(IContext context)
	{
		Map<java.lang.String, Object> params = new HashMap<>();
		java.util.List<IMendixObject> objs = Core.microflowCall("DatabaseConnector.dadosTelemetria").withParams(params).execute(context);
		if (objs == null) {
			return null;
		} else {
			return objs.stream()
				.map(obj -> databaseconnector.proxies.objDadosTelemetria.initialize(context, obj))
				.collect(java.util.stream.Collectors.toList());
		}
	}
	public static java.util.List<databaseconnector.proxies.objDadosTelemetria> dadosTelemetriaUmidade(IContext context)
	{
		Map<java.lang.String, Object> params = new HashMap<>();
		java.util.List<IMendixObject> objs = Core.microflowCall("DatabaseConnector.dadosTelemetriaUmidade").withParams(params).execute(context);
		if (objs == null) {
			return null;
		} else {
			return objs.stream()
				.map(obj -> databaseconnector.proxies.objDadosTelemetria.initialize(context, obj))
				.collect(java.util.stream.Collectors.toList());
		}
	}
	public static java.util.List<databaseconnector.proxies.objTemperatura> dadosTelemetriaVariacaoTemperatura(IContext context)
	{
		Map<java.lang.String, Object> params = new HashMap<>();
		java.util.List<IMendixObject> objs = Core.microflowCall("DatabaseConnector.dadosTelemetriaVariacaoTemperatura").withParams(params).execute(context);
		if (objs == null) {
			return null;
		} else {
			return objs.stream()
				.map(obj -> databaseconnector.proxies.objTemperatura.initialize(context, obj))
				.collect(java.util.stream.Collectors.toList());
		}
	}
	public static java.util.List<databaseconnector.proxies.objUmidade> dadosTelemetriaVariacaoUmidade(IContext context)
	{
		Map<java.lang.String, Object> params = new HashMap<>();
		java.util.List<IMendixObject> objs = Core.microflowCall("DatabaseConnector.dadosTelemetriaVariacaoUmidade").withParams(params).execute(context);
		if (objs == null) {
			return null;
		} else {
			return objs.stream()
				.map(obj -> databaseconnector.proxies.objUmidade.initialize(context, obj))
				.collect(java.util.stream.Collectors.toList());
		}
	}
}