package eu.transparency.lobbycal.repository.search;

import eu.transparency.lobbycal.domain.Submitter;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the Submitter entity.
 */
public interface SubmitterSearchRepository extends ElasticsearchRepository<Submitter, Long> {
}
