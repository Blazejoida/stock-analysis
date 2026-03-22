package pl.polsl.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity representing a single analysis operation.
 * Stores information about performed stock analysis.
 * 
 * @author Blazej Jamrozik
 * @version 1.0
 */
@Entity
@Table(name = "ANALYSIS_OPERATIONS")
@NamedQueries({
    @NamedQuery(name = "AnalysisOperation.findAll", 
                query = "SELECT a FROM AnalysisOperation a ORDER BY a.timestamp DESC"),
    @NamedQuery(name = "AnalysisOperation.findByType", 
                query = "SELECT a FROM AnalysisOperation a WHERE a.analysisType = :type ORDER BY a.timestamp DESC")
})
public class AnalysisOperation {
    
    /**
     * Primary key - auto-generated ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;
    
    /**
     * Type of analysis performed.
     */
    @Column(name = "ANALYSIS_TYPE", nullable = false, length = 50)
    private String analysisType;
    
    /**
     * File path used for analysis.
     */
    @Column(name = "FILE_PATH", nullable = false, length = 500)
    private String filePath;
    
    /**
     * Timestamp when analysis was performed.
     */
    @Column(name = "TIMESTAMP", nullable = false)
    private LocalDateTime timestamp;
    
    /**
     * Result of the analysis as text.
     */
    @Column(name = "RESULT_TEXT", length = 2000)
    private String resultText;
    
    /**
     * Number of records processed.
     */
    @Column(name = "RECORDS_COUNT")
    private Integer recordsCount;
    
    /**
     * One-to-many relationship with result details.
     */
    @OneToMany(mappedBy = "analysisOperation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnalysisResultDetail> resultDetails = new ArrayList<>();
    
    /**
     * Default constructor required by JPA.
     */
    public AnalysisOperation() {
    }
    
    /**
     * Constructor with parameters.
     * 
     * @param analysisType type of analysis
     * @param filePath file path used
     * @param timestamp timestamp of operation
     * @param resultText result as text
     * @param recordsCount number of records
     */
    public AnalysisOperation(String analysisType, String filePath, LocalDateTime timestamp, 
                            String resultText, Integer recordsCount) {
        this.analysisType = analysisType;
        this.filePath = filePath;
        this.timestamp = timestamp;
        this.resultText = resultText;
        this.recordsCount = recordsCount;
    }
    
    /**
     * Gets ID.
     * 
     * @return the ID
     */
    public Long getId() {
        return id;
    }
    
    /**
     * Sets ID.
     * 
     * @param id the ID to set
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    /**
     * Gets analysis type.
     * 
     * @return the analysis type
     */
    public String getAnalysisType() {
        return analysisType;
    }
    
    /**
     * Sets analysis type.
     * 
     * @param analysisType the analysis type to set
     */
    public void setAnalysisType(String analysisType) {
        this.analysisType = analysisType;
    }
    
    /**
     * Gets file path.
     * 
     * @return the file path
     */
    public String getFilePath() {
        return filePath;
    }
    
    /**
     * Sets file path.
     * 
     * @param filePath the file path to set
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    /**
     * Gets timestamp.
     * 
     * @return the timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    /**
     * Sets timestamp.
     * 
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * Gets result text.
     * 
     * @return the result text
     */
    public String getResultText() {
        return resultText;
    }
    
    /**
     * Sets result text.
     * 
     * @param resultText the result text to set
     */
    public void setResultText(String resultText) {
        this.resultText = resultText;
    }
    
    /**
     * Gets records count.
     * 
     * @return the records count
     */
    public Integer getRecordsCount() {
        return recordsCount;
    }
    
    /**
     * Sets records count.
     * 
     * @param recordsCount the records count to set
     */
    public void setRecordsCount(Integer recordsCount) {
        this.recordsCount = recordsCount;
    }
    
    /**
     * Gets result details.
     * 
     * @return list of result details
     */
    public List<AnalysisResultDetail> getResultDetails() {
        return resultDetails;
    }
    
    /**
     * Sets result details.
     * 
     * @param resultDetails the result details to set
     */
    public void setResultDetails(List<AnalysisResultDetail> resultDetails) {
        this.resultDetails = resultDetails;
    }
    
    /**
     * Adds a result detail.
     * 
     * @param detail the detail to add
     */
    public void addResultDetail(AnalysisResultDetail detail) {
        resultDetails.add(detail);
        detail.setAnalysisOperation(this);
    }
    
    /**
     * Removes a result detail.
     * 
     * @param detail the detail to remove
     */
    public void removeResultDetail(AnalysisResultDetail detail) {
        resultDetails.remove(detail);
        detail.setAnalysisOperation(null);
    }
    
    /**
     * Computes hash code based on ID.
     * 
     * @return hash code
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }
    
    /**
     * Checks equality based on ID.
     * 
     * @param object object to compare
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof AnalysisOperation)) {
            return false;
        }
        AnalysisOperation other = (AnalysisOperation) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }
    
    /**
     * Returns string representation of the entity.
     * 
     * @return string representation
     */
    @Override
    public String toString() {
        return "AnalysisOperation[ id=" + id + ", type=" + analysisType + ", timestamp=" + timestamp + " ]";
    }
}