ALTER TABLE egbs_DEMAND_V1 DROP CONSTRAINT uk_egbs_demand_v1_consumercode_businessservice;

CREATE UNIQUE INDEX uk_egbs_demand_v1_consumercode_businessservice ON egbs_DEMAND_V1 (consumercode, tenantid, taxperiodfrom, taxperiodto, businessservice, status) where status='ACTIVE';