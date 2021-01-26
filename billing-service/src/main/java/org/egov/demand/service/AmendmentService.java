package org.egov.demand.service;

import java.util.List;

import javax.validation.Valid;

import org.egov.demand.amendment.model.Amendment;
import org.egov.demand.amendment.model.AmendmentCriteria;
import org.egov.demand.amendment.model.AmendmentRequest;
import org.egov.demand.amendment.model.AmendmentUpdate;
import org.egov.demand.amendment.model.AmendmentUpdateRequest;
import org.egov.demand.amendment.model.State;
import org.egov.demand.repository.AmendmentRepository;
import org.egov.demand.util.Util;
import org.egov.demand.web.validator.AmendmentValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AmendmentService {
	
	@Autowired
	private Util util;
	
	@Autowired
	private AmendmentValidator amendmentValidator;
	
	@Autowired
	private AmendmentRepository amendmentRepository;
	
	public Amendment create(AmendmentRequest amendmentRequest) {
		
		amendmentValidator.validateAmendmentForCreate(amendmentRequest);
		amendmentValidator.enrichAmendmentForCreate(amendmentRequest);
		amendmentRepository.saveAmendment(amendmentRequest);
		
		return amendmentRequest.getAmendment();
	}
	
	
	public List<Amendment> search(AmendmentCriteria amendmentCriteria) {
		return amendmentRepository.getAmendments(amendmentCriteria);
	}


	public Amendment updateAmendment(@Valid AmendmentUpdateRequest amendmentUpdateRequest) {

		AmendmentUpdate amendmentUpdate = amendmentUpdateRequest.getAmendmentUpdate();
		amendmentValidator.validateAndEnrichAmendmentForUpdate(amendmentUpdateRequest);
		State resultantState = util.callWorkFlow(amendmentUpdate.getWorkflow(), amendmentUpdateRequest.getRequestInfo());
		amendmentUpdate.getWorkflow().setState(resultantState);
		amendmentRepository.updateAmendment(amendmentUpdate, resultantState.getState());
		// trigger demand update with new demand-details
		return search(amendmentUpdate.toSearchCriteria()).get(0);
	}
}
