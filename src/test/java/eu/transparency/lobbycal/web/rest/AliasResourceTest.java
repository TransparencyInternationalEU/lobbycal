package eu.transparency.lobbycal.web.rest;

import eu.transparency.lobbycal.Application;
import eu.transparency.lobbycal.domain.Alias;
import eu.transparency.lobbycal.repository.AliasRepository;
import eu.transparency.lobbycal.web.rest.AliasResource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.hasItem;

import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.ComponentScan;
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
 * Test class for the AliasResource REST controller.
 *
 * @see AliasResource
 */
@ComponentScan
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class AliasResourceTest {

    private static final String DEFAULT_ALIAS = "SAMPLE_TEXT";
    private static final String UPDATED_ALIAS = "UPDATED_TEXT";

    private static final Long DEFAULT_VERSION = 0L;
    private static final Long UPDATED_VERSION = 1L;

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    @Inject
    private AliasRepository aliasRepository;


    private MockMvc restAliasMockMvc;

    private Alias alias;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        AliasResource aliasResource = new AliasResource();
        ReflectionTestUtils.setField(aliasResource, "aliasRepository", aliasRepository);
        this.restAliasMockMvc = MockMvcBuilders.standaloneSetup(aliasResource).build();
    }

    @Before
    public void initTest() {
        alias = new Alias();
        alias.setAlias(DEFAULT_ALIAS);
        alias.setActive(DEFAULT_ACTIVE);
    }

    @Test
    @Transactional
    public void createAlias() throws Exception {
        int databaseSizeBeforeCreate = aliasRepository.findAll().size();

        // Create the Alias
        restAliasMockMvc.perform(post("/api/aliass")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(alias)))
                .andExpect(status().isCreated());

        // Validate the Alias in the database
        List<Alias> aliass = aliasRepository.findAll();
        assertThat(aliass).hasSize(databaseSizeBeforeCreate + 1);
        Alias testAlias = aliass.get(aliass.size() - 1);
        assertThat(testAlias.getAlias()).isEqualTo(DEFAULT_ALIAS);
        assertThat(testAlias.getActive()).isEqualTo(DEFAULT_ACTIVE);
    }

    @Test
    @Transactional
    public void checkAliasIsRequired() throws Exception {
        // Validate the database is empty
        assertThat(aliasRepository.findAll()).hasSize(0);
        // set the field null
        alias.setAlias(null);

        // Create the Alias, which fails.
        restAliasMockMvc.perform(post("/api/aliass")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(alias)))
                .andExpect(status().isBadRequest());

        // Validate the database is still empty
        List<Alias> aliass = aliasRepository.findAll();
        assertThat(aliass).hasSize(0);
    }

    @Test
    @Transactional
    public void checkVersionIsRequired() throws Exception {
        // Validate the database is empty
        assertThat(aliasRepository.findAll()).hasSize(0);
        // set the field null

        // Create the Alias, which fails.
        restAliasMockMvc.perform(post("/api/aliass")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(alias)))
                .andExpect(status().isBadRequest());

        // Validate the database is still empty
        List<Alias> aliass = aliasRepository.findAll();
        assertThat(aliass).hasSize(0);
    }

    @Test
    @Transactional
    public void getAllAliass() throws Exception {
        // Initialize the database
        aliasRepository.saveAndFlush(alias);

        // Get all the aliass
        restAliasMockMvc.perform(get("/api/aliass"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(alias.getId().intValue())))
                .andExpect(jsonPath("$.[*].alias").value(hasItem(DEFAULT_ALIAS.toString())))
                .andExpect(jsonPath("$.[*].version").value(hasItem(DEFAULT_VERSION.intValue())))
                .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())));
    }

    @Test
    @Transactional
    public void getAlias() throws Exception {
        // Initialize the database
        aliasRepository.saveAndFlush(alias);

        // Get the alias
        restAliasMockMvc.perform(get("/api/aliass/{id}", alias.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(alias.getId().intValue()))
            .andExpect(jsonPath("$.alias").value(DEFAULT_ALIAS.toString()))
            .andExpect(jsonPath("$.version").value(DEFAULT_VERSION.intValue()))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()));
    }

    @Test
    @Transactional
    public void getNonExistingAlias() throws Exception {
        // Get the alias
        restAliasMockMvc.perform(get("/api/aliass/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateAlias() throws Exception {
        // Initialize the database
        aliasRepository.saveAndFlush(alias);

		int databaseSizeBeforeUpdate = aliasRepository.findAll().size();

        // Update the alias
        alias.setAlias(UPDATED_ALIAS);
        alias.setActive(UPDATED_ACTIVE);
        restAliasMockMvc.perform(put("/api/aliass")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(alias)))
                ;

        // Validate the Alias in the database
        List<Alias> aliass = aliasRepository.findAll();
        assertThat(aliass).hasSize(databaseSizeBeforeUpdate);
        Alias testAlias = aliass.get(aliass.size() - 1);
        assertThat(testAlias.getAlias()).isEqualTo(UPDATED_ALIAS);
        assertThat(testAlias.getActive()).isEqualTo(UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    public void deleteAlias() throws Exception {
        // Initialize the database
        aliasRepository.saveAndFlush(alias);

		int databaseSizeBeforeDelete = aliasRepository.findAll().size();

        // Get the alias
        restAliasMockMvc.perform(delete("/api/aliass/{id}", alias.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8));

        // Validate the database is empty
        List<Alias> aliass = aliasRepository.findAll();
        assertThat(aliass).hasSize(databaseSizeBeforeDelete - 1);
    }
}
