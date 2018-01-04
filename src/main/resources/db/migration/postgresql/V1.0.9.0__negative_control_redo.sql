DROP TABLE IF EXISTS ${ohdsiSchema}.CONCEPT_SET_NEGATIVE_CONTROLS;
DROP SEQUENCE IF EXISTS ${ohdsiSchema}.CONCEPT_SET_NEGATIVE_CONTROLS_SEQUENCE;
CREATE SEQUENCE ${ohdsiSchema}.CONCEPT_SET_NEGATIVE_CONTROLS_SEQUENCE MAXVALUE 9223372036854775807 NO CYCLE;
CREATE TABLE ${ohdsiSchema}.CONCEPT_SET_NEGATIVE_CONTROLS (
    id INTEGER NOT NULL DEFAULT NEXTVAL('CONCEPT_SET_NEGATIVE_CONTROLS_SEQUENCE'),
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
    CONSTRAINT PK_CONCEPT_SET_NEGATIVE_CONTROLS PRIMARY KEY (id)
);

