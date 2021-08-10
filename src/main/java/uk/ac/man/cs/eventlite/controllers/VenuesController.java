package uk.ac.man.cs.eventlite.controllers;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.ServiceException;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Controller
@RequestMapping(value="/venues", produces= {MediaType.TEXT_HTML_VALUE})
public class VenuesController {

  @Autowired
  private EventService eventService;
  
  @Autowired
  private VenueService venueService;
  
  private List<String> error_msg_list = new LinkedList<String>();
  
  @RequestMapping(value="/new", method = RequestMethod.GET)
  public String newVenueFormView(Model model) {
    model.addAttribute("emptyFields", "");
    return "venues/new";
  }
  
  @RequestMapping(value="/new", method = RequestMethod.POST)
  public String saveNewEvent(@ModelAttribute("venue") Venue venue, Model model) {
    /******************* Error handling *******************/
    error_msg_list.clear();
    if (venue.getName().length() > 255)
      error_msg_list.add("Venue name can not exceed 255 characters.");
    if (venue.getName().trim().length() == 0)
      error_msg_list.add("Venue name can not be empty.");
    if (venue.getCapacity() < 0)
      error_msg_list.add("Venue capacity can not be negative!");
    if (venue.getPostcode().length() != 6)
      if (venue.getPostcode().length() != 7)
        error_msg_list.add("Venue postcode must be in the form XXXXXX or XXX XXX.");
    /******************************************************/
    
    model.addAttribute("events", eventService.findAll());
    model.addAttribute("venues", venueService.findAll());
    
    if (error_msg_list.size() > 0) {
      model.addAttribute("hasErrors", true);
      model.addAttribute("errors", error_msg_list);
      return "venues/new";
    } else {
      model.addAttribute("hasErrors", false);
      try { venueService.save(venue); }
      catch (ServiceException e) { e.printStackTrace(); }
      return "events/index";
    }
  }
  
  @RequestMapping(value="/search", method = RequestMethod.GET)
  public String searchVenueByName(HttpServletRequest request, Model model) {
    String venueName = request.getParameter("search").trim();
    List<Venue> found = new LinkedList<Venue>();
    for (Venue v : venueService.findAll()) {
      List<String> currentName = Arrays.asList(v.getName().toUpperCase().split(" "));
      if (currentName.contains(venueName.toUpperCase()) || venueName.toUpperCase().equals(v.getName().toUpperCase())) {
        found.add(v);
      }
    }

    if (found.size() == 0) {
      model.addAttribute("noVenueFound", true);
      model.addAttribute("venueName", venueName);
    }

    model.addAttribute("events", eventService.findAll());
    model.addAttribute("venues", found);
    return "events/index";
}

  @RequestMapping(value="/details/{id}", method=RequestMethod.GET)
  public String venueInformation(@PathVariable("id") long id, Model model) {
    model.addAttribute("venue", venueService.findOne(id));
    return "venues/information";
  }

  @RequestMapping(value="/update/{id}", method = RequestMethod.GET)
  public String updateVenueForm(@PathVariable("id") long id, Model model) {
    model.addAttribute("venue", venueService.findOne(id));
    return "venues/edit";
}
  
  @RequestMapping(value="/update/{id}", method = RequestMethod.POST)
  public String updateEvent(@PathVariable("id") long id, @ModelAttribute Venue venue, Model model) {
    /******************* Error handling *******************/
    error_msg_list.clear();
    if (venue.getName().length() > 255)
      error_msg_list.add("Venue name can not exceed 255 characters.");
    if (venue.getName().trim().length() <= 0)
      error_msg_list.add("Venue name can not be empty.");
    if (venue.getCapacity() < 0)
      error_msg_list.add("Venue capacity can not be negative!");
    if (venue.getPostcode().length() != 6)
      if (venue.getPostcode().length() != 7)
        error_msg_list.add("Venue postcode must be in the form XXXXXX or XXX XXX.");
    /******************************************************/

    model.addAttribute("events", eventService.findAll());
    model.addAttribute("venues", venueService.findAll());

    if (error_msg_list.size() > 0) {
        model.addAttribute("hasErrors", true);
        model.addAttribute("errors", error_msg_list);
    } else {
      model.addAttribute("hasErrors", false);
      try { venueService.save(venue); }
      catch (ServiceException e) { e.printStackTrace(); }
    }
    return "events/index";
  }
  
  @RequestMapping(value="/delete/{id}", method = RequestMethod.DELETE)
  public String deleteVenue(@PathVariable("id") long id, Model model) {
    Optional<Venue> foundVenue = venueService.findById(id);
    if (foundVenue.isEmpty()) {
      error_msg_list.add("Venue ID was not found.");
      model.addAttribute("events", eventService.findAll());
      model.addAttribute("venues", venueService.findAll());
      return "events/index";
    }

    Venue venue = foundVenue.get();
    Iterable<Event> allEvents = eventService.findAll();
    for (Event e : allEvents)
      if (e.getVenue().equals(venue)) {
        error_msg_list.add("The venue still has events associated with it.");
        model.addAttribute("events", eventService.findAll());
        model.addAttribute("venues", venueService.findAll());
        return "events/index";
      }

    venueService.deleteById(id);
    model.addAttribute("events", eventService.findAll());
    model.addAttribute("venues", venueService.findAll());
    return "events/index";
  }
}
