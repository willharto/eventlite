package uk.ac.man.cs.eventlite;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static uk.ac.man.cs.eventlite.testutil.FormUtil.getCsrfToken;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@ActiveProfiles("test")
public class SecurityIntegrationTest {

	@LocalServerPort
	private int port;

	private String baseUri;
	private String loginUri;
	private String loginUriError;
	private String logoutUri;
	private String eventsUri;

	// We need cookies for Web log in.
	// Initialize this each time we need it to ensure it's clean.
	private TestRestTemplate stateful;

	@BeforeEach
	public void setUp() {
		stateful = new TestRestTemplate(HttpClientOption.ENABLE_COOKIES);

		baseUri = "http://localhost:" + port + "/";
		loginUri = baseUri + "sign-in";
		loginUriError = loginUri + "?error";
		logoutUri = baseUri + "sign-out";
		eventsUri = baseUri + "events";
	}

	@Test
	public void login() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.TEXT_HTML));

		// GET the login form so we can read the CSRF token and session cookie.
		HttpEntity<String> formEntity = new HttpEntity<>(headers);
		ResponseEntity<String> formResponse = stateful.exchange(loginUri, HttpMethod.GET, formEntity, String.class);
		String csrfToken = getCsrfToken(formResponse.getBody());
		String cookie = formResponse.getHeaders().getFirst("Set-Cookie");

		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.set("Cookie", cookie);

		// Populate the form with the required data.
		MultiValueMap<String, String> login = new LinkedMultiValueMap<>();
		login.add("_csrf", csrfToken);
		login.add("username", "Rob");
		login.add("password", "Haines");

		// POST the populated login form.
		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(login,
				headers);
		ResponseEntity<String> loginResponse = stateful.exchange(loginUri, HttpMethod.POST, postEntity, String.class);
		assertThat(loginResponse.getStatusCode(), equalTo(HttpStatus.FOUND));
		assertThat(loginResponse.getHeaders().getLocation().toString(), equalTo(baseUri));
	}

	@Test
	public void loginBadUser() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.TEXT_HTML));

		// GET the login form so we can read the CSRF token and session cookie.
		HttpEntity<String> formEntity = new HttpEntity<>(headers);
		ResponseEntity<String> formResponse = stateful.exchange(loginUri, HttpMethod.GET, formEntity, String.class);
		String csrfToken = getCsrfToken(formResponse.getBody());
		String cookie = formResponse.getHeaders().getFirst("Set-Cookie");

		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.set("Cookie", cookie);

		// Populate the form with the required data.
		MultiValueMap<String, String> login = new LinkedMultiValueMap<>();
		login.add("_csrf", csrfToken);
		login.add("username", "Robert");
		login.add("password", "Haines");

		// POST the populated login form.
		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(login,
				headers);
		ResponseEntity<String> loginResponse = stateful.exchange(loginUri, HttpMethod.POST, postEntity, String.class);
		assertThat(loginResponse.getStatusCode(), equalTo(HttpStatus.FOUND));
		assertThat(loginResponse.getHeaders().getLocation().toString(), equalTo(loginUriError));
	}

	@Test
	public void logout() throws Exception {
		// We need to login before we can logout.
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.TEXT_HTML));

		// GET the login form so we can read the CSRF token and session cookie.
		HttpEntity<String> formEntity = new HttpEntity<>(headers);
		ResponseEntity<String> formResponse = stateful.exchange(loginUri, HttpMethod.GET, formEntity, String.class);
		String csrfToken = getCsrfToken(formResponse.getBody());
		String cookie = formResponse.getHeaders().getFirst("Set-Cookie");

		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.set("Cookie", cookie);

		// Populate the login form with the required data.
		MultiValueMap<String, String> login = new LinkedMultiValueMap<>();
		login.add("_csrf", csrfToken);
		login.add("username", "Rob");
		login.add("password", "Haines");

		// POST the populated login form to login.
		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(login,
				headers);
		stateful.exchange(loginUri, HttpMethod.POST, postEntity, String.class);

		// GET the events page so we can read the CSRF token for the logout
		// form.
		formResponse = stateful.exchange(eventsUri, HttpMethod.GET, formEntity, String.class);
		csrfToken = getCsrfToken(formResponse.getBody());

		// Populate the logout form with the required data (the CSRF token is
		// the same as before).
		MultiValueMap<String, String> logout = new LinkedMultiValueMap<>();
		logout.add("_csrf", csrfToken);

		// POST the populated logout form.
		postEntity = new HttpEntity<MultiValueMap<String, String>>(logout, headers);
		ResponseEntity<String> logoutResponse = stateful.exchange(logoutUri, HttpMethod.POST, postEntity, String.class);
		assertThat(logoutResponse.getStatusCode(), equalTo(HttpStatus.FOUND));
		assertThat(logoutResponse.getHeaders().getLocation().toString(), equalTo(baseUri));
	}
}
