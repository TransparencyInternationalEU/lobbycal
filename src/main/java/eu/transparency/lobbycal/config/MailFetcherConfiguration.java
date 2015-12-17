package eu.transparency.lobbycal.config;

import javax.mail.Message;
import javax.mail.search.SearchTerm;

import jodd.mail.ImapServer;
import jodd.mail.ImapSslServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class MailFetcherConfiguration implements EnvironmentAware {

	private static final String ENV_SPRING_MAIL = "catchall.mail.";
	private static final String DEFAULT_HOST = "127.0.0.1";
	private static final String PROP_HOST = "host";
	private static final String DEFAULT_PROP_HOST = "localhost";
	private static final String PROP_NOSSL = "nossl";
	private static final String PROP_PORT = "port";
	private static final String DEFAULT_PROP_PORT = "143";
	private static final String PROP_USER = "username";
	private static final String PROP_PASSWORD = "password";

	private final Logger log = LoggerFactory
			.getLogger(MailFetcherConfiguration.class);

	private RelaxedPropertyResolver propertyResolver;

	@Override
	public void setEnvironment(Environment environment) {
		this.propertyResolver = new RelaxedPropertyResolver(environment,
				ENV_SPRING_MAIL);
	}

	@Bean
	public ImapServer javaMailReceiver() {
		log.info("Configuring mail receiver");
		String host = propertyResolver
				.getProperty(PROP_HOST, DEFAULT_PROP_HOST);
		String user = propertyResolver.getProperty(PROP_USER);
		String password = propertyResolver.getProperty(PROP_PASSWORD);
		String port = propertyResolver.getProperty(PROP_PORT, DEFAULT_PROP_PORT);
		
		log.info(port);
        log.info(user+":" +host);

		ImapServer imapReceiver = null;
		if (host != null && !host.isEmpty()) {
			if(propertyResolver.getProperty(PROP_NOSSL).compareTo("true")!=0){
				imapReceiver = new ImapSslServer(host.trim(), user.trim(),password.trim());
			}else{
				int p = Integer.parseInt(port);
				imapReceiver = new ImapServer(host.trim(), p, user.trim(),password.trim());
            	
			}
		} else {
			log.error("Warning! Your receiving mail server is not configured. We will try to use one on localhost.");
			log.info("Did you configure your imap catchall.mail settings in your application.yml?");
			return null;
		}
		log.info("Configuring mail receiver");
		log.info("Configuring mail receiver");
		return imapReceiver;
	}

	@SuppressWarnings("serial")
	private class AcceptAllSearchTerm extends SearchTerm {
		public boolean match(Message mesg) {
			return true;
		}
	}
}
