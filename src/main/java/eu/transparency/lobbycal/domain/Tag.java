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

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A Tag.
 */
@Entity
@Table(name = "TAG")
@Document(indexName="tag")
public class Tag implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 2576407303364113935L;

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "i18n_key")
    private String i18nKey;

    @Column(name = "de")
    private String de;

    @Column(name = "en")
    private String en;

    @Column(name = "fr")
    private String fr;

    @Column(name = "es")
    private String es;

    @ManyToMany(mappedBy = "tags")
    @JsonIgnore
    private Set<Meeting> meetings = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String geti18nKey() {
        return i18nKey;
    }

    public void seti18nKey(String i18nKey) {
        this.i18nKey = i18nKey;
    }

    public String getDe() {
        return de;
    }

    public void setDe(String de) {
        this.de = de;
    }

    public String getEn() {
        return en;
    }

    public void setEn(String en) {
        this.en = en;
    }

    public String getFr() {
        return fr;
    }

    public void setFr(String fr) {
        this.fr = fr;
    }

    public String getEs() {
        return es;
    }

    public void setEs(String es) {
        this.es = es;
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

        Tag tag = (Tag) o;

        if ( ! Objects.equals(id, tag.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Tag{" +
                "id=" + id +
                ", i18nKey='" + i18nKey + "'" +
                ", de='" + de + "'" +
                ", en='" + en + "'" +
                ", fr='" + fr + "'" +
                ", es='" + es + "'" +
                '}';
    }
}
