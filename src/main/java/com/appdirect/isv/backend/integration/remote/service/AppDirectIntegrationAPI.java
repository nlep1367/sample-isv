package com.appdirect.isv.backend.integration.remote.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.appdirect.isv.backend.integration.remote.vo.BillingAPIResult;
import com.appdirect.isv.backend.integration.remote.vo.EventInfo;
import com.appdirect.isv.backend.integration.remote.vo.UsageBean;

@Path("api/integration/v1")
public interface AppDirectIntegrationAPI {
	@GET
	@Path("events/{token}")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public EventInfo readEvent(@PathParam("token") String eventToken);

	@POST
	@Path("billing/usage")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public BillingAPIResult billUsage(UsageBean usageBean);
}
