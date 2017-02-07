package eu.transparency.lobbycal.web.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Optional;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;

import eu.transparency.lobbycal.aop.audit.AuditEventPublisher;
import eu.transparency.lobbycal.domain.Alias;
import eu.transparency.lobbycal.domain.PersistentToken;
import eu.transparency.lobbycal.domain.User;
import eu.transparency.lobbycal.repository.AliasRepository;
import eu.transparency.lobbycal.repository.PersistentTokenRepository;
import eu.transparency.lobbycal.repository.UserRepository;
import eu.transparency.lobbycal.repository.search.AliasSearchRepository;
import eu.transparency.lobbycal.security.AuthoritiesConstants;
import eu.transparency.lobbycal.security.SecurityUtils;
import eu.transparency.lobbycal.service.MailService;
import eu.transparency.lobbycal.service.UserService;
import eu.transparency.lobbycal.web.rest.dto.KeyAndPasswordDTO;
import eu.transparency.lobbycal.web.rest.dto.UserDTO;
import eu.transparency.lobbycal.web.rest.util.HeaderUtil;

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping("/api")
public class AccountResource {

	private final Logger log = LoggerFactory.getLogger(AccountResource.class);
	@Inject
	private Environment env;


	
	@Inject
	private AuditEventPublisher auditPublisher;

	@Inject
	private AliasRepository aliasRepository;

	@Inject
	private AliasSearchRepository aliasSearchRepository;

	@Inject
	private UserRepository userRepository;

	@Inject
	private UserService userService;

	@Inject
	private PersistentTokenRepository persistentTokenRepository;

	@Inject
	private MailService mailService;

