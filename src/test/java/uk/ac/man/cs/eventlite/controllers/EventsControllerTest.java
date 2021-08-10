package uk.ac.man.cs.eventlite.controllers;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Collections;

import javax.servlet.Filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class EventsControllerTest {

	private MockMvc mvc;

	@Autowired
	private Filter springSecurityFilterChain;

	@Mock
	private Event event;

	@Mock
	private Venue venue;

	@Mock
	private EventService eventService;

	@Mock
	private VenueService venueService;

	@InjectMocks
	private EventsController eventsController;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mvc = MockMvcBuilders.standaloneSetup(eventsController).apply(springSecurity(springSecurityFilterChain))
				.build();
	}

	@Test
	public void getIndexWhenNoEvents() throws Exception {
		
		when(venueService.findAll()).thenReturn(Collections.<Venue> emptyList());
		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));

		verify(venueService).findAll();
		verifyZeroInteractions(event);
		verifyZeroInteractions(venue);
	}

	@Test
	public void getIndexWithEvents() throws Exception {

		when(venueService.findAll()).thenReturn(Collections.<Venue> singletonList(venue));
		mvc.perform(get("/events")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));

		verify(venueService).findAll();
		verifyZeroInteractions(event);
		verifyZeroInteractions(venue);
	}
	
	@Test
	public void formRedirectTest() throws Exception {
		
		when(venueService.findAll()).thenReturn(Collections.<Venue> singletonList(venue));
		mvc.perform(get("/events/new").accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("events/new"))
				.andExpect(handler().methodName("newEventFormView"));

		verify(venueService).findAll();
		verifyZeroInteractions(event);
		verifyZeroInteractions(venue);
	}
	
	@Test
	public void createEventWithoutCSRFTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post("/events/new").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isForbidden());

		verify(eventService, never()).save(event);
	}

	@Test
	public void deleteEventTest() throws Exception {
		
		mvc.perform(MockMvcRequestBuilders.delete("/events/delete/1").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.TEXT_HTML)
				.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(handler().methodName("deleteEvent"))
				.andExpect(view().name("events/index"));
	}

}
