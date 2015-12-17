package eu.transparency.lobbycal.repository.search;

import eu.transparency.lobbycal.domain.Partner;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the Partner entity.
 */
public interface PartnerSearchRepository extends ElasticsearchRepository<Partner, Long> {
}
