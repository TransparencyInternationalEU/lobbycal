package eu.transparency.lobbycal.web.rest.dto;

import java.io.Serializable;
import java.util.Objects;

import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.fasterxml.jackson.annotation.JsonView;

/**
 * A DTO for the Partner entity.
 */
public class PartnerDTO implements Serializable {

	@JsonView(DataTablesOutput.View.class)
	private Long id;

	@JsonView(DataTablesOutput.View.class)
	private String name;

	@JsonView(DataTablesOutput.View.class)
	private String transparencyRegisterID;

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

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		PartnerDTO partnerDTO = (PartnerDTO) o;

		if (!Objects.equals(id, partnerDTO.id))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}

	@Override
	public String toString() {
		return "PartnerDTO{" + "id=" + id + ", name='" + name + "'"
				+ ", transparencyRegisterID='" + transparencyRegisterID + "'"
				+ '}';
	}
}
