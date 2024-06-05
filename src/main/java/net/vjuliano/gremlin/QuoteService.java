package net.vjuliano.gremlin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

@Slf4j
public class QuoteService {

	private static final String PROPERTIES_FILE = "/app.properties";
	private static final String FORISMATIC_URL_KEY = "forismaticUrl";
	private static final String RUSSIAN = "russian";
	private static final String RU = "ru";
	private static final String EN = "en";

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record QuoteResponse(String quoteText, String quoteAuthor) {}

	private final ObjectMapper objectMapper = new ObjectMapper();;
	private final String forismaticUrl;


	public static void main(String[] args) {
		QuoteService quoteService = null;
		QuoteResponse response = null;

		try {
			quoteService = new QuoteService();
		} catch (Exception ex) {
			log.error("Exception loading properties", ex);
			System.exit(1);
		}

		try {
			response = (args.length == 0 || !args[0].equals(RUSSIAN))
					           ? quoteService.getQuote(EN) : quoteService.getQuote(RU);
		} catch (Exception ex) {
			log.error("Error calling forismatic api", ex);
			System.exit(2);
		}

		System.out.printf("\"%s\", %s", response.quoteText, response.quoteAuthor);
	}

	private QuoteService() throws IOException {
		final Properties properties = new Properties();
		properties.load(this.getClass().getResourceAsStream(PROPERTIES_FILE));

		forismaticUrl = properties.getProperty(FORISMATIC_URL_KEY);
	}

	private QuoteResponse getQuote(final String language) throws IOException, InterruptedException {
		final HttpResponse<String> response = HttpClient.newHttpClient()
				                                      .send(HttpRequest.newBuilder()
						                                            .uri(URI.create(forismaticUrl + language))
						                                            .build(),
						                                      HttpResponse.BodyHandlers.ofString());
		//handle other 2xx codes as success
		if (response.statusCode() != HttpURLConnection.HTTP_OK) {
			throw new RuntimeException("Non success status code: " + response.statusCode());
		}

		return objectMapper.readValue(response.body(), QuoteResponse.class);
	}
}
