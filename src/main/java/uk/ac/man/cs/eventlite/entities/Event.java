package uk.ac.man.cs.eventlite.entities;

import java.time.LocalDate;
import java.time.LocalTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name="events")
public class Event implements Comparable<Event> {

	@Id
	@GeneratedValue
	private long id;

	@JsonFormat(shape = JsonFormat.Shape.STRING)
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate date;

	@JsonFormat(shape = JsonFormat.Shape.STRING)
	@DateTimeFormat(pattern = "HH:mm")
	private LocalTime time;

	@NotEmpty(message="The event name cannot be empty.")
	@Size(max=255, message="The event name can not exceed 255 characters.")
	private String name;

	// One event can be hosted at one venue a time but, a venue may host many events
	@ManyToOne
	@NotEmpty(message="The venue name/id cannot be empty.")
	private Venue venue;	// Now references entire Venue object
	
//	@NotEmpty(message="The event description cannot be empty.")
	@Size(max=499, message="The event description can not exceed 499 characters.")
	private String description;
	
	public Event() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public LocalTime getTime() {
		return time;
	}

	public void setTime(LocalTime time) {
		this.time = time;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description ;
	}
	
	public Venue getVenue() {
		return venue;
	}

	public void setVenue(Venue venue) {
		this.venue = venue;
	}
	
	@Override
	public int compareTo(Event other) {
		int dateComparison = this.date.compareTo(other.date);
		if (dateComparison != 0)
			return dateComparison;

		int timeComparison = this.time.compareTo(other.time);
		if (timeComparison != 0)
			return timeComparison;

		return 0;
	}
}
