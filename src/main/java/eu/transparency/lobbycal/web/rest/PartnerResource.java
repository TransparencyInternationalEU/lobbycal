package eu.transparency.lobbycal.web.rest;

import com.codahale.metrics.annotation.Timed;

import eu.transparency.lobbycal.domain.Partner;
import eu.transparency.lobbycal.repository.PartnerRepository;
import eu.transparency.lobbycal.repository.search.PartnerSearchRepository;
import eu.transparency.lobbycal.web.rest.dto.PartnerDTO;
import eu.transparency.lobbycal.web.rest.mapper.PartnerMapper;
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
 * REST controller for managing Partner.
 */
@RestController
@RequestMapping("/api")
public class PartnerResource {

    private final Logger log = LoggerFactory.getLogger(PartnerResource.class);

    @Inject
    private PartnerRepository partnerRepository;

    @Inject
    private PartnerMapper partnerMapper;

    @Inject
    private PartnerSearchRepository partnerSearchRepository;

    /**
     * POST  /partners -> Create a new partner.
     */
    @RequestMapping(value = "/partners",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> create(@RequestBody PartnerDTO partnerDTO) throws URISyntaxException {
        log.debug("REST request to save Partner : {}", partnerDTO);
        if (partnerDTO.getId() != null) {
            return ResponseEntity.badRequest().header("Failure", "A new partner cannot already have an ID").build();
        }
        Partner partner = partnerMapper.partnerDTOToPartner(partnerDTO);
        partnerRepository.save(partner);
        partnerSearchRepository.save(partner);
        return ResponseEntity.created(new URI("/api/partners/" + partnerDTO.getId())).build();
    }

    /**
     * PUT  /partners -> Updates an existing partner.
     */
    @RequestMapping(value = "/partners",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> update(@RequestBody PartnerDTO partnerDTO) throws URISyntaxException {
        log.debug("REST request to update Partner : {}", partnerDTO);
        if (partnerDTO.getId() == null) {
            return create(partnerDTO);
        }
        Partner partner = partnerMapper.partnerDTOToPartner(partnerDTO);
        partnerRepository.save(partner);
        partnerSearchRepository.save(partner);
        return ResponseEntity.ok().build();
    }

    /**
     * GET  /partners -> get all the partners.
     */
    @RequestMapping(value = "/partners",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Transactional(readOnly = true)
    public ResponseEntity<List<PartnerDTO>> getAll(@RequestParam(value = "page" , required = false) Integer offset,
                                  @RequestParam(value = "per_page", required = false) Integer limit)
        throws URISyntaxException {
        Page<Partner> page = partnerRepository.findAll(PaginationUtil.generatePageRequest(offset, limit));
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/partners", offset, limit);
        return new ResponseEntity<>(page.getContent().stream()
            .map(partnerMapper::partnerToPartnerDTO)
            .collect(Collectors.toCollection(LinkedList::new)), headers, HttpStatus.OK);
    }

    /**
     * GET  /partners/:id -> get the "id" partner.
     */
    @RequestMapping(value = "/partners/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<PartnerDTO> get(@PathVariable Long id) {
        log.debug("REST request to get Partner : {}", id);
        return Optional.ofNullable(partnerRepository.findOne(id))
            .map(partnerMapper::partnerToPartnerDTO)
            .map(partnerDTO -> new ResponseEntity<>(
                partnerDTO,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /partners/:id -> delete the "id" partner.
     */
    @RequestMapping(value = "/partners/{id}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void>  delete(@PathVariable Long id) {
        log.info("REST request to delete Partner : {}", id);
        try {
			partnerRepository.delete(id);
			partnerSearchRepository.delete(id);
		} catch (org.springframework.dao.DataIntegrityViolationException e) {
			log.error(e.getLocalizedMessage());
			return ResponseEntity.unprocessableEntity().build();
		}
        log.info("");
        return ResponseEntity.ok().build();
    }

    /**
     * SEARCH  /_search/partners/:query -> search for the partner corresponding
     * to the query.
     */
    @RequestMapping(value = "/_search/partners/{query}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<Partner> search(@PathVariable String query) {
        return StreamSupport
            .stream(partnerSearchRepository.search(queryString(query)).spliterator(), false)
            .collect(Collectors.toList());
    }
}
