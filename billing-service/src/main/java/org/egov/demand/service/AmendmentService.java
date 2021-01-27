package org.egov.demand.service;

import java.util.List;

import org.egov.demand.amendment.model.Amendment;
import org.egov.demand.amendment.model.AmendmentCriteria;
import org.egov.demand.amendment.model.AmendmentRequest;
import org.egov.demand.amendment.model.AmendmentUpdate;
import org.egov.demand.amendment.model.AmendmentUpdateRequest;
import org.egov.demand.amendment.model.State;
import org.egov.demand.amendment.model.enums.AmendmentStatus;
import org.egov.demand.config.ApplicationProperties;
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
	private ApplicationProperties props;
	
	@Autowired
	private AmendmentValidator amendmentValidator;
	
	@Autowired
	private AmendmentRepository amendmentRepository;
	
	public Amendment create(AmendmentRequest amendmentRequest) {
		
		amendmentValidator.validateAmendmentForCreate(amendmentRequest);
		amendmentValidator.enrichAmendmentForCreate(amendmentRequest);
		if (props.getIsAmendmentworkflowEnabed()) {
			
			Amendment amendment = amendmentRequest.getAmendment();
			State state = util.callWorkFlow(amendment.getWorkflow(), amendmentRequest.getRequestInfo());
			amendment.setStatus(AmendmentStatus.fromValue(state.getState()));
		}
		amendmentRepository.saveAmendment(amendmentRequest);
		
		return amendmentRequest.getAmendment();
	}
	
	
	public List<Amendment> search(AmendmentCriteria amendmentCriteria) {
		
		amendmentValidator.validateAmendmentCriteriaForSearch(amendmentCriteria);
		return amendmentRepository.getAmendments(amendmentCriteria);
	}

	/**
	 * 
	 * @param amendmentUpdateRequest
	 * @param isRequestForWorkflowUpdate
	 * @return
	 */
	public Amendment updateAmendment(AmendmentUpdateRequest amendmentUpdateRequest, Boolean isRequestForWorkflowUpdate) {

		String resultantStatus = null;
		AmendmentUpdate amendmentUpdate = amendmentUpdateRequest.getAmendmentUpdate();
		amendmentValidator.validateAndEnrichAmendmentForUpdate(amendmentUpdateRequest, isRequestForWorkflowUpdate);
		
		if (isRequestForWorkflowUpdate) {

			State resultantState = util.callWorkFlow(amendmentUpdate.getWorkflow(), amendmentUpdateRequest.getRequestInfo());
			amendmentUpdate.getWorkflow().setState(resultantState);
			resultantStatus = resultantState.getState();
		} else {
			resultantStatus = AmendmentStatus.CONSUMED.toString();
		}
		
		amendmentRepository.updateAmendment(amendmentUpdate, resultantStatus);
		
		if(isRequestForWorkflowUpdate && resultantStatus.equalsIgnoreCase(AmendmentStatus.ACTIVE.toString())) {
			// trigger demand update with new demand-details
		}
		return search(amendmentUpdate.toSearchCriteria()).get(0);
	}

}
