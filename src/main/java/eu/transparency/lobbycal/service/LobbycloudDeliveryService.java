/**
 * 
 */
package eu.transparency.lobbycal.service;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.transparency.lobbycal.aop.audit.AuditEventPublisher;
import eu.transparency.lobbycal.domain.Alias;
import eu.transparency.lobbycal.domain.Submitter;
import eu.transparency.lobbycal.domain.User;
import eu.transparency.lobbycal.repository.AliasRepository;
import eu.transparency.lobbycal.repository.MeetingRepository;
import eu.transparency.lobbycal.repository.SubmitterRepository;
import eu.transparency.lobbycal.repository.UserRepository;
import eu.transparency.lobbycal.service.MailService.AcceptedMimeTypes;
import jodd.mail.EmailAttachment;
import jodd.mail.EmailMessage;
import jodd.mail.ImapServer;
import jodd.mail.MailAddress;
import jodd.mail.ReceivedEmail;

/**
 * @author lobbycal
 *
 */
@Service
@Component
@Transactional
public class LobbycloudDeliveryService {

	private final Logger log = LoggerFactory.getLogger(LobbycloudDeliveryService.class);


	@Inject
	private Environment env;


	@Inject
	private AuditEventPublisher auditPublisher;


	@Inject
	UserService userService;


	@Inject
	SubmitterRepository submitterRepository;


	@Inject
	private AliasRepository aliasRepository;


	@Inject
	private UserRepository userRepository;


	@Inject
	ImapServer imapServer;


	@Inject
	MailService mailService;


	boolean active = false;


	String sendTo = "";


	String url = "";


	String tag = "";


	String subMsg = "";

	@PostConstruct
	public void init()
			throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {

		active = env.getProperty("lobbycal.lobbycloud.active", "false").compareTo("true") == 0;
		sendTo = env.getProperty("lobbycal.lobbycloud.sendTo", "lobbycloud@localhost");
		url = env.getProperty("lobbycal.lobbycloud.triggerUrl", "http://localhost/lobbycloud");
		tag = env.getProperty("lobbycal.lobbycloud.tag", "#lobbycloud");

		System.setProperty("file.encoding", "UTF-8");
		Field charset = Charset.class.getDeclaredField("defaultCharset");
		charset.setAccessible(true);
		charset.set(null, null);
	}

	/**
	 * examines one email and checks whether the criteria for forwarding to
	 * lobbycloud are met:
	 * 
	 * 1: lobbycloud feature is active on platform
	 * 
	 * 2: mail contains at least one attachment of type doc, docx, pdf, xls,
	 * odt, tiff
	 * 
	 * 3: no email body is forwarded
	 * 
	 * 4: complete subject line is replicated
	 * 
	 * @param receivedEmail
	 */
	public ArrayList<EmailAttachment> processEmailForLobbycloud(ReceivedEmail receivedEmail) {

		if (active) {
			ArrayList<EmailAttachment> res = new ArrayList<EmailAttachment>();
			boolean relevantFileTypesExist = false;
			int attc = 0;

			
			for (EmailAttachment att : receivedEmail.getAttachments()) {
				String mimeType = att.getDataSource().getContentType();

				if (AcceptedMimeTypes.isInEnum(mimeType)) {
					log.info("Attachment No " + attc++ + " Name : " + att.getName() + " MIME: " + mimeType+ ": type ok" );
					relevantFileTypesExist = true;
					res.add(att);
				}else{
					log.info(mimeType + ": type not ok" );
				}
				
			}
			if (relevantFileTypesExist) {
				processRequest((MailAddress[]) ArrayUtils.addAll(receivedEmail.getCc(), receivedEmail.getTo()),
						receivedEmail, res);
				return res;
			}

		}
		return null;
	}

