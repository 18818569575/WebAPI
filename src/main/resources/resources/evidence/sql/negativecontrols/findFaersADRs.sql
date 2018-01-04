/*
Based off the following paper:
Evans SJ, Waller PC, Davis S. Use of proportional reporting ratios (PRRs) for
signal generation from spontaneous adverse drug reaction reports.
Pharmacoepidemiol Drug Saf. 2001 Oct-Nov;10(6):483-6. PubMed PMID: 11828828.
*/

WITH aeolusEvidence AS (
	SELECT
		A.CONCEPT_ID_1, 
		A.CONCEPT_ID_2 
	FROM (
	SELECT 
		CONCEPT_ID_1, 
		CONCEPT_ID_2, 
		EVIDENCE_TYPE, 
		STATISTIC_VALUE, 
		CASE 
	  	WHEN LOWER(EVIDENCE_TYPE) = LOWER('PRR') AND statistic_value >= 2 THEN 1
			WHEN LOWER(EVIDENCE_TYPE) = LOWER('COUNT') AND statistic_value >= 3 THEN 1
			WHEN LOWER(EVIDENCE_TYPE) = LOWER('CHI_SQUARE') AND statistic_value >= 4 THEN 1
			ELSE 0
		END MEETS_THRESHOLD
	FROM @evidenceSchema.CEM_UNIFIED 
	WHERE LOWER(SOURCE_ID) = LOWER('aeolus')
	  AND LOWER(EVIDENCE_TYPE) IN ('chi_square', 'count', 'prr')
	) A
	GROUP BY 
		A.CONCEPT_ID_1, 
		A.CONCEPT_ID_2 
	HAVING SUM(A.MEETS_THRESHOLD) = 3
)
SELECT DISTINCT c.CONCEPT_ID, c.CONCEPT_NAME
INTO #NC_FAERS_CONCEPTS
FROM aeolusEvidence a
	JOIN @vocabulary.CONCEPT c
		{@outcomeOfInterest == 'condition'}?{ON c.CONCEPT_ID = a.CONCEPT_ID_2 WHERE CONCEPT_ID_1 IN }
		{@outcomeOfInterest == 'drug'}?{ON c.CONCEPT_ID = a.CONCEPT_ID_1 WHERE CONCEPT_ID_2 IN }
(
	SELECT CONCEPT_ID
	FROM @vocabulary.CONCEPT
	WHERE CONCEPT_ID IN (@conceptsOfInterest)
	UNION ALL
	SELECT DESCENDANT_CONCEPT_ID
	FROM @vocabulary.CONCEPT_ANCESTOR
	WHERE ANCESTOR_CONCEPT_ID IN (@conceptsOfInterest)
)
;