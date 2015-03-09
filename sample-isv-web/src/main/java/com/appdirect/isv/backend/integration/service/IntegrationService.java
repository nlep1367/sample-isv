package com.appdirect.isv.backend.integration.service;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.appdirect.isv.backend.integration.remote.vo.APIResult;

@Path("integration/appdirect")
public interface IntegrationService {
	@GET
	@Path("processEvent")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public APIResult processEvent(@Context HttpServletRequest request, @QueryParam("eventUrl") String eventUrl, @QueryParam("token") String token);
}
