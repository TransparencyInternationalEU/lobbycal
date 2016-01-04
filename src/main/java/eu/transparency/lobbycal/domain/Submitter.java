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
 * A Submitter. as
 */



@Entity
@Table(name = "SUBMITTER")
@Document(indexName="submitter")
public class Submitter implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Size(min = 6, max = 70)
    @Column(name = "email", length = 70, nullable = false)
    private String email;

    @Column(name = "active")
    private Boolean active = Boolean.FALSE;


    @ManyToOne
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

        Submitter submitter = (Submitter) o;

        if ( ! Objects.equals(id, submitter.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Submitter{" +
                "id=" + id +
                ", email='" + email + "'" +
                ", active='" + active + "'" +
                '}';
    }
}
