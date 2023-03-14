package proj.concert.service.mapper;

import proj.concert.common.dto.ConcertInfoSubscriptionDTO;
import proj.concert.service.domain.ConcertInfoSubscription;

public class ConcertInfoSubscriptionMapper {
	public static ConcertInfoSubscription toDomainModel(ConcertInfoSubscriptionDTO dto) {
		ConcertInfoSubscription concertInfoSubsription = new ConcertInfoSubscription(dto.getConcertId(), dto.getDate(), dto.getPercentageBooked());
		return concertInfoSubsription;
	}
}
