package org.ohdsi.webapi.vocabulary;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author fdefalco
 */
@JsonInclude(Include.NON_NULL)
public class Concept {
    @JsonProperty("CONCEPT_ID")
    public Long conceptId;
    
    @JsonProperty("CONCEPT_NAME")
    public String conceptName;
    
    @JsonProperty("STANDARD_CONCEPT")
    public String GetStandardConcept() {
        switch (standardConcept) {
            case "N" : 
                return "Non-Standard";
            case "S" :
                return "Standard";
            case "C" :
                return "Classification";
            default:
                return "Unknown";
        }
    }
    
    @JsonIgnore
    public String standardConcept;
    
    @JsonProperty("INVALID_REASON")
    public String GetInvalidReason() {
        switch (invalidReason) {
            case "V" : 
                return "Valid";
            case "D" :
                return "Invalid";
            case "U" :
                return "Invalid";
            default:
                return "Unknown";
        }
    }
    
    @JsonIgnore
    public String invalidReason;
    
    @JsonProperty("CONCEPT_CODE")
    public String conceptCode;
    
    @JsonProperty("DOMAIN_ID")
    public String domainId;
    
    @JsonProperty("VOCABULARY_ID")
    public String vocabularyId;
    
    @JsonProperty("CONCEPT_CLASS_ID")
    public String conceptClassId;
}
