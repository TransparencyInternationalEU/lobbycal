package eu.transparency.lobbycal.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.CharEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

import eu.transparency.lobbycal.config.JHipsterProperties;
import eu.transparency.lobbycal.domain.Submitter;
import eu.transparency.lobbycal.domain.User;
import jodd.mail.EmailAttachment;

/**
 * Service for sending e-mails.
 * <p/>
 * <p>
 * We use the @Async annotation to send e-mails asynchronously.
 * </p>
 */
@Service
public class MailService {

	private final Logger log = LoggerFactory.getLogger(MailService.class);


	@Inject
	private JHipsterProperties jHipsterProperties;


	@Inject
	private JavaMailSenderImpl javaMailSender;


	@Inject
	private MessageSource messageSource;


	@Inject
	private SpringTemplateEngine templateEngine;


	/**
	 * System default email address that sends the e-mails.
	 */
	private String from;

	public enum AcceptedMimeTypes {
		ODS("APPLICATION/VND.OASIS.OPENDOCUMENT.SPREADSHEET"), XLS(
				"APPLICATION/VND.OPENXMLFORMATS-OFFICEDOCUMENT.SPREADSHEETML.SHEET"), DOC("APPLICATION/MSWORD"), DOCX(
						"APPLICATION/VND.OPENXMLFORMATS-OFFICEDOCUMENT.WORDPROCESSINGML.DOCUMENT"), ODT(
								"APPLICATION/VND.OASIS.OPENDOCUMENT.TEXT"), PDF(
										"APPLICATION/PDF"), RTF("APPLICATION/MSWORD"), TIFF("IMAGE/TIFF");

		public final String text;

		private AcceptedMimeTypes(final String text) {
			this.text = text;
		}

		@Override
		public String toString() {

			return text;
		}
		public static boolean isInEnum(String value) {
		     return Arrays.stream(AcceptedMimeTypes.values()).anyMatch(e -> e.toString().equals(value));
		}
	}

	@Async
	public void sendEmail(String to, String subject, String content, boolean isMultipart, boolean isHtml) {

		log.trace("Send e-mail[multipart '{}' and html '{}'] to '{}' with subject '{}' and content={}", isMultipart,
				isHtml, to, subject, content);

		// Prepare message using a Spring helper
		MimeMessage mimeMessage = javaMailSender.createMimeMessage();
		try {
			MimeMessageHelper message = new MimeMessageHelper(mimeMessage, isMultipart, CharEncoding.UTF_8);
			message.setTo(to);
			message.setFrom(jHipsterProperties.getMail().getFrom());
			message.setSubject(subject);
			message.setText(content, isHtml);
			javaMailSender.send(mimeMessage);
			log.debug("Sent e-mail to User '{}'", to);
		} catch (Exception e) {
			log.warn("E-mail could not be sent to user '{}', exception is: {}", to, e.getMessage());
			log.error(javaMailSender.getJavaMailProperties().toString());
		}
	}

	
	
	@Async
	public void forwardLobbycloudEmail(String to, String subject, ArrayList<EmailAttachment> attachments ) {

		MimeMessage mimeMessage = javaMailSender.createMimeMessage();
		try {
			MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, CharEncoding.UTF_8);
			message.setTo(to);
			message.setFrom(jHipsterProperties.getMail().getFrom());
			message.setSubject(subject);
			for (EmailAttachment emailAttachment : attachments) {
				message.addAttachment(emailAttachment.getEncodedName(), emailAttachment.getDataSource());
			}
			message.setText("see attachments", false);
			javaMailSender.send(mimeMessage);
			log.debug("Forwarded e-mail to '{}'", to);
		} catch (Exception e) {
			log.warn("E-mail could not be sent to user '{}', exception is: {}", to, e.getMessage());
			log.error(javaMailSender.getJavaMailProperties().toString());
		}
	}

	@Async
	public void sendActivationEmail(User user, String baseUrl) {

		log.debug("Sending activation e-mail to '{}'", user.getEmail());
		Locale locale = Locale.forLanguageTag(user.getLangKey());
		Context context = new Context(locale);
		context.setVariable("user", user);
		context.setVariable("baseUrl", baseUrl);
		String content = templateEngine.process("activationEmail", context);
		log.info(content);
		String subject = messageSource.getMessage("email.activation.title", null, locale);
		sendEmail(user.getEmail(), subject, content, false, true);
	}

	@Async
	public String sendNoActivityMail(User user, List<Submitter> receivers, boolean remindSubmitter, boolean remindUser,
			String periodDays) {

		try {
			log.info("Sending reminder e-mail to '{}'" + user.getEmail() + "  submitters? : " + remindSubmitter
					+ "  user? : " + remindUser);

			Locale locale = Locale.forLanguageTag(user.getLangKey());
			Context context = new Context(locale);
			context.setVariable("user", user);
			context.setVariable("periodDays", periodDays);
			String content = templateEngine.process("noNewMeetingsActivity", context);
			String subject = messageSource.getMessage("email.noNewMeetingsActivity.title", null, locale);
			if (remindSubmitter && receivers != null && receivers.size() > 0) {
				for (Submitter submitter : receivers) {
					if (submitter.getActive()) {
						sendEmail(submitter.getEmail(), subject, content, false, true);
					}
				}
			}
			if (remindUser) {
				sendEmail(user.getEmail(), subject, content, false, true);
			}
			return "ok";
		} catch (Exception e) {
			log.error(e.getMessage());
			return "agh!";
		}
	}

	@Async
	public void sendPasswordResetMail(User user, String baseUrl) {

		log.debug("Sending password reset e-mail to '{}'", user.getEmail());
		Locale locale = Locale.forLanguageTag(user.getLangKey());
		Context context = new Context(locale);
		context.setVariable("user", user);
		context.setVariable("baseUrl", baseUrl);
		String content = templateEngine.process("passwordResetEmail", context);
		String subject = messageSource.getMessage("email.reset.title", null, locale);
		sendEmail(user.getEmail(), subject, content, false, true);
	}
}