	/**
	 * 
	 * check if lobbycloud forward is active in user (mep) profile
	 * 
	 * @param mailTOsAndCCs
	 * @param toBeFwdedAttachements
	 * @param fromEmail
	 * @param emailSubject
	 */
	public void processRequest(MailAddress[] mailTOsAndCCs, ReceivedEmail receivedEmail,
			ArrayList<EmailAttachment> toBeFwdedAttachements) {

		try {
			log.debug("processing  mail for to/cc {} and submitter {}", mailTOsAndCCs,
					receivedEmail.getFrom().getEmail());

			// let's examine the senders

			// 1: CC must match one of user.aliases

			// 2: FROM must match either user.email or one of
			// user.submitters

			// 1: did they use a valid alias?
			User userWithAlias = null;
			String ali = null;
			for (MailAddress cc : mailTOsAndCCs) {
				ali = cc.getEmail().subSequence(0, cc.getEmail().indexOf("@")).toString();
				log.debug("Alias to check : " + ali);
				Alias aliasOfUser = aliasRepository.findOneByAlias(ali);
				if (aliasOfUser != null) {
					userWithAlias = aliasOfUser.getUser();
					log.debug("Alias belongs to  " + ali + " : User " + userWithAlias.getLogin() + " with email "
							+ userWithAlias.getEmail());
					break;
				}
			}
			if (userWithAlias == null) {
				log.debug("Message not forwarded. Sent from " + receivedEmail.getFrom()
						+ " to an alias not corresponding to the sender:" + ali);
				auditThis(new String[] { "message=Message not forwarded. Sent from " + receivedEmail.getFrom().getEmail()
						+ " to an alias not corresponding to the sender:  " + ali });
				return;
			}

			// 2: check if there is a user or submitter whose email matches
			// FROM:
			Optional<User> user = userRepository.findOneByEmail(userWithAlias.getEmail());
			String submitterInFact = null;
			if (user.isPresent()) {
				log.trace("" + user.get().getEmail());
				// .... or whose allowed submitter emails matches FROM:
				for (Submitter submitter : submitterRepository.findAllByUserId(user.get().getId())) {
					log.trace("Submitter email: " + submitter.getEmail());
					if (submitter.getEmail().compareToIgnoreCase(receivedEmail.getFrom().getEmail()) == 0) {
						// submitter is still active?
						log.trace("First submitter " + submitter.getId() + " active ? " + submitter.getActive()
								+ "   match in lobbycal for user login " + submitter.getUser().getLogin()
								+ " and alias " + ali);
						if (submitter.getActive()) {
							user = Optional.of(submitter.getUser());
							submitterInFact = submitter.getEmail();
							log.trace("Submitter active " + submitterInFact);
							break;
						} else {
							log.warn("Submitter not active " + submitterInFact);
							auditThis(new String[] {
									"message=Submitter not active. " + submitterInFact + " " + submitter.getEmail() });
						}
					} else {
						log.trace("mail sent from " + receivedEmail.getFrom());
						log.trace("lobbycal user {} himself is sender with email {} ", user.get().getLogin(),
								user.get().getEmail());

						submitterInFact = receivedEmail.getFrom().getEmail();
					}
				}

			} else {
				log.debug("User {} does not exist ", receivedEmail.getFrom());
				// submitterInFact = fromEmail.getEmail();
				auditThis(new String[] {
						"message=Lobbycloud delivery not successfull. User does not exist. Unauthorized attempt to create a meeting entry sent from "
								+ receivedEmail.getFrom().getEmail() });
				return;
			}

			if (!user.isPresent()) {
				auditThis(new String[] { "message=Lobbycloud delivery not successfull. No corresponding user  " });
				return;
			} else {

				log.debug("Processing email: deliver to lobbycloud");
				// TK XXX CONTINUE HERE
				log.info(submitterInFact);
				log.info(user.get().getFirstName() + " " + user.get().getLastName());
				// only if user setting lobbycloud forward is activated
				if (user.get().isLobbycloudSharingEnabled()) {

					mailService.forwardLobbycloudEmail(sendTo, receivedEmail.getSubject(), toBeFwdedAttachements);
					auditThis(new String[] { subMsg.substring(0, Math.min(subMsg.length(), 254)),
							"message="+toBeFwdedAttachements.size()+" attachments of email " + receivedEmail.getMessageNumber() + " sent by user  " +user.get().getLogin()
									+ " delivered to lobbycloud." });
					subMsg = "";
					callbackHook();

				} else {
					auditThis(new String[] { "User attempt to forward email " + receivedEmail.getMessageNumber()
							+ " to clobbycloud, but user profile settting prevents this. " });
					subMsg = "";
					return;
				}

			}
			log.debug("");
		} catch (Exception uec) {
			log.error("" + uec.getMessage());
			uec.printStackTrace();
		}
	}

	/**
	 * callback to lobbycal.lobbycloud.triggerUrl in case no email push is available 
	 */
	private void callbackHook() {
		try {
		    URL myURL = new URL(url);
		    URLConnection myURLConnection = myURL.openConnection();
		    myURLConnection.connect();
		} 
		catch (Exception e) {
			log.error(e.getMessage());
		}
		
	}

	void auditThis(String[] msg) {

		if (active) {
			AuditEvent event = new AuditEvent("system", AuditEventPublisher.TYPE_LOBBYCLOUD, msg);
			auditPublisher.publish(event);
		}

	}
}
