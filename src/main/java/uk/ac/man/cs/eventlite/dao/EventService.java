package uk.ac.man.cs.eventlite.dao;

import java.util.Optional;

import uk.ac.man.cs.eventlite.entities.Event;

public interface EventService {

	public long count();

	public Iterable<Event> findAll();
	
	public boolean find(Event e);
	
	public void save(Event event) throws ServiceException;
	
	public Event findOne(long id);

	public Optional<Event> findById(long id);
	
	public void deleteById(long id);

	public void delete(Event event);
}
