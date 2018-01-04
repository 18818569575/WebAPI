/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.evidence.negativecontrols;

import java.io.Serializable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author asena5
 */
@Entity(name = "NegativeControlRecord")
@Table(name = "CONCEPT_SET_NEGATIVE_CONTROLS")
public class NegativeControlRecord implements Serializable {

    @Id
    @GeneratedValue  
    @Access(AccessType.PROPERTY) 
    @Column(name = "id")
    private int id;
    
    @Column(name = "evidence_job_id")
    private Long evidenceJobId;
		
    @Column(name = "source_id")
    private int sourceId;
		
    @Column(name = "concept_set_id")
    private int conceptSetId;

    @Column(name = "concept_set_name")
    private String conceptSetName;

		@Column(name = "negative_control")
		private int negativeControl;

    @Column(name = "concept_id")
    private int conceptId;

    @Column(name = "concept_name")
    private String conceptName;

    @Column(name = "domain_id")
    private String domainId;

    @Column(name = "person_count_rc")
    private Long personCountRc;

    @Column(name = "person_count_dc")
    private Long personCountDc;

    @Column(name = "descendant_pmid_count")
    private int descendantPmidCount;

    @Column(name = "exact_pmid_count")
    private int exactPmidCount ;

    @Column(name = "parent_pmid_count")
    private int parentPmidCount ;

    @Column(name = "ancestor_pmid_count")
    private int ancestorPmidCount;

    @Column(name = "indication")
    private int indication;
		
		@Column(name = "too_broad")
		private int tooBroad;

    @Column(name = "drug_induced")
    private int drugInduced;
		
    @Column(name = "pregnancy")
    private int pregnancy;

    @Column(name = "splicer")
    private int splicer;

    @Column(name = "faers")
    private int faers;

    @Column(name = "user_excluded")
    private int userExcluded;

    @Column(name = "user_included")
    private int userIncluded;

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the evidenceJobId
	 */
	public Long getEvidenceJobId() {
		return evidenceJobId;
	}

	/**
	 * @param evidenceJobId the evidenceJobId to set
	 */
	public void setEvidenceJobId(Long evidenceJobId) {
		this.evidenceJobId = evidenceJobId;
	}

	/**
	 * @return the sourceId
	 */
	public int getSourceId() {
		return sourceId;
	}

	/**
	 * @param sourceId the sourceId to set
	 */
	public void setSourceId(int sourceId) {
		this.sourceId = sourceId;
	}

	/**
	 * @return the conceptSetId
	 */
	public int getConceptSetId() {
		return conceptSetId;
	}

	/**
	 * @param conceptSetId the conceptSetId to set
	 */
	public void setConceptSetId(int conceptSetId) {
		this.conceptSetId = conceptSetId;
	}

	/**
	 * @return the conceptSetName
	 */
	public String getConceptSetName() {
		return conceptSetName;
	}

	/**
	 * @param conceptSetName the conceptSetName to set
	 */
	public void setConceptSetName(String conceptSetName) {
		this.conceptSetName = conceptSetName;
	}

	/**
	 * @return the negativeControl
	 */
	public int getNegativeControl() {
		return negativeControl;
	}

	/**
	 * @param negativeControl the negativeControl to set
	 */
	public void setNegativeControl(int negativeControl) {
		this.negativeControl = negativeControl;
	}

	/**
	 * @return the conceptId
	 */
	public int getConceptId() {
		return conceptId;
	}

	/**
	 * @param conceptId the conceptId to set
	 */
	public void setConceptId(int conceptId) {
		this.conceptId = conceptId;
	}

	/**
	 * @return the conceptName
	 */
	public String getConceptName() {
		return conceptName;
	}

	/**
	 * @param conceptName the conceptName to set
	 */
	public void setConceptName(String conceptName) {
		this.conceptName = conceptName;
	}

	/**
	 * @return the domainId
	 */
	public String getDomainId() {
		return domainId;
	}

