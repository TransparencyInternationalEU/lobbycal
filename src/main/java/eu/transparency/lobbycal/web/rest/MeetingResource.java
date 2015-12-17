package eu.transparency.lobbycal.web.rest;

import static org.elasticsearch.index.query.QueryBuilders.queryString;

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
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.data.jpa.datatables.parameter.SearchParameter;
import org.springframework.data.jpa.domain.Specification;
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

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonView;

import eu.transparency.lobbycal.config.Constants;
import eu.transparency.lobbycal.domain.Meeting;
import eu.transparency.lobbycal.repository.MeetingRepository;
import eu.transparency.lobbycal.repository.datatables.MeetingDTRepository;
import eu.transparency.lobbycal.repository.search.MeetingSearchRepository;
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

	/**
	 * POST /meetings -> Create a new meeting.
	 */
	@RequestMapping(value = "/meetings", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<Void> create(@RequestBody MeetingDTO meetingDTO)
			throws URISyntaxException {
		log.debug("REST request to save Meeting : {}", meetingDTO);
		if (meetingDTO.getId() != null) {
			return ResponseEntity
					.badRequest()
					.header("Failure",
							"A new meeting cannot already have an ID").build();
		}
		Meeting meeting = meetingMapper.meetingDTOToMeeting(meetingDTO);
		meetingRepository.save(meeting);
		meetingSearchRepository.save(meeting);
		return ResponseEntity.created(
				new URI("/api/meetings/" + meetingDTO.getId())).build();
	}

	/**
	 * PUT /meetings -> Updates an existing meeting.
	 */
	@RequestMapping(value = "/meetings", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<Void> update(@RequestBody MeetingDTO meetingDTO)
			throws URISyntaxException {
		log.debug("REST request to update Meeting : {}", meetingDTO);
		if (meetingDTO.getId() == null) {
			return create(meetingDTO);
		}
		Meeting meeting = meetingMapper.meetingDTOToMeeting(meetingDTO);
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
	public ResponseEntity<List<MeetingDTO>> getAll(
			@RequestParam(value = "page", required = false) Integer offset,
			@RequestParam(value = "per_page", required = false) Integer limit)
			throws URISyntaxException {
		Page<Meeting> page;

		if (SecurityUtils.isUserInRole(AuthoritiesConstants.USER)) {
			log.info("Self can always see past and future meetings, admin sees all");
			if (!SecurityUtils.isUserInRole(AuthoritiesConstants.ADMIN)) {
				page = meetingRepository.findAllForCurrentUser(PaginationUtil
						.generatePageRequest(offset, limit));
			} else {
				page = meetingRepository.findAll(PaginationUtil
						.generatePageRequest(offset, limit));
			}

		} else {
			log.info("");
			switch (env.getProperty("lobbycal.meetings.delivery")) {
			case Constants.CALENDER_DELIVERY_PAST:
				log.info("");
				page = meetingRepository.findAllPast(PaginationUtil
						.generatePageRequest(offset, limit));
				break;
			case Constants.CALENDER_DELIVERY_FUTURE:
				log.info("");
				page = meetingRepository.findAllFuture(PaginationUtil
						.generatePageRequest(offset, limit));
				break;
			default:
				page = meetingRepository.findAll(PaginationUtil
						.generatePageRequest(offset, limit));
				break;
			}
		}
		HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
				page, "/api/meetings", offset, limit);
		log.info("");
		return new ResponseEntity<>(page.getContent().stream()
				.map(meetingMapper::meetingToMeetingDTO)
				.collect(Collectors.toCollection(LinkedList::new)), headers,
				HttpStatus.OK);
	}

	/**
	 * GET /meetings -> get all the meetings.
	 */
	@RequestMapping(value = "/meetings/mep/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	@Transactional(readOnly = true)
	public ResponseEntity<List<MeetingDTO>> getAllForMEP(@PathVariable Long id,
			@RequestParam(value = "page", required = false) Integer offset,
			@RequestParam(value = "per_page", required = false) Integer limit)
			throws URISyntaxException {
		Page<Meeting> page;
		log.trace("");
		if (SecurityUtils.isUserInRole(AuthoritiesConstants.USER)) {
			page = meetingRepository.getForMEP(
					PaginationUtil.generatePageRequest(offset, limit), id);

		} else {
			log.trace("" + env.getProperty("lobbycal.meetings.delivery"));
			switch (env.getProperty("lobbycal.meetings.delivery")) {
			case Constants.CALENDER_DELIVERY_PAST:
				log.trace("");
				page = meetingRepository.getPastForMEP(
						PaginationUtil.generatePageRequest(offset, limit), id);

				break;
			case Constants.CALENDER_DELIVERY_FUTURE:
				log.trace("");
				page = meetingRepository.getFutureForMEP(
						PaginationUtil.generatePageRequest(offset, limit), id);
				break;
			default:
				log.trace("");
				page = meetingRepository.getForMEP(
						PaginationUtil.generatePageRequest(offset, limit), id);
				break;
			}
		}
		HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
				page, "/api/meetings", offset, limit);
		log.trace("");
		return new ResponseEntity<>(page.getContent().stream()
				.map(meetingMapper::meetingToMeetingDTO)
				.collect(Collectors.toCollection(LinkedList::new)), headers,
				HttpStatus.OK);
	}

	/**
	 * GET /meetings -> get all past meetings for a list of mep, no disctinction
	 * of deliver config, as no group support in user mgnt - viewing future
	 * meetings of other parties meps might not be desired
	 */
	@RequestMapping(value = "/meetings/meps/{ids}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	@Transactional(readOnly = true)
	public ResponseEntity<List<MeetingDTO>> getAllForMEPs(
			@PathVariable String ids,
			@RequestParam(value = "page", required = false) Integer offset,
			@RequestParam(value = "per_page", required = false) Integer limit)
			throws URISyntaxException {
		Page<Meeting> page;
		log.trace("");
		Collection<Long> mepIds = new ArrayList<Long>();
		Iterator<String> sMepIds = Arrays.asList(ids.split(",")).iterator();
		while (sMepIds.hasNext()) {
			mepIds.add(Long.parseLong(sMepIds.next()));
		}
		page = meetingRepository.getPastForMEPs(
				PaginationUtil.generatePageRequest(offset, limit), mepIds);

		HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
				page, "/api/meetings", offset, limit);
		log.trace("");
		return new ResponseEntity<>(page.getContent().stream()
				.map(meetingMapper::meetingToMeetingDTO)
				.collect(Collectors.toCollection(LinkedList::new)), headers,
				HttpStatus.OK);
	}

	@RequestMapping(value = "/meetings/dt/{ids}", method = RequestMethod.GET)
	@Timed
	@JsonView(DataTablesOutput.View.class)
	@Transactional(readOnly = true)
	public DataTablesOutput<MeetingDTO> tableEndPoint(@PathVariable String ids,
			@Valid DataTablesInput input) throws URISyntaxException {
		log.info(ids);
		SearchParameter sp = input.getSearch();
		log.warn(sp.toString() + ": " + sp.getValue());
		DataTablesOutput<Meeting> dto = null;
		Specification<Meeting> searchSpec = MeetingSpecifications.hasTitle(sp
				.getValue().toLowerCase(), ids);
		Collection<Long> mepIds = new ArrayList<Long>();
		Iterator<String> sMepIds = Arrays.asList(ids.split(",")).iterator();
		while (sMepIds.hasNext()) {
			mepIds.add(Long.parseLong(sMepIds.next()));
		}
		dto = meetingDTRepository.findAll(input, searchSpec);
//		log.info(dto.getData().size() + "");
		DataTablesOutput<MeetingDTO> dtor = new DataTablesOutput<MeetingDTO>();
		try {
			if (dto != null && dto.getData() != null
					&& dto.getData().size() != 0) {
				dtor.setData(dto.getData().stream()
						.map(meetingMapper::meetingToMeetingDTO)
						.collect(Collectors.toCollection(LinkedList::new)));
				dtor.setDraw(dto.getDraw());
				dtor.setError(dto.getError());
				dtor.setRecordsFiltered(dto.getRecordsFiltered());
				dtor.setRecordsTotal(dto.getRecordsTotal());
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
	public ResponseEntity<MeetingDTO> get(@PathVariable Long id) {
		log.debug("REST request to get Meeting : {}", id);
		return Optional
				.ofNullable(meetingRepository.findOneWithEagerRelationships(id))
				.map(meetingMapper::meetingToMeetingDTO)
				.map(meetingDTO -> new ResponseEntity<>(meetingDTO,
						HttpStatus.OK))
				.orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	/**
	 * DELETE /meetings/:id -> delete the "id" meeting.
	 */
	@RequestMapping(value = "/meetings/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public void delete(@PathVariable Long id) {
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
	public List<Meeting> search(@PathVariable String query) {
		log.info(query);
		return StreamSupport.stream(
				meetingSearchRepository.search(queryString(query))
						.spliterator(), false).collect(Collectors.toList());
	}
}
