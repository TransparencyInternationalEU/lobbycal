package eu.transparency.lobbycal.repository;

import java.util.List;

import javax.annotation.security.RolesAllowed;

import org.elasticsearch.index.query.QueryBuilder;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import eu.transparency.lobbycal.domain.Submitter;
import eu.transparency.lobbycal.security.AuthoritiesConstants;

/**
 * Spring Data JPA repository for the Submitter entity.
 */
public interface SubmitterRepository extends JpaRepository<Submitter, Long> {

	@Query("select submitter from Submitter submitter where submitter.user.login = ?#{principal.username}")
	List<Submitter> findAllForCurrentUser();

	
	@Query("select submitter from Submitter submitter where submitter.user.login = ?#{principal.username}")
	Page<Submitter> findAllForCurrentUser(Pageable pageable);

	Submitter findOneByEmail(String email);

	@RolesAllowed(AuthoritiesConstants.ADMIN)
	List<Submitter> findAllByEmail(String email);

	@RolesAllowed(AuthoritiesConstants.ADMIN)
	List<Submitter> findAllByUser(String User);

	@RolesAllowed(AuthoritiesConstants.ADMIN)
	List<Submitter> findAllByUserId(Long id);

}
