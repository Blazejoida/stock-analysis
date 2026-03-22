package pl.polsl.entity;

import jakarta.persistence.*;

/**
 * JPA Entity representing detailed results of an analysis operation.
 * Related to AnalysisOperation via many-to-one relationship.
 * 
 * @author Blazej Jamrozik
 * @version 1.0
 */
@Entity
@Table(name = "ANALYSIS_RESULT_DETAILS")
@NamedQueries({
    @NamedQuery(name = "AnalysisResultDetail.findAll", 
                query = "SELECT d FROM AnalysisResultDetail d"),
    @NamedQuery(name = "AnalysisResultDetail.findByOperationId", 
                query = "SELECT d FROM AnalysisResultDetail d WHERE d.analysisOperation.id = :operationId")
})
public class AnalysisResultDetail {
    
    /**
     * Primary key - auto-generated ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;
    
    /**
     * Key describing what this detail represents.
     */
    @Column(name = "DETAIL_KEY", nullable = false, length = 100)
    private String detailKey;
    
    /**
     * Value of this detail.
     */
    @Column(name = "DETAIL_VALUE", length = 1000)
    private String detailValue;
    
    /**
     * Many-to-one relationship with analysis operation.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OPERATION_ID", nullable = false)
    private AnalysisOperation analysisOperation;
    
    /**
     * Default constructor required by JPA.
     */
    public AnalysisResultDetail() {
    }
    
    /**
     * Constructor with parameters.
     * 
     * @param detailKey key of the detail
     * @param detailValue value of the detail
     */
    public AnalysisResultDetail(String detailKey, String detailValue) {
        this.detailKey = detailKey;
        this.detailValue = detailValue;
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
     * Gets detail key.
     * 
     * @return the detail key
     */
    public String getDetailKey() {
        return detailKey;
    }
    
    /**
     * Sets detail key.
     * 
     * @param detailKey the detail key to set
     */
    public void setDetailKey(String detailKey) {
        this.detailKey = detailKey;
    }
    
    /**
     * Gets detail value.
     * 
     * @return the detail value
     */
    public String getDetailValue() {
        return detailValue;
    }
    
    /**
     * Sets detail value.
     * 
     * @param detailValue the detail value to set
     */
    public void setDetailValue(String detailValue) {
        this.detailValue = detailValue;
    }
    
    /**
     * Gets analysis operation.
     * 
     * @return the analysis operation
     */
    public AnalysisOperation getAnalysisOperation() {
        return analysisOperation;
    }
    
    /**
     * Sets analysis operation.
     * 
     * @param analysisOperation the analysis operation to set
     */
    public void setAnalysisOperation(AnalysisOperation analysisOperation) {
        this.analysisOperation = analysisOperation;
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
        if (!(object instanceof AnalysisResultDetail)) {
            return false;
        }
        AnalysisResultDetail other = (AnalysisResultDetail) object;
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
        return "AnalysisResultDetail[ id=" + id + ", key=" + detailKey + " ]";
    }
}