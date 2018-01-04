/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.evidence.negativecontrols;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author asena5
 */
public class NegativeControlMapper implements RowMapper<NegativeControlRecord> {
	
        @Override
	public NegativeControlRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
		NegativeControlRecord record = new NegativeControlRecord();
		record.setEvidenceJobId(rs.getLong("JOB_ID"));
		record.setSourceId(rs.getInt("SOURCE_ID"));
		record.setConceptSetId(rs.getInt("CONCEPT_SET_ID"));
		record.setConceptSetName(rs.getString("CONCEPT_SET_NAME"));
		record.setNegativeControl(rs.getInt("NEGATIVE_CONTROL"));
		record.setConceptId(rs.getInt("CONCEPT_ID"));
		record.setConceptName(rs.getString("CONCEPT_NAME"));
		record.setDomainId(rs.getString("DOMAIN_ID"));
		record.setPersonCountRc(rs.getLong("PERSON_COUNT_RC"));
		record.setPersonCountDc(rs.getLong("PERSON_COUNT_DC"));
		record.setDescendantPmidCount(rs.getInt("DESCENDANT_PMID_COUNT"));
		record.setExactPmidCount(rs.getInt("EXACT_PMID_COUNT"));
		record.setParentPmidCount(rs.getInt("PARENT_PMID_COUNT"));
		record.setAncestorPmidCount(rs.getInt("ANCESTOR_PMID_COUNT"));
		record.setIndication(rs.getInt("INDICATION"));
		record.setTooBroad(rs.getInt("TOO_BROAD"));
		record.setDrugInduced(rs.getInt("DRUG_INDUCED"));
		record.setPregnancy(rs.getInt("PREGNANCY"));
		record.setSplicer(rs.getInt("SPLICER"));
		record.setFaers(rs.getInt("FAERS"));
		record.setUserExcluded(rs.getInt("USER_EXCLUDED"));
		record.setUserIncluded(rs.getInt("USER_INCLUDED"));
                
		return record;
	}
    
}
