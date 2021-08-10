package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.StringEndsWith.endsWith;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.client.TestRestTemplate.HttpClientOption;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import uk.ac.man.cs.eventlite.EventLite;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class EventsControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	private HttpEntity<String> httpEntity;

	@Autowired
	private TestRestTemplate template;
	
	@LocalServerPort
  private int port;

  private String baseUrl;
  private String loginUrl;
  private String eventsCreateUrl;
  private String eventsDetailsUrl;
  private String eventsDeleteUrl;
  private String eventsUpdateUrl;

  private static final String INDEX = "/1";
  
  // We need cookies for Web log in.
  // Initialize this each time we need it to ensure it's clean.
  private TestRestTemplate stateful;
  
  // An anonymous and stateless log in.
  private final TestRestTemplate anon = new TestRestTemplate();
   
  /********** UTILITY METHODS **********/
  private String getCsrfToken(String body) {
    Pattern pattern = Pattern.compile("(?s).*name=\"_csrf\".*?value=\"([^\"]+).*");
    Matcher matcher = pattern.matcher(body);

    // matcher.matches() must be called!
    assertThat(matcher.matches(), equalTo(true));

    return matcher.group(1);
  }
  /**************************************/
  
	@BeforeEach
	public void setup() {

	  this.baseUrl = "http://localhost:" + port + "/events";
    this.loginUrl = "http://localhost:" + port + "/sign-in";
    this.eventsCreateUrl = baseUrl + "/new";
    this.eventsDetailsUrl = baseUrl + "/details" + INDEX;
    this.eventsDeleteUrl = baseUrl + "/delete" + INDEX;
    this.eventsUpdateUrl = baseUrl + "/update" + INDEX;
    
	  HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.TEXT_HTML));

		httpEntity = new HttpEntity<String>(headers);
	}

	@Test
  public void testGetAllEvents() {
	  
    ResponseEntity<String> response = template.exchange(baseUrl, HttpMethod.GET, httpEntity, String.class);
    System.out.println("\n\nThere are currently " + countRowsInTable("events") + " events in the database in get all events.\n");
    assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
  }
	
	@Test
  public void testSignInPage() {
    
    ResponseEntity<String> response = template.exchange(loginUrl, HttpMethod.GET, httpEntity, String.class);
    assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
  }
	
