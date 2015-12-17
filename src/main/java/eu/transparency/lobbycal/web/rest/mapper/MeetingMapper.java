package eu.transparency.lobbycal.web.rest.mapper;

import eu.transparency.lobbycal.domain.*;
import eu.transparency.lobbycal.web.rest.dto.MeetingDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity Meeting and its DTO MeetingDTO.
 */
@Mapper(componentModel = "spring", uses = { TagMapper.class,
		PartnerMapper.class, })
public interface MeetingMapper {

	@Mapping(source = "user.id", target = "userId")
	@Mapping(source = "user.login", target = "userLogin", ignore = true)
	@Mapping(source = "user.firstName", target = "userFirstName")
	@Mapping(source = "user.lastName", target = "userLastName")
	@Mapping(source = "submitter", target = "submitter", ignore = true)
	@Mapping(source = "aliasUsed", target = "aliasUsed", ignore = true)
	@Mapping(source = "uid", target = "uid", ignore = true)
	@Mapping(source = "mTag", target = "mTag")
	@Mapping(source = "mPartner", target = "mPartner")
	MeetingDTO meetingToMeetingDTO(Meeting meeting);

	@Mapping(source = "userId", target = "user")
	Meeting meetingDTOToMeeting(MeetingDTO meetingDTO);

	default Tag tagFromId(Long id) {
		if (id == null) {
			return null;
		}
		Tag tag = new Tag();
		tag.setId(id);
		return tag;
	}

	default Partner partnerFromId(Long id) {
		if (id == null) {
			return null;
		}
		Partner partner = new Partner();
		partner.setId(id);
		return partner;
	}

	default User userFromId(Long id) {
		if (id == null) {
			return null;
		}
		User user = new User();
		user.setId(id);
		return user;
	}
}
