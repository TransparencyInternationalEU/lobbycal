package eu.transparency.lobbycal.web.rest;

import com.codahale.metrics.annotation.Timed;

import eu.transparency.lobbycal.domain.Tag;
import eu.transparency.lobbycal.repository.TagRepository;
import eu.transparency.lobbycal.repository.search.TagSearchRepository;
import eu.transparency.lobbycal.security.AuthoritiesConstants;
import eu.transparency.lobbycal.security.SecurityUtils;
import eu.transparency.lobbycal.web.rest.dto.TagDTO;
import eu.transparency.lobbycal.web.rest.mapper.TagMapper;
import eu.transparency.lobbycal.web.rest.util.PaginationUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing Tag.
 */
@RestController
@RequestMapping("/api")
public class TagResource {

	private final Logger log = LoggerFactory.getLogger(TagResource.class);

	@Inject
	private TagRepository tagRepository;

	@Inject
	private TagMapper tagMapper;

	@Inject
	private TagSearchRepository tagSearchRepository;

	/**
	 * POST /tags -> Create a new tag.
	 */
	@RequestMapping(value = "/tags", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<Void> create(@RequestBody TagDTO tagDTO)
			throws URISyntaxException {
		log.trace("REST request to save Tag : {}", tagDTO);
		if (tagDTO.getId() != null) {
			return ResponseEntity.badRequest()
					.header("Failure", "A new tag cannot already have an ID")
					.build();
		}
		Tag tag = tagMapper.tagDTOToTag(tagDTO);
		tagRepository.save(tag);
		tagSearchRepository.save(tag);
		return ResponseEntity.created(new URI("/api/tags/" + tagDTO.getId()))
				.build();
	}

	/**
	 * PUT /tags -> Updates an existing tag.
	 */
	@RequestMapping(value = "/tags", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<Void> update(@RequestBody TagDTO tagDTO)
			throws URISyntaxException {
		log.trace("REST request to update Tag : {}", tagDTO);
		if (tagDTO.getId() == null) {
			return create(tagDTO);
		}
		Tag tag = tagMapper.tagDTOToTag(tagDTO);
		tagRepository.save(tag);
		tagSearchRepository.save(tag);
		return ResponseEntity.ok().build();
	}

	/**
	 * GET /tags -> get all the tags.
	 */
	@RequestMapping(value = "/tags", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	@Transactional(readOnly = true)
	public ResponseEntity<List<TagDTO>> getAll(
			@RequestParam(value = "page", required = false) Integer offset,
			@RequestParam(value = "per_page", required = false) Integer limit)
			throws URISyntaxException {
		Page<Tag> page = null;
//		if (SecurityUtils.isUserInRole(AuthoritiesConstants.ANONYMOUS)
//				|| SecurityUtils.isUserInRole(AuthoritiesConstants.ADMIN)) {
			page = tagRepository.findAll(PaginationUtil.generatePageRequest(offset,
					limit));
//		}else{
//			page = tagRepository.findAllForCurrentUser(PaginationUtil.generatePageRequest(offset,
//					limit));
//		}
		HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
				page, "/api/tags", offset, limit);
		return new ResponseEntity<>(page.getContent().stream()
				.map(tagMapper::tagToTagDTO)
				.collect(Collectors.toCollection(LinkedList::new)), headers,
				HttpStatus.OK);
	}

	/**
	 * GET /tags/:id -> get the "id" tag.
	 */
	@RequestMapping(value = "/tags/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<TagDTO> get(@PathVariable Long id) {
		log.trace("REST request to get Tag : {}", id);
		return Optional.ofNullable(tagRepository.findOne(id))
				.map(tagMapper::tagToTagDTO)
				.map(tagDTO -> new ResponseEntity<>(tagDTO, HttpStatus.OK))
				.orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	/**
	 * DELETE /tags/:id -> delete the "id" tag.
	 */
	@RequestMapping(value = "/tags/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public void delete(@PathVariable Long id) {
		log.trace("REST request to delete Tag : {}", id);
		tagRepository.delete(id);
		tagSearchRepository.delete(id);
	}

	/**
	 * SEARCH /_search/tags/:query -> search for the tag corresponding to the
	 * query.
	 */
	@RequestMapping(value = "/_search/tags/{query}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public List<Tag> search(@PathVariable String query) {
		return StreamSupport.stream(
				tagSearchRepository.search(queryString(query)).spliterator(),
				false).collect(Collectors.toList());
	}
}
