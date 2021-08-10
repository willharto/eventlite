package uk.ac.man.cs.eventlite.dao;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import uk.ac.man.cs.eventlite.entities.Event;

@Service
public class EventServiceImpl implements EventService {

	private final static Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

	
	@Autowired
	private EventRepository eventRepository;
	
	@Override
	public long count() {
		return eventRepository.count();
	}

	@Override
	public Iterable<Event> findAll() {
		Iterable<Event> events = eventRepository.findAll();
		Collections.sort((List<Event>) events);
		return events;
	}

	@Override
	public void save(Event event) {
		eventRepository.save(event);
		log.info("Saved [[ " + event + " ]] to database.");
	}

	@Override
	public Optional<Event> findById(long id) {
		return eventRepository.findById(id);
	}
	
	@Override
	public Event findOne(long id) {
		return findById(id).orElse(null);
	}

	@Override
	public void deleteById(long id) {
		eventRepository.deleteById(id);
		log.info("Deleted event with " + id + " from database.");
	}

	@Override
	public void delete(Event event) {
		eventRepository.delete(event);
		log.info("Deleted [[ " + event + " ]] from database.");
	}
	
	public boolean find(Event e) {
		Iterator<Event> events = findAll().iterator();
		while (events.hasNext()) {
			if (events.next().equals(e))
				return true;
		}
		return false;
	}
}
