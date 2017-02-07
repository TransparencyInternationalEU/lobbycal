package eu.transparency.lobbycal.service;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.transparency.lobbycal.aop.audit.AuditEventPublisher;
import eu.transparency.lobbycal.domain.Alias;
import eu.transparency.lobbycal.domain.Authority;
import eu.transparency.lobbycal.domain.Submitter;
import eu.transparency.lobbycal.domain.User;
import eu.transparency.lobbycal.repository.AliasRepository;
import eu.transparency.lobbycal.repository.AuthorityRepository;
import eu.transparency.lobbycal.repository.PersistentTokenRepository;
import eu.transparency.lobbycal.repository.SubmitterRepository;
import eu.transparency.lobbycal.repository.UserRepository;
import eu.transparency.lobbycal.repository.search.AliasSearchRepository;
import eu.transparency.lobbycal.repository.search.SubmitterSearchRepository;
import eu.transparency.lobbycal.repository.search.UserSearchRepository;
import eu.transparency.lobbycal.security.SecurityUtils;
import eu.transparency.lobbycal.service.util.RandomUtil;
import eu.transparency.lobbycal.web.rest.dto.ManagedUserDTO;

/**
 * Service class for managing users.
 */
@Service
@Transactional
public class UserService {

	private final Logger log = LoggerFactory.getLogger(UserService.class);

	@Inject
	private PasswordEncoder passwordEncoder;

	@Inject
	private UserRepository userRepository;

	@Inject
	private UserSearchRepository userSearchRepository;

	@Inject
	private SubmitterRepository submitterRepository;

	@Inject
	private SubmitterSearchRepository submitterSearchRepository;

	@Inject
	private AliasRepository aliasRepository;

	@Inject
	private AliasSearchRepository aliasSearchRepository;

	@Inject
	private AuditEventPublisher auditPublisher;

	@Inject
	private PersistentTokenRepository persistentTokenRepository;

	@Inject
	private AuthorityRepository authorityRepository;

	public Optional<User> activateRegistration(String key) {

		log.debug("Activating user for activation key {}", key);
		userRepository.findOneByActivationKey(key).map(user -> {
			// activate given user for the registration key.
			user.setActivated(true);
			user.setActivationKey(null);
			userRepository.save(user);
			// userSearchRepository.save(user);
			log.debug("Activated user: {}", user);
			String[] msg = new String[1];

			msg[0] = ("Account activated for " + user.getLogin());
			AuditEvent event = new AuditEvent(user.getLogin(), AuditEventPublisher.TYPE_USER, msg);
			auditPublisher.publish(event);
			return user;
		});
		return Optional.empty();
	}

	public Optional<User> completePasswordReset(String newPassword, String key) {

		log.debug("Reset user password for reset key {}", key);

		return userRepository.findOneByResetKey(key).filter(user -> {
			ZonedDateTime oneDayAgo = ZonedDateTime.now().minusHours(24);
			return user.getResetDate().isAfter(oneDayAgo);
		}).map(user -> {
			user.setPassword(passwordEncoder.encode(newPassword));
			user.setResetKey(null);
			user.setResetDate(null);
			userRepository.save(user);
			return user;
		});
	}

	public Optional<User> requestPasswordReset(String mail) {

		return userRepository.findOneByEmail(mail).filter(User::getActivated).map(user -> {
			user.setResetKey(RandomUtil.generateResetKey());
			user.setResetDate(ZonedDateTime.now());
			userRepository.save(user);
			return user;
		});
	}

	public User createUserInformation(String login, String password, String firstName, String lastName, String email,
			String langKey) {

		User newUser = new User();
		Authority authority = authorityRepository.findOne("ROLE_USER");
		Set<Authority> authorities = new HashSet<>();
		String encryptedPassword = passwordEncoder.encode(password);
		newUser.setLogin(login);
		// new user gets initially a generated password
		newUser.setPassword(encryptedPassword);
		newUser.setFirstName(firstName);
		newUser.setLastName(lastName);
		newUser.setEmail(email);
		newUser.setLangKey(langKey);
		// new user is not active
		newUser.setActivated(false);
		// new user gets registration key
		newUser.setActivationKey(RandomUtil.generateActivationKey());
		authorities.add(authority);
		newUser.setAuthorities(authorities);
		newUser.setLobbycloudSharingEnabled(false);
		userRepository.save(newUser);
		// userSearchRepository.save(newUser);
		log.debug("Created Information for User: {}", newUser);
		return newUser;
	}

