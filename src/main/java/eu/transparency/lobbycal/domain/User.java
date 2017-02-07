package eu.transparency.lobbycal.domain;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;
import org.springframework.data.elasticsearch.annotations.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A user.
 */
@Entity
@Table(name = "JHI_USER")
@Document(indexName="user")
public class User extends AbstractAuditingEntity implements Serializable {

	private static final long serialVersionUID = -7599942749171988866L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@NotNull
	@Pattern(regexp = "^[a-z0-9]*$")
	@Size(min = 5, max = 50)
	@Column(length = 50, unique = true, nullable = false)
	private String login;

	@JsonIgnore
	@NotNull
	@Size(min = 60, max = 60)
	@Column(length = 60)
	private String password;

	@Size(max = 50)
	@Column(name = "first_name", length = 50)
	private String firstName;

	@Size(max = 50)
	@Column(name = "last_name", length = 50)
	private String lastName;

	@Email
	@Size(max = 100)
	@Column(length = 100, unique = true)
	private String email;

	@Column(nullable = false)
	private boolean activated = false;

	@JsonIgnore
	@Column(nullable = false)
	private boolean notificationEnabled = true;

	@JsonIgnore
	@Column(nullable = false)
	private boolean notificationOfSubmittersEnabled = true;

	@JsonIgnore
	@Column(name = "last_notified", nullable = true)
	private ZonedDateTime lastNotified = null;

	
	@JsonIgnore
	@Column(nullable = false)
	private boolean showFutureMeetings = false;

	
	@JsonIgnore
	@Column(nullable = false)
	private boolean lobbycloudSharingEnabled = false;

	
	@Size(min = 2, max = 5)
	@Column(name = "lang_key", length = 5)
	private String langKey;

	@Size(max = 20)
	@Column(name = "activation_key", length = 20)
	@JsonIgnore
	private String activationKey;

	@Size(max = 20)
	@Column(name = "reset_key", length = 20)
	private String resetKey;

	@Column(name = "reset_date", nullable = true)
	private ZonedDateTime resetDate = null;

	@JsonIgnore
	@ManyToMany
	@JoinTable(name = "JHI_USER_AUTHORITY", joinColumns = { @JoinColumn(name = "user_id", referencedColumnName = "id") }, inverseJoinColumns = { @JoinColumn(name = "authority_name", referencedColumnName = "name") })
	private Set<Authority> authorities = new HashSet<>();

	@JsonIgnore
	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "USER_ALIAS", joinColumns = { @JoinColumn(name = "user_id", referencedColumnName = "id") }, inverseJoinColumns = { @JoinColumn(name = "alias_name", referencedColumnName = "alias") })
	private Set<Alias> aliases = new HashSet<>();

	@JsonIgnore
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "user")
	private Set<PersistentToken> persistentTokens = new HashSet<>();

	@JsonIgnore
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "user")
	private Set<Meeting> meetings = new HashSet<>();

	public boolean isShowFutureMeetings() {
	
		return showFutureMeetings;
	}


	public void setShowFutureMeetings(boolean showFutureMeetings) {
	
		this.showFutureMeetings = showFutureMeetings;
	}


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean getActivated() {
		return activated;
	}

	public void setActivated(boolean activated) {
		this.activated = activated;
	}

	public String getActivationKey() {
		return activationKey;
	}

	public void setActivationKey(String activationKey) {
		this.activationKey = activationKey;
	}

	public String getResetKey() {
		return resetKey;
	}

	public void setResetKey(String resetKey) {
		this.resetKey = resetKey;
	}

	public ZonedDateTime getResetDate() {
		return resetDate;
	}

	public void setResetDate(ZonedDateTime resetDate) {
		this.resetDate = resetDate;
	}

	public String getLangKey() {
		return langKey;
	}

	public void setLangKey(String langKey) {
		this.langKey = langKey;
	}

	public Set<Authority> getAuthorities() {
		return authorities;
	}

	public void setAuthorities(Set<Authority> authorities) {
		this.authorities = authorities;
	}

	public Set<PersistentToken> getPersistentTokens() {
		return persistentTokens;
	}

	public void setPersistentTokens(Set<PersistentToken> persistentTokens) {
		this.persistentTokens = persistentTokens;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		User user = (User) o;

		if (!login.equals(user.login)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return login.hashCode();
	}

	
	public Set<Alias> getAliases() {
		return aliases;
	}

	public void setAliases(Set<Alias> aliases) {
		this.aliases = aliases;
	}


	
	public boolean isNotificationEnabled() {
	
		return notificationEnabled;
	}


	
	public void setNotificationEnabled(boolean notificationEnabled) {
	
		this.notificationEnabled = notificationEnabled;
	}


	
	public boolean isNotificationOfSubmittersEnabled() {
	
		return notificationOfSubmittersEnabled;
	}


	
	public void setNotificationOfSubmittersEnabled(boolean notificationOfSubmittersEnabled) {
	
		this.notificationOfSubmittersEnabled = notificationOfSubmittersEnabled;
	}


	
	public ZonedDateTime getLastNotified() {
	
		return lastNotified;
	}


	
	public void setLastNotified(ZonedDateTime lastNotified) {
	
		this.lastNotified = lastNotified;
	}


	
	public boolean isLobbycloudSharingEnabled() {
	
		return lobbycloudSharingEnabled;
	}


	
	public void setLobbycloudSharingEnabled(boolean lobbycloudSharingEnabled) {
	
		this.lobbycloudSharingEnabled = lobbycloudSharingEnabled;
	}


	@Override
	public String toString() {

		return "User [id=" + id + ", login=" + login + ", firstName=" + firstName + ", lastName=" + lastName
				+ ", email=" + email + ", activated=" + activated + ", showFutureMeetings=" + showFutureMeetings
				+ ", langKey=" + langKey + ", activationKey=" + activationKey 
				+ ", resetDate=" + resetDate + ", notificationEnabled=" + notificationEnabled
				+ ", notificationOfSubmittersEnabled=" + notificationOfSubmittersEnabled 
				+ ", lobbycloudSharingEnabled=" + lobbycloudSharingEnabled + ", lastNotified="
				+ lastNotified + "]";
	}


}
