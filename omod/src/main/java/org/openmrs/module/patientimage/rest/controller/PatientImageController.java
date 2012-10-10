/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.patientimage.rest.controller;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.api.context.Context;
import org.openmrs.module.patientimage.rest.resource.PatientImageResource;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = "/rest/v1/patientimage/patientimage")
public class PatientImageController extends BaseRestController {
	
	/**
	 * @param patientid int
	 * @param pageid int
	 * @return ResponseEntity<byte[]> containing image binary data with JPEG
	 * 	image header.
	 * @throws ResponseException
	 * @throws IOException 
	 */
	@RequestMapping(value = "/{patientid}/{pageid}", method = RequestMethod.GET)
	public ResponseEntity<byte[]> retrieve(@PathVariable("patientid") String patientIdStr,
	        @PathVariable("pageid") String pageIdStr, HttpServletRequest request) throws IOException {
		Context.openSession();
		RequestContext context = RestUtil.getRequestContext(request);
		PatientImageResource r = Context.getService(RestService.class).getResource(PatientImageResource.class);
		int patientId = Integer.parseInt(patientIdStr);
		int pageId = Integer.parseInt(pageIdStr);
		final HttpHeaders headers = new HttpHeaders();
		byte[] imageData = null;
		HttpStatus status = null;
		try {
			imageData = r.retrieve(patientId, pageId);
			headers.setContentType(MediaType.IMAGE_JPEG);
			status = HttpStatus.OK;
		}
		catch (IOException e) {
			status = HttpStatus.NOT_FOUND;
		}
		Context.closeSession();
		return new ResponseEntity<byte[]>(imageData, headers, status);
	}
	
	@RequestMapping(value = "/{patientid}", method = RequestMethod.POST)
	public void create(@PathVariable("patientid") String patientIdStr, HttpServletRequest request) throws IOException {
		RestUtil.getRequestContext(request);
		PatientImageResource r = Context.getService(RestService.class).getResource(PatientImageResource.class);
		int patientId = Integer.parseInt(patientIdStr);
		byte[] imgData = new byte[request.getContentLength()];
		InputStream in = request.getInputStream();
		int offset = 0;
		in.read(imgData, 0, request.getContentLength());
		r.create(patientId, imgData);
	}
	
	@RequestMapping(value = "/{patientid}/{pageid}", method = RequestMethod.DELETE)
	public void delete(@PathVariable("patientid") String patientIdStr, @PathVariable("pageid") String pageIdStr,
	        HttpServletRequest request) throws IOException {
		RestUtil.getRequestContext(request);
		PatientImageResource r = Context.getService(RestService.class).getResource(PatientImageResource.class);
		int patientId = Integer.parseInt(patientIdStr);
		int pageId = Integer.parseInt(pageIdStr);
		r.delete(patientId, pageId);
	}
}
