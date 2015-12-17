package eu.transparency.lobbycal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import eu.transparency.lobbycal.domain.Tag;

/**
 * Spring Data JPA repository for the Tag entity.
 */
public interface TagRepository extends JpaRepository<Tag,Long> {

	

    List<Tag> findAll();
// TODO Add named query to fetch only those tags that have been assigned by a user    
//    @Query("select meeting.tags from Meeting meeting where meeting.user.login = ?#{principal.username}")
//	Page<Tag> findAllForCurrentUser(Pageable generatePageRequest);
}
