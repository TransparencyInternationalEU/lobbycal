/**
 * 
 */
package eu.transparency.lobbycal.aop.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.listener.AuditApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Configuration;

import eu.transparency.lobbycal.service.MeetingService;

/**
 * @author lobbycal
 *
 */
@Configuration
public class AuditEventPublisher implements ApplicationEventPublisherAware {
	private final Logger log = LoggerFactory
			.getLogger(AuditEventPublisher.class);

	private ApplicationEventPublisher publisher;

	public static String TYPE_USER = "USER_EVENT";

	public static String TYPE_MEETING = "MEETING_EVENT";

	public static String TYPE_MAIL = "MAIL_EVENT";

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
		log.warn("");

	}

	public void publish(AuditEvent event) {
		log.warn(event.getPrincipal());
		if (this.publisher != null) {
			this.publisher.publishEvent(new AuditApplicationEvent(event));
		}
	}

}
