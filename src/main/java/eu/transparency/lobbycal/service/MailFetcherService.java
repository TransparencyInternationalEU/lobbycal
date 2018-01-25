/**
 * 
 */
package eu.transparency.lobbycal.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.mail.Flags;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.transparency.lobbycal.domain.Meeting;
import jodd.mail.EmailAttachment;
import jodd.mail.EmailFilter;
import jodd.mail.EmailMessage;
import jodd.mail.ImapServer;
import jodd.mail.MailAddress;
import jodd.mail.ReceiveMailSession;
import jodd.mail.ReceivedEmail;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Property;

/**
 * @author lobbycal
 *
 */
@Service
@Component
@Transactional
public class MailFetcherService {

	private final Logger log = LoggerFactory.getLogger(MailFetcherService.class);


	@Inject
	private Environment env;


	@Inject
	MeetingService meetingService;


	@Inject
	ImapServer imapServer;


	@Inject
	MailService mailService;


	@Inject
	LobbycloudDeliveryService lobbycloudDeliveryService;

	@PostConstruct
	public void init()
			throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {

		log.info(env.getProperty("catchall.mail.username"));
		log.info(env.getProperty("catchall.mail.folder", "INBOX"));
		System.setProperty("file.encoding", "UTF-8");
		Field charset = Charset.class.getDeclaredField("defaultCharset");
		log.info("Set file encoding to " + System.getProperty("file.encoding"));
		charset.setAccessible(true);
		charset.set(null, null);
	}

	// LIVE: Agreed 1 minute
	@Scheduled(fixedDelay = 20000)
	public void fetchAllEmail() {

		ReceiveMailSession session = imapServer.createSession();
		log.trace(imapServer.getHost() + "  " + imapServer.getAuthenticator());
		try {
			session.open();
			session.useFolder(env.getProperty("catchall.mail.folder", "INBOX"));
		} catch (Exception e1) {
			log.error(e1.getLocalizedMessage());
			return;
		}

		ReceivedEmail[] emails = session.receiveEmailAndMarkSeen(EmailFilter.filter().flag(Flags.Flag.SEEN, false));
		log.info("Mails to process " + session.getUnreadMessageCount());

		try {
			if (emails == null) {
				session.close();
				log.trace("");
				return;
			}
			for (ReceivedEmail calEmail : emails) {

				String encodingOfEmail = "";

				log.debug("_____________________________________________________________________________________");
				log.debug(" Mail No " + calEmail.getMessageNumber() + "  " + calEmail.getSubject());
				if (calEmail.getAttachments() != null) {
					int attc = 0;
					
					
					// lobbycloud integration
					lobbycloudDeliveryService.processEmailForLobbycloud(calEmail);
					
					for (EmailAttachment att : calEmail.getAttachments()) {
						// take the last one, assuming that a reply will only be
						for (EmailMessage em : calEmail.getAllMessages()) {
							encodingOfEmail = em.getEncoding();

						}
						String mimeType = att.getDataSource().getContentType();
						log.debug("Attachment No " + attc++ + " Name : " + att.getName() + " MIME: " + mimeType);
						if (mimeType.equalsIgnoreCase("text/calendar") || mimeType.equalsIgnoreCase("text/plain")
								|| mimeType.equalsIgnoreCase("application/ics")) {

							// IOUtils.toString(
							// new InputStreamReader(att.getDataSource()
							// .getInputStream())).trim();

							Calendar calendar = null;
							try {
								calendar = new CalendarBuilder()
										.build(new InputStreamReader(att.getDataSource().getInputStream()));
								Property method = calendar.getProperty("METHOD");

								log.info(method.toString().trim());
								Optional<Meeting> m = meetingService.processRequest(
										(MailAddress[]) ArrayUtils.addAll(calEmail.getCc(), calEmail.getTo()),
										calEmail.getFrom(), calendar, calEmail.getSubject(), encodingOfEmail);
								if (!m.isPresent()) {
									break;
								}
							} catch (ParserException e) {
								log.error(e.getLocalizedMessage());
								log.error("  from " + calEmail.getFrom() + " sent " + calEmail.getSentDate());
							}

						} else {
							log.trace("not ical event");
						}
					}
				}

			}
			session.close();

		} catch (IOException e) {
			log.error(e.getMessage());
		}

	}
}
