package eu.transparency.lobbycal.web.rest;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.inject.Inject;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.data.jpa.datatables.parameter.SearchParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonView;

import eu.transparency.lobbycal.config.Constants;
import eu.transparency.lobbycal.domain.Meeting;
import eu.transparency.lobbycal.domain.User;
import eu.transparency.lobbycal.repository.MeetingRepository;
import eu.transparency.lobbycal.repository.UserRepository;
import eu.transparency.lobbycal.repository.datatables.MeetingDTRepository;
import eu.transparency.lobbycal.repository.search.MeetingSearchRepository;
import eu.transparency.lobbycal.repository.search.UserSearchRepository;
import eu.transparency.lobbycal.security.AuthoritiesConstants;
import eu.transparency.lobbycal.security.SecurityUtils;
import eu.transparency.lobbycal.web.rest.dto.MeetingDTO;
import eu.transparency.lobbycal.web.rest.mapper.MeetingMapper;
import eu.transparency.lobbycal.web.rest.util.PaginationUtil;

/**
 * REST controller for managing Meeting.
 */
@RestController
@RequestMapping("/api")
public class MeetingResource {

	private final Logger log = LoggerFactory.getLogger(MeetingResource.class);

	@Inject
	private Environment env;

	@Inject
	private MeetingRepository meetingRepository;

	@Inject
	private MeetingDTRepository meetingDTRepository;

	@Inject
	private MeetingMapper meetingMapper;

	@Inject
	private MeetingSearchRepository meetingSearchRepository;

	@Inject
	private UserRepository userRepository;

