package eu.transparency.lobbycal.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
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

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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
import eu.transparency.lobbycal.domain.Meeting;
import eu.transparency.lobbycal.repository.MeetingRepository;
import eu.transparency.lobbycal.repository.search.MeetingSearchRepository;
import eu.transparency.lobbycal.web.rest.MeetingResource;
import eu.transparency.lobbycal.web.rest.TestUtil;
import eu.transparency.lobbycal.web.rest.mapper.MeetingMapper;

/**
 * Test class for the MeetingResource REST controller.
 *
 * @see MeetingResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class MeetingResourceTest {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private static final String DEFAULT_TITLE = "SAMPLE_TEXT";
    private static final String UPDATED_TITLE = "UPDATED_TEXT";
    private static final String DEFAULT_SUBMITTER = "SAMPLE_TEXT";
    private static final String UPDATED_SUBMITTER = "UPDATED_TEXT";
    private static final String DEFAULT_ALIAS_USED = "SAMPLE_TEXT";
    private static final String UPDATED_ALIAS_USED = "UPDATED_TEXT";

    private static final DateTime DEFAULT_START_DATE = new DateTime(0L, DateTimeZone.UTC);
    private static final DateTime UPDATED_START_DATE = new DateTime(DateTimeZone.UTC).withMillisOfSecond(0);
    private static final String DEFAULT_START_DATE_STR = dateTimeFormatter.print(DEFAULT_START_DATE);

    private static final DateTime DEFAULT_END_DATE = new DateTime(0L, DateTimeZone.UTC);
    private static final DateTime UPDATED_END_DATE = new DateTime(DateTimeZone.UTC).withMillisOfSecond(0);
    private static final String DEFAULT_END_DATE_STR = dateTimeFormatter.print(DEFAULT_END_DATE);
    private static final String DEFAULT_U_ID = "SAMPLE_TEXT";
    private static final String UPDATED_U_ID = "UPDATED_TEXT";

    @Inject
    private MeetingRepository meetingRepository;

    @Inject
    private MeetingMapper meetingMapper;

    @Inject
    private MeetingSearchRepository meetingSearchRepository;

    private MockMvc restMeetingMockMvc;

    private Meeting meeting;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        MeetingResource meetingResource = new MeetingResource();
        ReflectionTestUtils.setField(meetingResource, "meetingRepository", meetingRepository);
        ReflectionTestUtils.setField(meetingResource, "meetingMapper", meetingMapper);
        ReflectionTestUtils.setField(meetingResource, "meetingSearchRepository", meetingSearchRepository);
        this.restMeetingMockMvc = MockMvcBuilders.standaloneSetup(meetingResource).build();
    }

    @Before
    public void initTest() {
        meeting = new Meeting();
        meeting.setTitle(DEFAULT_TITLE);
        meeting.setSubmitter(DEFAULT_SUBMITTER);
        meeting.setAliasUsed(DEFAULT_ALIAS_USED);
        meeting.setStartDate(DEFAULT_START_DATE);
        meeting.setEndDate(DEFAULT_END_DATE);
        meeting.setUid(DEFAULT_U_ID);
    }

    @Test
    @Transactional
    public void createMeeting() throws Exception {
        int databaseSizeBeforeCreate = meetingRepository.findAll().size();

        // Create the Meeting
        restMeetingMockMvc.perform(post("/api/meetings")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(meeting)))
                .andExpect(status().isCreated());

        // Validate the Meeting in the database
        List<Meeting> meetings = meetingRepository.findAll();
        assertThat(meetings).hasSize(databaseSizeBeforeCreate + 1);
        Meeting testMeeting = meetings.get(meetings.size() - 1);
        assertThat(testMeeting.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testMeeting.getSubmitter()).isEqualTo(DEFAULT_SUBMITTER);
        assertThat(testMeeting.getAliasUsed()).isEqualTo(DEFAULT_ALIAS_USED);
        assertThat(testMeeting.getStartDate().toDateTime(DateTimeZone.UTC)).isEqualTo(DEFAULT_START_DATE);
        assertThat(testMeeting.getEndDate().toDateTime(DateTimeZone.UTC)).isEqualTo(DEFAULT_END_DATE);
        assertThat(testMeeting.getUid()).isEqualTo(DEFAULT_U_ID);
    }

    @Test
    @Transactional
    public void getAllMeetings() throws Exception {
        // Initialize the database
        meetingRepository.saveAndFlush(meeting);

        // Get all the meetings
        restMeetingMockMvc.perform(get("/api/meetings"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(meeting.getId().intValue())))
                .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE.toString())))
                .andExpect(jsonPath("$.[*].submitter").value(hasItem(DEFAULT_SUBMITTER.toString())))
                .andExpect(jsonPath("$.[*].aliasUsed").value(hasItem(DEFAULT_ALIAS_USED.toString())))
                .andExpect(jsonPath("$.[*].startDate").value(hasItem(DEFAULT_START_DATE_STR)))
                .andExpect(jsonPath("$.[*].endDate").value(hasItem(DEFAULT_END_DATE_STR)))
                .andExpect(jsonPath("$.[*].uid").value(hasItem(DEFAULT_U_ID.toString())));
    }

    @Test
    @Transactional
    public void getMeeting() throws Exception {
        // Initialize the database
        meetingRepository.saveAndFlush(meeting);

        // Get the meeting
        restMeetingMockMvc.perform(get("/api/meetings/{id}", meeting.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(meeting.getId().intValue()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE.toString()))
            .andExpect(jsonPath("$.submitter").value(DEFAULT_SUBMITTER.toString()))
            .andExpect(jsonPath("$.aliasUsed").value(DEFAULT_ALIAS_USED.toString()))
            .andExpect(jsonPath("$.startDate").value(DEFAULT_START_DATE_STR))
            .andExpect(jsonPath("$.endDate").value(DEFAULT_END_DATE_STR))
            .andExpect(jsonPath("$.uid").value(DEFAULT_U_ID.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingMeeting() throws Exception {
        // Get the meeting
        restMeetingMockMvc.perform(get("/api/meetings/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateMeeting() throws Exception {
        // Initialize the database
        meetingRepository.saveAndFlush(meeting);

		int databaseSizeBeforeUpdate = meetingRepository.findAll().size();

        // Update the meeting
        meeting.setTitle(UPDATED_TITLE);
        meeting.setSubmitter(UPDATED_SUBMITTER);
        meeting.setAliasUsed(UPDATED_ALIAS_USED);
        meeting.setStartDate(UPDATED_START_DATE);
        meeting.setEndDate(UPDATED_END_DATE);
        meeting.setUid(UPDATED_U_ID);
        restMeetingMockMvc.perform(put("/api/meetings")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(meeting)))
                .andExpect(status().isOk());

        // Validate the Meeting in the database
        List<Meeting> meetings = meetingRepository.findAll();
        assertThat(meetings).hasSize(databaseSizeBeforeUpdate);
        Meeting testMeeting = meetings.get(meetings.size() - 1);
        assertThat(testMeeting.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testMeeting.getSubmitter()).isEqualTo(UPDATED_SUBMITTER);
        assertThat(testMeeting.getAliasUsed()).isEqualTo(UPDATED_ALIAS_USED);
        assertThat(testMeeting.getStartDate().toDateTime(DateTimeZone.UTC)).isEqualTo(UPDATED_START_DATE);
        assertThat(testMeeting.getEndDate().toDateTime(DateTimeZone.UTC)).isEqualTo(UPDATED_END_DATE);
        assertThat(testMeeting.getUid()).isEqualTo(UPDATED_U_ID);
    }

    @Test
    @Transactional
    public void deleteMeeting() throws Exception {
        // Initialize the database
        meetingRepository.saveAndFlush(meeting);

		int databaseSizeBeforeDelete = meetingRepository.findAll().size();

        // Get the meeting
        restMeetingMockMvc.perform(delete("/api/meetings/{id}", meeting.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate the database is empty
        List<Meeting> meetings = meetingRepository.findAll();
        assertThat(meetings).hasSize(databaseSizeBeforeDelete - 1);
    }
}
