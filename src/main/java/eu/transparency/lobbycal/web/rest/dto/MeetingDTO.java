package eu.transparency.lobbycal.web.rest.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import eu.transparency.lobbycal.domain.util.JSR310DateTimeSerializer;
import eu.transparency.lobbycal.domain.util.JSR310LocalDateDeserializer;

/**
 * A DTO for the Meeting entity.
 */
public class MeetingDTO implements Serializable {

	private static final long serialVersionUID = -8986664418417104916L;

	public MeetingDTO() {
	}

	@JsonView(DataTablesOutput.View.class)
	private Long id;

	@JsonView(DataTablesOutput.View.class)
	private String title;

	@JsonView(DataTablesOutput.View.class)
	private String submitter;

	@JsonView(DataTablesOutput.View.class)
	private String aliasUsed;

	@JsonView(DataTablesOutput.View.class)
	private ZonedDateTime startDate;

	@JsonView(DataTablesOutput.View.class)
	private ZonedDateTime endDate;

	private String uid;

	@JsonView(DataTablesOutput.View.class)
	private Set<TagDTO> tags = new HashSet<>();

	@JsonView(DataTablesOutput.View.class)
	private Set<PartnerDTO> partners = new HashSet<>();

	@JsonView(DataTablesOutput.View.class)
	private Long userId;

	private String userLogin;

	@JsonView(DataTablesOutput.View.class)
	private String userFirstName;

	@JsonView(DataTablesOutput.View.class)
	private String userLastName;

	/**
	 * @since lobbcal v.2
	 */
	@JsonView(DataTablesOutput.View.class)
	private String mTag;

	@JsonView(DataTablesOutput.View.class)
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

	public String getUserFirstName() {

		return userFirstName;
	}

	public void setUserFirstName(String userFirstName) {

		this.userFirstName = userFirstName;
	}

	public String getUserLastName() {

		return userLastName;
	}

	public void setUserLastName(String userLastName) {

		this.userLastName = userLastName;
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

	public Set<TagDTO> getTags() {

		return tags;
	}

	public void setTags(Set<TagDTO> tags) {

		this.tags = tags;
	}

	public Set<PartnerDTO> getPartners() {

		return partners;
	}

	public void setPartners(Set<PartnerDTO> partners) {

		this.partners = partners;
	}

	public Long getUserId() {

		return userId;
	}

	public void setUserId(Long userId) {

		this.userId = userId;
	}

	public String getUserLogin() {

		return userLogin;
	}

	public void setUserLogin(String userLogin) {

		this.userLogin = userLogin;
	}

	@Override
	public boolean equals(Object o) {

		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		MeetingDTO meetingDTO = (MeetingDTO) o;

		if (!Objects.equals(id, meetingDTO.id))
			return false;

		return true;
	}

	@Override
	public int hashCode() {

		return Objects.hashCode(id);
	}

	@Override
	public String toString() {

		return "MeetingDTO [id=" + id + ", title=" + title + ", submitter=" + submitter + ", aliasUsed=" + aliasUsed
				+ ", startDate=" + startDate + ", endDate=" + endDate + ", uid=" + uid + ", tags=" + tags
				+ ", partners=" + partners + ", userId=" + userId + ", userLogin=" + userLogin + ", userFirstName="
				+ userFirstName + ", userLastName=" + userLastName + ", mTag=" + mTag + ", mPartner=" + mPartner + "]";
	}
}
