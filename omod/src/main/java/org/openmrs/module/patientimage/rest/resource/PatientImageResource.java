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
package org.openmrs.module.patientimage.rest.resource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.patientimage.PatientImage;
import org.openmrs.module.patientimage.PatientImageService;
import org.openmrs.module.webservices.rest.web.resource.api.Resource;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

public class PatientImageResource implements Resource {
	
	/**
	 * @param patientId
	 * @param pageId
	 * @return byte[] of image data.
	 * @throws ResponseException
	 * @throws IOException
	 */
	public byte[] retrieve(int patientId, int pageId) throws IOException {
		PatientImageService pis = Context.getService(PatientImageService.class);
		InputStream in = new FileInputStream(pis.getPath(patientId, pageId));
		return IOUtils.toByteArray(in);
	}
	
	public void create(int patientId, byte[] imageData) throws IOException {
		PatientImageService pis = Context.getService(PatientImageService.class);
		pis.createImage(patientId, imageData);
	}
	
	public void delete(int patientId, int pageId) throws IOException {
		PatientImageService pis = Context.getService(PatientImageService.class);
		PatientImage p = new PatientImage();
		p.setPatientId(patientId);
		p.setId(pageId);
		pis.deleteImage(p);
	}
	
	@Override
	public String getUri(Object arg0) {
		// Unused.
		return null;
	}
	
}
