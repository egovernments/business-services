package org.egov.demand.web.validator;

import static org.egov.demand.util.Constants.BUSINESSSERVICE_PATH_CODE;
import static org.egov.demand.util.Constants.TAXHEADMASTER_PATH_CODE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.egov.demand.amendment.model.Amendment;
import org.egov.demand.amendment.model.AmendmentCriteria;
import org.egov.demand.amendment.model.AmendmentRequest;
import org.egov.demand.amendment.model.AmendmentUpdate;
import org.egov.demand.amendment.model.AmendmentUpdateRequest;
import org.egov.demand.amendment.model.ProcessInstance;
import org.egov.demand.amendment.model.enums.AmendmentStatus;
import org.egov.demand.config.ApplicationProperties;
import org.egov.demand.model.Demand;
import org.egov.demand.model.DemandCriteria;
import org.egov.demand.model.DemandDetail;
import org.egov.demand.model.TaxHeadMaster;
import org.egov.demand.repository.AmendmentRepository;
import org.egov.demand.service.DemandService;
import org.egov.demand.util.Util;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;

@Component
public class AmendmentValidator {
	
	@Autowired
	private Util util;
	
	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private DemandService demandService;
	
	@Autowired
	private ApplicationProperties props;
	
	@Autowired
	private AmendmentRepository amendmentRepository;
	
	
	/**
	 * Validate amendment request for create
	 * @param amendmentRequest
	 */
	public void validateAmendmentForCreate(AmendmentRequest amendmentRequest) {

		Amendment amendment = amendmentRequest.getAmendment();
		DocumentContext mdmsData = util.getMDMSData(amendmentRequest.getRequestInfo(), amendmentRequest.getAmendment().getTenantId());
		
		/*
		 * Extracting the respective masters from DocumentContext 
		 * 
		 * Validating the master data fields - business-service and tax-heads
		 */
		List<String> businessServiceCodes = mdmsData.read(BUSINESSSERVICE_PATH_CODE);
		List<TaxHeadMaster> taxHeads = Arrays.asList(mapper.convertValue(mdmsData.read(TAXHEADMASTER_PATH_CODE), TaxHeadMaster[].class));
		Map<String, Set<String>> businessTaxCodeSet = taxHeads.stream().collect(Collectors.groupingBy(
				TaxHeadMaster::getService, Collectors.mapping(TaxHeadMaster::getCode, Collectors.toSet())));
		
		List<String> MissingTaxHeadCodes = new ArrayList<>();
		Map<String,String> errorMap = new HashMap<>();

		if (!businessServiceCodes.contains(amendment.getBusinessService())) {
			errorMap.put("EG_BS_AMENDMENT_BUSINESS_ERROR",
					"Business service not found for the given code : " + amendment.getBusinessService());
		}

		Set<String> taxheadcodes = businessTaxCodeSet.get(amendment.getBusinessService());

		if (!CollectionUtils.isEmpty(taxheadcodes)) {
			
			for (DemandDetail detail : amendment.getDemandDetails()) {
				if (!taxheadcodes.contains(detail.getTaxHeadMasterCode()))
					MissingTaxHeadCodes.add(detail.getTaxHeadMasterCode());
			}
			
			if (!MissingTaxHeadCodes.isEmpty()) {
				errorMap.put("EG_BS_AMENDMENT_TAXHEAD_ERROR",
						"Taxheads not found for the following codes : " + MissingTaxHeadCodes);
			}
		} else {
			errorMap.put("EG_BS_AMENDMENT_TAXHEAD_ERROR",
					"No taxheads found for the given business service : " + amendment.getBusinessService());
		}
	
		/*
		 * validating from and to periods
		 */
		if (amendment.getEffectiveFrom() == null && amendment.getEffectiveTill() != null) {
			
			errorMap.put("EG_BS_AMENDMENT_PERIOD_ERROR", "End period cannot be given without from period");
		} else if (amendment.getEffectiveFrom() != null && amendment.getEffectiveTill() != null
				&& amendment.getEffectiveFrom().compareTo(amendment.getEffectiveTill()) >= 0) {
			errorMap.put("EG_BS_AMENDMENT_PERIOD_ERROR",
					"From period cannot be greater or equal to end period in amendment");
		}
		
		if(!CollectionUtils.isEmpty(errorMap))
			throw new CustomException(errorMap);
		
		/*
		 * verifying the consumer-code presence in demand system
		 */
		DemandCriteria demandCriteria = DemandCriteria.builder()
		.tenantId(amendment.getTenantId())
		.businessService(amendment.getBusinessService())
		.consumerCode(new HashSet<>(Arrays.asList(amendment.getConsumerCode())))
		.build();
		
		List<Demand> demands = demandService.getDemands(demandCriteria, amendmentRequest.getRequestInfo());
		
		if (CollectionUtils.isEmpty(demands))
			throw new CustomException("EG_BS_AMENDMENT_CONSUMERCODE_ERROR",
					"No demands found in the system for the given consumer code, An amendment cannot be created without demands in the system.");
	}

