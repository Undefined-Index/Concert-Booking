package proj.concert.service.mapper;
import java.util.ArrayList;
import java.util.List;
import proj.concert.common.dto.BookingDTO;
import proj.concert.service.domain.Booking;
import proj.concert.common.dto.SeatDTO;
import proj.concert.service.domain.Seat;

public class BookingMapper {
	public static BookingDTO toDto(Booking b) {
		List<SeatDTO> seatDTO = new ArrayList<>();
		List<Seat> seats = b.getSeats();
		for (Seat seat: seats) {
			seatDTO.add(SeatMapper.toDto(seat));
		}
		BookingDTO bookingDTO = new BookingDTO(b.getConcertId(), b.getDate(), seatDTO);
		return bookingDTO;
	}
}
