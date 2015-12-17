package eu.transparency.lobbycal.repository.search;

import eu.transparency.lobbycal.domain.Tag;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data ElasticSearch repository for the Tag entity.
 */
public interface TagSearchRepository extends ElasticsearchRepository<Tag, Long> {
}
