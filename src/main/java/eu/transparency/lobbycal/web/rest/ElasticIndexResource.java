package eu.transparency.lobbycal.web.rest;


import java.net.URISyntaxException;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;

import eu.transparency.lobbycal.security.AuthoritiesConstants;
import eu.transparency.lobbycal.security.SecurityUtils;
import eu.transparency.lobbycal.service.ElasticIndexService;

/**
 * REST controller for managing the Elasticsearch index.
 */
@RestController
@RequestMapping("/api")
public class ElasticIndexResource {

    private final Logger log = LoggerFactory.getLogger(ElasticIndexResource.class);

    @Inject
    private ElasticIndexService elasticsearchIndexService;

    /**
     * POST  /elasticsearch/index -> Reindex all Elasticsearch documents
     */
    @RequestMapping(value = "/elasticsearch/index",
        produces = MediaType.TEXT_PLAIN_VALUE)
    @Timed
    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.ADMIN + "')")
    public ResponseEntity<String> reindexAll() throws URISyntaxException {
        log.info("REST request to reindex Elasticsearch by user : {}", SecurityUtils.getCurrentUserLogin());
        elasticsearchIndexService.reindexAll();
        return new ResponseEntity<>("Request accepted, performing full Elasticsearch reindexing.", HttpStatus.ACCEPTED);
    }
}
