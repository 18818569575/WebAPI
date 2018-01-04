{@outcomeOfInterest == 'condition'}?{
  SELECT DISTINCT DESCENDANT_CONCEPT_ID AS CONCEPT_ID
  INTO #NC_SPLICER_CONCEPTS
  FROM @vocabulary.CONCEPT_ANCESTOR ca
  WHERE ca.ANCESTOR_CONCEPT_ID IN (
  	SELECT CONCEPT_ID_2
  	FROM @evidenceSchema.CEM_UNIFIED s
  		JOIN @vocabulary.CONCEPT_ANCESTOR ca
  			ON ca.DESCENDANT_CONCEPT_ID = s.CONCEPT_ID_1
  		JOIN @vocabulary.CONCEPT c1
  			ON c1.CONCEPT_ID = ca.ANCESTOR_CONCEPT_ID
  			AND lower(c1.CONCEPT_CLASS_ID) = 'ingredient'
  			AND c1.CONCEPT_ID IN (@conceptsOfInterest)
  	WHERE LOWER(source_id) = LOWER('splicer')
  );
}

{@outcomeOfInterest == 'drug'}?{
  SELECT DISTINCT CONCEPT_ID AS CONCEPT_ID
  INTO #NC_SPLICER_CONCEPTS
  FROM (
    SELECT DISTINCT DESCENDANT_CONCEPT_ID AS CONCEPT_ID
    FROM @vocabulary.CONCEPT_ANCESTOR ca
    WHERE ca.ANCESTOR_CONCEPT_ID IN (
    	SELECT CONCEPT_ID_1
			FROM @evidenceSchema.CEM_UNIFIED s
    		JOIN @vocabulary.CONCEPT_ANCESTOR ca
    			ON ca.DESCENDANT_CONCEPT_ID = s.CONCEPT_ID_2
    		JOIN @vocabulary.CONCEPT c1
    			ON c1.CONCEPT_ID = ca.ANCESTOR_CONCEPT_ID
    			AND c1.CONCEPT_ID IN (@conceptsOfInterest)
    	WHERE LOWER(source_id) = LOWER('splicer')
    )
    UNION ALL
    SELECT DISTINCT c.CONCEPT_ID AS CONCEPT_ID
    FROM @vocabulary.CONCEPT_ANCESTOR ca
  	JOIN @vocabulary.CONCEPT c
  		ON c.CONCEPT_ID = ca.ANCESTOR_CONCEPT_ID
  		AND lower(c.DOMAIN_ID) = 'drug'
  		AND lower(c.standard_concept) = 's'
    WHERE ca.DESCENDANT_CONCEPT_ID IN (
    	SELECT CONCEPT_ID_1
			FROM @evidenceSchema.CEM_UNIFIED s
    		JOIN @vocabulary.CONCEPT_ANCESTOR ca
    			ON ca.DESCENDANT_CONCEPT_ID = s.CONCEPT_ID_2
    		JOIN @vocabulary.CONCEPT c1
    			ON c1.CONCEPT_ID = ca.ANCESTOR_CONCEPT_ID
    			AND c1.CONCEPT_ID IN (@conceptsOfInterest)
    	WHERE LOWER(source_id) = LOWER('splicer')
    )
  ) z;
}
