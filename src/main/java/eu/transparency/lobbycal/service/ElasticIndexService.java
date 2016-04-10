package eu.transparency.lobbycal.service;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codahale.metrics.annotation.Timed;

import eu.transparency.lobbycal.domain.Alias;
import eu.transparency.lobbycal.domain.Meeting;
import eu.transparency.lobbycal.domain.Partner;
import eu.transparency.lobbycal.domain.Submitter;
import eu.transparency.lobbycal.domain.Tag;
import eu.transparency.lobbycal.domain.User;
import eu.transparency.lobbycal.repository.AliasRepository;
import eu.transparency.lobbycal.repository.MeetingRepository;
import eu.transparency.lobbycal.repository.PartnerRepository;
import eu.transparency.lobbycal.repository.SubmitterRepository;
import eu.transparency.lobbycal.repository.TagRepository;
import eu.transparency.lobbycal.repository.UserRepository;
import eu.transparency.lobbycal.repository.search.AliasSearchRepository;
import eu.transparency.lobbycal.repository.search.MeetingSearchRepository;
import eu.transparency.lobbycal.repository.search.PartnerSearchRepository;
import eu.transparency.lobbycal.repository.search.SubmitterSearchRepository;
import eu.transparency.lobbycal.repository.search.TagSearchRepository;
import eu.transparency.lobbycal.repository.search.UserSearchRepository;

@Service
@Transactional
public class ElasticIndexService {

	private final Logger log = LoggerFactory.getLogger(ElasticIndexService.class);

	@Inject
	private MeetingRepository meetingRepository;

	@Inject
	private MeetingSearchRepository meetingSearchRepository;

	@Inject
	private UserRepository userRepository;

	@Inject
	private UserSearchRepository userSearchRepository;

	@Inject
	private PartnerRepository partnerRepository;

	@Inject
	private PartnerSearchRepository partnerSearchRepository;

	@Inject
	private TagRepository tagRepository;

	@Inject
	private TagSearchRepository tagSearchRepository;

	@Inject
	private AliasRepository aliasRepository;

	@Inject
	private AliasSearchRepository aliasSearchRepository;

	@Inject
	private SubmitterRepository submitterRepository;

	@Inject
	private SubmitterSearchRepository submitterSearchRepository;

	@Inject
	private ElasticsearchTemplate elasticsearchTemplate;

	/**
	 * MAKE SURE TO LEAVE THE LOG MESSAGES IN! THEY ENSURE LAZY LOADING OF
	 * MEMBERS (TAGS; PARTNERS)
	 * 
	 */
	@Async
	@Timed
	public void reindexAll() {

		elasticsearchTemplate.deleteIndex(Meeting.class);
		if (meetingRepository.count() > 0) {
			List<Meeting> mlist = meetingRepository.findAll();

			log.trace("");
			for (Meeting meeting : mlist) {
				log.info("" + meeting.getPartners().size());
				log.info("" + meeting.getTags().size());
				log.info("" + meeting.getmTag());
				log.info("" + meeting.getmPartner());
			}
			meetingSearchRepository.save(mlist);
			log.trace("Elasticsearch: Indexed all meetings");
		} else {
			log.error("Deleting of index / reindexing failed");
		}

		elasticsearchTemplate.deleteIndex(Tag.class);
		if (tagRepository.count() > 0) {
			List<Tag> tagList = tagRepository.findAll();
			log.debug("");
			for (Tag tag : tagList) {
				log.info(tag.geti18nKey());
			}
			tagSearchRepository.save(tagList);
			log.info("Elasticsearch: Indexed all tags");
		} else {
			log.error("Deleting of index / reindexing failed");
		}

		elasticsearchTemplate.deleteIndex(Partner.class);
		if (partnerRepository.count() > 0) {
			partnerSearchRepository.save(partnerRepository.findAll());
			log.info("Elasticsearch: Indexed all partners");
		} else {
			log.error("Deleting of index / reindexing failed");
		}

		elasticsearchTemplate.deleteIndex(Alias.class);
		if (aliasRepository.count() > 0) {
			aliasSearchRepository.save(aliasRepository.findAll());
			log.info("Elasticsearch: Indexed all aliases");
		} else {
			log.error("Deleting of index / reindexing failed");
		}

		elasticsearchTemplate.deleteIndex(Submitter.class);
		if (submitterRepository.count() > 0) {
			submitterSearchRepository.save(submitterRepository.findAll());
			log.info("Elasticsearch: Indexed all submitters");
		} else {
			log.error("Deleting of index / reindexing failed");
		}

		elasticsearchTemplate.deleteIndex(User.class);
		if (userRepository.count() > 0) {
			userSearchRepository.save(userRepository.findAll());
			log.info("Elasticsearch: Indexed all user");
		} else {
			log.error("Deleting of index / reindexing failed");
		}

		
		log.info("Elasticsearch: Successfully performed reindexing");
	}
}