	/**
	 * POST /register -> register the user.
	 */
	@RequestMapping(value = "/register", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
	@Timed
	public ResponseEntity<?> registerAccount(@Valid
	@RequestBody
	UserDTO userDTO, HttpServletRequest request) {

		log.info("");

		return userRepository.findOneByLogin(userDTO.getLogin())
				.map(user -> new ResponseEntity<>("login already in use", HttpStatus.BAD_REQUEST))
				.orElseGet(() -> userRepository.findOneByEmail(userDTO.getEmail())
						.map(user -> new ResponseEntity<>("e-mail address already in use", HttpStatus.BAD_REQUEST))
						.orElseGet(() -> {
							User user = userService.createUserInformation(userDTO.getLogin(), userDTO.getPassword(),
									userDTO.getFirstName(), userDTO.getLastName(), userDTO.getEmail().toLowerCase(),
									userDTO.getLangKey());
							String baseUrl = request.getScheme() + // "http"
									"://" + // "://"
									request.getServerName() + // "myhost"
									":" + // ":"
									request.getServerPort() + // "80"
									request.getContextPath(); // "/myContextPath"
																// or
																// ""
																// if
																// deployed
																// in
																// root
																// context

							// add a default Alias

							try {
								Alias alias = new Alias();
								alias.setAlias(userDTO.getLogin());
								alias.setActive(true);
								alias.setUser(user);

								aliasRepository.save(alias);
								aliasSearchRepository.save(alias);
							} catch (Exception e) {
								e.printStackTrace();
								log.error("Alias could not be created " + e.getMessage());
							}

							mailService.sendActivationEmail(user, baseUrl);
							String[] sarr = { "message=Account created for " + user.getLogin() };
							AuditEvent event = new AuditEvent(user.getLogin().toString(), AuditEventPublisher.TYPE_USER,
									sarr);
							auditPublisher.publish(event);
							return new ResponseEntity<>(HttpStatus.CREATED);
						}));
	}

	/**
	 * GET /activate -> activate the registered user.
	 */
	@RequestMapping(value = "/activate", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<String> activateAccount(@RequestParam(value = "key")
	String key) {

		log.info("");

		return Optional.ofNullable(userService.activateRegistration(key))
				.map(user -> new ResponseEntity<String>(HttpStatus.OK))
				.orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
	}

	/**
	 * GET /authenticate -> check if the user is authenticated, and return its
	 * login.
	 */
	@RequestMapping(value = "/authenticate", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public String isAuthenticated(HttpServletRequest request) {

		log.debug("REST request to check if the current user is authenticated");
		return request.getRemoteUser();
	}

	/**
	 * GET /account -> get the current user.
	 */
	@RequestMapping(value = "/account", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	@RolesAllowed({ AuthoritiesConstants.ADMIN, AuthoritiesConstants.USER })
	public ResponseEntity<UserDTO> getAccount() {
		HttpHeaders headers = new HttpHeaders();
		headers.add("days", env.getProperty("lobbycal.meetings.reminder.noNewMeeting.sinceLastDays", "" + 21));

		log.info("");
		return Optional.ofNullable(userService.getUserWithAuthorities())
				.map(user -> new ResponseEntity<>(new UserDTO(user), headers, HttpStatus.OK))
				.orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
	}

	/**
	 * POST /account -> update the current user information.
	 */
	@RequestMapping(value = "/account", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<String> saveAccount(@RequestBody
	UserDTO userDTO) {

		Optional<User> existingUser = userRepository.findOneByEmail(userDTO.getEmail());
		log.info("show future? " + userDTO.isShowFutureMeetings());

		if (existingUser.isPresent() && (!existingUser.get().getLogin().equalsIgnoreCase(userDTO.getLogin()))) {
			return ResponseEntity.badRequest()
					.headers(HeaderUtil.createFailureAlert("user-management", "emailexists", "Email already in use"))
					.body(null);
		}
		return userRepository.findOneByLogin(SecurityUtils.getCurrentUser().getUsername()).map(u -> {
			userService.updateUserInformation(userDTO.getFirstName(), userDTO.getLastName(), userDTO.getEmail(),
					userDTO.getLangKey(), userDTO.isShowFutureMeetings(), userDTO.isNotificationEnabled(), userDTO.isNotificationOfSubmittersEnabled(), null, userDTO.isLobbycloudSharingEnabled());
			return new ResponseEntity<String>(HttpStatus.OK);
		}).orElseGet(() -> new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
	}

	/**
	 * POST /change_password -> changes the current user's password
	 */
	@RequestMapping(value = "/account/change_password", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<?> changePassword(@RequestBody
	String password) {

		log.info("");

		if (!checkPasswordLength(password)) {
			return new ResponseEntity<>("Incorrect password", HttpStatus.BAD_REQUEST);
		}
		userService.changePassword(password);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * GET /account/sessions -> get the current open sessions.
	 */
	@RequestMapping(value = "/account/sessions", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<List<PersistentToken>> getCurrentSessions() {

		log.info("");

		return userRepository.findOneByLogin(SecurityUtils.getCurrentUser().getUsername())
				.map(user -> new ResponseEntity<>(persistentTokenRepository.findByUser(user), HttpStatus.OK))
				.orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
	}

	/**
	 * DELETE /account/sessions?series={series} -> invalidate an existing
	 * session.
	 *
	 * - You can only delete your own sessions, not any other user's session -
	 * If you delete one of your existing sessions, and that you are currently
	 * logged in on that session, you will still be able to use that session,
	 * until you quit your browser: it does not work in real time (there is no
	 * API for that), it only removes the "remember me" cookie - This is also
	 * true if you invalidate your current session: you will still be able to
	 * use it until you close your browser or that the session times out. But
	 * automatic login (the "remember me" cookie) will not work anymore. There
	 * is an API to invalidate the current session, but there is no API to check
	 * which session uses which cookie.
	 */
	@RequestMapping(value = "/account/sessions/{series}", method = RequestMethod.DELETE)
	@Timed
	public void invalidateSession(@PathVariable
	String series) throws UnsupportedEncodingException {

		log.info("");

		String decodedSeries = URLDecoder.decode(series, "UTF-8");
		userRepository.findOneByLogin(SecurityUtils.getCurrentUser().getUsername()).ifPresent(u -> {
			persistentTokenRepository.findByUser(u).stream()
					.filter(persistentToken -> StringUtils.equals(persistentToken.getSeries(), decodedSeries)).findAny()
					.ifPresent(t -> persistentTokenRepository.delete(decodedSeries));
		});
	}

	@RequestMapping(value = "/account/reset_password/init", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
	@Timed
	public ResponseEntity<?> requestPasswordReset(@RequestBody
	String mail, HttpServletRequest request) {

		return userService.requestPasswordReset(mail).map(user -> {
			String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
					+ request.getContextPath();
			mailService.sendPasswordResetMail(user, baseUrl);
			return new ResponseEntity<>("e-mail was sent", HttpStatus.OK);
		}).orElse(new ResponseEntity<>("e-mail address not registered", HttpStatus.BAD_REQUEST));
	}

	@RequestMapping(value = "/account/reset_password/finish", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<String> finishPasswordReset(@RequestBody
	KeyAndPasswordDTO keyAndPassword) {

		if (!checkPasswordLength(keyAndPassword.getNewPassword())) {
			return new ResponseEntity<>("Incorrect password", HttpStatus.BAD_REQUEST);
		}
		return userService.completePasswordReset(keyAndPassword.getNewPassword(), keyAndPassword.getKey())
				.map(user -> new ResponseEntity<String>(HttpStatus.OK))
				.orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
	}

	private boolean checkPasswordLength(String password) {

		return (!StringUtils.isEmpty(password) && password.length() >= UserDTO.PASSWORD_MIN_LENGTH
				&& password.length() <= UserDTO.PASSWORD_MAX_LENGTH);
	}
}
