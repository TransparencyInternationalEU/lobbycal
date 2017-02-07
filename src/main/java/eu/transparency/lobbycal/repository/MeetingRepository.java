package eu.transparency.lobbycal.repository;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.transparency.lobbycal.domain.Meeting;

/**
 * Spring Data JPA repository for the Meeting entity.
 */
public interface MeetingRepository extends JpaRepository<Meeting, Long> {

	@Query("select meeting from Meeting meeting where meeting.user.login = ?#{principal.username} order by meeting.startDate desc")
	Page<Meeting> findAllForCurrentUser(Pageable pageable);

	@Query("select meeting from Meeting meeting order by meeting.startDate desc")
	Page<Meeting> findAll(Pageable pageable);

	@Query("select meeting from Meeting meeting where meeting.startDate > current_date order by meeting.startDate desc")
	Page<Meeting> findAllFuture(Pageable pageable);

	@Query("select meeting from Meeting meeting where meeting.startDate < current_date order by meeting.startDate desc")
	Page<Meeting> findAllPast(Pageable pageable);

	@Query("select meeting from Meeting meeting left join fetch meeting.tags left join fetch meeting.partners where meeting.id =:id")
	Meeting findOneWithEagerRelationships(@Param("id") Long id);

	@Query("select meeting from Meeting meeting where meeting.user.id =:id and meeting.startDate > current_date order by meeting.startDate desc")
	Page<Meeting> getFutureForMEP(Pageable pageable, @Param("id") Long id);

	@Query("select meeting from Meeting meeting where meeting.user.id =:id and meeting.startDate < current_date order by meeting.startDate desc")
	Page<Meeting> getPastForMEP(Pageable pageable, @Param("id") Long id);

	@Query("select meeting from Meeting meeting where meeting.user.id IN (:ids) and meeting.startDate < current_date order by meeting.startDate desc")
	Page<Meeting> getPastForMEPs(Pageable pageable, @Param("ids") Collection<Long> ids);

	@Query("select meeting from Meeting meeting where meeting.user.id =:id order by meeting.startDate desc ")
	Page<Meeting> getForMEP(Pageable pageable, @Param("id") Long id);

	/**
	 * 
	 * @param id
	 * @param offset after 
	 * @return
	 */
	@Query("select meeting from Meeting meeting where meeting.user.id =:id AND meeting.createdDate > :offset order by meeting.startDate desc")
	List<Meeting> getMeetingsForMEPAfter(@Param("id") Long id, @Param("offset") ZonedDateTime offset);

	Optional<Meeting> findOneByUid(String uid);
	
	
	
	
	@Override
	void delete(Meeting uid);

}
