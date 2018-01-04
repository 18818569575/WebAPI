package org.ohdsi.webapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import static org.ohdsi.webapi.util.SecurityUtils.whitelist;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.stream.Collectors;
import javax.ws.rs.Consumes;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import net.minidev.json.JSONStyle;
import net.minidev.json.JSONValue;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.conceptset.ConceptSetGenerationInfoRepository;
//import org.ohdsi.webapi.cohortanalysis.CohortAnalysisTasklet;
import org.ohdsi.webapi.evidence.CohortStudyMapping;
import org.ohdsi.webapi.evidence.CohortStudyMappingRepository;
import org.ohdsi.webapi.evidence.ConceptCohortMapping;
import org.ohdsi.webapi.evidence.ConceptCohortMappingRepository;
import org.ohdsi.webapi.evidence.ConceptOfInterestMapping;
import org.ohdsi.webapi.evidence.ConceptOfInterestMappingRepository;
import org.ohdsi.webapi.evidence.DrugEvidence;
import org.ohdsi.webapi.evidence.EvidenceDetails;
import org.ohdsi.webapi.evidence.EvidenceSummary;
import org.ohdsi.webapi.evidence.EvidenceUniverse;
import org.ohdsi.webapi.evidence.HoiEvidence;
import org.ohdsi.webapi.evidence.DrugHoiEvidence;
import org.ohdsi.webapi.evidence.DrugLabel;
import org.ohdsi.webapi.evidence.DrugLabelRepository;
import org.ohdsi.webapi.evidence.EvidenceInfo;
import org.ohdsi.webapi.evidence.DrugRollUpEvidence;
import org.ohdsi.webapi.evidence.Evidence;
import org.ohdsi.webapi.evidence.LinkoutData;
import org.ohdsi.webapi.evidence.SpontaneousReport;
import org.ohdsi.webapi.evidence.EvidenceSearch;
import org.ohdsi.webapi.evidence.negativecontrols.NegativeControlTaskParameters;
import org.ohdsi.webapi.evidence.negativecontrols.NegativeControlRecord;
import org.ohdsi.webapi.evidence.negativecontrols.NegativeControlRepository;
import org.ohdsi.webapi.evidence.negativecontrols.NegativeControlTasklet;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * @author rkboyce based on the vocabulary service written by fdefalco and an
 * initial api written by m_rasteger
 */
@Path("/evidence")
@Component
public class EvidenceService extends AbstractDaoService {
  @Autowired
  private JobTemplate jobTemplate;
        
  @Autowired
  private DrugLabelRepository drugLabelRepository;

  @Autowired
  private ConceptCohortMappingRepository mappingRepository;
  
  @Autowired
  private ConceptOfInterestMappingRepository conceptOfInterestMappingRepository;

  @Autowired
  private CohortStudyMappingRepository cohortStudyMappingRepository;
  
  @Autowired
  private NegativeControlRepository negativeControlRepository;
  
  @Autowired
  private ConceptSetGenerationInfoRepository conceptSetGenerationInfoRepository;
  
  @GET
  @Path("study/{cohortId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<CohortStudyMapping> getCohortStudyMapping(@PathParam("cohortId") int cohortId) {
    return cohortStudyMappingRepository.findByCohortDefinitionId(cohortId);
  }

  @GET
  @Path("mapping/{conceptId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<ConceptCohortMapping> getConceptCohortMapping(@PathParam("conceptId") int conceptId) {
    return mappingRepository.findByConceptId(conceptId);
  }

  @GET
  @Path("conceptofinterest/{conceptId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<ConceptOfInterestMapping> getConceptOfInterest(@PathParam("conceptId") int conceptId) {
    return conceptOfInterestMappingRepository.findAllByConceptId(conceptId);
  }
  
  @GET
  @Path("label/{setid}")
  @Produces(MediaType.APPLICATION_JSON) 
  public Collection<DrugLabel> getDrugLabels(@PathParam("setid") String setid) {
    return drugLabelRepository.findAllBySetid(setid);
  }
  
