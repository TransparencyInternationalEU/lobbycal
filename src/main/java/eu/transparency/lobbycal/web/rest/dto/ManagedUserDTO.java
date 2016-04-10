package eu.transparency.lobbycal.web.rest.dto;

import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transparency.lobbycal.domain.User;

/**
 * A DTO extending the UserDTO, which is meant to be used in the user management
 * UI.
 */
public class ManagedUserDTO extends UserDTO {

	private Long id;

	private ZonedDateTime createdDate;

	private String lastModifiedBy;

	private ZonedDateTime lastModifiedDate;
	private final Logger log = LoggerFactory.getLogger(ManagedUserDTO.class);

	public ManagedUserDTO() {
	}

	public ManagedUserDTO(User user) {
		super(user);
		this.id = user.getId();
		this.createdDate = user.getCreatedDate();
		this.lastModifiedBy = user.getLastModifiedBy();
		this.lastModifiedDate = user.getLastModifiedDate();
		log.trace("");
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ZonedDateTime getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(ZonedDateTime createdDate) {
		this.createdDate = createdDate;
	}

	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public ZonedDateTime getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(ZonedDateTime lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	@Override
	public String toString() {
		return "ManagedUserDTO{" + "id=" + id + ", createdDate=" + createdDate
				+ ", lastModifiedBy='" + lastModifiedBy + '\''
				+ ", lastModifiedDate=" + lastModifiedDate + "} "
				+ super.toString();
	}
}
