package eu.transparency.lobbycal.web.rest.dto;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.fasterxml.jackson.annotation.JsonView;

/**
 * A DTO for the Tag entity.
 */
public class TagDTO implements Serializable {

	@JsonView(DataTablesOutput.View.class)
    private Long id;

	@JsonView(DataTablesOutput.View.class)
    private String i18nKey;

	@JsonView(DataTablesOutput.View.class)
    private String de;

	@JsonView(DataTablesOutput.View.class)
    private String en;

	@JsonView(DataTablesOutput.View.class)
    private String fr;

	@JsonView(DataTablesOutput.View.class)
    private String es;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TagDTO tagDTO = (TagDTO) o;

        if ( ! Objects.equals(id, tagDTO.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "TagDTO{" +
                "id=" + id +
                ", i18nKey='" + i18nKey + "'" +
                ", de='" + de + "'" +
                ", en='" + en + "'" +
                ", fr='" + fr + "'" +
                ", es='" + es + "'" +
                '}';
    }
}
