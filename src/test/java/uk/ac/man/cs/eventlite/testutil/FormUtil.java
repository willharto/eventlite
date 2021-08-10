package uk.ac.man.cs.eventlite.testutil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormUtil {

	public static String getCsrfToken(String body) {
		Pattern pattern = Pattern.compile("(?s).*name=\"_csrf\".*?value=\"([^\"]+).*");
		Matcher matcher = pattern.matcher(body);

		// matcher.matches() must be called to run the actual match!
		assertThat(matcher.matches(), equalTo(true));

		return matcher.group(1);
	}
}
