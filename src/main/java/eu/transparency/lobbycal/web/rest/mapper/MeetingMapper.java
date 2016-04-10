package eu.transparency.lobbycal.web.rest.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transparency.lobbycal.domain.Meeting;
import eu.transparency.lobbycal.domain.Partner;
import eu.transparency.lobbycal.domain.Tag;
import eu.transparency.lobbycal.domain.User;
import eu.transparency.lobbycal.web.rest.dto.MeetingDTO;
import eu.transparency.lobbycal.web.rest.dto.UserDTO;

/**
 * Mapper for the entity Meeting and its DTO MeetingDTO.
 */
@Mapper(componentModel = "spring", uses = { TagMapper.class, PartnerMapper.class, })
public interface MeetingMapper {

	final Logger log = LoggerFactory.getLogger(MeetingMapper.class);

	@Mapping(source = "user.id", target = "userId")
	@Mapping(source = "user.login", target = "userLogin", ignore = true)
	@Mapping(source = "user.firstName", target = "userFirstName")
	@Mapping(source = "user.lastName", target = "userLastName")
	@Mapping(source = "submitter", target = "submitter", ignore = true)
	@Mapping(source = "aliasUsed", target = "aliasUsed", ignore = true)
	@Mapping(source = "uid", target = "uid")
	@Mapping(source = "mTag", target = "mTag")
	@Mapping(source = "tags", target = "tags")
	@Mapping(source = "mPartner", target = "mPartner")
	@Mapping(source = "partners", target = "partners")
	MeetingDTO meetingToMeetingDTO(Meeting meeting);

	@Mapping(target = "submitter", source = "submitter", ignore = true)
	@Mapping(target = "aliasUsed", source = "aliasUsed", ignore = true)
	@Mapping(target = "uid", source = "uid")
	@Mapping(target = "mTag", source = "mTag")
	@Mapping(target = "tags", source = "tags")
	@Mapping(target = "mPartner", source = "mPartner")
	@Mapping(target = "partners", source = "partners")
	@Mapping(source = "userId", target = "user")
	Meeting meetingDTOToMeeting(MeetingDTO meetingDTO);
    
	
	@Mapping(target = "authorities", ignore = true)
	UserDTO userToUserDTO(User user);

	@AfterMapping
	default void flattenTagAndPartner(Meeting src, @MappingTarget
	MeetingDTO target) {

		if (src.getmTag() != null && src.getmTag() != "") {
			log.trace("A value for tag has been set manually. AUDIT EVENT TBD");
		} else {
			log.trace("" + src.getId());
			if (src != null && src.getTags() != null && !src.getTags().isEmpty()) {
				String e = "";
				for (Tag t : src.getTags()) {
					e += " " + t.geti18nKey();
					log.trace("" + e);
				}
				target.setmTag(e);
			} else {
				target.setmTag("");
			}
		}
		if (src.getmPartner() != null && src.getmPartner() != "") {
			log.trace("A value for partner has been set manually . AUDIT EVENT TBD");
		} else {
			log.trace("");
			if (src.getmPartner() != null && !src.getPartners().isEmpty()) {
				String e = "";
				for (Partner p : src.getPartners()) {
					e += " " + p.getName();
					log.trace("" + e);
				}
				target.setmPartner(e);
			} else {
				target.setmPartner("");
			}
		}

	}

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
		log.info(user.getId()+"");
		user.setId(id);
		return user;
	}
}
