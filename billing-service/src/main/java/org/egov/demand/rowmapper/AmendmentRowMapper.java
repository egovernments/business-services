package org.egov.demand.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.egov.demand.amendment.model.Amendment;
import org.egov.demand.util.Util;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class AmendmentRowMapper implements ResultSetExtractor<List<Amendment>> {

	@Autowired
	private ObjectMapper mapper;
	
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

				amendment = mapper.convertValue(util.getJsonValue((PGobject) rs.getObject("additionaldetails")), Amendment.class);
				amendmentMap.put(amendmentId, amendment);
			}
			// sub objects TODO
		}
		return new ArrayList<>(amendmentMap.values());
	}
}
