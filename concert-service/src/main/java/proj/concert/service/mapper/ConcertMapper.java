package proj.concert.service.mapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import proj.concert.common.dto.ConcertDTO;
import proj.concert.common.dto.PerformerDTO;
import proj.concert.service.domain.Concert;
import proj.concert.service.domain.Performer;

public class ConcertMapper {
	public static ConcertDTO toDto(Concert concert){
    	List<Performer> performers = concert.getPerformers();
    	List<PerformerDTO> performersDTO = new ArrayList<>();
        Set<LocalDateTime> dates = concert.getDates();
        List<LocalDateTime> listDates = new ArrayList<>();
        for (Performer p : performers){
            performersDTO.add(PerformerMapper.toDto(p));
        }

        ConcertDTO concertDTO = new ConcertDTO(
        		concert.getId(),
        		concert.getTitle(),
        		concert.getImageName(),
        		concert.getBlurb()
        );

        for (LocalDateTime date: dates) {
        	listDates.add(date);
        }
        concertDTO.setDates(listDates);
        concertDTO.setPerformers(performersDTO);
		return concertDTO;
	}
}
