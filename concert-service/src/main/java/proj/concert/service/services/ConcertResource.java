package proj.concert.service.services;

import proj.concert.service.domain.*;

import proj.concert.service.jaxrs.LocalDateTimeParam;

import proj.concert.common.dto.*;
import proj.concert.common.types.BookingStatus;
import proj.concert.service.mapper.*;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;

import javax.persistence.*;

import org.slf4j.*;

@Path("/concert-service")
public class ConcertResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConcertResource.class);

    private static final List<AsyncResponse> Subscriptions = new ArrayList<>();

    @GET
    @Path("/concerts")
    @Produces(MediaType.APPLICATION_JSON)
    public Response retrieveAllConcerts() {
        List<ConcertDTO> concertsDTO = new ArrayList<>();
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            em.getTransaction().begin();
            TypedQuery<Concert> concertQuery = em.createQuery("select c from Concert c", Concert.class);
            List<Concert> concerts = concertQuery.getResultList();
            if (concerts == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            for (Concert c : concerts) {
                concertsDTO.add(ConcertMapper.toDto(c));
            }
            em.getTransaction().commit();

            LOGGER.info("Retrieving all concerts.");
            return Response.ok(concertsDTO).build();

        } finally {
            em.close();
        }
    }

    @GET
    @Path("/concerts/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response retrieveConcert(@PathParam("id") long id) {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            em.getTransaction().begin();
            Concert concert = em.find(Concert.class, id);
            em.getTransaction().commit();
            if (concert == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            LOGGER.info("Retrieving concert with id: " + id + ".");
            return Response.ok(ConcertMapper.toDto(concert)).build();
        } finally {
            em.close();
        }
    }

    @GET
    @Path("performers/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response retrievePerformer(@PathParam("id") long id) {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            em.getTransaction().begin();
            Performer performer = em.find(Performer.class, id);
            em.getTransaction().commit();
            if (performer == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            LOGGER.info("Retrieving performer with id: " + id + ".");
            return Response.ok(PerformerMapper.toDto(performer)).build();
        } finally {
            em.close();
        }
    }

    @GET
    @Path("/performers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response retrieveAllPerformers() {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            em.getTransaction().begin();
            TypedQuery<Performer> performerQuery = em.createQuery("select p from Performer p", Performer.class);
            List<Performer> performers = performerQuery.getResultList();
            List<PerformerDTO> performersDTO = new ArrayList<>();
            em.getTransaction().commit();
            if (performers == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            for (Performer performer : performers) {
                performersDTO.add(PerformerMapper.toDto(performer));
            }
            LOGGER.info("Retrieving all performers.");
            return Response.ok(performersDTO).build();
        } finally {
            em.close();
        }
    }

    @GET
    @Path("/concerts/summaries")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getConcertSummaries() {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            em.getTransaction().begin();
            TypedQuery<Concert> concertQuery = em.createQuery("select c from Concert c", Concert.class);
            List<Concert> concerts = concertQuery.getResultList();
            List<ConcertSummaryDTO> concertSummariesDTO = new ArrayList<>();
            em.getTransaction().commit();
            for (Concert concert : concerts) {
                concertSummariesDTO.add(new ConcertSummaryDTO(concert.getId(), concert.getTitle(), concert.getImageName()));
            }
            LOGGER.info("Retrieving all summaries");
            return Response.ok(concertSummariesDTO).build();
        } finally {
            em.close();
        }
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(UserDTO userDTO) {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            em.getTransaction().begin();
            TypedQuery<User> userQuery = em.createQuery("select u from User u where u.username = :username AND u.password = :password", User.class).setParameter("username", userDTO.getUsername()).setParameter("password", userDTO.getPassword());
            User user = userQuery.getResultList().stream().findFirst().orElse(null);
            if (user == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            NewCookie newCookie = new NewCookie("auth", UUID.randomUUID().toString());
            user.setCookie(newCookie.getValue());
            em.getTransaction().commit();
            LOGGER.info(userDTO.getUsername() + "log in.");
            return Response.ok().cookie(newCookie).build();
        } finally {
            em.close();
        }
    }

    @GET
    @Path("/bookings")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBookings(@CookieParam("auth") Cookie cookie) {
        if (cookie == null){
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            User user = loggedJudge(cookie,em);
            if (user ==  null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            em.getTransaction().begin();
            TypedQuery<Booking> bookingQuery = em.createQuery("SELECT b FROM Booking b WHERE b.user = :user", Booking.class).setLockMode(LockModeType.PESSIMISTIC_READ);
            bookingQuery.setParameter("user", user);
            List<Booking> bookings = bookingQuery.getResultList();
            em.getTransaction().commit();

            List<BookingDTO> bookingsDTO = new ArrayList<>();
            for (Booking booking : bookings) {
                bookingsDTO.add(BookingMapper.toDto(booking));
            }
            LOGGER.info("Retrieving " + user.getUsername() + " bookings");
            return Response.ok(bookingsDTO).build();
        } finally {
            em.close();
        }
    }

    @GET
    @Path("/bookings/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBookingsById(@PathParam("id") long id, @CookieParam("auth") Cookie cookie) {
        if (cookie == null){
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        LOGGER.info("Retrieving bookings");
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            User user = loggedJudge(cookie,em);
            if (user == null){
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            em.getTransaction().begin();
            Booking bookings = em.find(Booking.class, id, LockModeType.PESSIMISTIC_READ);
            if (!bookings.getUser().equals(user)) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
            BookingDTO booking = BookingMapper.toDto(bookings);
            em.getTransaction().commit();
            LOGGER.info("Retrieving " + user.getUsername() + " bookings");
            return Response.ok(booking).build();
        } finally {
            em.close();
        }
    }

    @POST
    @Path("/bookings")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response booking(BookingRequestDTO req, @CookieParam("auth") Cookie cookie) {
        if (cookie == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        long concertId = req.getConcertId();
        LocalDateTime date = req.getDate();
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            User user = loggedJudge(cookie,em);
            if (user == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            em.getTransaction().begin();
            Concert concert = em.find(Concert.class, concertId);
            em.getTransaction().commit();

            if (concert == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            Set<LocalDateTime> dates = concert.getDates();
            if (!dates.contains(date)) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            em.getTransaction().begin();
            List<Seat> seats = new ArrayList<>();
            for (String label : req.getSeatLabels()) {
                TypedQuery<Seat> seatQuery = em.createQuery("select s from Seat s where s.label = :label AND s.date = :date", Seat.class).setLockMode(LockModeType.PESSIMISTIC_READ);
                seatQuery.setParameter("label", label);
                seatQuery.setParameter("date", date);
                seats.add(seatQuery.getSingleResult());
            }
            em.getTransaction().commit();

            em.getTransaction().begin();
            for (Seat seat : seats) {
                if (!seat.getIsBooked()) {
                    seat.setIsBooked(true);
                } else {
                    return Response.status(Response.Status.FORBIDDEN).build();
                }
            }
            Booking booking = new Booking(concertId, date, seats);
            booking.setUser(user);
            em.persist(booking);
            for (Seat seat : seats) {
                em.persist(seat);
            }
            TypedQuery<Seat> seatQuery = em.createQuery("select s from Seat s where s.date = :date", Seat.class).setLockMode(LockModeType.PESSIMISTIC_READ).setParameter("date", date);
            List<Seat> totalSeats;
            totalSeats = seatQuery.getResultList();
            em.getTransaction().commit();

            int totalBooking = 0;
            for (Seat seat : totalSeats) {
                if (seat.getIsBooked())
                    totalBooking += 1;
            }
            generateConcertSubscription(concertId, date, totalSeats.size(), totalBooking);
            LOGGER.info(user.getUsername() + "booked seat");
            return Response.created(URI.create("/concert-service/bookings/" + user.getId().toString())).build();
        } finally {
            em.close();
        }
    }

    @GET
    @Path("seats/{date}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkForSeats(@PathParam("date") LocalDateTimeParam dateParam, @QueryParam("status") BookingStatus status) {
        LocalDateTime date = dateParam.getLocalDateTime();
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            List<Seat> seats;
            em.getTransaction().begin();
            TypedQuery<Seat> seatQuery = em.createQuery("select s from Seat s where s.date = :date", Seat.class);
            seatQuery.setParameter("date", date);
            seats = seatQuery.getResultList();
            em.getTransaction().commit();

            List<SeatDTO> bookedSeats = new ArrayList<>();
            if (status == BookingStatus.Booked) {
                for (Seat seat : seats) {
                    if (seat.getIsBooked()) {
                        bookedSeats.add(SeatMapper.toDto(seat));
                    }
                }
            } else if (status == BookingStatus.Unbooked) {
                for (Seat seat : seats) {
                    if (!seat.getIsBooked()) {
                        bookedSeats.add(SeatMapper.toDto(seat));
                    }
                }
            } else {
                for (Seat seat : seats) {
                    bookedSeats.add(SeatMapper.toDto(seat));
                }
            }
            LOGGER.info("Checking for success seat booking");
            return Response.ok(bookedSeats).build();
        } finally {
            em.close();
        }
    }

    @POST
    @Path("/subscribe/concertInfo")
    @Consumes(MediaType.APPLICATION_JSON)
    public void subMessage(@Suspended AsyncResponse sub, ConcertInfoSubscriptionDTO subInfo, @CookieParam("auth") Cookie cookie) {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        if (loggedJudge(cookie,em) == null) {
            sub.resume(Response.status(Response.Status.UNAUTHORIZED).build());
            return;
        }
        try {
            long concertId = subInfo.getConcertId();
            LocalDateTime date = subInfo.getDate();

            em.getTransaction().begin();
            Concert concert = em.find(Concert.class, concertId);
            em.persist(ConcertInfoSubscriptionMapper.toDomainModel(subInfo));
            em.getTransaction().commit();

            if (concert == null) {
                sub.resume(Response.status(Response.Status.BAD_REQUEST).build());
                throw null;
            }
            if (!concert.getDates().contains(date)) {
                sub.resume(Response.status(Response.Status.BAD_REQUEST).build());
                throw null;
            }
            LOGGER.info("Add subscribe to ConcertInfo.");
            Subscriptions.add(sub);
        } finally {
            em.close();
        }
    }

    @Produces(MediaType.APPLICATION_JSON)
    private void generateConcertSubscription(long concertId, LocalDateTime date, int seats, int seatsBooked) {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            em.getTransaction().begin();
            TypedQuery<ConcertInfoSubscription> concertInfo = em.createQuery("SELECT cs FROM ConcertInfoSubscription cs WHERE cs.concertId =: concertId", ConcertInfoSubscription.class).setParameter("concertId", concertId);
            List<ConcertInfoSubscription> ConcertInfoSubscriptions = concertInfo.getResultList();
            em.getTransaction().commit();

            for (ConcertInfoSubscription subscription : ConcertInfoSubscriptions) {
                if ((int) ((float) seatsBooked / (float) seats * 100) >= subscription.getPercentageBooked() & subscription.getConcertId() == concertId & subscription.getDate().equals(date)) {
                    ConcertInfoNotificationDTO cin = new ConcertInfoNotificationDTO(seats - seatsBooked);
                    synchronized (Subscriptions) {
                        for (AsyncResponse sub : Subscriptions) {
                            sub.resume(Response.ok(cin, MediaType.APPLICATION_JSON).build());
                        }
                        Subscriptions.clear();
                    }
                }
            }
        } finally {
            em.close();
        }
    }

    public User loggedJudge(Cookie cookie, EntityManager em){
        if (cookie == null || em == null){
            return null;
        }
        try{
            em.getTransaction().begin();
            TypedQuery<User> userQuery = em.createQuery("select u from User u where u.cookie = :cookie", User.class);
            userQuery.setParameter("cookie", cookie.getValue());
            User user = userQuery.getResultList().stream().findFirst().orElse(null);
            em.getTransaction().commit();
            return user;
        } catch (NoResultException e) {
            return null;
        }
    }
}

