package org.egov.demand.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.egov.common.contract.request.RequestInfo;
import org.egov.demand.amendment.model.Amendment;
import org.egov.demand.amendment.model.AmendmentCriteria;
import org.egov.demand.amendment.model.AmendmentRequest;
import org.egov.demand.amendment.model.AmendmentUpdate;
import org.egov.demand.amendment.model.AmendmentUpdateRequest;
import org.egov.demand.amendment.model.State;
import org.egov.demand.amendment.model.enums.AmendmentStatus;
import org.egov.demand.config.ApplicationProperties;
import org.egov.demand.model.AuditDetails;
import org.egov.demand.model.Demand;
import org.egov.demand.model.DemandCriteria;
import org.egov.demand.repository.AmendmentRepository;
import org.egov.demand.util.Util;
import org.egov.demand.web.contract.DemandRequest;
import org.egov.demand.web.validator.AmendmentValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class AmendmentService {
	
	@Autowired
	private Util util;
	
	@Autowired
	private ApplicationProperties props;
	
	@Autowired
	private DemandService demandService;
	
	@Autowired
	private AmendmentValidator amendmentValidator;
	
	@Autowired
	private AmendmentRepository amendmentRepository;
	
	/**
	 * Search amendment based on criteria
	 * 
	 * @param amendmentCriteria
	 */
	public List<Amendment> search(AmendmentCriteria amendmentCriteria, RequestInfo requestInfo) {

		amendmentValidator.validateAmendmentCriteriaForSearch(amendmentCriteria);
		if (amendmentCriteria.getMobileNumber() != null) {

			DemandCriteria demandCriteria = DemandCriteria.builder()
					.mobileNumber(amendmentCriteria.getMobileNumber())
					.tenantId(amendmentCriteria.getTenantId())
					.build();
			
			List<Demand> demands = demandService.getDemands(demandCriteria, requestInfo);
			if (!CollectionUtils.isEmpty(demands)) {
				amendmentCriteria.getConsumerCode()
						.addAll(demands.stream().map(Demand::getConsumerCode).collect(Collectors.toSet()));
			}
		}
		return amendmentRepository.getAmendments(amendmentCriteria);
	}
	
	/**
	 * 
	 * @param amendmentRequest
	 */
	public Amendment create(AmendmentRequest amendmentRequest) {
		
		RequestInfo requestInfo = amendmentRequest.getRequestInfo();
		Amendment amendment = amendmentRequest.getAmendment();
		
		amendmentValidator.validateAmendmentForCreate(amendmentRequest);
		amendmentValidator.enrichAmendmentForCreate(amendmentRequest);
		if (props.getIsAmendmentworkflowEnabed()) {
			
			State state = util.callWorkFlow(amendment.getWorkflow(), requestInfo);
			amendment.setStatus(AmendmentStatus.fromValue(state.getApplicationStatus()));
		}
		amendmentRepository.saveAmendment(amendmentRequest);
		if (!props.getIsAmendmentworkflowEnabed()) {
			updateDemandWithAmendmentTax(requestInfo, amendment);
		}
		return amendmentRequest.getAmendment();
	}
	
	
	/**
	 * update method for amendment, used only with workflow. if workflow is not available then method is not called
	 * 
	 * @param amendmentUpdateRequest
	 * @param isRequestForWorkflowUpdate
	 */
	public Amendment updateAmendment(AmendmentUpdateRequest amendmentUpdateRequest) {
		
		RequestInfo requestInfo = amendmentUpdateRequest.getRequestInfo();
		AmendmentUpdate amendmentUpdate = amendmentUpdateRequest.getAmendmentUpdate();
		Amendment amendmentFromSearch = amendmentValidator.validateAndEnrichAmendmentForUpdate(amendmentUpdateRequest);
		
		/*
		 * Workflow update
		 */
		if (props.getIsAmendmentworkflowEnabed()) {
			State resultantState = util.callWorkFlow(amendmentUpdate.getWorkflow(), requestInfo);
			amendmentUpdate.getWorkflow().setState(resultantState);
			amendmentUpdate.setStatus(AmendmentStatus.fromValue(resultantState.getApplicationStatus()));
		}
		
		/*
		 * amendment update 
		 */
		amendmentRepository.updateAmendment(Arrays.asList(amendmentUpdate));
		
		if (amendmentUpdate.getStatus().equals(AmendmentStatus.ACTIVE)) {
			updateDemandWithAmendmentTax(requestInfo, amendmentFromSearch);
		}
		return search(amendmentUpdate.toSearchCriteria(), requestInfo).get(0);
	}


	/**
	 * Method to update demand after an amendment is ACTIVE
	 * 
	 * if no demands found then ignored
	 * 
	 * @param requestInfo
	 * @param amendment
	 */
	public void updateDemandWithAmendmentTax(RequestInfo requestInfo, Amendment amendment) {
		
		
		DemandCriteria demandCriteria = DemandCriteria.builder()
				.consumerCode(Stream.of(amendment.getConsumerCode()).collect(Collectors.toSet()))
				.businessService(amendment.getBusinessService())
				.tenantId(amendment.getTenantId())
				.isPaymentCompleted(false)
				.build();
		
		List<Demand> demands = demandService.getDemands(demandCriteria, requestInfo);
		if(!CollectionUtils.isEmpty(demands)) {
			
			AuditDetails auditDetails = util.getAuditDetail(requestInfo);
			if (demands.size() > 1)
				Collections.sort(demands, Comparator.comparing(Demand::getTaxPeriodFrom)
						.thenComparing(Demand::getTaxPeriodTo).reversed());
			Demand demand = demands.get(0);
			amendment.getDemandDetails().forEach(detail -> {
			
				detail.setAuditDetails(auditDetails);
				detail.setDemandId(demand.getId());
				detail.setTenantId(demand.getTenantId());
			});
			demand.getDemandDetails().addAll(amendment.getDemandDetails());
			demandService.update(new DemandRequest(requestInfo, Arrays.asList(demand)), null);
			
			AmendmentUpdate amendmentUpdate = AmendmentUpdate.builder()
					.additionalDetails(amendment.getAdditionalDetails())
					.amendmentId(amendment.getAmendmentId())
					.tenantId(amendment.getTenantId())
					.status(AmendmentStatus.CONSUMED)
					.amendedDemandId(demand.getId())
					.auditDetails(auditDetails)
					.build();
			
			amendmentRepository.updateAmendment(Arrays.asList(amendmentUpdate));
		}
	}

}