	public User createUser(ManagedUserDTO managedUserDTO) {

		User user = new User();
		user.setLogin(managedUserDTO.getLogin());
		user.setFirstName(managedUserDTO.getFirstName());
		user.setLastName(managedUserDTO.getLastName());
		user.setEmail(managedUserDTO.getEmail());
		if (managedUserDTO.getLangKey() == null) {
			user.setLangKey("en"); // default language is English
		} else {
			user.setLangKey(managedUserDTO.getLangKey());
		}
		Set<Authority> authorities = new HashSet<>();
		managedUserDTO.getAuthorities().stream()
				.forEach(authority -> authorities.add(authorityRepository.findOne(authority)));
		user.setAuthorities(authorities);
		String encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword());
		user.setPassword(encryptedPassword);
		user.setResetKey(RandomUtil.generateResetKey());
		user.setResetDate(ZonedDateTime.now());
		user.setActivated(true);
		user.setShowFutureMeetings(false);
		user.setLobbycloudSharingEnabled(false);
		userRepository.save(user);
		log.debug("Created Information for User: {}", user);
		return user;
	}

	public void updateUserInformation(String firstName, String lastName, String email, String langKey, boolean showFuture, boolean notificationEnabled,
			boolean notificationOfSubmittersEnabled, ZonedDateTime lastNotified, boolean lobbycloudSharingEnabled) {
		log.info(""+showFuture);
		userRepository.findOneByLogin(SecurityUtils.getCurrentUser().getUsername()).ifPresent(u -> {
			u.setFirstName(firstName);
			u.setLastName(lastName);
			u.setEmail(email);
			u.setLangKey(langKey);
			u.setShowFutureMeetings(showFuture);
			u.setNotificationEnabled(notificationEnabled);
			u.setNotificationOfSubmittersEnabled(notificationOfSubmittersEnabled);
			u.setLobbycloudSharingEnabled(lobbycloudSharingEnabled);
			userRepository.save(u);
			// userSearchRepository.save(u);
			log.info("Changed Information for User: {}", u);
		});
	}

	public void deleteUserInformation(String login) {

		User deleter = getUserWithAuthorities();
		userRepository.findOneByLogin(login).ifPresent(u -> {
			String[] msg = new String[1];
			// delete related aliases
			List<Alias> relatedAliass = aliasRepository.findAllByUserLogin(login);
			for (Alias a : relatedAliass) {
				aliasRepository.delete(a);
				aliasSearchRepository.delete(a);

				msg[0] = ("message=Deleted alias: " + a.getAlias());
				log.info(msg[0]);
				AuditEvent event = new AuditEvent(deleter.getLogin(), AuditEventPublisher.TYPE_USER, msg);
				auditPublisher.publish(event);
			}

			List<Submitter> relatedSubmitters = submitterRepository.findAllByUserId(u.getId());
			for (Submitter s : relatedSubmitters) {
				submitterRepository.delete(s);
				submitterSearchRepository.delete(s);
				msg[0] = ("message=Deleted submitter: " + s.getEmail());
				log.info(msg[0]);
				AuditEvent event = new AuditEvent(deleter.getLogin(), AuditEventPublisher.TYPE_USER, msg);
				auditPublisher.publish(event);
			}

			userRepository.delete(u);
			userSearchRepository.delete(u);

			msg[0] = ("message=Deleted user: " + login);
			AuditEvent event = new AuditEvent(deleter.getLogin(), AuditEventPublisher.TYPE_USER, msg);
			auditPublisher.publish(event);
			log.debug("Deleted User: {}", u);
		});
	}

	public void changePassword(String password) {

		userRepository.findOneByLogin(SecurityUtils.getCurrentUser().getUsername()).ifPresent(u -> {
			String encryptedPassword = passwordEncoder.encode(password);
			u.setPassword(encryptedPassword);
			userRepository.save(u);
			log.debug("Changed password for User: {}", u);
		});
	}

	@Transactional(readOnly = true)
	public Optional<User> getUserWithAuthoritiesByLogin(String login) {

		log.info("");
		return userRepository.findOneByLogin(login).map(u -> {
			u.getAuthorities().size();
			return u;
		});
	}

	@Transactional(readOnly = true)
	public User getUserWithAuthorities(Long id) {

		log.debug("");
		User user = userRepository.findOne(id);
		user.getAuthorities().size(); // eagerly load the association
		return user;
	}
	
	
	@Transactional
	public void setUserNotified(Long id) {
		log.debug("");
		User user = userRepository.findOne(id);
		user.setLastNotified(ZonedDateTime.now()); // eagerly load the association
		userRepository.save(user);
		
	}

	@Transactional(readOnly = true)
	public User getUserWithAuthorities() {

		User user = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();
		user.getAuthorities().size(); // eagerly load the association
		log.trace(user.getId() + "");
		return user;
	}

	@Transactional(readOnly = true)
	public List<User> getUsersWithNotificationsActivated() {

		List<User> user = userRepository.findAllByActivatedIsTrueAndNotificationEnabledIsTrue();
		return user;
	}

	@Transactional(readOnly = true)
	public List<User> getUsersWithNotificationsOfSubmittersActivated() {

		List<User> user = userRepository.findAllByActivatedIsTrueAndNotificationOfSubmittersEnabledIsTrue();
		return user;
	}

	
	
	
	/**
	 * Persistent Token are used for providing automatic authentication, they
	 * should be automatically deleted after 30 days.
	 * <p/>
	 * <p>
	 * This is scheduled to get fired everyday, at midnight.
	 * </p>
	 */
	@Scheduled(cron = "0 0 0 * * ?")
	public void removeOldPersistentTokens() {

		LocalDate now = LocalDate.now();
		persistentTokenRepository.findByTokenDateBefore(now.minusMonths(1)).stream().forEach(token -> {
			log.debug("Deleting token {}", token.getSeries());
			User user = token.getUser();
			user.getPersistentTokens().remove(token);
			persistentTokenRepository.delete(token);
		});
	}

	/**
	 * Not activated users should be automatically deleted after 3 days.
	 * <p/>
	 * <p>
	 * This is scheduled to get fired everyday, at 01:00 (am).
	 * </p>
	 */
	// @Scheduled(cron = "0 0 1 * * ?")
	public void removeNotActivatedUsers() {

		ZonedDateTime now = ZonedDateTime.now();
		List<User> users = userRepository.findAllByActivatedIsFalseAndCreatedDateBefore(now.minusDays(3));
		for (User user : users) {
			log.debug("Deleting not activated user {}", user.getLogin());
			userRepository.delete(user);
			// userSearchRepository.delete(user);
		}
	}
}
