package eu.transparency.lobbycal.repository.datatables;

import java.io.Serializable;

import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
public interface CalendarDTRepository<T, ID extends Serializable>
		extends PagingAndSortingRepository<T, ID>, JpaSpecificationExecutor<T> {

	@Transactional(readOnly = true)
	DataTablesOutput<T> findAll(DataTablesInput input, Specification<T> additionalSpecification);

	@Transactional(readOnly = true)
	DataTablesOutput<T> findAll(DataTablesInput input);

	DataTablesOutput<T> findAllForOne(DataTablesInput input, Specification<T> additionalSpecification, boolean future);

}
