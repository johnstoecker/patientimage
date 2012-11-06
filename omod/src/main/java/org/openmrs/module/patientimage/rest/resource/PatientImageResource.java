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
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.patientimage.PatientImage;
import org.openmrs.module.patientimage.servlet.PatientImageService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.Resource;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.PatientImageResource;

@Resource("patientimage")
@Handler(supports = PatientImage.class, order = -1)
public class PatientImageResource extends DataDelegatingCrudResource<PatientImage> {
        
        /**
         * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#getRepresentationDescription(org.openmrs.module.webservices.rest.web.representation.Representation)
         */
        @Override
        public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
                if (rep instanceof DefaultRepresentation) {
                        DelegatingResourceDescription description = new DelegatingResourceDescription();
                        description.addProperty("uuid");
                        description.addProperty("display", findMethod("getDisplayString"));
                        description.addProperty("patientImageDatetime");
                        description.addProperty("patient", Representation.REF);
                        description.addProperty("location", Representation.REF);
                        description.addProperty("form", Representation.REF);
                        description.addProperty("patientImageType", Representation.REF);
                        description.addProperty("provider", Representation.REF);
                        description.addProperty("obs", Representation.REF);
                        description.addProperty("orders", Representation.REF);
                        description.addProperty("voided");
                        description.addSelfLink();
                        description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
                        return description;
                } else if (rep instanceof FullRepresentation) {
                        DelegatingResourceDescription description = new DelegatingResourceDescription();
                        description.addProperty("uuid");
                        description.addProperty("display", findMethod("getDisplayString"));
                        description.addProperty("patientImageDatetime");
                        description.addProperty("patient", Representation.REF);
                        description.addProperty("location");
                        description.addProperty("form");
                        description.addProperty("patientImageType");
                        description.addProperty("provider");
                        description.addProperty("obs");
                        description.addProperty("orders");
                        description.addProperty("voided");
                        description.addProperty("auditInfo", findMethod("getAuditInfo"));
                        description.addSelfLink();
                        return description;
                }
                return null;
        }
        
        /**
         * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getCreatableProperties()
         * @should create an patientImage type
         */
        @Override
        public DelegatingResourceDescription getCreatableProperties() {
                DelegatingResourceDescription description = new DelegatingResourceDescription();
                
                description.addRequiredProperty("patientImageDatetime");
                description.addRequiredProperty("patient");
                description.addRequiredProperty("patientImageType");
                
                description.addProperty("location");
                description.addProperty("form");
                description.addProperty("provider");
                description.addProperty("orders");
                description.addProperty("obs");
                
                return description;
        }
        
        /**
         * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#save(org.openmrs.PatientImage)
         */
        @Override
        public PatientImage save(PatientImage enc) {
                return Context.getService(PatientImage.class).savePatientImage(enc);
        }
        
        /**
         * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#getByUniqueId(java.lang.String)
         */
        @Override
        public PatientImage getByUniqueId(String uuid) {
                return Context.getService(PatientImage.class).getPatientImageByUuid(uuid);
        }
        
        /**
         * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#delete(org.openmrs.PatientImage, 
         * java.lang.String, 
         * org.openmrs.module.webservices.rest.web.RequestContext)
         */
        @Override
        public void delete(PatientImage enc, String reason, RequestContext context) throws ResponseException {
                if (enc.isVoided()) {
                        // DELETE is idempotent, so we return success here
                        return;
                }
                Context.getService(PatientImage.class).voidPatientImage(enc, reason);
        }
        
        /**
         * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#purge(org.openmrs.PatientImage,
         * org.openmrs.module.webservices.rest.web.RequestContext)
         */
        @Override
        public void purge(PatientImage enc, RequestContext context) throws ResponseException {
                if (enc == null) {
                        // DELETE is idempotent, so we return success here
                        return;
                }
                Context.getPatientImageService().purgePatientImage(enc);
        }
        
        /**
         * @param patientImage
         * @return patientImage type and date
         */
        public String getDisplayString(PatientImage patientImage) {
                String ret = patientImage == null ? "?" : patientImage.getPatientImageType().getName();
                ret += " ";
                ret += patientImage.getPatientImageDatetime() == null ? "?" : Context.getDateFormat().format(
                    patientImage.getPatientImageDatetime());
                return ret;
        }
        
        /**
         * Gets patientImages for the given patient (paged according to context if necessary)
         * 
         * @param patientUniqueId @see {@link PatientResource#getByUniqueId(String)} for interpretation
         * @param context
         * @return
         * @throws ResponseException 
         */
        public SimpleObject getPatientImagesByPatient(String patientUniqueId, RequestContext context) throws ResponseException {
                Patient patient = Context.getService(RestService.class).getResource(PatientResource.class).getByUniqueId(
                    patientUniqueId);
                if (patient == null)
                        throw new ObjectNotFoundException();
                List<PatientImage> encs = Context.getPatientImageService().getPatientImagesByPatient(patient);
                return new NeedsPaging<PatientImage>(encs, context).toSimpleObject();
        }
        
        /**
         * @param instance
         * @return all non-voided top-level obs from the given patientImage
         */
        @PropertyGetter("obs")
        public static Object getObsAtTopLevel(PatientImage instance) {
                return instance.getObsAtTopLevel(false);
        }
        
        @PropertySetter("obs")
        public static void setObs(PatientImage instance, Set<Obs> obs) {
                for (Obs o : obs)
                        instance.addObs(o);
        }
        
        @PropertySetter("order")
        public static void setOrders(PatientImage instance, Set<Order> orders) {
                for (Order o : orders)
                        instance.addOrder(o);
        }
        
        /**
         * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doSearch(java.lang.String,
         *      org.openmrs.module.webservices.rest.web.RequestContext)
         */
        @Override
        protected AlreadyPaged<PatientImage> doSearch(String query, RequestContext context) {
                return new ServiceSearcher<PatientImage>(PatientImageService.class, "getPatientImages", "getCountOfPatientImages").search(query,
                    context);
        }
        
        
    //	/**
//	 * @param patientId
//	 * @param pageId
//	 * @return byte[] of image data.
//	 * @throws ResponseException
//	 * @throws IOException
//	 */
//	public byte[] retrieve(int patientId, int pageId) throws IOException {
//		InputStream in = new FileInputStream(PatientImageService.getImagePath(patientId, pageId));
//		return IOUtils.toByteArray(in);
//	}
//	
//	@Override
//	public String getUri(Object arg0) {
//		// Unused.
//		return null;
//	}
//	
}
