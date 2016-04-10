package eu.transparency.lobbycal.web.rest.dto;

import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	@Size(min = 2, max = 5)
	private String langKey;

	private Set<String> authorities;

	public Set<String> getAuthorities() {
		return authorities;
	}

	public UserDTO() {
	}

	public UserDTO(User user) {
		this(user.getLogin(), null, user.getFirstName(), user.getLastName(),
				user.getEmail(), user.getActivated(), user.getLangKey(), user
						.getAuthorities().stream().map(Authority::getName)
						.collect(Collectors.toSet()), user.getId());
	}

	public UserDTO(String login, String password, String firstName,
			String lastName, String email, boolean activated, String langKey,
			Set<String> authorities, Long id) {

		this.login = login;
		this.password = password;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.activated = activated;
		this.langKey = langKey;
		this.authorities = authorities;
		this.id=id;
		log.trace(""+this.id);
		
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

	@Override
	public String toString() {
		return "UserDTO{" + "login='" + login + '\'' + ", password='"
				+ password + '\'' + ", firstName='" + firstName + '\''
				+ ", lastName='" + lastName + '\'' + ", email='" + email + '\''
				+ ", langKey='" + langKey + '\'' + ", roles=" + authorities
				+ '}';
	}
}
