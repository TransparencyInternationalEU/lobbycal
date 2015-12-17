package eu.transparency.lobbycal.repository.datatables;
//package eu.transparency.lobbycal.repository.datatables;
//
//import java.io.Serializable;
//import java.util.Collection;
//import java.util.List;
//
//import javax.persistence.EntityManager;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
//import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
//import org.springframework.data.jpa.datatables.repository.DataTablesRepositoryImpl;
//import org.springframework.data.jpa.datatables.repository.DataTablesSpecification;
//import org.springframework.data.jpa.domain.Specification;
//import org.springframework.data.jpa.domain.Specifications;
//import org.springframework.data.jpa.repository.support.JpaEntityInformation;
//import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
//
//import eu.transparency.lobbycal.domain.Meeting;
//
//public abstract class MeetingDTRepositoryImpl<Meeting, ID extends Serializable> extends DataTablesRepositoryImpl<Meeting, Serializable>
//		implements MeetingDTRepository {
//	public MeetingDTRepositoryImpl(
//			JpaEntityInformation<Meeting, ?> entityInformation,
//			EntityManager entityManager) {
//		super(entityInformation, entityManager);
//	}
//
//	private static final Logger log = LoggerFactory
//			.getLogger(MeetingDTRepositoryImpl.class);
//
//	@Override
//	public DataTablesOutput<T> findAll(DataTablesInput input,
//			Specification<T> additionalSpecification) {
//		DataTablesOutput<T> output = new DataTablesOutput<T>();
//		output.setDraw(input.getDraw());
//
//		try {
//			output.setRecordsTotal(count());
//
//			Page<T> data = findAll(
//					Specifications.where(new DataTablesSpecification<T>(input))
//							.and(additionalSpecification), getPageable(input));
//
//			output.setData(data.getContent());
//			output.setRecordsFiltered(data.getTotalElements());
//
//		} catch (Exception e) {
//			output.setError(e.toString());
//			output.setRecordsFiltered(0L);
//		}
//
//		return output;
//	}
//	
//	@Override
//	public DataTablesOutput<T> findAllByUserIdIn(
//			DataTablesInput input,
//			Specification<Meeting> additionalSpecification,
//			Collection<Long> ids) {
//		DataTablesOutput<Meeting> output = new DataTablesOutput<Meeting>();
//		output.setDraw(input.getDraw());
//
//		try {
//			output.setRecordsTotal(count());
//
//			Page<Meeting> data = findAll(
//					Specifications.where(new DataTablesSpecification<Meeting>(input))
//							.and(additionalSpecification), getPageable(input));
//
//			output.setData(data.getContent());
//			output.setRecordsFiltered(data.getTotalElements());
//
//		} catch (Exception e) {
//			output.setError(e.toString());
//			output.setRecordsFiltered(0L);
//		}
//
//		return output;
//	}
//
//}
