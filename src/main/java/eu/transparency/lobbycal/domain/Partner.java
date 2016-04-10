package eu.transparency.lobbycal.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A Partner.
 */
@Entity
@Table(name = "PARTNER")
@Document(indexName = "partner")
public class Partner implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "name")
	@Field(type = FieldType.String )
	private String name;

	@Column(name = "transparency_register_id")
	private String transparencyRegisterID;

	@ManyToMany(mappedBy = "partners")
	@JsonIgnore
	private Set<Meeting> meetings = new HashSet<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTransparencyRegisterID() {
		return transparencyRegisterID;
	}

	public void setTransparencyRegisterID(String transparencyRegisterID) {
		this.transparencyRegisterID = transparencyRegisterID;
	}

	public Set<Meeting> getMeetings() {
		return meetings;
	}

	public void setMeetings(Set<Meeting> meetings) {
		this.meetings = meetings;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Partner partner = (Partner) o;

		if (!Objects.equals(id, partner.id))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}

	@Override
	public String toString() {
		return "Partner{" + "id=" + id + ", name='" + name + "'" + ", transparencyRegisterID='" + transparencyRegisterID
				+ "'" + '}';
	}
}
