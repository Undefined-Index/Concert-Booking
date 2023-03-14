package proj.concert.service.mapper;

import proj.concert.common.dto.PerformerDTO;
import proj.concert.service.domain.Performer;

public class PerformerMapper {
    public static PerformerDTO toDto(Performer performer){
        PerformerDTO performerDTO = new PerformerDTO(
            performer.getId(),
            performer.getName(), 
            performer.getImageName(),
            performer.getGenre(),
            performer.getBlurb());
        return performerDTO;
    }
}
