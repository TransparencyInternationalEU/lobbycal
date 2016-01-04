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

/**
 * Mapper for the entity Meeting and its DTO MeetingDTO.
 */
@Mapper(componentModel = "spring", uses = { TagMapper.class,
		PartnerMapper.class, })
public interface MeetingMapper {
	final Logger log = LoggerFactory.getLogger(MeetingMapper.class);

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

	@AfterMapping
	default void flattenTagAndPartner(Meeting src,
			@MappingTarget MeetingDTO target) {
		if (src.getmTag() != null && src.getmTag() != "") {
			log.info("A value for tag has been set manually. AUDIT EVENT TBD");
		} else {
			log.trace("");
			String e = "";
			for (Tag t : src.getTags()) {
				e += " " + t.geti18nKey();
			}
			target.setmTag(e);
		}
		if (src.getmPartner() != null && src.getmPartner() != "") {
			log.info("A value for partner has been set manually . AUDIT EVENT TBD");
		} else {
			log.trace("");
			String e = "";
			for (Partner p : src.getPartners()) {
				e += " " + p.getName();
			}
			target.setmPartner(e);
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
		user.setId(id);
		return user;
	}
}
