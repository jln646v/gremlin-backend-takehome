package net.vjuliano.gremlin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import mockit.Mock;
import mockit.MockUp;
import org.junit.jupiter.api.Test;

import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOut;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@WireMockTest(httpPort = 8080)
public class QuoteServiceTest {

	private static final Integer SUCCESS_CODE = 200;
	private static final Integer FAILURE_CODE = 400;
	private static final String PATH = "/api/1.0?lang=";
	private static final String RUSSIAN = "russian";
	private static final String ENGLISH = "english";
	private static final String RU = "ru";
	private static final String EN = "en";
	private static final String RESULT_FORMAT = "\"%s\", %s";

	private record QuoteResponse(String quoteText, String quoteAuthor) {}

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	public void testQuoteService_russian() throws Exception {
		final String quoteText = "quoteText0";
		final String quoteAuthor = "quoteAuthor0";

		createWireMockStub(RU, true, quoteText, quoteAuthor);

		String result = tapSystemOut(() -> QuoteService.main(new String[]{RUSSIAN}));

		 assertTrue(result.contains(String.format(RESULT_FORMAT, quoteText,  quoteAuthor)));
	}

	@Test
	public void testQuoteService_english() throws Exception {
		final String quoteText = "quoteText1";
		final String quoteAuthor = "quoteAuthor1";

		createWireMockStub(EN, true, quoteText, quoteAuthor);

		String result = tapSystemOut(() -> QuoteService.main(new String[]{ENGLISH}));

		assertTrue(result.contains(String.format(RESULT_FORMAT, quoteText,  quoteAuthor)));
	}

	@Test
	public void testQuoteService_noArgs() throws Exception {
		final String quoteText = "quoteText2";
		final String quoteAuthor = "quoteAuthor2";

		createWireMockStub(EN, true, quoteText, quoteAuthor);

		String result = tapSystemOut(() -> QuoteService.main(new String[]{}));

		assertTrue(result.contains(String.format(RESULT_FORMAT, quoteText,  quoteAuthor)));
	}

	@Test
	public void testQuoteService_httpFail() throws Exception {
		final String quoteText = "quoteText3";
		final String quoteAuthor = "quoteAuthor3";

		new MockUp<System>() {
			@Mock
			public void exit(int value) {
				throw new RuntimeException("SYSTEM EXIT EXCEPTION: " + value);
			}
		};

		createWireMockStub(EN, false, quoteText, quoteAuthor);

		Exception ex = assertThrows( RuntimeException.class,
				() -> QuoteService.main(new String[]{ENGLISH}));

		assertEquals("SYSTEM EXIT EXCEPTION: 2", ex.getMessage());
	}

	private void createWireMockStub(String lang, boolean success, String quoteText, String quoteAuthor) throws JsonProcessingException {
		stubFor(
				get(PATH + lang)
						.willReturn(
								aResponse()
										.withBody(
												objectMapper.writeValueAsString(
														new QuoteResponse(quoteText,  quoteAuthor)))
										.withStatus(success ? SUCCESS_CODE : FAILURE_CODE)));
	}
}
