package eu.transparency.lobbycal.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;

import eu.transparency.lobbycal.domain.Authority;
import eu.transparency.lobbycal.domain.User;
import eu.transparency.lobbycal.repository.AuthorityRepository;
import eu.transparency.lobbycal.repository.UserRepository;
import eu.transparency.lobbycal.security.AuthoritiesConstants;
import eu.transparency.lobbycal.security.SecurityUtils;
import eu.transparency.lobbycal.service.MailService;
import eu.transparency.lobbycal.service.UserService;
import eu.transparency.lobbycal.web.rest.dto.ManagedUserDTO;
import eu.transparency.lobbycal.web.rest.util.HeaderUtil;
import eu.transparency.lobbycal.web.rest.util.PaginationUtil;

/**
 * REST controller for managing users.
 *
 * <p>
 * This class accesses the User entity, and needs to fetch its collection of
 * authorities.
 * </p>
 * <p>
 * For a normal use-case, it would be better to have an eager relationship
 * between User and Authority, and send everything to the client side: there
 * would be no DTO, a lot less code, and an outer-join which would be good for
 * performance.
 * </p>
 * <p>
 * We use a DTO for 3 reasons:
 * <ul>
 * <li>We want to keep a lazy association between the user and the authorities,
 * because people will quite often do relationships with the user, and we don't
 * want them to get the authorities all the time for nothing (for performance
 * reasons). This is the #1 goal: we should not impact our users' application
 * because of this use-case.</li>
 * <li>Not having an outer join causes n+1 requests to the database. This is not
 * a real issue as we have by default a second-level cache. This means on the
 * first HTTP call we do the n+1 requests, but then all authorities come from
 * the cache, so in fact it's much better than doing an outer join (which will
 * get lots of data from the database, for each HTTP call).</li>
 * <li>As this manages users, for security reasons, we'd rather have a DTO
 * layer.</li>
 * </p>
 * <p>
 * Another option would be to have a specific JPA entity graph to handle this
 * case.
 * </p>
 */
@RestController
@RequestMapping("/api")
public class UserResource {

	private final Logger log = LoggerFactory.getLogger(UserResource.class);


	@Inject
	private UserRepository userRepository;

	@Inject
	private Environment env;



	@Inject
	private MailService mailService;


	@Inject
	private AuthorityRepository authorityRepository;


	@Inject
	private UserService userService;