  @GET
  @Path("labelsearch/{searchTerm}")
  @Produces(MediaType.APPLICATION_JSON) 
  public Collection<DrugLabel> searchDrugLabels(@PathParam("searchTerm") String searchTerm) {
    return drugLabelRepository.searchNameContainsTerm(searchTerm);
  }
  
  
  @GET
  @Path("{sourceKey}/info")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<EvidenceInfo> getInfo(@PathParam("sourceKey") String sourceKey) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String sqlPath = "/resources/evidence/sql/getInfo.sql";
    String tqName = "evidenceSchema";
    String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Evidence);
    PreparedStatementRenderer psr = new PreparedStatementRenderer(source, sqlPath, tqName, tqValue);
    return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), (rs, rowNum) -> {

      EvidenceInfo info = new EvidenceInfo();
      info.title = rs.getString("TITLE");
      info.description = rs.getString("DESCRIPTION");
			info.provenance = rs.getString("PROVENANCE");
      info.contributor = rs.getString("CONTRIBUTOR");
      info.contactName = rs.getString("CONTACT_NAME");
      info.creationDate = rs.getDate("CREATION_DATE");
			info.coverageStartDate = rs.getDate("COVERAGE_START_DATE");
			info.coverageEndDate = rs.getDate("COVERAGE_END_DATE");
			info.versionIdentifier = rs.getString("VERSION_IDENTIFIER");
      return info;
    });
  }

  /**
   * @param id
   * @return
   */
  @GET
  @Path("{sourceKey}/drug/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<DrugEvidence> getDrugEvidence(@PathParam("sourceKey") String sourceKey, @PathParam("id") final Long id) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
		PreparedStatementRenderer psr = prepareGetEvidenceForConcept(source, id);
    return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), (rs, rowNum) -> {
      String evidenceSource = rs.getString("SOURCE_ID");
      String hoi = rs.getString("CONCEPT_ID_2");
      String hoiName = rs.getString("CONCEPT_ID_2_NAME");
      String statType = rs.getString("STATISTIC_VALUE_TYPE");
      BigDecimal statVal = rs.getBigDecimal("STATISTIC_VALUE");
			String relationshipType = rs.getString("RELATIONSHIP_ID");
      String uniqueIdentifier = rs.getString("UNIQUE_IDENTIFIER");
			String uniqueIdentifierType = rs.getString("UNIQUE_IDENTIFIER_TYPE");
      
      DrugEvidence evidence = new DrugEvidence();
      evidence.evidenceSource = evidenceSource;
      evidence.hoiConceptId = hoi;
      evidence.hoiConceptName = hoiName;
      evidence.relationshipType = relationshipType;
      evidence.statisticType = statType;
			evidence.statisticValue = statVal;
			evidence.uniqueIdentifier = uniqueIdentifier;
			evidence.uniqueIdentifierType = uniqueIdentifierType;

      return evidence;
		});
  }

  /**
   * @param id
   * @return
   */
  @GET
  @Path("{sourceKey}/hoi/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<HoiEvidence> getHoiEvidence(@PathParam("sourceKey") String sourceKey, @PathParam("id") final Long id) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
		PreparedStatementRenderer psr = prepareGetEvidenceForConcept(source, id);
    return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), (rs, rowNum) -> {
      String evidenceSource = rs.getString("SOURCE_ID");
      String drug = rs.getString("CONCEPT_ID_1");
      String drugName = rs.getString("CONCEPT_ID_1_NAME");
      String statType = rs.getString("STATISTIC_VALUE_TYPE");
      BigDecimal statVal = rs.getBigDecimal("STATISTIC_VALUE");
			String relationshipType = rs.getString("RELATIONSHIP_ID");
      String uniqueIdentifier = rs.getString("UNIQUE_IDENTIFIER");
			String uniqueIdentifierType = rs.getString("UNIQUE_IDENTIFIER_TYPE");
      
      HoiEvidence evidence = new HoiEvidence();
      evidence.evidenceSource = evidenceSource;
      evidence.drugConceptId = drug;
      evidence.drugConceptName = drugName;
      evidence.relationshipType = relationshipType;
      evidence.statisticType = statType;
			evidence.statisticValue = statVal;
			evidence.uniqueIdentifier = uniqueIdentifier;
			evidence.uniqueIdentifierType = uniqueIdentifierType;

      return evidence;
		});
  }

  /**
   * @param id
   * @return
   */
  @GET
  @Path("{sourceKey}/drughoi/{key}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<DrugHoiEvidence> getDrugHoiEvidence(@PathParam("sourceKey") String sourceKey, @PathParam("key") final String key) {

    Source source = getSourceRepository().findBySourceKey(sourceKey);
    PreparedStatementRenderer psr = prepareGetDrugHoiEvidence(key, source);
    return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), (rs, rowNum) -> {
      String evidenceSource = rs.getString("SOURCE_ID");
      String drug = rs.getString("CONCEPT_ID_1");
      String drugName = rs.getString("CONCEPT_ID_1_NAME");
      String hoi = rs.getString("CONCEPT_ID_2");
      String hoiName = rs.getString("CONCEPT_ID_2_NAME");
      String statType = rs.getString("STATISTIC_VALUE_TYPE");
      BigDecimal statVal = rs.getBigDecimal("STATISTIC_VALUE");
			String relationshipType = rs.getString("RELATIONSHIP_ID");
      String uniqueIdentifier = rs.getString("UNIQUE_IDENTIFIER");
			String uniqueIdentifierType = rs.getString("UNIQUE_IDENTIFIER_TYPE");
			
      DrugHoiEvidence evidence = new DrugHoiEvidence();
      evidence.evidenceSource = evidenceSource;
      evidence.drugConceptId = drug;
      evidence.drugConceptName = drugName;
      evidence.hoiConceptId = hoi;
      evidence.hoiConceptName = hoiName;
      evidence.relationshipType = relationshipType;
      evidence.statisticType = statType;
			evidence.statisticValue = statVal;
			evidence.uniqueIdentifier = uniqueIdentifier;
			evidence.uniqueIdentifierType = uniqueIdentifierType;

      return evidence;
    });
  }

  /**
   * @param id
   * @param filter
   * @return
   */
  @GET
  @Path("{sourceKey}/drugrollup/{filter}/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<DrugRollUpEvidence> getDrugRollupIngredientEvidence(@PathParam("sourceKey") String sourceKey, @PathParam("id") final Long id, @PathParam("filter") final String filter) {
    
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String evidenceTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Evidence);
    String vocabularyTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
    
    String sqlPath = "";
    if (filter.equals("ingredient")) {
      sqlPath = "/resources/evidence/sql/getDrugEvidenceRollupByIngredient.sql";
    } else if (filter.equals("clinicaldrug")) {
      sqlPath = "/resources/evidence/sql/getDrugEvidenceRollupByClinicalDrug.sql";
    } else if (filter.equals("brandeddrug")) {
      sqlPath = "/resources/evidence/sql/getDrugEvidenceRollupByBrandedDrug.sql";
    } else {
      log.debug(String.format("The call did not specify an appropriate roll up filter (e.g., ingredient, clinicaldrug, brandeddrug)", id));
      return null;
    }
    String[] tableQualifierNames = new String[]{"OHDSI_schema", "CDM_schema"};
    String[] tableQualifierValues = new String[]{evidenceTableQualifier, vocabularyTableQualifier};
    String[] names = new String[]{"id"};
    Object[] values = new Object[]{whitelist(id)};
    PreparedStatementRenderer psr = new PreparedStatementRenderer(source, sqlPath, tableQualifierNames, tableQualifierValues, names, values);

    //try {
    return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), (rs, rowNum) -> {
      DrugRollUpEvidence evidence = new DrugRollUpEvidence();
      evidence.reportName =  rs.getString("REPORT_NAME");
      evidence.ingredientId = rs.getInt("INGREDIENT_ID");
      evidence.ingredientName = rs.getString("INGREDIENT");
      evidence.clinicalDrugId = rs.getInt("CLINICAL_DRUG_ID");
      evidence.clinicalDrugName = rs.getString("CLINICAL_DRUG");
      evidence.hoiId = rs.getInt("HOI_ID");
      evidence.hoiName = rs.getString("HOI");
      evidence.pubmedMeshCTcount = rs.getInt("MEDLINE_CT_COUNT");
      evidence.pubmedMeshCaseReportcount = rs.getInt("MEDLINE_CASE_COUNT");
      evidence.pubmedMeshOthercount = rs.getInt("MEDLINE_OTHER_COUNT");
      evidence.ctdChemicalDiseaseCount = rs.getInt("CTD_CHEMICAL_DISEASE_COUNT");
      evidence.splicerCount = rs.getInt("SPLICER_COUNT");
      evidence.euSPCcount = rs.getInt("EU_SPC_COUNT");
      evidence.semmedCTcount = rs.getInt("SEMMEDDB_CT_COUNT");
      evidence.semmedCaseReportcount = rs.getInt("SEMMEDDB_CASE_COUNT");
      evidence.semmedOthercount = rs.getInt("SEMMEDDB_OTHER_COUNT");
      evidence.semmedNegCTcount = rs.getInt("SEMMEDDB_NEG_CT_COUNT");
      evidence.semmedNegCaseReportcount = rs.getInt("SEMMEDDB_NEG_CASE_COUNT");
      evidence.semmedNegOthercount = rs.getInt("SEMMEDDB_NEG_OTHER_COUNT");
      evidence.aersReportCount = rs.getInt("AERS_REPORT_COUNT");
      evidence.prr = rs.getBigDecimal("PRR");
      return evidence;
    });
    //} catch (EmptyResultDataAccessException e) {
    //    log.debug(String.format("Request for conceptId=%s resulted in 0 results", id));
    //    throw new WebApplicationException(Response.Status.RESET_CONTENT); // http 205
    //}
  }
  
  
  @GET
  @Path("{sourceKey}/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<Evidence> getEvidence(@PathParam("sourceKey") String sourceKey, @PathParam("id") final Long id) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
		PreparedStatementRenderer psr = prepareGetEvidenceForConcept(source, id);
    return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), (rs, rowNum) -> {
      String evidenceSource = rs.getString("SOURCE_ID");
      String drug = rs.getString("CONCEPT_ID_1");
      String drugName = rs.getString("CONCEPT_ID_1_NAME");
      String hoi = rs.getString("CONCEPT_ID_2");
      String hoiName = rs.getString("CONCEPT_ID_2_NAME");
      String statType = rs.getString("STATISTIC_VALUE_TYPE");
      BigDecimal statVal = rs.getBigDecimal("STATISTIC_VALUE");
			String relationshipType = rs.getString("RELATIONSHIP_ID");
      String uniqueIdentifier = rs.getString("UNIQUE_IDENTIFIER");
			String uniqueIdentifierType = rs.getString("UNIQUE_IDENTIFIER_TYPE");
      
      Evidence evidence = new Evidence();
      evidence.evidenceSource = evidenceSource;
			evidence.drugConceptId = drug;
			evidence.drugConceptName = drugName;
      evidence.hoiConceptId = hoi;
      evidence.hoiConceptName = hoiName;
      evidence.relationshipType = relationshipType;
      evidence.statisticType = statType;
			evidence.statisticValue = statVal;
			evidence.uniqueIdentifier = uniqueIdentifier;
			evidence.uniqueIdentifierType = uniqueIdentifierType;

      return evidence;
		});
  }

  
  /**
   * @param conditionID
   * @param drugID
   * @param evidenceGroup
   * @return
   */
  @GET
  @Path("{sourceKey}/evidencesummary")
  @Produces(MediaType.APPLICATION_JSON)
  public List<EvidenceSummary> getEvidenceSummaryBySource(@PathParam("sourceKey") String sourceKey, @QueryParam("conditionID") String conditionID, @QueryParam("drugID") String drugID, @QueryParam("evidenceGroup") String evidenceGroup) {

    Source source = getSourceRepository().findBySourceKey(sourceKey);
    PreparedStatementRenderer psr = prepareGetEvidenceSummaryBySource(conditionID, drugID, evidenceGroup, source);
    return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), (rs, rowNum) -> {

      EvidenceSummary e = new EvidenceSummary();
      e.evidence_group_name = evidenceGroup;
      //e.evidence_id = BigInteger.valueOf((long)rs.get("id"));
      e.evidence_type = rs.getString("evidence_type");
      //e.relationshipType = (Character)rs.get("relationshipType");
      e.evidence_count = rs.getDouble("statistic_value");
      return e;
    });

  }

  /**
   * @param conditionID
   * @param drugID
   * @param evidenceType
   * @return
   */
  @GET
  @Path("{sourceKey}/evidencedetails")
  @Produces(MediaType.APPLICATION_JSON)
  public List<EvidenceDetails> getEvidenceDetails(@PathParam("sourceKey") String sourceKey,
                                                        @QueryParam("conditionID") String conditionID,
                                                        @QueryParam("drugID") String drugID,
                                                        @QueryParam("evidenceType") String evidenceType)
      throws JSONException, IOException {

    SparqlService sparqlentity = new SparqlService();
	  Source source = getSourceRepository().findBySourceKey(sourceKey);
    PreparedStatementRenderer psr = prepareGetEvidenceDetails(Integer.valueOf(drugID), Integer.valueOf(conditionID), evidenceType, source);
    List<String> linkoutsList = getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(),
      (rs, rowNum) -> rs.getString("evidence_linkouts"));

    List<EvidenceDetails> result = new ArrayList<>();
    for (String linkoutlist : linkoutsList) {

      String[] linkouts = new String[]{};
      if (linkoutlist != null) {
        if (linkoutlist.contains("|")) {
          linkouts = linkoutlist.split(Pattern.quote("|"));
        } else {
          linkouts = new String[]{linkoutlist};
        }
      }
      if (linkouts != null) {
        for (int i = 0; i < linkouts.length; i++) {
          linkouts[i] = sparqlentity.expandUrl(linkouts[i]);
          linkouts[i] = URIUtil.decode(linkouts[i]);
          linkouts[i] = URIUtil.encodeQuery(linkouts[i]);
          JSONArray lineItems = sparqlentity.readJSONFeed(linkouts[i]);

          for (int j = 0; j < lineItems.length(); ++j) {
            EvidenceDetails e = new EvidenceDetails();
            if (linkouts[i].contains("mesh")) {
              e = getPubMedlinkout(lineItems, j);
            }
            if (linkouts[i].contains("ADR")) {
              e = getADRlinkout(lineItems, j);
            }
            if (linkouts[i].contains("semmed")) {
              e = getSemMedlinkout(lineItems, j);
            }
            result.add(e);
          }
        }
      }
    }

    return result;
  }

  @POST
  @Path("{sourceKey}/spontaneousreports")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public List<SpontaneousReport> getSpontaneousReports(@PathParam("sourceKey") String sourceKey, EvidenceSearch search) throws JSONException, IOException {

    Source source = getSourceRepository().findBySourceKey(sourceKey);
    PreparedStatementRenderer psr = prepareGetSpontaneousReports(search, source);

    return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(),
      (rs, rowNum) -> {
        SpontaneousReport e = new SpontaneousReport();
        e.conditionConceptId = rs.getString("CONDITION_CONCEPT_ID");
        e.conditionConceptName = rs.getString("CONDITION_CONCEPT_NAME");
        e.ingredientConceptId = rs.getString("INGREDIENT_CONCEPT_ID");
        e.ingredientConceptName = rs.getString("INGREDIENT_CONCEPT_NAME");
        e.reportCount = rs.getInt("AERS");
        e.prr = rs.getBigDecimal("AERS_PRR_ORIGINAL");
        return e;
    });
  }

  @POST
  @Path("{sourceKey}/evidencesearch")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public List<EvidenceUniverse> evidenceSearch(@PathParam("sourceKey") String sourceKey, EvidenceSearch search) throws JSONException, IOException {

    Source source = getSourceRepository().findBySourceKey(sourceKey);
    PreparedStatementRenderer psr = prepareEvidenceSearch(search, source);

    return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), (rs, rowNum) -> {
        EvidenceUniverse e = new EvidenceUniverse();
        //e.evidence_id = rs.getInt("ID");
        e.condition_concept_id = rs.getInt("CONDITION_CONCEPT_ID");
        e.condition_concept_name = rs.getString("CONDITION_CONCEPT_NAME");
        e.ingredient_concept_id = rs.getInt("INGREDIENT_CONCEPT_ID");
        e.ingredient_concept_name = rs.getString("INGREDIENT_CONCEPT_NAME");
        e.evidence_type = rs.getString("EVIDENCE_TYPE");

        if (rs.getObject("SUPPORTS") == null) {
          e.supports = 'u';
        } else if (rs.getString("SUPPORTS").equalsIgnoreCase("t")) {
          e.supports = 't';
        } else {
          e.supports = 'f';
        }

        e.statistic_value = rs.getBigDecimal("STATISTIC_VALUE");
        e.evidence_linkouts = rs.getString("EVIDENCE_LINKOUTS");
        return e;
      });
  }
  
  @POST
  @Path("{sourceKey}/labelevidence")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public List<EvidenceUniverse> labelEvidence(@PathParam("sourceKey") String sourceKey, EvidenceSearch search) throws JSONException, IOException {

    Source source = getSourceRepository().findBySourceKey(sourceKey);
    PreparedStatementRenderer psr = prepareLabelEvidence(search, source);
    return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), (rs, rowNum) -> {
      EvidenceUniverse e = new EvidenceUniverse();
      e.hasEvidence = rs.getString("Has_Evidence");
      e.ingredient_concept_id = rs.getInt("INGREDIENT_CONCEPT_ID");
      e.ingredient_concept_name = rs.getString("INGREDIENT_CONCEPT_NAME");
      return e;
    });
  }

  @GET
  @Path("{sourceKey}/negativecontrols-sql")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.APPLICATION_JSON)
	public String getNegativeControlSql(@PathParam("sourceKey") String sourceKey) throws Exception {
		Long jobId = new Long(1);
		// Refactor to take this as a paramter
	  Source dbsource = getSourceRepository().findBySourceKey(sourceKey);
		
		// Create the task
		NegativeControlTaskParameters task = new NegativeControlTaskParameters();
		String outcomeOfInterest = "drug";
		String[] conceptsOfInterest = {"4344040"};
		String conceptsToExclude = "0";
		String conceptsToInclude = "1186087,19015230,1381504,757688,1314865,715233,950933,1563600,980311,1541079,19008009,19010482,1311078,1304643,1338512,1301125,1151789,1352213,1304850,1542948,1597235,19048493,19097463,1536743,751889,19041065,787787,1512480,19117912,40238188,1112921,1351541,1192218,19024227,1305058,909841,708298,1114220,1522957,785788,1378382,19071160,19049105,753626,42903728,19090761,1584910,836208,1236744,40171288";
		
		task.setSource(dbsource);
		task.setOutcomeOfInterest(outcomeOfInterest);
		task.setConceptsOfInterest(conceptsOfInterest);
		task.setConceptsToInclude(conceptsToInclude);
		task.setConceptsToExclude(conceptsToExclude);
		return getNegativeControlSql(task, jobId);
	}

