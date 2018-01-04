IF (EXISTS (SELECT * 
            FROM INFORMATION_SCHEMA.TABLES 
            WHERE TABLE_SCHEMA = '${ohdsiSchema}' 
            AND  TABLE_NAME = 'CONCEPT_SET_NEGATIVE_CONTROLS'))
BEGIN
    DROP TABLE [${ohdsiSchema}].[CONCEPT_SET_NEGATIVE_CONTROLS]
END

CREATE TABLE [${ohdsiSchema}].CONCEPT_SET_NEGATIVE_CONTROLS (
    [id] [int] IDENTITY(1,1) NOT NULL,
		evidence_job_id BIGINT NOT NULL,
    source_id INTEGER NOT NULL,
    concept_set_id INTEGER NOT NULL,
    concept_set_name varchar(255) NOT NULL,
		negative_control INTEGER NOT NULL,
    concept_id INTEGER NOT NULL,
    concept_name varchar(255) NOT NULL,
    domain_id varchar(255) NOT NULL,
    person_count_rc bigint,
    person_count_dc bigint,
    descendant_pmid_count INTEGER,
    exact_pmid_count INTEGER,
    parent_pmid_count INTEGER,
    ancestor_pmid_count INTEGER,
    indication INTEGER,
    too_broad INTEGER,
    drug_induced INTEGER,
    pregnancy INTEGER,
    splicer INTEGER,
    faers INTEGER,
    user_excluded INTEGER,
    user_included INTEGER,
		CONSTRAINT [PK_CONCEPT_SET_NEGATIVE_CONTROLS] PRIMARY KEY CLUSTERED 
		 (
			 [id] ASC
		 ) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
);

