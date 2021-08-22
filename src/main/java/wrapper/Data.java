package wrapper;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "timestamp",
        "sourceBank",
        "sourceAccount",
        "sourceCheck",
        "destinationBank",
        "destinationAccount",
        "destinationCheck"
})
@Generated("jsonschema2pojo")
public class Data {

    @JsonProperty("timestamp")
    private String timestamp;
    @JsonProperty("sourceBank")
    private String sourceBank;
    @JsonProperty("sourceAccount")
    private Integer sourceAccount;
    @JsonProperty("sourceCheck")
    private Integer sourceCheck;
    @JsonProperty("destinationBank")
    private String destinationBank;
    @JsonProperty("destinationAccount")
    private Integer destinationAccount;
    @JsonProperty("destinationCheck")
    private Integer destinationCheck;
    @JsonProperty("q")
    private Integer q;
    @JsonProperty("number")
    private Integer number;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("timestamp")
    public String getTimestamp() {
        return timestamp;
    }

    @JsonProperty("timestamp")
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @JsonProperty("sourceBank")
    public String getSourceBank() {
        return sourceBank;
    }

    @JsonProperty("sourceBank")
    public void setSourceBank(String sourceBank) {
        this.sourceBank = sourceBank;
    }

    @JsonProperty("sourceAccount")
    public Integer getSourceAccount() {
        return sourceAccount;
    }

    @JsonProperty("sourceAccount")
    public void setSourceAccount(Integer sourceAccount) {
        this.sourceAccount = sourceAccount;
    }

    @JsonProperty("sourceCheck")
    public Integer getSourceCheck() {
        return sourceCheck;
    }

    @JsonProperty("sourceCheck")
    public void setSourceCheck(Integer sourceCheck) {
        this.sourceCheck = sourceCheck;
    }

    @JsonProperty("destinationBank")
    public String getDestinationBank() {
        return destinationBank;
    }

    @JsonProperty("destinationBank")
    public void setDestinationBank(String destinationBank) {
        this.destinationBank = destinationBank;
    }

    @JsonProperty("destinationAccount")
    public Integer getDestinationAccount() {
        return destinationAccount;
    }

    @JsonProperty("destinationAccount")
    public void setDestinationAccount(Integer destinationAccount) {
        this.destinationAccount = destinationAccount;
    }

    @JsonProperty("destinationCheck")
    public Integer getDestinationCheck() {
        return destinationCheck;
    }

    @JsonProperty("destinationCheck")
    public void setDestinationCheck(Integer destinationCheck) {
        this.destinationCheck = destinationCheck;
    }

    @JsonProperty("q")
    public Integer getQ() {
        return q;
    }

    @JsonProperty("q")
    public void setQ(Integer q) {
        this.q = q;
    }

    @JsonProperty("number")
    public Integer getNumber() {
        return number;
    }

    @JsonProperty("number")
    public void setNumber(Integer number) {
        this.number = number;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public Data(String timestamp, String sourceBank, Integer sourceAccount, Integer sourceCheck, String destinationBank, Integer destinationAccount, Integer destinationCheck, Integer q, Integer number) {
        this.timestamp = timestamp;
        this.sourceBank = sourceBank;
        this.sourceAccount = sourceAccount;
        this.sourceCheck = sourceCheck;
        this.destinationBank = destinationBank;
        this.destinationAccount = destinationAccount;
        this.destinationCheck = destinationCheck;
        this.q = q;
        this.number = number;
    }

    public Data() {
    }
}