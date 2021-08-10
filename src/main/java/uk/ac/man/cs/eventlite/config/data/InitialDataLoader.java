package uk.ac.man.cs.eventlite.config.data;

import java.time.LocalDate;
import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.ServiceException;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Component
@Profile({ "default", "test" })
public class InitialDataLoader implements ApplicationListener<ContextRefreshedEvent> {

    private final static Logger log = LoggerFactory.getLogger(InitialDataLoader.class);

    @Autowired
    private EventService eventService;

    @Autowired
    private VenueService venueService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent applicationEvent) {
        if (venueService.count() > 0) {
            log.info("Database already populated. Skipping data initialization.");
            return;
        }

        try {
            Venue venue1 = new Venue();
            venue1.setName("Kilburn Building");
            venue1.setCapacity(100);
            venue1.setRoadname("Oxford Road");
            venue1.setPostcode("M13 9PL");
            venueService.save(venue1);

            Venue venue2 = new Venue();
            venue2.setName("Alan Gilbert Learning Commons");
            venue2.setCapacity(500);
            venue2.setRoadname("Oxford Road");
            venue2.setPostcode("M13 9NR");
            venueService.save(venue2);


            Event event1 = new Event();
            event1.setDate(LocalDate.of(2020, 06, 17));
            event1.setName("El Evento");
            event1.setTime(LocalTime.of(12, 30));
            event1.setVenue(venue1);
            event1.setDescription("Mad party");
            eventService.save(event1);

            Event event2 = new Event();
            event2.setDate(LocalDate.of(2020, 02, 28));
            event2.setName("Uno Evento");
            event2.setTime(LocalTime.of(16, 15));
            event2.setVenue(venue1);
            event2.setDescription("Mad party UNO");
            eventService.save(event2);

            Event event3 = new Event();
            event3.setDate(LocalDate.of(2020, 03, 15));
            event3.setName("Dos Evento");
            event3.setTime(LocalTime.of(9, 45));
            event3.setVenue(venue2);
            event3.setDescription("Mad party Dos");
            eventService.save(event3);
        }
        catch(ServiceException exception) {
            log.info(exception.getMessage());
        }
    }
}
