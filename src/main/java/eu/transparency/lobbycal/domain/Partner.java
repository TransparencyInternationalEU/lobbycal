package eu.transparency.lobbycal.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import eu.transparency.lobbycal.domain.util.CustomDateTimeDeserializer;
import eu.transparency.lobbycal.domain.util.CustomDateTimeSerializer;
import eu.transparency.lobbycal.domain.util.CustomLocalDateSerializer;
import eu.transparency.lobbycal.domain.util.ISO8601LocalDateDeserializer;

import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;
import org.joda.time.DateTime;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A Partner.
 */
@Entity
@Table(name = "PARTNER")
@Document(indexName="partner")
public class Partner implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "name")
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

        if ( ! Objects.equals(id, partner.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Partner{" +
                "id=" + id +
                ", name='" + name + "'" +
                ", transparencyRegisterID='" + transparencyRegisterID + "'" +
                '}';
    }
}
