package eu.transparency.lobbycal.domain;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.data.elasticsearch.annotations.Document;

/**
 * A Alias. 
 */
@Entity
@Table(name = "ALIAS")
@Document(indexName = "alias")
public class Alias implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@NotNull
	@Size(min = 5, max = 20)
	@Column(name = "alias", length = 20, nullable = false, unique=true)
	private String alias;
	

	@Column(name = "active")
	private Boolean active;

	@ManyToOne
	private User user;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}


	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Alias alias = (Alias) o;

		if (!Objects.equals(id, alias.id))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}

	@Override
	public String toString() {
		return "Alias{" + "id=" + id + ", alias='" + alias + "'"
				 + "'" + ", active='" + active + "'"
				+ '}';
	}
}
