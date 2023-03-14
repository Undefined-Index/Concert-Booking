package proj.concert.service.domain;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import proj.concert.common.jackson.LocalDateTimeDeserializer;
import proj.concert.common.jackson.LocalDateTimeSerializer;

@Entity
public class Booking {
	@Column(name = "VERSION")
    private String version;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long concertId;
    private LocalDateTime date;

    @OneToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
    private List<Seat> seats;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user = null;

    public Booking(){}

    public Booking(long concertId, LocalDateTime date, List<Seat> seats){
        this.concertId = concertId;
        this.date = date;
        this.seats = seats;
    }

    public long getId(){
        return id;
    }

    public void setId(long id){
        this.id = id;
    }

    public long getConcertId(){
        return this.concertId;
    }

    public void setConcertId(long id){
        this.concertId = id;
    }

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime getDate(){
        return date;
    }
    
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public void setDate(LocalDateTime date){
        this.date = date;
    }

    public List<Seat> getSeats(){
        return seats;
    }

    public void setSeats(List<Seat> seats){
        this.seats = seats;
    }

    public User getUser(){
        return user;
    }

    public void setUser(User user){
        this.user = user;
    }
}