	/**
	 * @param domainId the domainId to set
	 */
	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}

	/**
	 * @return the personCountRc
	 */
	public Long getPersonCountRc() {
		return personCountRc;
	}

	/**
	 * @param personCountRc the personCountRc to set
	 */
	public void setPersonCountRc(Long personCountRc) {
		this.personCountRc = personCountRc;
	}

	/**
	 * @return the personCountDc
	 */
	public Long getPersonCountDc() {
		return personCountDc;
	}

	/**
	 * @param personCountDc the personCountDc to set
	 */
	public void setPersonCountDc(Long personCountDc) {
		this.personCountDc = personCountDc;
	}

	/**
	 * @return the descendantPmidCount
	 */
	public int getDescendantPmidCount() {
		return descendantPmidCount;
	}

	/**
	 * @param descendantPmidCount the descendantPmidCount to set
	 */
	public void setDescendantPmidCount(int descendantPmidCount) {
		this.descendantPmidCount = descendantPmidCount;
	}

	/**
	 * @return the exactPmidCount
	 */
	public int getExactPmidCount() {
		return exactPmidCount;
	}

	/**
	 * @param exactPmidCount the exactPmidCount to set
	 */
	public void setExactPmidCount(int exactPmidCount) {
		this.exactPmidCount = exactPmidCount;
	}

	/**
	 * @return the parentPmidCount
	 */
	public int getParentPmidCount() {
		return parentPmidCount;
	}

	/**
	 * @param parentPmidCount the parentPmidCount to set
	 */
	public void setParentPmidCount(int parentPmidCount) {
		this.parentPmidCount = parentPmidCount;
	}

	/**
	 * @return the ancestorPmidCount
	 */
	public int getAncestorPmidCount() {
		return ancestorPmidCount;
	}

	/**
	 * @param ancestorPmidCount the ancestorPmidCount to set
	 */
	public void setAncestorPmidCount(int ancestorPmidCount) {
		this.ancestorPmidCount = ancestorPmidCount;
	}

	/**
	 * @return the indication
	 */
	public int getIndication() {
		return indication;
	}

	/**
	 * @param indication the indication to set
	 */
	public void setIndication(int indication) {
		this.indication = indication;
	}

	/**
	 * @return the drugInduced
	 */
	public int getDrugInduced() {
		return drugInduced;
	}

	/**
	 * @param drugInduced the drugInduced to set
	 */
	public void setDrugInduced(int drugInduced) {
		this.drugInduced = drugInduced;
	}

	/**
	 * @return the splicer
	 */
	public int getSplicer() {
		return splicer;
	}

	/**
	 * @param splicer the splicer to set
	 */
	public void setSplicer(int splicer) {
		this.splicer = splicer;
	}

	/**
	 * @return the faers
	 */
	public int getFaers() {
		return faers;
	}

	/**
	 * @param faers the faers to set
	 */
	public void setFaers(int faers) {
		this.faers = faers;
	}

	/**
	 * @return the userExcluded
	 */
	public int getUserExcluded() {
		return userExcluded;
	}

	/**
	 * @param userExcluded the userExcluded to set
	 */
	public void setUserExcluded(int userExcluded) {
		this.userExcluded = userExcluded;
	}

	/**
	 * @return the userIncluded
	 */
	public int getUserIncluded() {
		return userIncluded;
	}

	/**
	 * @param userIncluded the userIncluded to set
	 */
	public void setUserIncluded(int userIncluded) {
		this.userIncluded = userIncluded;
	}

	/**
	 * @return the tooBroad
	 */
	public int getTooBroad() {
		return tooBroad;
	}

	/**
	 * @param tooBroad the tooBroad to set
	 */
	public void setTooBroad(int tooBroad) {
		this.tooBroad = tooBroad;
	}

	/**
	 * @return the pregnancy
	 */
	public int getPregnancy() {
		return pregnancy;
	}

	/**
	 * @param pregnancy the pregnancy to set
	 */
	public void setPregnancy(int pregnancy) {
		this.pregnancy = pregnancy;
	}


}
