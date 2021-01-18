package org.egov.demand.amendment.model;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.egov.demand.model.AuditDetails;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The update object which carries the workflow action info along with the
 * amendment id
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmendmentUpdate {

	@NotNull
	@JsonProperty("amendmentId")
	private String amendmentId;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;

	@JsonProperty("additionalDetails")
	private Object additionalDetails;

	@JsonProperty("workflow")
	private ProcessInstance workflow;

	@JsonProperty("documents")
	@Valid
	private List<Document> documents;

}
