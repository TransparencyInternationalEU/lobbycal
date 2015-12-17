package eu.transparency.lobbycal.repository.datatables;

import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.data.jpa.domain.Specification;

import eu.transparency.lobbycal.domain.Meeting;

public interface MeetingDTRepository extends
		DataTablesRepository<Meeting, Long> {

	DataTablesOutput<Meeting> findAll(DataTablesInput input,
			Specification<Meeting> additionalSpecification);

	DataTablesOutput<Meeting> findAll(DataTablesInput input);

}
