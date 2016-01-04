package eu.transparency.lobbycal.repository.datatables;

import java.io.Serializable;

import javax.persistence.EntityManager;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

public class CalendarDTRepositoryFactoryBean<R extends JpaRepository<T, ID>, T, ID extends Serializable>
		extends JpaRepositoryFactoryBean<R, T, ID> {

	protected RepositoryFactorySupport createRepositoryFactory(
			EntityManager entityManager) {
		return new DataTablesRepositoryFactory<T, ID>(entityManager);
	}

	private static class DataTablesRepositoryFactory<T, ID extends Serializable>
			extends JpaRepositoryFactory {

		private final EntityManager entityManager;

		public DataTablesRepositoryFactory(EntityManager entityManager) {

			super(entityManager);
			this.entityManager = entityManager;
		}

		@SuppressWarnings({ "unchecked" })
		protected Object getTargetRepository(RepositoryMetadata metadata) {
			JpaEntityInformation<T, Serializable> entityInformation = (JpaEntityInformation<T, Serializable>) getEntityInformation(metadata
					.getDomainType());
			return new CalendarDTRepositoryImpl<T, ID>(entityInformation,
					entityManager);
		}

		protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
			return CalendarDTRepositoryImpl.class;
		}
	}
}
