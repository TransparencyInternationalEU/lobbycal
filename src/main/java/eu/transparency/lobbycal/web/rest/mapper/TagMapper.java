package eu.transparency.lobbycal.web.rest.mapper;

import eu.transparency.lobbycal.domain.*;
import eu.transparency.lobbycal.web.rest.dto.TagDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity Tag and its DTO TagDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface TagMapper {

    TagDTO tagToTagDTO(Tag tag);

    @Mapping(target = "meetings", ignore = true)
    Tag tagDTOToTag(TagDTO tagDTO);
}
