package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

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
public class VenuesControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	private HttpEntity<String> httpEntity;

	@Autowired
	private TestRestTemplate template;
	
	@LocalServerPort
  private int port;

  private String baseUrl;
  private String loginUrl;
  private String venuesCreateUrl;
  private String venuesDetailsUrl;
  private String venuesDeleteUrl;
  private String venuesUpdateUrl;

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

	  this.baseUrl = "http://localhost:" + port + "/venues";
    this.loginUrl = "http://localhost:" + port + "/sign-in";
    this.venuesCreateUrl = baseUrl + "/new";
    this.venuesDetailsUrl = baseUrl + "/details" + INDEX;
    this.venuesDeleteUrl = baseUrl + "/delete" + INDEX;
    this.venuesUpdateUrl = baseUrl + "/update" + INDEX;
    
	  HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.TEXT_HTML));

		httpEntity = new HttpEntity<String>(headers);
	}
	
  @Test
  public void testCreateVenueWithoutLogin() {
    HttpHeaders postHeaders = new HttpHeaders();
    postHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
    postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("name", "Venue name");
    form.add("capacity", "10");
    form.add("longitude", "0.0");
    form.add("latitude", "0.0");
    form.add("roadname", "Venue Roadname");
    form.add("postcode", "Venue Postcode");
    HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(form,
        postHeaders);

    ResponseEntity<String> response = anon.exchange(venuesCreateUrl, HttpMethod.POST, postEntity, String.class);

    assertThat(response.getStatusCode(), equalTo(HttpStatus.FOUND));
    assertThat(response.getHeaders().getLocation().toString(), equalTo(loginUrl));
    assertThat(2, equalTo(countRowsInTable("venues")));
  }

  @Test
  @DirtiesContext
  public void testCreateVenueWithLogin() {
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

    // Set the session cookie and GET the new venue form so we can read
    // the new CSRF token.
    getHeaders.set("Cookie", cookie);
    getEntity = new HttpEntity<>(getHeaders);
    formResponse = stateful.exchange(loginUrl, HttpMethod.GET, getEntity, String.class);
    csrfToken = getCsrfToken(formResponse.getBody());

    // Populate the new Venue form.
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("_csrf", csrfToken);
    form.add("name", "Venue name");
    form.add("capacity", "10");
    form.add("longitude", "0.0");
    form.add("latitude", "0.0");
    form.add("roadname", "Venue Roadname");
    form.add("postcode", "Venue Postcode");
    postEntity = new HttpEntity<MultiValueMap<String, String>>(form, postHeaders);

    // POST the new Venue.
    ResponseEntity<String> response = stateful.exchange(venuesCreateUrl, HttpMethod.POST, postEntity, String.class);

    // Did it work?
    assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
    assertThat(2, equalTo(countRowsInTable("venues")));
  }
}