package uk.ac.man.cs.eventlite.controllers;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import uk.ac.man.cs.eventlite.api.twitter.Tweet;
import uk.ac.man.cs.eventlite.api.twitter.TwitterInterface;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.dao.ServiceException;
import uk.ac.man.cs.eventlite.entities.Event;

@Controller
@RequestMapping(value = "/events", produces = { MediaType.TEXT_HTML_VALUE })
public class EventsController {

	@Autowired
	private EventService eventService;
	
	@Autowired
	private VenueService venueService;
	
	private List<String> error_msg_list = new LinkedList<String>();
	private Twitter twitter = new TwitterInterface().getTwitterInstance();
	
	@RequestMapping(method = RequestMethod.GET)
	public String getAllEvents(Model model) throws TwitterException {
	    getTwitterTimeline(model);
		model.addAttribute("events", eventService.findAll());
		model.addAttribute("venues", venueService.findAll());
		return "events/index";
	}
	
	@RequestMapping(value="/new", method = RequestMethod.GET)
	public String newEventFormView(Model model) {
		
		model.addAttribute("emptyFields", "");
		model.addAttribute("venues", venueService.findAll());
		return "events/new";
	}
	
	@RequestMapping(value="/new", method = RequestMethod.POST)
	public String saveNewEvent(@ModelAttribute("event") Event event, Model model) {

	  /**** Error handling ****/
	  error_msg_list.clear();
	  if (event.getName().length() > 255)
        error_msg_list.add("Event name can not exceed 255 characters.");
	  if (event.getVenue() == null)
	    error_msg_list.add("Event venue can not be empty.");
	  if (event.getDate().compareTo(LocalDate.now()) < 0)
	    error_msg_list.add("Event date must be in the future.");
	  
	  if (error_msg_list.size() > 0) {
	    model.addAttribute("hasErrors", true);
	    model.addAttribute("errors", error_msg_list);
	    model.addAttribute("venues", venueService.findAll());
	    return "events/new";
	  }
	  /************************/
	  try { eventService.save(event); }
	  catch (ServiceException e) { e.printStackTrace(); }
	  
	  model.addAttribute("hasErrors", false);
	  model.addAttribute("events", eventService.findAll());
	  model.addAttribute("venues", venueService.findAll());
	  return "events/index";
	}

	@RequestMapping(value = "/details/{id}", method = RequestMethod.GET)
	public String eventDetails(@PathVariable("id") long id, Model model) {

		model.addAttribute("event", eventService.findOne(id));		
		return "events/information";
	}
	
	@RequestMapping(value="/delete/{id}", method = RequestMethod.DELETE)
	public String deleteEvent(@PathVariable("id") long id, Model model) {
		
		eventService.deleteById(id);
		model.addAttribute("events", eventService.findAll());
		model.addAttribute("venues", venueService.findAll());
		return "events/index";
	}
	
	@RequestMapping(value="/update/{id}", method = RequestMethod.GET)
	public String updateEventForm(@PathVariable("id") long id, Model model) {

	  Event e = eventService.findOne(id);
	  if (e.getDate().compareTo(LocalDate.now()) < 0)
        e.setDate(null);
	  model.addAttribute("event", e);
	  model.addAttribute("hasErrors", true);
	  model.addAttribute("errors", error_msg_list);
	  model.addAttribute("venues", venueService.findAll());
      return "events/edit";
	}
	
