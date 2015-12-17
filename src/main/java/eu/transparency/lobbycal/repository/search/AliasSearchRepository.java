package eu.transparency.lobbycal.repository.search;

import eu.transparency.lobbycal.domain.Alias;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the Alias entity.
 */
public interface AliasSearchRepository extends ElasticsearchRepository<Alias, Long> {
}
