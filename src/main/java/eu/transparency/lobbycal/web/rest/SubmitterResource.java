package eu.transparency.lobbycal.web.rest;

import com.codahale.metrics.annotation.Timed;

import eu.transparency.lobbycal.domain.Submitter;
import eu.transparency.lobbycal.repository.SubmitterRepository;
import eu.transparency.lobbycal.repository.search.SubmitterSearchRepository;
import eu.transparency.lobbycal.security.AuthoritiesConstants;
import eu.transparency.lobbycal.security.SecurityUtils;
import eu.transparency.lobbycal.web.rest.util.PaginationUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing Submitter.
 */
@RestController
@RequestMapping("/api")
public class SubmitterResource {

    private final Logger log = LoggerFactory.getLogger(SubmitterResource.class);

    @Inject
    private SubmitterRepository submitterRepository;

    @Inject
    private SubmitterSearchRepository submitterSearchRepository;

    /**
     * POST  /submitters -> Create a new submitter.
     */
    @RequestMapping(value = "/submitters",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> create(@Valid @RequestBody Submitter submitter) throws URISyntaxException {
        log.debug("REST request to save Submitter : {}", submitter);
        if (submitter.getId() != null) {
            return ResponseEntity.badRequest().header("Failure", "A new submitter cannot already have an ID").build();
        }
        submitterRepository.save(submitter);
        submitterSearchRepository.save(submitter);
        return ResponseEntity.created(new URI("/api/submitters/" + submitter.getId())).build();
    }

    /**
     * PUT  /submitters -> Updates an existing submitter.
     */
    @RequestMapping(value = "/submitters",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> update(@Valid @RequestBody Submitter submitter) throws URISyntaxException {
        log.debug("REST request to update Submitter : {}", submitter);
        if (submitter.getId() == null) {
            return create(submitter);
        }
        submitterRepository.save(submitter);
        submitterSearchRepository.save(submitter);
        return ResponseEntity.ok().build();
    }

    /**
     * GET  /submitters -> get all the submitters.
     */
    @RequestMapping(value = "/submitters",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Submitter>> getAll(@RequestParam(value = "page" , required = false) Integer offset,
                                  @RequestParam(value = "per_page", required = false) Integer limit)
        throws URISyntaxException {
        Page<Submitter> page ;
        if(SecurityUtils.isUserInRole(AuthoritiesConstants.ADMIN)){
        	page = submitterRepository.findAll(PaginationUtil.generatePageRequest(offset, limit));
        }else{
        	page = submitterRepository.findAllForCurrentUser(PaginationUtil.generatePageRequest(offset, limit));
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/submitters", offset, limit);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /submitters/:id -> get the "id" submitter.
     */
    @RequestMapping(value = "/submitters/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Submitter> get(@PathVariable Long id) {
        log.debug("REST request to get Submitter : {}", id);
        return Optional.ofNullable(submitterRepository.findOne(id))
            .map(submitter -> new ResponseEntity<>(
                submitter,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /submitters/:id -> delete the "id" submitter.
     */
    @RequestMapping(value = "/submitters/{id}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public void delete(@PathVariable Long id) {
        log.debug("REST request to delete Submitter : {}", id);
        submitterRepository.delete(id);
        submitterSearchRepository.delete(id);
    }

    /**
     * SEARCH  /_search/submitters/:query -> search for the submitter corresponding
     * to the query.
     */
    @RequestMapping(value = "/_search/submitters/{query}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<Submitter> search(@PathVariable String query) {
        return StreamSupport
            .stream(submitterSearchRepository.search(queryString(query)).spliterator(), false)
            .collect(Collectors.toList());
    }
}