	@RequestMapping(value="/update/{id}", method = RequestMethod.POST)
	public String updateEvent(@PathVariable("id") long id, @ModelAttribute Event event, Model model) {
		
		/******************* Error handling *******************/
        error_msg_list.clear();
        if (eventService.findOne(id).getName().length() > 255)
          error_msg_list.add("Event name can not exceed 255 characters.");
        if (eventService.findOne(id).getVenue() == null)
          error_msg_list.add("Event venue can not be empty.");
        if (eventService.findOne(id).getDate().compareTo(LocalDate.now()) < 0)
          error_msg_list.add("Event date must be in the future.");
      
        if (error_msg_list.size() > 0) {
	      Event e = eventService.findOne(id);
      	  e.setDate(null);
      	  model.addAttribute("event", e);
          model.addAttribute("hasErrors", true);
          model.addAttribute("errors", error_msg_list);
          model.addAttribute("venues", venueService.findAll());
          return "events/new";
        }
        /******************************************************/

  	    try { eventService.save(event); }
  	    catch (ServiceException e) { e.printStackTrace(); }
		model.addAttribute("events", eventService.findAll());
		model.addAttribute("venues", venueService.findAll());
		return "events/index";
	}
	
	@RequestMapping(value="/search", method = RequestMethod.GET)
	public String searchEventByName(HttpServletRequest request, Model model) {
		
		String eventName = request.getParameter("search").trim();
		List<Event> found = new LinkedList<Event>();
		for (Event e : eventService.findAll()) {
			List<String> currentName = Arrays.asList(e.getName().toUpperCase().split(" "));
			if (currentName.contains(eventName.toUpperCase()) || eventName.toUpperCase().equals(e.getName().toUpperCase())) {
				found.add(e);
			}
		}
		
		if (found.size() == 0) {
			model.addAttribute("noEventFound", true);
			model.addAttribute("eventName", eventName);
		}
		
		model.addAttribute("events", found);
		model.addAttribute("venues", venueService.findAll());
		return "events/index";
	}
	
	@RequestMapping(value="/sort/name", method = RequestMethod.GET)
	public String sortByName(Model model) {
		
		Iterable<Event> events = eventService.findAll();
		Collections.sort((List<Event>) events,
			new Comparator<Event>() {
			  public int compare(Event o1, Event o2) {
			      return o1.getName().compareTo(o2.getName());
			  }
			});
		model.addAttribute("events", events);
		model.addAttribute("venues", venueService.findAll());
		return "events/index";
	}
	
	@RequestMapping(value="/sort/date", method = RequestMethod.GET)
	public String sortByDate(Model model) {
		
		Iterable<Event> events = eventService.findAll();
		Collections.sort((List<Event>) events,
			new Comparator<Event>() {
			  // 0 - equal, -1 - o1 < o2, 1 - o1 > o2
			  public int compare(Event o1, Event o2) {
				  if (o1.getDate() == null && o2.getDate() == null)
			        return 0;
		          else if (o1.getDate() != null && o2.getDate() == null)
			        return 1;
			      else if (o1.getDate() == null && o2.getDate() != null)
		            return -1;
			      else
			        return o1.getDate().compareTo(o2.getDate());
			  }
			});
		model.addAttribute("events", events);
		model.addAttribute("venues", venueService.findAll());
		return "events/index";
	}
	
	private void getTwitterTimeline(Model model) throws TwitterException {
      List<Status> timeline = twitter.getUserTimeline();
      List<Tweet> tweets = new LinkedList<Tweet>();
      int counter = 1;
      for (Status s : timeline) {
          Tweet tweet = new Tweet();
          tweet.setDate(String.valueOf(s.getCreatedAt()));
          tweet.setTweet(s.getText());
          tweet.setTweetURL(""+s.getId());
          tweets.add(tweet);
          counter ++;
          if (counter > 5)
              break;
    }
    model.addAttribute("tweets", tweets);
    }
	
	@RequestMapping(value="/tweet/{id}", method=RequestMethod.POST)
    public String postTweet(@PathVariable("id") long id, @ModelAttribute("tweet") String tweet, RedirectAttributes redirectAttr) throws TwitterException {
      Status status = twitter.updateStatus(tweet);
      System.out.println("Successfully updated the status to [" + status.getText() + "].");
    
      redirectAttr.addFlashAttribute("tweetSuccess", status.getText());
      return "redirect:/events/details/" + id;
    }
}
