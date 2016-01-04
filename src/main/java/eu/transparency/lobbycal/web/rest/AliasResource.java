package eu.transparency.lobbycal.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.inject.Inject;
import javax.validation.Valid;

import jodd.util.RandomString;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;

import eu.transparency.lobbycal.domain.Alias;
import eu.transparency.lobbycal.repository.AliasRepository;
import eu.transparency.lobbycal.repository.search.AliasSearchRepository;
import eu.transparency.lobbycal.security.AuthoritiesConstants;
import eu.transparency.lobbycal.security.SecurityUtils;
import eu.transparency.lobbycal.web.rest.util.PaginationUtil;

import static org.elasticsearch.index.query.QueryBuilders.*;
/**
 * REST controller for managing Alias.
 */
@RestController
@RequestMapping("/api")
public class AliasResource {

    private final Logger log = LoggerFactory.getLogger(AliasResource.class);

    @Inject
    private AliasRepository aliasRepository;

    @Inject
    private AliasSearchRepository aliasSearchRepository;

    /**
     * POST  /aliass -> Create a new alias.
     */
    @RequestMapping(value = "/aliass",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> create(@Valid @RequestBody Alias alias) throws URISyntaxException {
        log.debug("REST request to save Alias : {}", alias);
        if (alias.getId() != null) {
            return ResponseEntity.badRequest().header("Failure", "A new alias cannot already have an ID").build();
        }
        aliasRepository.save(alias);
        aliasSearchRepository.save(alias);
        return ResponseEntity.created(new URI("/api/aliass/" + alias.getId())).build();
    }

    /**
     * PUT  /aliass -> Updates an existing alias.
     */
    @RequestMapping(value = "/aliass",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> update(@Valid @RequestBody Alias alias) throws URISyntaxException {
        log.debug("REST request to update Alias : {}", alias);
        if (alias.getId() == null) {
            return create(alias);
        }
        aliasRepository.save(alias);
        aliasSearchRepository.save(alias);
        return ResponseEntity.ok().build();
    }

    /**
     * GET  /aliass -> get all the aliass.
     */
    @RequestMapping(value = "/aliass",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Alias>> getAll(@RequestParam(value = "page" , required = false) Integer offset,
                                  @RequestParam(value = "per_page", required = false) Integer limit)
        throws URISyntaxException {
    	Page<Alias> page;
		if (SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.ADMIN)) {
    		page = aliasRepository.findAll(PaginationUtil.generatePageRequest(offset, limit));
    	}else{
    		page = aliasRepository.findAllForCurrentUser(PaginationUtil.generatePageRequest(offset, limit));
    	}
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/aliass", offset, limit);
        headers.add("X-Random-Alias",RandomString.getInstance().randomAscii(20));
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /aliass/:id -> get the "id" alias.
     */
    @RequestMapping(value = "/aliass/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Alias> get(@PathVariable Long id) {
        log.debug("REST request to get Alias : {}", id);
        return Optional.ofNullable(aliasRepository.findOne(id))
            .map(alias -> new ResponseEntity<>(
                alias,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /aliass/:id -> delete the "id" alias.
     */
    @RequestMapping(value = "/aliass/{id}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public void delete(@PathVariable Long id) {
        log.debug("REST request to delete Alias : {}", id);
        aliasRepository.delete(id);
        aliasSearchRepository.delete(id);
    }

    /**
     * SEARCH  /_search/aliass/:query -> search for the alias corresponding
     * to the query.
     */
    @RequestMapping(value = "/_search/aliass/{query}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<Alias> search(@PathVariable String query) {
        return StreamSupport
            .stream(aliasSearchRepository.search(queryStringQuery(query)).spliterator(), false)
            .collect(Collectors.toList());
    }
}
