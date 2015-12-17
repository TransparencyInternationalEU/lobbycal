package eu.transparency.lobbycal.service;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import eu.transparency.lobbycal.Application;
import eu.transparency.lobbycal.service.MailFetcherService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
@EnableAutoConfiguration
public class MailFetcherServiceTest {


	@Inject
	private MailFetcherService mailFetcherService;

	@Test
	public void testReceiveFolder() {
//		this.mailFetcherService.fetchAllEmail();
	}

}