/**
 * Queues up a negative control task, that generates and translates SQL for the
 * given concept ids
 *
 * @param task - the negative control task to run
 * @return information about the negative control job
 * @throws Exception
 */
  @POST
  @Path("{sourceKey}/negativecontrols")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public JobExecutionResource queueNegativeControlsJob(@PathParam("sourceKey") String sourceKey, NegativeControlTaskParameters task) throws Exception {
        if (task == null) {
                return null;
        }
        JobParametersBuilder builder = new JobParametersBuilder();

        // Get a JDBC template for the OHDSI source repository 
        // and the source dialect for use when we write the results
        // back to the OHDSI repository
        JdbcTemplate jdbcTemplate = getJdbcTemplate();
        task.setJdbcTemplate(jdbcTemplate);
        String ohdsiDatasourceSourceDialect = getSourceDialect();
        task.setSourceDialect(ohdsiDatasourceSourceDialect);
        task.setOhdsiSchema(this.getOhdsiSchema());

        // source key comes from the client, we look it up here and hand it off to the tasklet
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        task.setSource(source);

        if (!StringUtils.isEmpty(task.getJobName())) {
            builder.addString("jobName", limitJobParams(task.getJobName()));
        }
        builder.addString("concept_set_id", ("" + task.getConceptSetId()));
        builder.addString("concept_set_name", task.getConceptSetName());
        builder.addString("concept_domain_id", task.getConceptDomainId());
        builder.addString("source_id", ("" + source.getSourceId()));
				
				// Create a set of parameters to store with the generation info
				JSONObject params = new JSONObject();
				// If/when we want to treat these concepts as lists, this
				// code will do the trick
				//JSONArray conceptsToInclude = new JSONArray();
				//JSONArray conceptsToExclude = new JSONArray();
				//for(int i = 0; i < task.getConceptsToInclude().length; i++) {
				//	conceptsToInclude.put(task.getConceptsToInclude()[i]);
				//}
				//for(int i = 0; i < task.getConceptsToExclude().length; i++) {
				//	conceptsToExclude.put(task.getConceptsToExclude()[i]);
				//}
				params.put("conceptsToInclude", task.getConceptsToInclude());
				params.put("conceptsToExclude", task.getConceptsToExclude());
				builder.addString("params", params.toString());
        
        final String taskString = task.toString();
        final JobParameters jobParameters = builder.toJobParameters();
        log.info(String.format("Beginning run for negative controls analysis task: \n %s", taskString));

        NegativeControlTasklet tasklet = new NegativeControlTasklet(task, getSourceJdbcTemplate(task.getSource()), task.getJdbcTemplate(),
                        getTransactionTemplate(), this.conceptSetGenerationInfoRepository, this.getSourceDialect());

        return this.jobTemplate.launchTasklet("negativeControlsAnalysisJob", "negativeControlsAnalysisStep", tasklet, jobParameters);
}

  @GET
  @Path("{sourceKey}/negativecontrols/{conceptsetid}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Collection<NegativeControlRecord> getNegativeControls(@PathParam("sourceKey") String sourceKey, @PathParam("conceptsetid") int conceptSetId) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    return negativeControlRepository.findAllBySourceIdAndConceptId(source.getSourceId(), conceptSetId);
  }
	
	public static String getEvidenceJobIdSql(NegativeControlTaskParameters task) {
    String resourceRoot = "/resources/evidence/sql/negativecontrols/";
    String sql = ResourceHelper.GetResourceAsString(resourceRoot + "getJobId.sql");
    String evidenceSchema = task.getSource().getTableQualifier(SourceDaimon.DaimonType.Evidence);
    String[] params = new String[]{"evidenceSchema"};
    String[] values = new String[]{evidenceSchema};
    sql = SqlRender.renderSql(sql, params, values);
    sql = SqlTranslate.translateSql(sql, task.getSource().getSourceDialect());

    return sql;
	}
	
	public static String getNegativeControlSql(NegativeControlTaskParameters task, Long jobId ) {
		StringBuilder sb = new StringBuilder();
    String resourceRoot = "/resources/evidence/sql/negativecontrols/";
		Source source = task.getSource();
	  String evidenceSchema = source.getTableQualifier(SourceDaimon.DaimonType.Evidence);
		String vocabularySchema = evidenceSchema;
		try {
			vocabularySchema = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
		} catch (Exception e) {
			// Default the vocabulary schema to the evidence schema
		}
				
		String outcomeOfInterest = task.getOutcomeOfInterest().toLowerCase();
		String conceptsOfInterest = JoinArray(task.getConceptsOfInterest());
		String conceptsToExclude = task.getConceptsToExclude();
		String conceptsToInclude = task.getConceptsToInclude();
		String conceptsToExcludeData = "#NC_EXCLUDED_CONCEPTS";
		String conceptsToIncludeData = "#NC_INCLUDED_CONCEPTS";
		String broadConceptsData = evidenceSchema + ".NC_LU_BROAD_CONDITIONS";
		String drugInducedConditionsData = evidenceSchema + ".NC_LU_DRUG_INDUCED_CONDITIONS";
		String pregnancyConditionData = evidenceSchema + ".NC_LU_PREGNANCY_CONDITIONS";
	
		String[] params = new String[]{"outcomeOfInterest", "conceptsOfInterest", "vocabulary", "evidenceSchema"};
    String[] values = new String[]{outcomeOfInterest, conceptsOfInterest, vocabularySchema, evidenceSchema};
		
		String sql = ResourceHelper.GetResourceAsString(resourceRoot + "findConceptUniverse.sql");
    sql = SqlRender.renderSql(sql, params, values);
		sb.append(sql + "\n\n");
		
		sql = ResourceHelper.GetResourceAsString(resourceRoot + "splicerConcepts.sql");
    sql = SqlRender.renderSql(sql, params, values);
		sb.append(sql + "\n\n");

		sql = ResourceHelper.GetResourceAsString(resourceRoot + "findDrugIndications.sql");
    sql = SqlRender.renderSql(sql, params, values);
		sb.append(sql + "\n\n");
		
		sql = ResourceHelper.GetResourceAsString(resourceRoot + "findConcepts.sql");
    sql = SqlRender.renderSql(sql, params, values);
		sql = SqlRender.renderSql(sql, 
			new String[] {"storeData", "concepts"}, 
			new String[] {conceptsToExcludeData, conceptsToExclude});
		sb.append(sql + "\n\n");
		
	
		sql = ResourceHelper.GetResourceAsString(resourceRoot + "findConcepts.sql");
    sql = SqlRender.renderSql(sql, params, values);
		sql = SqlRender.renderSql(sql, 
			new String[] {"storeData", "concepts"}, new
				String[] {conceptsToIncludeData, conceptsToInclude});
		sb.append(sql + "\n\n");
		
		sql = ResourceHelper.GetResourceAsString(resourceRoot + "findFaersADRs.sql");
    sql = SqlRender.renderSql(sql, params, values);
		sb.append(sql + "\n\n");

		sql = ResourceHelper.GetResourceAsString(resourceRoot + "pullEvidence.sql");
    sql = SqlRender.renderSql(sql, params, values);
		sb.append(sql + "\n\n");
		
		sql = ResourceHelper.GetResourceAsString(resourceRoot + "summarizeEvidence.sql");
    sql = SqlRender.renderSql(sql, params, values);
		sql = SqlRender.renderSql(sql, 
			new String[] {"broadConceptsData", "drugInducedConditionsData", "pregnancyConditionData", "conceptsToExclude", "conceptsToInclude"}, 
			new String[] {broadConceptsData, drugInducedConditionsData, pregnancyConditionData, conceptsToExcludeData, conceptsToIncludeData});
		sb.append(sql + "\n\n");
		
		sql = ResourceHelper.GetResourceAsString(resourceRoot + "optimizeEvidence.sql");
    sql = SqlRender.renderSql(sql, params, values);
		sb.append(sql + "\n\n");
		
		sql = ResourceHelper.GetResourceAsString(resourceRoot + "deleteJobResults.sql");
    sql = SqlRender.renderSql(sql, params, values);
		sql = SqlRender.renderSql(sql, 
			new String[] {"jobId"}, 
			new String[] {Long.toString(jobId)});
		sb.append(sql + "\n\n");
		
		sql = ResourceHelper.GetResourceAsString(resourceRoot + "exportNegativeControls.sql");
    sql = SqlRender.renderSql(sql, params, values);
		sql = SqlRender.renderSql(sql, 
			new String[] {"jobId"}, 
			new String[] {Long.toString(jobId)});
		sb.append(sql + "\n\n");
		
		sql = SqlTranslate.translateSql(sb.toString(), source.getSourceDialect());

		return sql;
	}

  public static String getNegativeControlDeleteStatementSql(NegativeControlTaskParameters task){
    String sql = ResourceHelper.GetResourceAsString("/resources/evidence/sql/negativecontrols/deleteNegativeControls.sql");
    sql = SqlRender.renderSql(sql, new String[] { "ohdsiSchema" },  new String[] { task.getOhdsiSchema() });
    sql = SqlTranslate.translateSql(sql, task.getSourceDialect());

    return sql;
}

  public static String getNegativeControlInsertStatementSql(NegativeControlTaskParameters task){
    String sql = ResourceHelper.GetResourceAsString("/resources/evidence/sql/negativecontrols/insertNegativeControls.sql");
    sql = SqlRender.renderSql(sql, new String[] { "ohdsiSchema" },  new String[] { task.getOhdsiSchema() });
    sql = SqlTranslate.translateSql(sql, task.getSourceDialect());

    return sql;
}
	
	public static String getNegativeControlsFromEvidenceSource(NegativeControlTaskParameters task, Long jobId) {
    String resourceRoot = "/resources/evidence/sql/negativecontrols/";
		String sql = ResourceHelper.GetResourceAsString(resourceRoot + "getNegativeControls.sql");
		String evidenceSchema = task.getSource().getTableQualifier(SourceDaimon.DaimonType.Evidence);
    String[] params = new String[]{"CONCEPT_SET_ID", "CONCEPT_SET_NAME", "outcomeOfInterest", "SOURCE_ID", "evidenceSchema", "jobId"};
    String[] values = new String[]{String.valueOf(task.getConceptSetId()), task.getConceptSetName(), task.getOutcomeOfInterest().toUpperCase(), String.valueOf(task.getSource().getSourceId()), evidenceSchema, Long.toString(jobId)};
    sql = SqlRender.renderSql(sql, params, values);

    return sql;
		
	}

  protected PreparedStatementRenderer prepareGetDrugHoiEvidence(final String key, Source source) {

    String[] par = key.split("-");
    String drug_id = par[0];
    String hoi_id = par[1];
    String sqlPath = "/resources/evidence/sql/getDrugHoiEvidence.sql";
    String evidenceSchema = source.getTableQualifier(SourceDaimon.DaimonType.Evidence);
		String vocabularySchema = evidenceSchema;
		try {
			vocabularySchema = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
		} catch (Exception e) {
			// Ignore - The vocabularySchema will default to the evidence schema
		}
    String[] tableQualifierNames = new String[]{"evidenceSchema", "vocabularySchema"};
    String[] tableQualifierValues = new String[]{evidenceSchema, vocabularySchema};
    String[] names = new String[]{"drug_id", "hoi_id"};
    Object[] values = new Integer[]{Integer.parseInt(drug_id), Integer.parseInt(hoi_id)};
    return new PreparedStatementRenderer(source, sqlPath, tableQualifierNames, tableQualifierValues, names, values);
  }

	protected PreparedStatementRenderer prepareGetEvidenceForConcept(Source source, Long conceptId) {
    String sqlPath = "/resources/evidence/sql/getEvidenceForConcept.sql";
    String evidenceSchema = source.getTableQualifier(SourceDaimon.DaimonType.Evidence);
		String vocabularySchema = evidenceSchema;
		try {
			vocabularySchema = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
		} catch (Exception e) {
			// Ignore - The vocabularySchema will default to the evidence schema
		}
    String[] tableQualifierNames = new String[]{"evidenceSchema", "vocabularySchema"};
    String[] tableQualifierValues = new String[]{evidenceSchema, vocabularySchema};
    String[] names = new String[]{"id"};
    Object[] values = new Long[]{conceptId};
    return new PreparedStatementRenderer(source, sqlPath, tableQualifierNames, tableQualifierValues, names, values);
  }
	
  protected PreparedStatementRenderer prepareGetEvidenceSummaryBySource(
      String conditionID, String drugID, String evidenceGroup,
      Source source) {

    String sqlPath = "/resources/evidence/sql/getEvidenceSummaryBySource.sql";
    String tqName = "tableQualifier";
    String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Evidence);

    String evidenceType = null;
    if (evidenceGroup.equalsIgnoreCase("Literature"))
      evidenceType = "MEDLINE";
    String[] names = new String[]{"drugID", "conditionID", "evidenceGroup"};
    Object[] values = new Object[]{Integer.valueOf(drugID), Integer.valueOf(conditionID), evidenceType};
    return new PreparedStatementRenderer(source, sqlPath, tqName, tqValue, names, values);
  }
  
  protected PreparedStatementRenderer prepareGetEvidenceDetails(
      Integer conditionID, Integer drugID, String evidenceType,
      Source source) {

    String sqlPath = "/resources/evidence/sql/getEvidenceDetails.sql";
    String tqName = "tableQualifier";
    String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Evidence);
    String[] names = new String[]{"drugID", "conditionID", "evidenceType"};
    Object[] values = new Object[]{drugID, conditionID, evidenceType};
    PreparedStatementRenderer psr = new PreparedStatementRenderer(source, sqlPath, tqName, tqValue, names, values);
    return psr;
  }

	protected PreparedStatementRenderer prepareGetSpontaneousReports(
      EvidenceSearch search, Source source) {

    String sqlPath = "/resources/evidence/sql/getSpontaneousReports.sql";
    String tqName = "tableQualifier";
    String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Evidence);
    String[] names = new String[]{"conditionConceptList", "ingredientConceptList"};
    List<Integer> conditionConceptArray =  Arrays.stream(search.conditionConceptList).map(NumberUtils::toInt).collect(Collectors.toList());
    List<Integer> ingredientConceptArray =  Arrays.stream(search.ingredientConceptList).map(NumberUtils::toInt).collect(Collectors.toList());
    Object[] variableValues = new Object[]{conditionConceptArray.<Integer>toArray(), ingredientConceptArray.<Integer>toArray()};

    return new PreparedStatementRenderer(source, sqlPath, tqName, tqValue, names, variableValues);
  }

  protected PreparedStatementRenderer prepareEvidenceSearch(EvidenceSearch search, Source source) {

    String resourcePath = "/resources/evidence/sql/getEvidenceFromUniverse.sql";
    String[] searchRegexes = new String[]{"tableQualifier"};
    String[] replacementStrings = new String[]{source.getTableQualifier(SourceDaimon.DaimonType.Evidence)};
    String[] names = new String[]{"conditionConceptList", "ingredientConceptList", "evidenceTypeList"};
    List<Integer> conditionConceptArray =  Arrays.stream(search.conditionConceptList).map(NumberUtils::toInt).collect(Collectors.toList());
    List<Integer> ingredientConceptArray =  Arrays.stream(search.ingredientConceptList).map(NumberUtils::toInt).collect(Collectors.toList());
    Object[] values = new Object[]{conditionConceptArray.<Integer>toArray(), ingredientConceptArray.<Integer>toArray(), search.evidenceTypeList};

    return new PreparedStatementRenderer(source, resourcePath, searchRegexes, replacementStrings, names, values);
  }
	
  protected PreparedStatementRenderer prepareLabelEvidence(EvidenceSearch search, Source source) {

    String resourcePath = "/resources/evidence/sql/getLabelEvidence.sql";
    String[] searchStrings = new String[]{"tableQualifier"};
    String[] replacementStrings = new String[]{source.getTableQualifier(SourceDaimon.DaimonType.Evidence)};
    String[] variableNames = new String[]{"conditionConceptList", "ingredientConceptList", "evidenceTypeList"};
    List<Integer> conditionConceptArray =  Arrays.stream(search.conditionConceptList).map(NumberUtils::toInt).collect(Collectors.toList());
    List<Integer> ingredientConceptArray =  Arrays.stream(search.ingredientConceptList).map(NumberUtils::toInt).collect(Collectors.toList());
    Object[] variableValues = new Object[]{conditionConceptArray.<Integer>toArray(), ingredientConceptArray.<Integer>toArray(), search.evidenceTypeList};

    return new PreparedStatementRenderer(source, resourcePath, searchStrings, replacementStrings, variableNames, variableValues);
  }
	
	//parse ADRAnnotation linkouts
  private EvidenceDetails getADRlinkout(JSONArray lineItems,int j) throws JSONException {
	  EvidenceDetails e = new EvidenceDetails();
	  JSONObject tempItem = lineItems.getJSONObject(j);
      JSONObject tempSource;
      if(tempItem.has("an")) {
    	  tempSource = tempItem.getJSONObject("an");
    	  e.label = tempSource.getString("value");
      }
      if(tempItem.has("body")) {
    	  tempSource = tempItem.getJSONObject("body");
    	  e.bodyLabel = tempSource.getString("value");
      }
      if(tempItem.has("target")) {
    	  tempSource = tempItem.getJSONObject("target");
    	  e.target = tempSource.getString("value");
      }
      if(tempItem.has("sourceURL")) {
    	  tempSource = tempItem.getJSONObject("sourceURL");
    	  e.sourceURL = tempSource.getString("value");
      }
      if(tempItem.has("selector")) {
    	  tempSource = tempItem.getJSONObject("selector");
    	  e.selector = tempSource.getString("value");
      }
      if(tempItem.has("spl")) {
    	  tempSource = tempItem.getJSONObject("spl");
    	  e.splSection = tempSource.getString("value");
      }
      if(tempItem.has("text")) {
    	  tempSource = tempItem.getJSONObject("text");
    	  e.text = tempSource.getString("value");
      }
	  return e;
  }
  
  //parse Mesh linkouts
  private EvidenceDetails getPubMedlinkout(JSONArray lineItems,int j) throws JSONException {
	  EvidenceDetails e = new EvidenceDetails();
	  JSONObject tempItem = lineItems.getJSONObject(j);
      JSONObject tempSource;
      if(tempItem.has("an")) {
    	  tempSource = tempItem.getJSONObject("an");
    	  e.label = tempSource.getString("value");
      }
      if(tempItem.has("source")) {
    	  tempSource = tempItem.getJSONObject("source");
    	  e.sourceURL = tempSource.getString("value");
      }
      if(tempItem.has("pmid")) {
    	  tempSource = tempItem.getJSONObject("pmid");
    	  e.sourceURL = tempSource.getString("value");
      }

    return e;
  }
  
  //parse SemMed linkouts
  private EvidenceDetails getSemMedlinkout(JSONArray lineItems,int j) throws JSONException {
	  EvidenceDetails e = new EvidenceDetails();
	  JSONObject tempItem = lineItems.getJSONObject(j);
      JSONObject tempSource;
      if(tempItem.has("predicateLab")) {
    	  tempSource= tempItem.getJSONObject("predicateLab");
    	  e.predicateLabel = tempSource.getString("value");
      }
      if(tempItem.has("pmid")) {
    	  tempSource = tempItem.getJSONObject("pmid");
    	  e.sourceURL = tempSource.getString("value");
      }
      if(tempItem.has("studyType")) {
    	  tempSource = tempItem.getJSONObject("studyType");
    	  e.studyType = tempSource.getString("value");
      }
      if(tempItem.has("exact")) {
    	  tempSource = tempItem.getJSONObject("exact");
    	  e.exact = tempSource.getString("value");
      }
      if(tempItem.has("prefix")) {
    	  tempSource = tempItem.getJSONObject("prefix");
    	  e.prefix = tempSource.getString("value");
      }
      if(tempItem.has("postfix")) {
    	  tempSource = tempItem.getJSONObject("postfix");
    	  e.postfix = tempSource.getString("value");
      }
	  return e;
  }
  
  private static String JoinArray(final String[] array) {
    String result = "";

    for (int i = 0; i < array.length; i++) {
      if (i > 0) {
        result += ",";
      }

      result += "'" + array[i] + "'";
    }

    return result;
  }

  private static String limitJobParams(String param) {
            if (param.length() >= 250) {
                    return param.substring(0, 245) + "...";
            }
            return param;
    }
}
