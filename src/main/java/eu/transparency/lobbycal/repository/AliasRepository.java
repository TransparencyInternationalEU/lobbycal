package eu.transparency.lobbycal.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import eu.transparency.lobbycal.domain.Alias;

/**
 * Spring Data JPA repository for the Alias entity.
 */
public interface AliasRepository extends JpaRepository<Alias, Long> {

	@Query("select alias from Alias alias where alias.user.login = ?#{principal.username}")
	List<Alias> findAllForCurrentUser();

	@Query("select alias from Alias alias where alias.user.login = ?#{principal.username}")
	Page<Alias> findAllForCurrentUser(Pageable pageable);

	List<Alias> findAll();

	Alias findOneByAlias(String string);

}
