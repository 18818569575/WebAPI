select c1.concept_id as concept_id, 
  c1.concept_name as concept_name, 
	hr1.count_value as count_value
from @ohdsi_database_schema.heracles_results hr1
	inner join
	@cdm_database_schema.concept c1
	on CAST(hr1.stratum_1 AS INT) = c1.concept_id
where hr1.analysis_id = 2
and c1.concept_id in (8507, 8532)
and cohort_definition_id in (@cohortDefinitionId)