//	@Test
//  public void testGetEventInformation() {
//    
//    ResponseEntity<String> response = anon.exchange(eventsUpdateUrl, HttpMethod.GET, httpEntity, String.class);
//    assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
//  }
	
	@Test
  public void testLogin() {
    stateful = new TestRestTemplate(HttpClientOption.ENABLE_COOKIES);

    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.TEXT_HTML));

    HttpEntity<String> formEntity = new HttpEntity<>(headers);
    ResponseEntity<String> formResponse = stateful.exchange(loginUrl, HttpMethod.GET, formEntity, String.class);
    String csrfToken = getCsrfToken(formResponse.getBody());
    String cookie = formResponse.getHeaders().getFirst("Set-Cookie");

    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.set("Cookie", cookie);

    MultiValueMap<String, String> login = new LinkedMultiValueMap<>();
    login.add("_csrf", csrfToken);
    login.add("username", "Markel");
    login.add("password", "Vigo");

    HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(login,
        headers);
    ResponseEntity<String> loginResponse = stateful.exchange(loginUrl, HttpMethod.POST, postEntity, String.class);
    assertThat(loginResponse.getStatusCode(), equalTo(HttpStatus.FOUND));
    assertThat(loginResponse.getHeaders().getLocation().toString(), endsWith(":" + this.port + "/"));
  }

  @Test
  public void testBadUserLogin() {
    stateful = new TestRestTemplate(HttpClientOption.ENABLE_COOKIES);

    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.TEXT_HTML));

    HttpEntity<String> formEntity = new HttpEntity<>(headers);
    ResponseEntity<String> formResponse = stateful.exchange(loginUrl, HttpMethod.GET, formEntity, String.class);
    String csrfToken = getCsrfToken(formResponse.getBody());
    String cookie = formResponse.getHeaders().getFirst("Set-Cookie");

    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.set("Cookie", cookie);

    MultiValueMap<String, String> login = new LinkedMultiValueMap<>();
    login.add("_csrf", csrfToken);
    login.add("username", "Robert");
    login.add("password", "Mustafa");

    HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(login,
        headers);
    ResponseEntity<String> loginResponse = stateful.exchange(loginUrl, HttpMethod.POST, postEntity, String.class);
    assertThat(loginResponse.getStatusCode(), equalTo(HttpStatus.FOUND));
    assertThat(loginResponse.getHeaders().getLocation().toString(), endsWith("/sign-in?error"));
  }

  @Test
  public void testBadPasswordLogin() {
    stateful = new TestRestTemplate(HttpClientOption.ENABLE_COOKIES);

    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.TEXT_HTML));

    HttpEntity<String> formEntity = new HttpEntity<>(headers);
    ResponseEntity<String> formResponse = stateful.exchange(loginUrl, HttpMethod.GET, formEntity, String.class);
    String csrfToken = getCsrfToken(formResponse.getBody());
    String cookie = formResponse.getHeaders().getFirst("Set-Cookie");

    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.set("Cookie", cookie);

    MultiValueMap<String, String> login = new LinkedMultiValueMap<>();
    login.add("_csrf", csrfToken);
    login.add("username", "Caroline");
    login.add("password", "J");

    HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(login,
        headers);
    ResponseEntity<String> loginResponse = stateful.exchange(loginUrl, HttpMethod.POST, postEntity, String.class);
    assertThat(loginResponse.getStatusCode(), equalTo(HttpStatus.FOUND));
    assertThat(loginResponse.getHeaders().getLocation().toString(), endsWith("/sign-in?error"));
  }
  
  @Test
  public void testCreateEventWithoutLogin() {
    HttpHeaders postHeaders = new HttpHeaders();
    postHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
    postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("name", "Event name");
    form.add("date", "2020-12-31");
    form.add("time", "00:00");
    form.add("venue.id", "1");
    form.add("description", "Event description");
    HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(form,
        postHeaders);

    ResponseEntity<String> response = anon.exchange(eventsCreateUrl, HttpMethod.POST, postEntity, String.class);

    assertThat(response.getStatusCode(), equalTo(HttpStatus.FOUND));
    assertThat(response.getHeaders().getLocation().toString(), equalTo(loginUrl));
    assertThat(3, equalTo(countRowsInTable("events")));
  }

  @Test
  @DirtiesContext
  public void testCreateEventWithLogin() {
    stateful = new TestRestTemplate(HttpClientOption.ENABLE_COOKIES);

    // Set up headers for GETting and POSTing.
    HttpHeaders getHeaders = new HttpHeaders();
    getHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));

    HttpHeaders postHeaders = new HttpHeaders();
    postHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
    postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    // GET the log in page so we can read the CSRF token and the session
    // cookie.
    HttpEntity<String> getEntity = new HttpEntity<>(getHeaders);
    ResponseEntity<String> formResponse = stateful.exchange(loginUrl, HttpMethod.GET, getEntity, String.class);
    String csrfToken = getCsrfToken(formResponse.getBody());
    String cookie = formResponse.getHeaders().getFirst("Set-Cookie").split(";")[0];

    // Set the session cookie and populate the log in form.
    postHeaders.set("Cookie", cookie);
    MultiValueMap<String, String> login = new LinkedMultiValueMap<>();
    login.add("_csrf", csrfToken);
    login.add("username", "Mustafa");
    login.add("password", "Mustafa");

    // Log in.
    HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(login,
        postHeaders);
    ResponseEntity<String> loginResponse = stateful.exchange(loginUrl, HttpMethod.POST, postEntity, String.class);
    assertThat(loginResponse.getStatusCode(), equalTo(HttpStatus.FOUND));

    // Set the session cookie and GET the new event form so we can read
    // the new CSRF token.
    getHeaders.set("Cookie", cookie);
    getEntity = new HttpEntity<>(getHeaders);
    formResponse = stateful.exchange(loginUrl, HttpMethod.GET, getEntity, String.class);
    csrfToken = getCsrfToken(formResponse.getBody());

    // Populate the new event form.
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("_csrf", csrfToken);
    form.add("name", "Event name");
    form.add("date", "2020-12-31");
    form.add("time", "00:00");
    form.add("venue.id", "1");
    form.add("description", "Event description");
    postEntity = new HttpEntity<MultiValueMap<String, String>>(form, postHeaders);

    // POST the new event.
    ResponseEntity<String> response = stateful.exchange(eventsCreateUrl, HttpMethod.POST, postEntity, String.class);

    // Did it work?
    assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
//    assertThat(response.getHeaders().getLocation().toString(), containsString(baseUrl));
    assertThat(4, equalTo(countRowsInTable("events")));
  }
}