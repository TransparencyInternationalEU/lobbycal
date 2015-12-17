package eu.transparency.lobbycal.web.rest;

import eu.transparency.lobbycal.Application;
import eu.transparency.lobbycal.domain.Submitter;
import eu.transparency.lobbycal.repository.SubmitterRepository;
import eu.transparency.lobbycal.repository.search.SubmitterSearchRepository;
import eu.transparency.lobbycal.web.rest.SubmitterResource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.hasItem;

import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the SubmitterResource REST controller.
 *
 * @see SubmitterResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class SubmitterResourceTest {

	private static final String DEFAULT_EMAIL = "SAMPLE_TEXT";
	private static final String UPDATED_EMAIL = "UPDATED_TEXT";

	private static final Boolean DEFAULT_ACTIVE = false;
	private static final Boolean UPDATED_ACTIVE = true;

	private static final Long DEFAULT_VERSION = 0L;
	private static final Long UPDATED_VERSION = 1L;

	@Inject
	private SubmitterRepository submitterRepository;

	@Inject
	private SubmitterSearchRepository submitterSearchRepository;

	private MockMvc restSubmitterMockMvc;

	private Submitter submitter;

	@PostConstruct
	public void setup() {
		MockitoAnnotations.initMocks(this);
		SubmitterResource submitterResource = new SubmitterResource();
		ReflectionTestUtils.setField(submitterResource, "submitterRepository",
				submitterRepository);
		ReflectionTestUtils.setField(submitterResource,
				"submitterSearchRepository", submitterSearchRepository);
		this.restSubmitterMockMvc = MockMvcBuilders.standaloneSetup(
				submitterResource).build();
	}

	@Before
	public void initTest() {
		submitter = new Submitter();
		submitter.setEmail(DEFAULT_EMAIL);
		submitter.setActive(DEFAULT_ACTIVE);
	}

	@Test
	@Transactional
	public void createSubmitter() throws Exception {
		int databaseSizeBeforeCreate = submitterRepository.findAll().size();

		// Create the Submitter
		restSubmitterMockMvc.perform(
				post("/api/submitters").contentType(
						TestUtil.APPLICATION_JSON_UTF8).content(
						TestUtil.convertObjectToJsonBytes(submitter)))
				.andExpect(status().isCreated());

		// Validate the Submitter in the database
		List<Submitter> submitters = submitterRepository.findAll();
		assertThat(submitters).hasSize(databaseSizeBeforeCreate + 1);
		Submitter testSubmitter = submitters.get(submitters.size() - 1);
		assertThat(testSubmitter.getEmail()).isEqualTo(DEFAULT_EMAIL);
		assertThat(testSubmitter.getActive()).isEqualTo(DEFAULT_ACTIVE);
	}

	@Test
	@Transactional
	public void checkEmailIsRequired() throws Exception {
		// Validate the database is empty
		// assertThat(submitterRepository.findAll()).hasSize(0);
		// set the field null
		submitter.setEmail(null);

		// Create the Submitter, which fails.
		restSubmitterMockMvc.perform(
				post("/api/submitters").contentType(
						TestUtil.APPLICATION_JSON_UTF8).content(
						TestUtil.convertObjectToJsonBytes(submitter)))
				.andExpect(status().isBadRequest());

		// Validate the database is still empty
		List<Submitter> submitters = submitterRepository.findAll();
		assertThat(submitters).hasSize(0);
	}

	@Test
	@Transactional
	public void checkVersionIsRequired() throws Exception {
		// Validate the database is empty
		assertThat(submitterRepository.findAll()).hasSize(0);
		// set the field null

		// Create the Submitter, which fails.
		restSubmitterMockMvc.perform(
				post("/api/submitters").contentType(
						TestUtil.APPLICATION_JSON_UTF8).content(
						TestUtil.convertObjectToJsonBytes(submitter)))
				.andExpect(status().isBadRequest());

		// Validate the database is still empty
		List<Submitter> submitters = submitterRepository.findAll();
		assertThat(submitters).hasSize(0);
	}

	@Test
	@Transactional
	public void getAllSubmitters() throws Exception {
		// Initialize the database
		submitterRepository.saveAndFlush(submitter);

		// Get all the submitters
		restSubmitterMockMvc
				.perform(get("/api/submitters"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(
						jsonPath("$.[*].id").value(
								hasItem(submitter.getId().intValue())))
				.andExpect(
						jsonPath("$.[*].email").value(
								hasItem(DEFAULT_EMAIL.toString())))
				.andExpect(
						jsonPath("$.[*].active").value(
								hasItem(DEFAULT_ACTIVE.booleanValue())))
				.andExpect(
						jsonPath("$.[*].version").value(
								hasItem(DEFAULT_VERSION.intValue())));
	}

	@Test
	@Transactional
	public void getSubmitter() throws Exception {
		// Initialize the database
		submitterRepository.saveAndFlush(submitter);

		// Get the submitter
		restSubmitterMockMvc
				.perform(get("/api/submitters/{id}", submitter.getId()))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id").value(submitter.getId().intValue()))
				.andExpect(jsonPath("$.email").value(DEFAULT_EMAIL.toString()))
				.andExpect(
						jsonPath("$.active").value(
								DEFAULT_ACTIVE.booleanValue()))
				.andExpect(
						jsonPath("$.version").value(DEFAULT_VERSION.intValue()));
	}

	@Test
	@Transactional
	public void getNonExistingSubmitter() throws Exception {
		// Get the submitter
		restSubmitterMockMvc.perform(
				get("/api/submitters/{id}", Long.MAX_VALUE)).andExpect(
				status().isNotFound());
	}

	@Test
	@Transactional
	public void updateSubmitter() throws Exception {
		// Initialize the database
		submitterRepository.saveAndFlush(submitter);

		int databaseSizeBeforeUpdate = submitterRepository.findAll().size();

		// Update the submitter
		submitter.setEmail(UPDATED_EMAIL);
		submitter.setActive(UPDATED_ACTIVE);
		restSubmitterMockMvc.perform(
				put("/api/submitters").contentType(
						TestUtil.APPLICATION_JSON_UTF8).content(
						TestUtil.convertObjectToJsonBytes(submitter)))
				.andExpect(status().isOk());

		// Validate the Submitter in the database
		List<Submitter> submitters = submitterRepository.findAll();
		assertThat(submitters).hasSize(databaseSizeBeforeUpdate);
		Submitter testSubmitter = submitters.get(submitters.size() - 1);
		assertThat(testSubmitter.getEmail()).isEqualTo(UPDATED_EMAIL);
		assertThat(testSubmitter.getActive()).isEqualTo(UPDATED_ACTIVE);
	}

	@Test
	@Transactional
	public void deleteSubmitter() throws Exception {
		// Initialize the database
		submitterRepository.saveAndFlush(submitter);

		int databaseSizeBeforeDelete = submitterRepository.findAll().size();

		// Get the submitter
		restSubmitterMockMvc.perform(
				delete("/api/submitters/{id}", submitter.getId()).accept(
						TestUtil.APPLICATION_JSON_UTF8)).andExpect(
				status().isOk());

		// Validate the database is empty
		List<Submitter> submitters = submitterRepository.findAll();
		assertThat(submitters).hasSize(databaseSizeBeforeDelete - 1);
	}
}
