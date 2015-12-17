package eu.transparency.lobbycal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.transparency.lobbycal.domain.Partner;

/**
 * Spring Data JPA repository for the Partner entity.
 */
public interface PartnerRepository extends JpaRepository<Partner,Long> {

	public Optional<Partner> findOneByTransparencyRegisterID(String transparencyRegisterID); 
}
