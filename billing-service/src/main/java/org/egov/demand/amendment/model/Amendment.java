package org.egov.demand.amendment.model;

import java.math.BigDecimal;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.egov.demand.amendment.model.enums.AmendmentReason;
import org.egov.demand.amendment.model.enums.AmendmentStatus;
import org.egov.demand.model.AuditDetails;
import org.egov.demand.model.DemandDetail;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A Object which holds the basic info about the revenue assessment for which
 * the demand is generated like module name, consumercode, owner, etc.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Amendment {

	@JsonProperty("id")
	private String id;

	@JsonProperty("demandIds")
	@Valid
	private List<String> demandIds;

	@JsonProperty("amendedDemandId")
	private String amendedDemandId;

	@NotNull
	@JsonProperty("tenantId")
	private String tenantId;

	@NotNull
	@JsonProperty("consumerCode")
	private String consumerCode;

	@JsonProperty("amendmentId")
	private String amendmentId;

	@NotNull
	@JsonProperty("businessService")
	private String businessService;

	@NotNull
	@JsonProperty("amendmentReason")
	private AmendmentReason amendmentReason;

	@JsonProperty("reasonDocumentNumber")
	private String reasonDocumentNumber;

	@JsonProperty("status")
	private AmendmentStatus status;

	@JsonProperty("workflow")
	private ProcessInstance workflow;

	@JsonProperty("demandDetails")
	@Valid
	@NotNull
	private List<DemandDetail> demandDetails;

	@NotNull
	@JsonProperty("documents")
	@Valid
	private List<Document> documents;

	@JsonProperty("effectiveFrom")
	private BigDecimal effectiveFrom;

	@JsonProperty("effectiveTill")
	private BigDecimal effectiveTill;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;

	@JsonProperty("additionalDetails")
	private Object additionalDetails;

}