	/**
	 * Validating search criteria
	 * 
	 */
	public void validateAmendmentCriteriaForSearch(AmendmentCriteria criteria) {

		// mobile-number integration
		Map<String, String> errorMap = new HashMap<>();

		/*
		 * validation consumer-code and business-service combination
		 */
		if (!ObjectUtils.isEmpty(criteria.getConsumerCode()) && ObjectUtils.isEmpty(criteria.getBusinessService()))
			errorMap.put("EG_BS_AMENDMENT_CRITERIA_ERROR",
					"Consumer codes require businesService as mandatory parameter for search");

		if (!CollectionUtils.isEmpty(errorMap))
			throw new CustomException(errorMap);
	}
	
	/**
	 * enrich amendment request for create
	 * @param amendmentRequest
	 */
	public void enrichAmendmentForCreate (AmendmentRequest amendmentRequest) {
		
		Amendment amendment = amendmentRequest.getAmendment();
		
		amendment.setId(UUID.randomUUID().toString());
		
		// get amendment ID format TODO FIXME
		amendment.setAmendmentId(UUID.randomUUID().toString());
		amendment.setAuditDetails(util.getAuditDetail(amendmentRequest.getRequestInfo()));
		
		amendment.getDemandDetails().forEach(detail -> {
			detail.setId(UUID.randomUUID().toString());
		});
		
		amendment.getDocuments().forEach(doc -> {
			doc.setId(UUID.randomUUID().toString());
		});
		
		/*
		 * enrich workflow fields if enabled
		 */
		if (props.getIsAmendmentworkflowEnabed()) {

			ProcessInstance processInstance = ProcessInstance.builder()
					.moduleName(props.getAmendmentWfModuleName())
					.businessService(props.getAmendmentWfName())
					.action(props.getAmendmentWfOpenAction())
					.tenantId(amendment.getTenantId())
					.id(amendment.getAmendmentId())
					.build();
			
			amendment.setWorkflow(processInstance);
		} else {
			amendment.setStatus(AmendmentStatus.ACTIVE);
		}
		
	}

	/**
	 * Validating for update 
	 * @param amendmentUpdateRequest
	 */
	public void validateAndEnrichAmendmentForUpdate(@Valid AmendmentUpdateRequest amendmentUpdateRequest, Boolean isRequestForWorkflowUpdate) {
		
		Map<String,String> errorMap = new HashMap<>();
		AmendmentUpdate amendmentUpdate = amendmentUpdateRequest.getAmendmentUpdate();
		
		/*
		 * checking for amendment in system
		 */
		List<Amendment> amendments = amendmentRepository.getAmendments(amendmentUpdate.toSearchCriteria());
		if(CollectionUtils.isEmpty(amendments))
			errorMap.put("EG_BS_AMENDMENT_UPDATE_ERROR", "No Amendment found in the system for the given amendmentId, Please provide valid id for update");

		/*
		 * validating workflow fields
		 */
		ProcessInstance workflow = amendmentUpdate.getWorkflow();
		if (isRequestForWorkflowUpdate && props.getIsAmendmentworkflowEnabed() 
				&& workflow.getAction() == null
				|| workflow.getBusinessId() == null 
				|| workflow.getBusinessService() == null
				|| workflow.getModuleName() == null) {
			
			errorMap.put("EG_BS_AMENDMENT_UPDATE_WF_ERROR",
					"Mandatory workflow fileds missing in the update request, Please add all the following fields module, businessservice, businessid and action");
		}
		
		if (!CollectionUtils.isEmpty(errorMap))
			throw new CustomException(errorMap);
		
		/*
		 * enriching the update object
		 */
		amendmentUpdate.setAdditionalDetails(util.jsonMerge(amendments.get(0).getAdditionalDetails(), amendmentUpdate.getAdditionalDetails()));
		amendmentUpdate.setAuditDetails(util.getAuditDetail(amendmentUpdateRequest.getRequestInfo()));
	}
	
}
