package eu.transparency.lobbycal.service;

import com.codahale.metrics.annotation.Timed;
import eu.transparency.lobbycal.domain.*;
import eu.transparency.lobbycal.repository.*;
import eu.transparency.lobbycal.repository.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

@Service
@Transactional
public class ElasticIndexService {

	private final Logger log = LoggerFactory
			.getLogger(ElasticIndexService.class);

	@Inject
	private MeetingRepository meetingRepository;

	@Inject
	private MeetingSearchRepository meetingSearchRepository;

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

	@Async
	@Timed
	public void reindexAll() {
		elasticsearchTemplate.deleteIndex(Meeting.class);
		if (meetingRepository.count() > 0) {
			meetingSearchRepository.save(meetingRepository.findAll());
			log.info("Elasticsearch: Indexed all meetings");
		} else {
			log.error("Deleting of index / reindexing failed");
		}

		elasticsearchTemplate.deleteIndex(Tag.class);
		if (tagRepository.count() > 0) {
			tagSearchRepository.save(tagRepository.findAll());
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

		log.info("Elasticsearch: Successfully performed reindexing");
	}
}
