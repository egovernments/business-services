package org.egov.demand.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.egov.demand.amendment.model.Amendment;
import org.egov.demand.amendment.model.Document;
import org.egov.demand.amendment.model.enums.AmendmentReason;
import org.egov.demand.amendment.model.enums.AmendmentStatus;
import org.egov.demand.model.DemandDetail;
import org.egov.demand.util.Util;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class AmendmentRowMapper implements ResultSetExtractor<List<Amendment>> {

	@Autowired
	private Util util;

	@Override
	public List<Amendment> extractData(ResultSet rs) throws SQLException, DataAccessException {

		Map<String, Amendment> amendmentMap = new LinkedHashMap<>();
		String amendmentIdRsName = "amendmentid";

		while (rs.next()) {

			String amendmentId = rs.getString(amendmentIdRsName);
			Amendment amendment = amendmentMap.get(amendmentId);

			if (amendment == null) {

				amendment = Amendment.builder()
						.additionalDetails(util.getJsonValue((PGobject) rs.getObject("additionaldetails")))
						.amendmentReason(AmendmentReason.fromValue(rs.getString("amendmentReason")))
						.status(AmendmentStatus.fromValue(rs.getString("amendmentstatus")))
						.reasonDocumentNumber(rs.getString("reasonDocumentNumber"))
						.amendedDemandId(rs.getString("amendedDemandId"))
						.businessService(rs.getString("businessService"))
						.consumerCode(rs.getString("consumerCode"))
						.effectiveFrom(rs.getLong("effectiveFrom"))
						.effectiveTill(rs.getLong("effectiveTill"))
						.tenantId(rs.getString("tenantId"))
						.id(rs.getString("amendmentuuid"))
						.amendmentId(amendmentId)
						.demandDetails(new ArrayList<>())
						/*
						 * Initiating with empty arrays because document and tax-details are mandatory
						 * for amendment
						 */
						.documents(new ArrayList<>()) 
						.build();
				
						
				amendmentMap.put(amendmentId, amendment);
			}

			addDemandDetailToAmendment(amendment, rs);
			addDocumentToAmendment(amendment, rs);
		}
		return new ArrayList<>(amendmentMap.values());
	}

	private void addDemandDetailToAmendment(Amendment amendment, ResultSet rs) throws SQLException {

		List<DemandDetail> taxDetails = amendment.getDemandDetails();
		String detailId = rs.getString("detailid");

		if (!CollectionUtils.isEmpty(taxDetails))
			for (DemandDetail detail : taxDetails) {
				if (detail.getId().equals(detailId))
					return;
			}
		
		DemandDetail amendmentDetail = DemandDetail.builder()
				.taxHeadMasterCode(rs.getString("taxheadcode"))
				.taxAmount(rs.getBigDecimal("taxAmount"))
				.id(detailId)
				.build();

		taxDetails.add(amendmentDetail);
	}

	private void addDocumentToAmendment(Amendment amendment, ResultSet rs) throws SQLException {

		List<Document> docs = amendment.getDocuments();
		String docId = rs.getString("docid");

		if (!CollectionUtils.isEmpty(docs))
			for (Document doc : docs) {
				if (doc.getId().equals(docId))
					return;
			}
		
		Document newDoc = Document.builder()
				.documentType(rs.getString("documentType"))
				.documentUid(rs.getString("documentUid"))
				.fileStore(rs.getString("fileStore"))
				.id(docId)
				.build();
		
		docs.add(newDoc);
	}

}