	/**
	 * POST /users -> Creates a new user.
	 * <p>
	 * Creates a new user if the login and email are not already used, and sends
	 * an mail with an activation link. The user needs to be activated on
	 * creation.
	 * </p>
	 */
	@RequestMapping(value = "/users", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	@Secured(AuthoritiesConstants.ADMIN)
	public ResponseEntity<?> createUser(@RequestBody
	ManagedUserDTO managedUserDTO, HttpServletRequest request) throws URISyntaxException {

		log.info("REST request to save User : {}", managedUserDTO);
		if (userRepository.findOneByLogin(managedUserDTO.getLogin()).isPresent()) {
			return ResponseEntity.badRequest()
					.headers(HeaderUtil.createFailureAlert("user-management", "userexists", "Login already in use"))
					.body(null);
		} else if (userRepository.findOneByEmail(managedUserDTO.getEmail()).isPresent()) {
			return ResponseEntity.badRequest()
					.headers(HeaderUtil.createFailureAlert("user-management", "emailexists", "Email already in use"))
					.body(null);
		} else {
			User newUser = userService.createUser(managedUserDTO);
			String baseUrl = request.getScheme() + // "http"
					"://" + // "://"
					request.getServerName() + // "myhost"
					":" + // ":"
					request.getServerPort() + // "80"
					request.getContextPath(); // "/myContextPath" or "" if
												// deployed in root context
			log.info("");
			mailService.sendActivationEmail(newUser, baseUrl);
			return ResponseEntity.created(new URI("/api/users/" + newUser.getLogin()))
					.headers(HeaderUtil.createAlert("user-management.created", newUser.getLogin())).body(newUser);
		}
	}

	/**
	 * PUT /users -> Updates an existing User.
	 */
	@RequestMapping(value = "/users", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	@Transactional
	@Secured(AuthoritiesConstants.ADMIN)
	public ResponseEntity<ManagedUserDTO> updateUser(@RequestBody
	ManagedUserDTO managedUserDTO) throws URISyntaxException {

		log.debug("REST request to update User : {}", managedUserDTO);
		Optional<User> existingUser = userRepository.findOneByEmail(managedUserDTO.getEmail());
		if (existingUser.isPresent() && (!existingUser.get().getLogin().equalsIgnoreCase(managedUserDTO.getLogin()))) {
			return ResponseEntity.badRequest()
					.headers(HeaderUtil.createFailureAlert("user-management", "emailexists", "Email already in use"))
					.body(null);
		}
		return userRepository.findOneById(managedUserDTO.getId()).map(user -> {
			user.setLogin(managedUserDTO.getLogin());
			user.setFirstName(managedUserDTO.getFirstName());
			user.setLastName(managedUserDTO.getLastName());
			user.setEmail(managedUserDTO.getEmail());
			user.setActivated(managedUserDTO.isActivated());
			user.setShowFutureMeetings(managedUserDTO.isShowFutureMeetings());
			user.setLangKey(managedUserDTO.getLangKey());
			user.setLastNotified(managedUserDTO.getLastNotified());
			user.setNotificationEnabled(managedUserDTO.isNotificationEnabled());
			user.setNotificationOfSubmittersEnabled(managedUserDTO.isNotificationOfSubmittersEnabled());
			Set<Authority> authorities = user.getAuthorities();
			authorities.clear();
			managedUserDTO.getAuthorities().stream()
					.forEach(authority -> authorities.add(authorityRepository.findOne(authority)));
			return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert("user", managedUserDTO.getLogin()))
					.body(new ManagedUserDTO(userRepository.findOne(managedUserDTO.getId())));
		}).orElseGet(() ->ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.headers(HeaderUtil.createFailureAlert("user-management", "failed", "User not updated"))
				.body(null));

	}

	/**
	 * GET /users -> get all users.
	 */
	@RequestMapping(value = "/users", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	@Transactional(readOnly = true)
	public ResponseEntity<List<ManagedUserDTO>> getAllUsers(Pageable pageable) throws URISyntaxException {

		log.info("" + pageable.getSort());

		log.info("" + pageable.getOffset());

		log.info("" + pageable.getPageNumber());

		log.info("" + pageable.getPageSize());
		Page<User> page = null;
		List<ManagedUserDTO> managedUserDTOs = null;

		if (SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.ADMIN)) {
			page = userRepository.findAllByOrderByLastNameAscLoginAsc(pageable);
		} else {
			String currentUser=null;
			try {
				currentUser= SecurityUtils.getCurrentUser().getUsername();
			} catch (IllegalStateException e) {
				log.error(e.getMessage() + " while retrieving all users");
			}
			page = userRepository.findOneByLoginOrderByLastNameAsc(pageable,
					currentUser);
		}
		managedUserDTOs = page.getContent().stream().map(user -> new ManagedUserDTO(user)).collect(Collectors.toList());
		HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/users");
		return new ResponseEntity<>(managedUserDTOs, headers, HttpStatus.OK);
	}

	/**
	 * GET /users/:login -> get the "login" user.
	 */
	@RequestMapping(value = "/users/{login}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<ManagedUserDTO> getUser(@PathVariable
	String login) {

		HttpHeaders headers = new HttpHeaders();
		headers.add("days", env.getProperty("lobbycal.meetings.reminder.noNewMeeting.sinceLastDays", "" + 21));

		log.info("REST request to get User : {}", login);
		return userService.getUserWithAuthoritiesByLogin(login).map(ManagedUserDTO::new)
				.map(managedUserDTO -> new ResponseEntity<>(managedUserDTO, headers, HttpStatus.OK))
				.orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	/**
	 * DELETE USER :login -> delete the "login" User.
	 */
	@RequestMapping(value = "/users/{login}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	@Secured(AuthoritiesConstants.ADMIN)
	public ResponseEntity<Void> deleteUser(@PathVariable
	String login) {

		log.debug("REST request to delete User: {}", login);
		userService.deleteUserInformation(login);
		return ResponseEntity.ok().headers(HeaderUtil.createAlert("user-management.deleted", login)).build();
	}
}
