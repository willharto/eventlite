package uk.ac.man.cs.eventlite;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.servlet.Filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecurityTest {

	private MockMvc mvc;

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private Filter springSecurityFilterChain;

	@BeforeEach
	public void setup() {
		mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity(springSecurityFilterChain)).build();
	}

	@Test
	public void getSignInForm() throws Exception {
		mvc.perform(get("/sign-in").accept(MediaType.TEXT_HTML)).andExpect(status().isOk());
	}

	@Test
	public void postSignInNoData() throws Exception {
		mvc.perform(post("/sign-in").accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isFound())
				.andExpect(redirectedUrl("/sign-in?error"));
	}

	@Test
	public void postSignInBadPassword() throws Exception {
		mvc.perform(post("/sign-in").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("username", "Rob").param("password", "H").with(csrf())).andExpect(status().isFound())
				.andExpect(redirectedUrl("/sign-in?error"));
	}

	@Test
	public void postSignInBadUser() throws Exception {
		mvc.perform(post("/sign-in").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("username", "R").param("password", "Haines").with(csrf())).andExpect(status().isFound())
				.andExpect(redirectedUrl("/sign-in?error"));
	}

	@Test
	public void postSignIn() throws Exception {
		mvc.perform(post("/sign-in").accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("username", "Rob").param("password", "Haines").with(csrf())).andExpect(status().isFound())
				.andExpect(redirectedUrl("/"));
	}

	@Test
	public void postSignOut() throws Exception {
		mvc.perform(post("/sign-out").accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isFound())
				.andExpect(redirectedUrl("/"));
	}
}
