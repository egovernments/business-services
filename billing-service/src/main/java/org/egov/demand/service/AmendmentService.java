package org.egov.demand.service;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.egov.demand.amendment.model.Amendment;
import org.egov.demand.amendment.model.AmendmentCriteria;
import org.egov.demand.amendment.model.AmendmentRequest;
import org.egov.demand.amendment.model.AmendmentResponse;
import org.egov.demand.model.AuditDetails;
import org.egov.demand.rowmapper.AmendmentRowMapper;
import org.egov.demand.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Service;

@Service
public class AmendmentService {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private AmendmentRowMapper amendmentRowMapper;
	
	@Autowired
	private Util util;

	public AmendmentResponse create(AmendmentRequest amendmentRequest) {
		
		Amendment amendment = amendmentRequest.getAmendment();
		amendment.setId(UUID.randomUUID().toString());
		amendment.setAmendmentId( UUID.randomUUID().toString());
		String insertSql = "INSERT INTO egbs_amendment(id,amendmentid,consumercode,createdtime,createdby,tenantid,additionaldetails) VALUES (?,?,?,?,?,?,?);";

		AuditDetails auditDetails = util.getAuditDetail(amendmentRequest.getRequestInfo());
		jdbcTemplate.update(insertSql, new PreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps) throws SQLException {

				ps.setString(1, amendment.getId());
				ps.setString(2, amendment.getAmendmentId());
				ps.setString(3, amendment.getConsumerCode());
				ps.setLong(4, auditDetails.getCreatedTime());
				ps.setString(5, auditDetails.getCreatedBy());
				ps.setString(6, amendment.getTenantId());
				ps.setObject(7, util.getPGObject(amendment));

			}
		});
		return AmendmentResponse.builder().amendments(Arrays.asList(amendment)).build();
	}
	
	
	public List<Amendment> search(AmendmentCriteria amendmentCriteria) {
		
		String searchQuery = "select * from egbs_amendment where amendmentid=? and tenantid=?";
		Object[] psValues = new Object[]{amendmentCriteria.getAmendmentId(), amendmentCriteria.getTenantId()}; 
		List<Amendment> amendments = jdbcTemplate.query(searchQuery, psValues, amendmentRowMapper);
		return amendments;
	}
}
