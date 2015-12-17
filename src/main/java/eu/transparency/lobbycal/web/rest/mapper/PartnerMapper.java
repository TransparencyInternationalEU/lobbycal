package eu.transparency.lobbycal.web.rest.mapper;

import eu.transparency.lobbycal.domain.*;
import eu.transparency.lobbycal.web.rest.dto.PartnerDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity Partner and its DTO PartnerDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface PartnerMapper {

    PartnerDTO partnerToPartnerDTO(Partner partner);

    @Mapping(target = "meetings", ignore = true)
    Partner partnerDTOToPartner(PartnerDTO partnerDTO);
}
