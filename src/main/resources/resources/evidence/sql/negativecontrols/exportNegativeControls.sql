INSERT INTO @evidenceSchema.NC_RESULTS (
	job_id, 
	negative_control, 
	outcome_of_interest_concept_id, 
	outcome_of_interest_concept_name, 
	person_count_rc, 
	person_count_dc, 
	descendant_pmid_count, 
	exact_pmid_count, 
	parent_pmid_count, 
	ancestor_pmid_count, 
	indication, 
	too_broad,
	drug_induced, 
	pregnancy,
	splicer, 
	faers, 
	user_excluded, 
	user_included
)
select 
	@jobId,
	CASE WHEN o.OUTCOME_OF_INTEREST_CONCEPT_ID IS NULL THEN 0 ELSE 1 END NEGATIVE_CONTROL,
	d.outcome_of_interest_concept_id, 
	d.outcome_of_interest_concept_name, 
	d.person_count_rc, 
	d.person_count_dc, 
	d.descendant_pmid_count, 
	d.exact_pmid_count, 
	d.parent_pmid_count, 
	d.ancestor_pmid_count, 
	d.indication, 
	d.too_broad,
	d.drug_induced,
	d.pregnancy,
	d.splicer, 
	d.faers, 
	d.user_excluded, 
	d.user_included
FROM #NC_SUMMARY d
  LEFT OUTER JOIN #NC_SUMMARY_OPTIMIZED o
    ON d.OUTCOME_OF_INTEREST_CONCEPT_ID = o.OUTCOME_OF_INTEREST_CONCEPT_ID
{@outcomeOfInterest == 'drug'}?{
  WHERE d.OUTCOME_OF_INTEREST_CONCEPT_ID IN (
    SELECT CONCEPT_ID FROM @vocabulary.CONCEPT WHERE DOMAIN_ID = 'Drug' AND CONCEPT_CLASS_ID = 'Ingredient'
  )
}
ORDER BY 1 DESC, PERSON_COUNT_DC DESC, PERSON_COUNT_RC DESC, OUTCOME_OF_INTEREST_CONCEPT_NAME;
