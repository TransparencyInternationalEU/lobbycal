package eu.transparency.lobbycal.repository.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import eu.transparency.lobbycal.domain.Meeting;

/**
 * Spring Data ElasticSearch repository for the Meeting entity.
 */
public interface MeetingSearchRepository extends ElasticsearchRepository<Meeting, Long> {
	
	

	
}
