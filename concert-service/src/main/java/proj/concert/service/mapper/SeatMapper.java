package proj.concert.service.mapper;

import java.time.LocalDateTime;

import proj.concert.common.dto.SeatDTO;
import proj.concert.service.domain.Seat;

public class SeatMapper {
	public static SeatDTO toDto(Seat seat) {
		SeatDTO seatDTO = new SeatDTO(seat.getLabel(), seat.getPrice());
		return seatDTO;
	}
}
