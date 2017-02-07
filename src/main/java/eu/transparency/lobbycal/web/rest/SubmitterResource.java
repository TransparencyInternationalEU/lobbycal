package eu.transparency.lobbycal.web.rest;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.inject.Inject;
import javax.validation.Valid;

import org.apache.tomcat.util.net.jsse.openssl.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;

import eu.transparency.lobbycal.aop.audit.AuditEventPublisher;
import eu.transparency.lobbycal.domain.Submitter;
import eu.transparency.lobbycal.domain.User;
import eu.transparency.lobbycal.repository.SubmitterRepository;
import eu.transparency.lobbycal.repository.UserRepository;
import eu.transparency.lobbycal.repository.search.SubmitterSearchRepository;
import eu.transparency.lobbycal.security.AuthoritiesConstants;
import eu.transparency.lobbycal.security.SecurityUtils;
import eu.transparency.lobbycal.web.rest.util.PaginationUtil;

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
	private AuditEventPublisher auditPublisher;


	@Inject
	private UserRepository userRepository;


	@Inject
	private SubmitterSearchRepository submitterSearchRepository;

	/**
	 * POST /submitters -> Create a new submitter.
	 */
	@RequestMapping(value = "/submitters", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<Void> create(@Valid
	@RequestBody
	Submitter submitter) throws URISyntaxException {

		log.debug("REST request to save Submitter : {}", submitter);
		if (submitter.getId() != null) {
			return ResponseEntity.badRequest().header("Failure", "A new submitter cannot already have an ID").build();
		}
		if (SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.ADMIN)) {
			log.info("admin creates new submitter");
			Optional<User> selected = userRepository.findOneById(submitter.getUser().getId());
			if (selected.isPresent()) {
				log.info(selected.get().toString());
				submitter.setUser(selected.get());
			} else {
				return ResponseEntity.badRequest().header("Failure", "No corresponding user exists").build();

			}
		} else {
			log.info("user " + SecurityUtils.getCurrentUser().getUsername() + " with login "
					+ SecurityUtils.getCurrentUserLogin() + " class " + SecurityUtils.getCurrentUser().getClass()
					+ "  creates new submitter");
			Optional<User> self = userRepository.findOneByLogin(SecurityUtils.getCurrentUser().getUsername());
			submitter.setUser(self.get());
		}
		submitterRepository.save(submitter);
		submitterSearchRepository.save(submitter);
		return ResponseEntity.created(new URI("/api/submitters/" + submitter.getId())).build();
	}

	/**
	 * PUT /submitters -> Updates an existing submitter.
	 */
	@RequestMapping(value = "/submitters", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<Void> update(@Valid
	@RequestBody
	Submitter submitter) throws URISyntaxException {

		log.debug("REST request to update Submitter : {}", submitter);
		if (submitter.getId() == null) {
			if (SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.ADMIN)) {
				log.info("admin update new submitter");
			} else {
				log.info("user " + SecurityUtils.getCurrentUser().getUsername() + " with login "
						+ SecurityUtils.getCurrentUserLogin() + " class " + SecurityUtils.getCurrentUser().getClass()
						+ "  update new submitter");
				Optional<User> self = userRepository.findOneByLogin(SecurityUtils.getCurrentUser().getUsername());
				submitter.setUser(self.get());
			}
			return create(submitter);
		}
		if (SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.ADMIN)) {
			log.info("admin update new submitter");
		} else {
			log.info("user " + SecurityUtils.getCurrentUser().getUsername() + " with login "
					+ SecurityUtils.getCurrentUserLogin() + " class " + SecurityUtils.getCurrentUser().getClass()
					+ "  update new submitter");
			Optional<User> self = userRepository.findOneByLogin(SecurityUtils.getCurrentUser().getUsername());
			submitter.setUser(self.get());
		}
		submitterRepository.save(submitter);
		submitterSearchRepository.save(submitter);
		return ResponseEntity.ok().build();
	}

	/**
	 * GET /submitters -> get all the submitters.
	 */
	@RequestMapping(value = "/submitters", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<List<Submitter>> getAll(@RequestParam(value = "page", required = false)
	Integer offset, @RequestParam(value = "per_page", required = false)
	Integer limit) throws URISyntaxException {

		Page<Submitter> page;
		if (SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.ADMIN)) {
			page = submitterRepository.findAll(PaginationUtil.generatePageRequest(offset, limit));
		} else {
			page = submitterRepository.findAllForCurrentUser(PaginationUtil.generatePageRequest(offset, limit));
		}
		HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/submitters", offset, limit);
		return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
	}

	/**
	 * GET /submitters/:id -> get the "id" submitter.
	 */
	@RequestMapping(value = "/submitters/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<Submitter> get(@PathVariable
	Long id) {

		log.debug("REST request to get Submitter : {}", id);
		return Optional.ofNullable(submitterRepository.findOne(id))
				.map(submitter -> new ResponseEntity<>(submitter, HttpStatus.OK))
				.orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	/**
	 * DELETE /submitters/:id -> delete the "id" submitter.
	 */
	@RequestMapping(value = "/submitters/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public void delete(@PathVariable
	Long id) {

		log.debug("REST request to delete Submitter : {}", id);
		submitterRepository.delete(id);
		submitterSearchRepository.delete(id);
	}

	/**
	 * SEARCH /_search/submitters/:query -> search for the submitter
	 * corresponding to the query.
	 */
	@RequestMapping(value = "/_search/submitters/{query}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public List<Submitter> search(@PathVariable
	String query) {

		List<Submitter> subs = StreamSupport
				.stream(submitterSearchRepository.search(queryStringQuery(query)).spliterator(), false)
				.collect(Collectors.toList());
		List<Submitter> reurnable = new ArrayList<Submitter>();

		for (Submitter submitter : subs) {
			// next line needed for fetching user info
			log.info(submitter.getUser().toString() + "  -  " + SecurityUtils.getCurrentUser().getUsername());

			if (!SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.ADMIN)) {
				if (submitter.getUser() != null) {

					if (submitter.getUser().getLogin() == SecurityUtils.getCurrentUser().getUsername()) {
						log.info("ho");
						reurnable.add(submitter);
					}
				}
			} else {
				return subs;
			}
		}
		return reurnable;
	}
}
