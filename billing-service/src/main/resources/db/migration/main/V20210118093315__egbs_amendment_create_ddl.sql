CREATE TABLE EGBS_AMENDMENT(

id CHARACTER VARYING(128),
tenantid character varying(256) NOT NULL,
amendmentId CHARACTER VARYING(256) NOT NULL,
businessService CHARACTER VARYING(256),
consumercode CHARACTER VARYING(256),	
amendmentReason CHARACTER VARYING(256),
reasonDocumentNumber CHARACTER VARYING(256),
status CHARACTER VARYING(256),
effectiveTill CHARACTER VARYING(256),
effectiveFrom CHARACTER VARYING(256),
amendedDemandId CHARACTER VARYING(256),
createdby character varying(256) NOT NULL,
createdtime bigint NOT NULL,
lastmodifiedby character varying(256),
lastmodifiedtime bigint,
additionaldetails jsonb,
CONSTRAINT pk_egbs_amendment PRIMARY KEY (amendmentId, tenantid),
CONSTRAINT uk_egbs_amendment UNIQUE (id)

);
