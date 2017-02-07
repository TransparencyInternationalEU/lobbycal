package eu.transparency.lobbycal.domain;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A Meeting.
 */
@Entity
@Table(name = "MEETING")
@Document(indexName = "meeting")
public class Meeting implements Serializable {

	public Meeting() {
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "title", length = 1000)
	@Field(type = FieldType.String, index = FieldIndex.analyzed)
	private String title;

	@Column(name = "submitter")
	private String submitter;

	@Column(name = "alias_used")
	private String aliasUsed;

	@Column(name = "start_date")
	private ZonedDateTime startDate;

	@Column(name = "end_date")
	private ZonedDateTime endDate;

	
	@CreatedDate
    @NotNull
    @Column(name = "created_date", nullable = false)
    @JsonIgnore
    private ZonedDateTime createdDate = ZonedDateTime.now();

	
	
	@Column(name = "u_id")
	private String uid;

	@Field(type = FieldType.Nested)
	@ManyToMany(cascade = { CascadeType.MERGE })
	@JoinTable(name = "MEETING_TAG", joinColumns = @JoinColumn(name = "meetings_id", referencedColumnName = "ID") , inverseJoinColumns = @JoinColumn(name = "tags_id", referencedColumnName = "ID") )
	private Set<Tag> tags = new HashSet<>();

	@Field(type = FieldType.Nested)
	@ManyToMany(cascade = { CascadeType.MERGE })
	@JoinTable(name = "MEETING_PARTNER", joinColumns = @JoinColumn(name = "meetings_id", referencedColumnName = "ID") , inverseJoinColumns = @JoinColumn(name = "partners_id", referencedColumnName = "ID") )
	private Set<Partner> partners = new HashSet<>();

	@ManyToOne
	@Field(type = FieldType.Object)
	private User user;

	/**
	 * @since lobbcal v.2
	 */
	@Column(name = "mTag")
	private String mTag;

	@Column(name = "mPartner")
	private String mPartner;

	public String getmTag() {

		return mTag;
	}

	public void setmTag(String mTag) {

		this.mTag = mTag;
	}

	public String getmPartner() {

		return mPartner;
	}

	public void setmPartner(String mPartner) {

		this.mPartner = mPartner;
	}

	public Long getId() {

		return id;
	}

	public void setId(Long id) {

		this.id = id;
	}

	public String getTitle() {

		return title;
	}

	public void setTitle(String title) {

		this.title = title;
	}

	public String getSubmitter() {

		return submitter;
	}

	public void setSubmitter(String submitter) {

		this.submitter = submitter;
	}

	public String getAliasUsed() {

		return aliasUsed;
	}

	public void setAliasUsed(String aliasUsed) {

		this.aliasUsed = aliasUsed;
	}

	public ZonedDateTime getStartDate() {

		return startDate;
	}

	public void setStartDate(ZonedDateTime startDate) {

		this.startDate = startDate;
	}

	public ZonedDateTime getEndDate() {

		return endDate;
	}

	public void setEndDate(ZonedDateTime endDate) {

		this.endDate = endDate;
	}

	public String getUid() {

		return uid;
	}

	public void setUid(String uid) {

		this.uid = uid;
	}

	public Set<Tag> getTags() {

		return tags;
	}

	public void setTags(Set<Tag> tags) {

		this.tags = tags;
	}

	public Set<Partner> getPartners() {

		return partners;
	}

	public void setPartners(Set<Partner> partners) {

		this.partners = partners;
	}

	public User getUser() {

		return user;
	}

	public void setUser(User user) {

		this.user = user;
	}
	
	 public ZonedDateTime getCreatedDate() {
	        return createdDate;
	    }

	    public void setCreatedDate(ZonedDateTime createdDate) {
	        this.createdDate = createdDate;
	    }


	@Override
	public boolean equals(Object o) {

		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Meeting meeting = (Meeting) o;

		if (!Objects.equals(id, meeting.id))
			return false;

		return true;
	}

	@Override
	public String toString() {

		return "Meeting [id=" + id + ", title=" + title + ", submitter=" + submitter + ", aliasUsed=" + aliasUsed
				+ ", startDate=" + startDate + ", endDate=" + endDate + ", uid=" + uid + ", tags=" + tags
				+ ", partners=" + partners + ", user=" + user + ", mTag=" + mTag + ", mPartner=" + mPartner + "]";
	}

	@Override
	public int hashCode() {

		return Objects.hashCode(id);
	}
}
