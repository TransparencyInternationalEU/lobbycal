package eu.transparency.lobbycal.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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

import eu.transparency.lobbycal.Application;
import eu.transparency.lobbycal.domain.Tag;
import eu.transparency.lobbycal.repository.TagRepository;
import eu.transparency.lobbycal.web.rest.mapper.TagMapper;

/**
 * Test class for the TagResource REST controller.
 *
 * @see TagResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class TagResourceTest {

    private static final String DEFAULT_I18N_KEY = "SAMPLE_TEXT";
    private static final String UPDATED_I18N_KEY = "UPDATED_TEXT";
    private static final String DEFAULT_DE = "SAMPLE_TEXT";
    private static final String UPDATED_DE = "UPDATED_TEXT";
    private static final String DEFAULT_EN = "SAMPLE_TEXT";
    private static final String UPDATED_EN = "UPDATED_TEXT";
    private static final String DEFAULT_FR = "SAMPLE_TEXT";
    private static final String UPDATED_FR = "UPDATED_TEXT";
    private static final String DEFAULT_ES = "SAMPLE_TEXT";
    private static final String UPDATED_ES = "UPDATED_TEXT";

    @Inject
    private TagRepository tagRepository;

    @Inject
    private TagMapper tagMapper;


    private MockMvc restTagMockMvc;

    private Tag tag;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        TagResource tagResource = new TagResource();
        ReflectionTestUtils.setField(tagResource, "tagRepository", tagRepository);
        ReflectionTestUtils.setField(tagResource, "tagMapper", tagMapper);
        this.restTagMockMvc = MockMvcBuilders.standaloneSetup(tagResource).build();
    }

    @Before
    public void initTest() {
        tag = new Tag();
        tag.seti18nKey(DEFAULT_I18N_KEY);
        tag.setDe(DEFAULT_DE);
        tag.setEn(DEFAULT_EN);
        tag.setFr(DEFAULT_FR);
        tag.setEs(DEFAULT_ES);
    }

    @Test
    @Transactional
    public void createTag() throws Exception {
        int databaseSizeBeforeCreate = tagRepository.findAll().size();

        // Create the Tag
        restTagMockMvc.perform(post("/api/tags")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(tag)))
                .andExpect(status().isCreated());

        // Validate the Tag in the database
        List<Tag> tags = tagRepository.findAll();
        assertThat(tags).hasSize(databaseSizeBeforeCreate + 1);
        Tag testTag = tags.get(tags.size() - 1);
        assertThat(testTag.geti18nKey()).isEqualTo(DEFAULT_I18N_KEY);
        assertThat(testTag.getDe()).isEqualTo(DEFAULT_DE);
        assertThat(testTag.getEn()).isEqualTo(DEFAULT_EN);
        assertThat(testTag.getFr()).isEqualTo(DEFAULT_FR);
        assertThat(testTag.getEs()).isEqualTo(DEFAULT_ES);
    }

    @Test
    @Transactional
    public void getAllTags() throws Exception {
        // Initialize the database
        tagRepository.saveAndFlush(tag);

        // Get all the tags
        restTagMockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(tag.getId().intValue())))
                .andExpect(jsonPath("$.[*].i18nKey").value(hasItem(DEFAULT_I18N_KEY.toString())))
                .andExpect(jsonPath("$.[*].de").value(hasItem(DEFAULT_DE.toString())))
                .andExpect(jsonPath("$.[*].en").value(hasItem(DEFAULT_EN.toString())))
                .andExpect(jsonPath("$.[*].fr").value(hasItem(DEFAULT_FR.toString())))
                .andExpect(jsonPath("$.[*].es").value(hasItem(DEFAULT_ES.toString())));
    }

    @Test
    @Transactional
    public void getTag() throws Exception {
        // Initialize the database
        tagRepository.saveAndFlush(tag);

        // Get the tag
        restTagMockMvc.perform(get("/api/tags/{id}", tag.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(tag.getId().intValue()))
            .andExpect(jsonPath("$.i18nKey").value(DEFAULT_I18N_KEY.toString()))
            .andExpect(jsonPath("$.de").value(DEFAULT_DE.toString()))
            .andExpect(jsonPath("$.en").value(DEFAULT_EN.toString()))
            .andExpect(jsonPath("$.fr").value(DEFAULT_FR.toString()))
            .andExpect(jsonPath("$.es").value(DEFAULT_ES.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingTag() throws Exception {
        // Get the tag
        restTagMockMvc.perform(get("/api/tags/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateTag() throws Exception {
        // Initialize the database
        tagRepository.saveAndFlush(tag);

		int databaseSizeBeforeUpdate = tagRepository.findAll().size();

        // Update the tag
        tag.seti18nKey(UPDATED_I18N_KEY);
        tag.setDe(UPDATED_DE);
        tag.setEn(UPDATED_EN);
        tag.setFr(UPDATED_FR);
        tag.setEs(UPDATED_ES);
        restTagMockMvc.perform(put("/api/tags")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(tag)))
                .andExpect(status().isOk());

        // Validate the Tag in the database
        List<Tag> tags = tagRepository.findAll();
        assertThat(tags).hasSize(databaseSizeBeforeUpdate);
        Tag testTag = tags.get(tags.size() - 1);
        assertThat(testTag.geti18nKey()).isEqualTo(UPDATED_I18N_KEY);
        assertThat(testTag.getDe()).isEqualTo(UPDATED_DE);
        assertThat(testTag.getEn()).isEqualTo(UPDATED_EN);
        assertThat(testTag.getFr()).isEqualTo(UPDATED_FR);
        assertThat(testTag.getEs()).isEqualTo(UPDATED_ES);
    }

    @Test
    @Transactional
    public void deleteTag() throws Exception {
        // Initialize the database
        tagRepository.saveAndFlush(tag);

		int databaseSizeBeforeDelete = tagRepository.findAll().size();

        // Get the tag
        restTagMockMvc.perform(delete("/api/tags/{id}", tag.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate the database is empty
        List<Tag> tags = tagRepository.findAll();
        assertThat(tags).hasSize(databaseSizeBeforeDelete - 1);
    }
}
