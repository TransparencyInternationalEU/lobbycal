package eu.transparency.lobbycal.web.rest.dto;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.transparency.lobbycal.domain.Authority;
import eu.transparency.lobbycal.domain.User;

public class UserDTO {

	public static final int PASSWORD_MIN_LENGTH = 5;


	public static final int PASSWORD_MAX_LENGTH = 100;


	private final Logger log = LoggerFactory.getLogger(UserDTO.class);


	@Pattern(regexp = "^[a-z0-9]*$")
	@NotNull
	@Size(min = 5, max = 50)
	private String login;


	private Long id;

	public Long getId() {

		return id;
	}


	@NotNull
	@Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH)
	private String password;


	@Size(max = 50)
	private String firstName;


	@Size(max = 50)
	private String lastName;


	@Email
	@Size(min = 5, max = 100)
	private String email;


	private boolean activated = false;


	private boolean showFutureMeetings = false;


	private boolean lobbycloudSharingEnabled = false;


	private boolean notificationEnabled = false;


	private boolean notificationOfSubmittersEnabled = false;


	private ZonedDateTime lastNotified = null;


	@Size(min = 2, max = 5)
	private String langKey;


	private Set<String> authorities;

	public Set<String> getAuthorities() {

		return authorities;
	}

	public UserDTO() {
	}

	public UserDTO(User user) {
		this(user.getLogin(), null, user.getFirstName(), user.getLastName(), user.getEmail(), user.getActivated(),
				user.getLangKey(), user.getAuthorities().stream().map(Authority::getName).collect(Collectors.toSet()),
				user.getId(), user.isShowFutureMeetings(), user.isNotificationEnabled(),
				user.isNotificationOfSubmittersEnabled(), user.getLastNotified(), user.isLobbycloudSharingEnabled());
	}

	public UserDTO(String login, String password, String firstName, String lastName, String email, boolean activated,
			String langKey, Set<String> authorities, Long id, boolean showFutureMeetings, boolean notificationEnabled,
			boolean notificationOfSubmittersEnabled, ZonedDateTime lastNotified, boolean lobbycloudSharingEnabled) {

		this.login = login;
		this.password = password;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.activated = activated;
		this.langKey = langKey;
		this.authorities = authorities;
		this.id = id;
		this.lastNotified = lastNotified;
		this.notificationEnabled = notificationEnabled;
		this.notificationOfSubmittersEnabled = notificationOfSubmittersEnabled;
		this.showFutureMeetings = showFutureMeetings;
		this.lobbycloudSharingEnabled = lobbycloudSharingEnabled;
		log.trace("" + this.id);

	}

	public UserDTO(String string, String string2, String string3, String string4, String string5, boolean b,
			String string6, HashSet hashSet, Object object) {
		// TODO Auto-generated constructor stub
	}

	public String getFirstName() {

		return firstName;
	}

	public String getPassword() {

		return password;
	}

	public String getLogin() {

		return login;
	}

	public String getLastName() {

		return lastName;
	}

	public String getEmail() {

		return email;
	}

	public String getLangKey() {

		return langKey;
	}

	public boolean isActivated() {

		return activated;
	}

	public boolean isShowFutureMeetings() {

		return showFutureMeetings;
	}

	public void setShowFutureMeetings(boolean showFutureMeetings) {

		this.showFutureMeetings = showFutureMeetings;
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

		return "UserDTO [login=" + login + ", firstName=" + firstName + ", lastName=" + lastName + ", email=" + email
				+ ", activated=" + activated + ", showFutureMeetings=" + showFutureMeetings + ", notificationEnabled="
				+ notificationEnabled + ", notificationOfSubmittersEnabled=" + notificationOfSubmittersEnabled
				+ ", lastNotified=" + lastNotified + ", langKey=" + langKey + ", authorities=" + authorities + "], lobbycloudSharingEnabled="+lobbycloudSharingEnabled;
	}

}
