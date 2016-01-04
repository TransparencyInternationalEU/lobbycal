package eu.transparency.lobbycal.repository.datatables;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.data.jpa.datatables.parameter.ColumnParameter;
import org.springframework.data.jpa.datatables.parameter.OrderParameter;
import org.springframework.data.jpa.datatables.repository.DataTablesSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import eu.transparency.lobbycal.web.rest.MeetingSpecifications;

public class CalendarDTRepositoryImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID>
		implements CalendarDTRepository<T, ID> {

	private static final Logger log = LoggerFactory.getLogger(CalendarDTRepositoryImpl.class);

	public CalendarDTRepositoryImpl(Class<T> domainClass, EntityManager em) {
		super(domainClass, em);
		log.info("");
	}

	public CalendarDTRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
		super(entityInformation, entityManager);
		log.info("");
	}

	@Override
	public DataTablesOutput<T> findAll(DataTablesInput input) {
		log.info("");
		return findAll(input, null);
	}

	@Override
	public DataTablesOutput<T> findAll(DataTablesInput input, Specification<T> additionalSpecification) {
		DataTablesOutput<T> output = new DataTablesOutput<T>();
		output.setDraw(input.getDraw());

		log.info("" + additionalSpecification.toString());
		try {
			output.setRecordsTotal(count());

			log.info("");
			Page<T> data = findAll(Specifications.where(new DataTablesSpecification<T>(input))
					.and(additionalSpecification).and((Specification<T>) MeetingSpecifications.past())
					.or((Specification<T>) MeetingSpecifications.hasPartner(input.getSearch().getValue().toLowerCase(),
							null))
					.and(additionalSpecification).and((Specification<T>) MeetingSpecifications.past())
					.or((Specification<T>) MeetingSpecifications.hasTag(input.getSearch().getValue().toLowerCase(),
							null))
					.and(additionalSpecification)
					.and((Specification<T>) MeetingSpecifications.past()
							), getPageable(input));

			log.info("");
			output.setData(data.getContent());
			log.info("");
			output.setRecordsFiltered(data.getTotalElements());
			log.info("");

		} catch (Exception e) {
			output.setError(e.toString());
			output.setRecordsFiltered(0L);
		}

		log.info("");
		return output;
	}

	/**
	 * Creates a 'LIMIT .. OFFSET .. ORDER BY ..' clause for the given
	 * {@link DataTablesInput}.
	 * 
	 * @param input
	 *            the {@link DataTablesInput} mapped from the Ajax request
	 * @return a {@link Pageable}, must not be {@literal null}.
	 */
	private Pageable getPageable(DataTablesInput input) {
		List<Order> orders = new ArrayList<Order>();
		log.info("");
		for (OrderParameter order : input.getOrder()) {
			ColumnParameter column = input.getColumns().get(order.getColumn());
			if (column.getOrderable()) {
				String sortColumn = column.getData();
				Direction sortDirection = Direction.fromString(order.getDir());
				orders.add(new Order(sortDirection, sortColumn));
			}
			log.info("");
		}
		Sort sort = orders.isEmpty() ? null : new Sort(orders);

		log.info("");
		if (input.getLength() == -1) {
			input.setStart(0);
			input.setLength(Integer.MAX_VALUE);
		}
		log.info("");
		return new PageRequest(input.getStart() / input.getLength(), input.getLength(), sort);
	}
}
