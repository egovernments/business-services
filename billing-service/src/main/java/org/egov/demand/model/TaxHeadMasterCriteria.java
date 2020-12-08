package org.egov.demand.model;

import java.util.HashSet;
import java.util.Set;

import javax.validation.constraints.NotNull;

import lombok.*;
import lombok.Builder.Default;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@EqualsAndHashCode
public class TaxHeadMasterCriteria {

	@NotNull
	private String tenantId;
	@NotNull
	private String service;
	private String category;
	private String name;
	
	@Default
	private Set<String> code=new HashSet<>();
	private Boolean isDebit;
	private Boolean isActualDemand;
	
	@Default
	private Set<String> id=new HashSet<>();

}