	/**
	 * POST /meetings -> Create a new meeting.
	 */
	@RequestMapping(value = "/meetings", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<Void> create(@RequestBody
	MeetingDTO meetingDTO) throws URISyntaxException {
		Optional<User> u2 = userRepository.findOneByLogin(SecurityUtils.getCurrentUser().getUsername());
		log.debug("REST request to save Meeting : {}", meetingDTO);
		log.info(""+u2.get().getLogin());
		if (meetingDTO.getId() != null) {
			return ResponseEntity.badRequest().header("Failure", "A new meeting cannot already have an ID").build();
		}
		Meeting meeting = meetingMapper.meetingDTOToMeeting(meetingDTO);
		
		log.info("set user from "  + u2.get().getLogin());
		meeting.setUser(u2.get());
		meetingRepository.save(meeting);
		meetingSearchRepository.save(meeting);
		return ResponseEntity.created(new URI("/api/meetings/" + meetingDTO.getId())).build();
	}

	/**
	 * PUT /meetings -> Updates an existing meeting.
	 */
	@RequestMapping(value = "/meetings", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<Void> update(@RequestBody
	MeetingDTO meetingDTO) throws URISyntaxException {

		log.debug("REST request to update Meeting : {}", meetingDTO);
		if (meetingDTO.getId() == null) {
			log.debug("");
			return create(meetingDTO);
		}
		// maintain submitter and alias and uid 
		Meeting existingMeeting = meetingRepository.findOne(meetingDTO.getId());
		log.info(meetingDTO.getUserLastName());
		Meeting meeting = meetingMapper.meetingDTOToMeeting(meetingDTO);
		meeting.setAliasUsed(existingMeeting.getAliasUsed());
		meeting.setUid(existingMeeting.getUid());
		meeting.setSubmitter(existingMeeting.getSubmitter());
		User u2 = userRepository.findOne(meetingDTO.getUserId());
		meeting.setUser(u2);
		meetingRepository.save(meeting);
		meetingSearchRepository.save(meeting);
		return ResponseEntity.ok().build();
	}

	/**
	 * GET /meetings -> get all the meetings.
	 */
	@RequestMapping(value = "/meetings", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	@Transactional(readOnly = true)
	public ResponseEntity<List<MeetingDTO>> getAll(@RequestParam(value = "page", required = false)
	Integer offset, @RequestParam(value = "per_page", required = false)
	Integer limit) throws URISyntaxException {

		Page<Meeting> page;

		if (SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.USER)) {
			log.info("Self can always see past and future meetings, admin sees all");
			if (!SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.ADMIN)) {
				page = meetingRepository.findAllForCurrentUser(PaginationUtil.generatePageRequest(offset, limit));
			} else {
				page = meetingRepository.findAll(PaginationUtil.generatePageRequest(offset, limit));
			}

		} else {
			log.info("");
			switch (env.getProperty("lobbycal.meetings.delivery")) {
			case Constants.CALENDER_DELIVERY_PAST:
				log.info("");
				page = meetingRepository.findAllPast(PaginationUtil.generatePageRequest(offset, limit));
				log.debug(page.getSize() + "");
				break;
			case Constants.CALENDER_DELIVERY_FUTURE:
				log.info("");
				page = meetingRepository.findAllFuture(PaginationUtil.generatePageRequest(offset, limit));
				break;
			default:
				page = meetingRepository.findAll(PaginationUtil.generatePageRequest(offset, limit));
				break;
			}
		}
		HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/meetings", offset, limit);
		log.info("");
		if (!SecurityUtils.isAuthenticated()) {
			log.info("");
			String targ = env.getProperty("lobbycal.meetings.fullListURL", "/all");
			headers.add("Location", targ);
		}

		return new ResponseEntity<>(page.getContent().stream().map(meetingMapper::meetingToMeetingDTO)
				.collect(Collectors.toCollection(LinkedList::new)), headers, HttpStatus.OK);
	}

	/**
	 * GET /meetings -> get all the meetings.
	 */
	@RequestMapping(value = "/meetings/mep/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	@Transactional(readOnly = true)
	public ResponseEntity<List<MeetingDTO>> getAllForMEP(@PathVariable
	Long id, @RequestParam(value = "page", required = false)
	Integer offset, @RequestParam(value = "per_page", required = false)
	Integer limit) throws URISyntaxException {

		Page<Meeting> page;
		log.trace("");
		if (SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.USER)) {
			page = meetingRepository.getForMEP(PaginationUtil.generatePageRequest(offset, limit), id);

		} else {
			log.trace("" + env.getProperty("lobbycal.meetings.delivery"));
			switch (env.getProperty("lobbycal.meetings.delivery")) {
			case Constants.CALENDER_DELIVERY_PAST:
				log.trace("");
				page = meetingRepository.getPastForMEP(PaginationUtil.generatePageRequest(offset, limit), id);

				break;
			case Constants.CALENDER_DELIVERY_FUTURE:
				log.trace("");
				page = meetingRepository.getFutureForMEP(PaginationUtil.generatePageRequest(offset, limit), id);
				break;
			default:
				log.trace("");
				page = meetingRepository.getForMEP(PaginationUtil.generatePageRequest(offset, limit), id);
				break;
			}
		}
		HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/meetings", offset, limit);
		log.trace("");
		return new ResponseEntity<>(page.getContent().stream().map(meetingMapper::meetingToMeetingDTO)
				.collect(Collectors.toCollection(LinkedList::new)), headers, HttpStatus.OK);
	}

	/**
	 * GET /meetings -> get all past meetings for a list of mep, no disctinction
	 * of deliver config, as no group support in user mgnt - viewing future
	 * meetings of other parties meps might not be desired
	 */
	@RequestMapping(value = "/meetings/meps/{ids}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	@Transactional(readOnly = true)
	public ResponseEntity<List<MeetingDTO>> getAllForMEPs(@PathVariable
	String ids, @RequestParam(value = "page", required = false)
	Integer offset, @RequestParam(value = "per_page", required = false)
	Integer limit) throws URISyntaxException {

		Page<Meeting> page;
		log.trace("");
		Collection<Long> mepIds = new ArrayList<Long>();
		Iterator<String> sMepIds = Arrays.asList(ids.split(",")).iterator();
		while (sMepIds.hasNext()) {
			mepIds.add(Long.parseLong(sMepIds.next()));
		}
		page = meetingRepository.getPastForMEPs(PaginationUtil.generatePageRequest(offset, limit), mepIds);

		HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/meetings", offset, limit);
		log.trace("");
		return new ResponseEntity<>(page.getContent().stream().map(meetingMapper::meetingToMeetingDTO)
				.collect(Collectors.toCollection(LinkedList::new)), headers, HttpStatus.OK);
	}

	String mepIds = "";

	@RequestMapping(value = "/meetings/dt", method = RequestMethod.GET)
	@Timed
	@JsonView(DataTablesOutput.View.class)
	@Transactional(readOnly = true)
	public DataTablesOutput<MeetingDTO> getAllForDatatable(@Valid
	DataTablesInput input) throws URISyntaxException {

		userRepository.findAll().stream().forEach(user -> {
			mepIds += user.getId() + ",";
		});
		return getAllForDatatable(mepIds, input);
	}

	@RequestMapping(value = "/meetings/dt/{ids}", method = RequestMethod.GET)
	@Timed
	@JsonView(DataTablesOutput.View.class)
	@Transactional(readOnly = true)
	public DataTablesOutput<MeetingDTO> getAllForDatatable(@PathVariable
	String ids, @Valid
	DataTablesInput input) throws URISyntaxException {

		SearchParameter sp = input.getSearch();
		log.warn("terms: "+sp.toString() + ": " + sp.getValue());
		DataTablesOutput<Meeting> dto = null;

		Collection<Long> mepIds = new ArrayList<Long>();
		Iterator<String> sMepIds = Arrays.asList(ids.split(",")).iterator();
		while (sMepIds.hasNext()) {
			mepIds.add(Long.parseLong(sMepIds.next()));
		}

		String sf = sp.getValue().toLowerCase();

		dto = meetingDTRepository.findAll(input, MeetingSpecifications.hasTerm(sf, mepIds));
		if (dto.getData() != null) {
			log.info("hits: "+dto.getData().size() + "");
		}
		DataTablesOutput<MeetingDTO> dtor = new DataTablesOutput<MeetingDTO>();
		try {
			if (dto != null && dto.getData() != null && dto.getData().size() != 0) {
				dtor.setData(dto.getData().stream().map(meetingMapper::meetingToMeetingDTO)
						.collect(Collectors.toCollection(LinkedList::new)));
				dtor.setDraw(dto.getDraw());
				dtor.setError(dto.getError());
				dtor.setRecordsFiltered(dto.getRecordsFiltered());
				dtor.setRecordsTotal(dto.getRecordsTotal());
			} else if (dto.getData().size() == 0) {
				dtor.setData(dto.getData().stream().map(meetingMapper::meetingToMeetingDTO)
						.collect(Collectors.toCollection(LinkedList::new)));
				dtor.setDraw(dto.getDraw());
				dtor.setError(dto.getError());
				dtor.setRecordsFiltered(0l);
				dtor.setRecordsTotal(0l);
			}

		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
			dtor.setError(e.getLocalizedMessage());
			return dtor;
		}
		return dtor;
	}

	/**
	 * GET /meetings/:id -> get the "id" meeting.
	 */
	@RequestMapping(value = "/meetings/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<MeetingDTO> get(@PathVariable
	Long id) {

		log.debug("REST request to get Meeting : {}", id);
		return Optional.ofNullable(meetingRepository.findOneWithEagerRelationships(id))
				.map(meetingMapper::meetingToMeetingDTO)
				.map(meetingDTO -> new ResponseEntity<>(meetingDTO, HttpStatus.OK))
				.orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	/**
	 * DELETE /meetings/:id -> delete the "id" meeting.
	 */
	@RequestMapping(value = "/meetings/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public void delete(@PathVariable
	Long id) {

		log.debug("REST request to delete Meeting : {}", id);
		meetingRepository.delete(id);
		meetingSearchRepository.delete(id);
	}

	/**
	 * SEARCH /_search/meetings/:query -> search for the meeting corresponding
	 * to the query.
	 */

	@RequestMapping(value = "/_search/meetings/{query}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public List<MeetingDTO> search(@PathVariable
	String query) {

		log.info(query + "*");
		List<Sort.Order> orders = new ArrayList<>();
		orders.add(new Order(Sort.Direction.DESC, "startDate"));
		Sort sort = new Sort(orders);
		NativeSearchQuery q = new NativeSearchQuery(queryStringQuery(query + "*"));
		q.addSort(sort);
		Pageable pageable = new PageRequest(0, 10000);
		q.setPageable(pageable);
		return StreamSupport.stream(meetingSearchRepository.search(q).spliterator(), false)
				.map(meetingMapper::meetingToMeetingDTO).collect(Collectors.toList());
	}

	// @RequestMapping(value = "/_search/meetings/{query}", method =
	// RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	// @Timed
	// @Transactional(readOnly = true)
	// public ResponseEntity<List<MeetingDTO>> search(@PathVariable String
	// query,
	// @RequestParam(value = "page", required = false) Integer offset,
	// @RequestParam(value = "per_page", required = false) Integer limit) throws
	// URISyntaxException {
	// log.info(query + "*");
	//
	// Page<Meeting> page =
	// meetingSearchRepository.search(queryStringQuery(query),
	// PaginationUtil.generatePageRequest(offset, limit));
	// HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page,
	// "/_search/meetings/" + query);
	// List<Meeting> meetingList = page.getContent();
	// List<MeetingDTO> meetingDTOList =
	// StreamSupport.stream(meetingList.spliterator(), false)
	// .map(meetingMapper::meetingToMeetingDTO).collect(Collectors.toList());
	//// return new ResponseEntity<>(meetingDTOList, headers, HttpStatus.OK);
	//
	// return new
	// ResponseEntity<>(page.getContent().stream().map(meetingMapper::meetingToMeetingDTO)
	// .collect(Collectors.toCollection(LinkedList::new)), headers,
	// HttpStatus.OK);
	// }

	// @RequestMapping(value = "/_search/meetings/{query}",
	// method = RequestMethod.GET,
	// produces = MediaType.APPLICATION_JSON_VALUE)
	// @Timed
	// public ResponseEntity<List<MeetingDTO>> searchEntities(@PathVariable
	// String query, Pageable pageable) throws URISyntaxException {
	// Page<Meeting> page =
	// meetingSearchRepository.search(queryStringQuery(query), pageable);
	// HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page,
	// "/_search/meetings/" + query);
	// List<Meeting> meetingList = page.getContent();
	// List<MeetingDTO> meetingDTOList =
	// StreamSupport.stream(meetingList.spliterator(),
	// false).map(meetingMapper::meetingToMeetingDTO).collect(Collectors.toList());
	// return new ResponseEntity<>(meetingDTOList, headers, HttpStatus.OK);
	// }
}
