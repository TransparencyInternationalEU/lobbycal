/**
 * 
 */
package eu.transparency.lobbycal.service;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.time.ZonedDateTime;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.transparency.lobbycal.aop.audit.AuditEventPublisher;
import eu.transparency.lobbycal.domain.Meeting;
import eu.transparency.lobbycal.domain.Submitter;
import eu.transparency.lobbycal.domain.User;
import eu.transparency.lobbycal.repository.MeetingRepository;
import eu.transparency.lobbycal.repository.SubmitterRepository;
import eu.transparency.lobbycal.security.SecurityUtils;
import jodd.mail.ImapServer;

/**
 * @author lobbycal
 *
 */
@Service
@Component
@Transactional
public class NoNewMeetingsReminderService {

	private final Logger log = LoggerFactory.getLogger(NoNewMeetingsReminderService.class);


	@Inject
	private Environment env;


	@Inject
	private MeetingRepository meetingRepository;


	@Inject
	private AuditEventPublisher auditPublisher;


	@Inject
	UserService userService;


	@Inject
	SubmitterRepository submitterRepository;


	@Inject
	ImapServer imapServer;


	@Inject
	MailService mailService;


	Long period = 0l;

	@PostConstruct
	public void init()
			throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {

		period = Long.parseLong(env.getProperty("lobbycal.meetings.reminder.noNewMeeting.sinceLastDays", "" + 21));

		log.info("Days span: " + period);
		System.setProperty("file.encoding", "UTF-8");
		Field charset = Charset.class.getDeclaredField("defaultCharset");
		log.info("Set file encoding to " + System.getProperty("file.encoding"));
		charset.setAccessible(true);
		charset.set(null, null);
	}


	String subMsg = "";

	@Scheduled(fixedDelay = 60000)
	public void fetchMEPToBeNotified() {

		List<User> userToNotify = userService.getUsersWithNotificationsActivated();

		ZonedDateTime offsetDay = ZonedDateTime.now().minusDays(period);

		for (User user : userToNotify) {
			log.trace("" + user.getEmail());
			if (user.getLastNotified() == null || user.getLastNotified().isBefore(offsetDay)) {
				List<Meeting> meetingsInLastX = meetingRepository.getMeetingsForMEPAfter(user.getId(), offsetDay);
				if (!meetingsInLastX.isEmpty()) {
					log.debug(user.getEmail() + " reported " + meetingsInLastX.size() + " meetings since "
							+ offsetDay.toString());
				} else {
					List<Submitter> submitterForUser = null;
					if (user.isNotificationOfSubmittersEnabled()) {
						submitterForUser = submitterRepository.findAllByUserId(user.getId());
					}
					log.info("Notify me and submitters");
					log.info("" + mailService.sendNoActivityMail(user, submitterForUser,
							user.isNotificationOfSubmittersEnabled(), user.isNotificationEnabled(), period + ""));
					userService.setUserNotified(user.getId());

					if (user.isNotificationOfSubmittersEnabled()) {
						submitterForUser.forEach(s -> {
							subMsg += " " + s.getEmail();
						});
					}
					String m = "message=Notification sent to user: " + user.getLogin() + " himself: "
							+ user.isNotificationEnabled() + " submitters: \n" + subMsg;
					auditThis("system", new String[] { m.substring(0, Math.min(m.length(), 254)) },
							AuditEventPublisher.TYPE_MAIL);
					subMsg = "";

				}
			} else {
				log.trace(user.getLogin() + " notification sent less than " + period + " days ago ");
			}
		}
	}

	void auditThis(String login, String[] msg, String type) {

		if (type == null) {
			type = AuditEventPublisher.TYPE_MEETING;
		}

		AuditEvent event = new AuditEvent(login, type, msg);
		auditPublisher.publish(event);

	}
}
