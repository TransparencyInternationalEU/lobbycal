package eu.transparency.lobbycal.repository.datatables;

import java.io.Serializable;

import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

@NoRepositoryBean
public interface CalendarDTRepository<T, ID extends Serializable> extends
		PagingAndSortingRepository<T, ID>, JpaSpecificationExecutor<T> {

	DataTablesOutput<T> findAll(DataTablesInput input,
			Specification<T> additionalSpecification);

	DataTablesOutput<T> findAll(DataTablesInput input);

}
