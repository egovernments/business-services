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
	

	public void validateAmendmentForCreate(AmendmentRequest amendmentRequest) {

		Amendment amendment = amendmentRequest.getAmendment();
		DocumentContext mdmsData = util.getMDMSData(amendmentRequest.getRequestInfo(), amendmentRequest.getAmendment().getTenantId());
		
		/*
		 * Extracting the respective masters from DocumentContext 
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
		
		if (amendment.getEffectiveFrom() != null && amendment.getEffectiveTill() != null
				&& amendment.getEffectiveFrom().compareTo(amendment.getEffectiveTill()) >= 0) {
			errorMap.put("EG_BS_AMENDMENT_PERIOD_ERROR",
					"From period cannot be greater or equal to end period in amendment");
		}
		
		if(!CollectionUtils.isEmpty(errorMap))
			throw new CustomException(errorMap);
		
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

	public void validateAmendmentCriteriaForSearch(AmendmentCriteria criteria) {

		// mobile-number integration
		Map<String, String> errorMap = new HashMap<>();

		if (!ObjectUtils.isEmpty(criteria.getConsumerCode()) && ObjectUtils.isEmpty(criteria.getBusinessService()))
			errorMap.put("EG_BS_AMENDMENT_CRITERIA_ERROR",
					"Consumer codes require businesService as mandatory parameter for search");

		if (!CollectionUtils.isEmpty(errorMap))
			throw new CustomException(errorMap);
	}
	
	public void enrichAmendmentForCreate (AmendmentRequest amendmentRequest) {
		
		Amendment amendment = amendmentRequest.getAmendment();
		
		amendment.setId(UUID.randomUUID().toString());
		
		// get amendment ID format
		amendment.setAmendmentId(UUID.randomUUID().toString());
		amendment.setAuditDetails(util.getAuditDetail(amendmentRequest.getRequestInfo()));
		
		amendment.getDemandDetails().forEach(detail -> {
			detail.setId(UUID.randomUUID().toString());
		});
		
		amendment.getDocuments().forEach(doc -> {
			doc.setId(UUID.randomUUID().toString());
		});
		
		if (props.getIsAmendmentworkflowEnabed()) {

			ProcessInstance processInstance = ProcessInstance.builder()
					.action(null)
					.businessService(null)
					.moduleName(amendment.getBusinessService())
					.tenantId(amendment.getTenantId())
					.build();
			
			amendment.setWorkflow(processInstance);
		}
		
	}

	public void validateAndEnrichAmendmentForUpdate(@Valid AmendmentUpdateRequest amendmentUpdateRequest) {
		
		Map<String,String> errorMap = new HashMap<>();
		AmendmentUpdate amendmentUpdate = amendmentUpdateRequest.getAmendmentUpdate();
		
		List<Amendment> amendments = amendmentRepository.getAmendments(amendmentUpdate.toSearchCriteria());
		
		if(CollectionUtils.isEmpty(amendments))
			errorMap.put("EG_BS_AMENDMENT_UPDATE_ERROR", "No Amendment found in the system for the given amendmentId, Please provide valid id for update");

		ProcessInstance workflow = amendmentUpdate.getWorkflow();
		if (props.getIsAmendmentworkflowEnabed() && workflow.getAction() == null || workflow.getBusinessId() == null
				|| workflow.getBusinessService() == null || workflow.getModuleName() == null) {
			errorMap.put("EG_BS_AMENDMENT_UPDATE_WF_ERROR",
					"Mandatory workflow fileds missing in the update request, Please add all the following fields module, businessservice, businessid and action");
		}
		
		if (!CollectionUtils.isEmpty(errorMap))
			throw new CustomException(errorMap);
		
		amendmentUpdate.setAdditionalDetails(util.jsonMerge(amendments.get(0).getAdditionalDetails(), amendmentUpdate.getAdditionalDetails()));
		amendmentUpdate.setAuditDetails(util.getAuditDetail(amendmentUpdateRequest.getRequestInfo()));
	}
	
}
