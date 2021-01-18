/*
 * eGov suite of products aim to improve the internal efficiency,transparency,
 *    accountability and the service delivery of the government  organizations.
 *
 *     Copyright (C) <2015>  eGovernments Foundation
 *
 *     The updated version of eGov suite of products as by eGovernments Foundation
 *     is available at http://www.egovernments.org
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see http://www.gnu.org/licenses/ or
 *     http://www.gnu.org/licenses/gpl.html .
 *
 *     In addition to the terms of the GPL license to be adhered to in using this
 *     program, the following additional terms are to be complied with:
 *
 *         1) All versions of this program, verbatim or modified must carry this
 *            Legal Notice.
 *
 *         2) Any misrepresentation of the origin of the material is prohibited. It
 *            is required that all modified versions of this material be marked in
 *            reasonable ways as different from the original version.
 *
 *         3) This license does not grant any rights to any user of the program
 *            with regards to rights under trademark law for use of the trade names
 *            or trademarks of eGovernments Foundation.
 *
 *   In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */
package org.egov.demand.web.controller;

import java.util.List;

import javax.validation.Valid;

import org.egov.common.contract.request.RequestInfo;
import org.egov.demand.amendment.model.Amendment;
import org.egov.demand.amendment.model.AmendmentCriteria;
import org.egov.demand.amendment.model.AmendmentRequest;
import org.egov.demand.amendment.model.AmendmentResponse;
import org.egov.demand.service.AmendmentService;
import org.egov.demand.web.contract.RequestInfoWrapper;
import org.egov.demand.web.contract.factory.ResponseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/amendment")
@Slf4j
public class AmendmentController {

	

	@Autowired
	private AmendmentService amendmentService;

	@Autowired
	private ResponseFactory responseFactory;
	
	@PostMapping("_search")
	public ResponseEntity<?> search(@RequestBody RequestInfoWrapper requestInfoWrapper,
			@ModelAttribute @Valid AmendmentCriteria amendmentCriteria) {

		RequestInfo requestInfo = requestInfoWrapper.getRequestInfo();
		List<Amendment> amendments = amendmentService.search(amendmentCriteria);
		AmendmentResponse response = AmendmentResponse.builder().amendments(amendments)
				.responseInfo(responseFactory.getResponseInfo(requestInfo, HttpStatus.OK)).build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	/**
	 * API to create demands
	 *
	 * @param demandRequest
	 * @return ResponseEntity<?>
	 */

	@PostMapping("_create")
	@ResponseBody
	public ResponseEntity<?> create(@RequestBody @Valid AmendmentRequest amendmentRequest) {

		AmendmentResponse amendmentResponse = amendmentService.create(amendmentRequest);
		return new ResponseEntity<>(amendmentResponse, HttpStatus.CREATED);
	}

//	@PostMapping("_update")
//	public ResponseEntity<?> update(@RequestHeader HttpHeaders headers, @RequestBody @Valid DemandRequest demandRequest) {
//
//		return new ResponseEntity<>(demandService.updateAsync(demandRequest, null), HttpStatus.CREATED);
//	}


}