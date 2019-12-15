package commons.securelogging.jsonlogger;

import com.jayway.jsonassert.JsonAssert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;

public class JsonContentMaskerTest {

    static final String REDACTED_CC = "444455******7777";
    static final String REDACTED_PASSWORD = "********";
    static final String REDACTED_SSN = "*****6789";
    static final String REDACTED_ACCT = "********4444";

    static final String FLAT_JSON = "{\n" +
            "  \"name\": \"SpiderMan\",\n" +
            "  \"accountNumber\": \"1111222233334444\",\n" +
            "  \"socialSecurityNumber\": \"123456789\",\n" +
            "  \"creditCardNumber\": \"4444555566667777\",\n" +
            "  \"password\": \"secret\"\n" +
            "}";

    static final String ARRAY_JSON = "{\n" +
            "  \"count\": 3,\n" +
            "  \"people\": [\n" +
            "    {\n" +
            "      \"name\": \"SpiderMan\",\n" +
            "      \"creditCardNumber\": \"4444555566667777\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"SuperMan\",\n" +
            "      \"creditCardNumber\": \"4444555566667777\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"BatMan\",\n" +
            "      \"creditCardNumber\": \"4444555566667777\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    @Test
    public void testRedactSSNFromFlatJson() {

        JsonRedactor redacter = new JsonRedactor(new JsonRedactionDirective("$.socialSecurityNumber", new SSNRedactionFunction()));

        JsonAssert
                .with(redacter.redact(FLAT_JSON))
                .assertThat("$.socialSecurityNumber", equalTo(REDACTED_SSN));
    }

    @Test
    public void testRedactPANFromFlatJson() {

        JsonRedactor redacter = new JsonRedactor(new JsonRedactionDirective("$.creditCardNumber", new PANRedactionFunction()));

        JsonAssert
                .with(redacter.redact(FLAT_JSON))
                .assertThat("$.creditCardNumber", equalTo(REDACTED_CC));
    }

    @Test
    public void testRedactPasswordFromFlatJson() {

        JsonRedactor redacter = new JsonRedactor(new JsonRedactionDirective("$.password"));

        JsonAssert
                .with(redacter.redact(FLAT_JSON))
                .assertNotDefined("$.password");
    }

    @Test
    public void testRedactAccountNumberFromFlatJson() {

        JsonRedactor redacter = new JsonRedactor(new JsonRedactionDirective("$.accountNumber", new AccountNumberRedactionFunction()));

        JsonAssert
                .with(redacter.redact(FLAT_JSON))
                .assertThat("$.accountNumber", equalTo(REDACTED_ACCT));
    }

    @Test
    public void testRedactPANFromArrayJson() {

        JsonRedactor redacter = new JsonRedactor(new JsonRedactionDirective("$.people[*].creditCardNumber", new PANRedactionFunction()));

        JsonAssert
                .with(redacter.redact(ARRAY_JSON))
                .assertThat("$.people[0].creditCardNumber", equalTo(REDACTED_CC))
                .assertThat("$.people[1].creditCardNumber", equalTo(REDACTED_CC))
                .assertThat("$.people[2].creditCardNumber", equalTo(REDACTED_CC));
    }

    @Test
    public void testMultipleRedactionsFromFlatJson() {

        JsonRedactor redacter = new JsonRedactor(
                new JsonRedactionDirective("$.accountNumber", new AccountNumberRedactionFunction()),
                new JsonRedactionDirective("$.socialSecurityNumber", new SSNRedactionFunction()),
                new JsonRedactionDirective("$.creditCardNumber", new PANRedactionFunction()));

        JsonAssert
                .with(redacter.redact(FLAT_JSON))
                .assertThat("$.accountNumber", equalTo(REDACTED_ACCT))
                .assertThat("$.socialSecurityNumber", equalTo(REDACTED_SSN))
                .assertThat("$.creditCardNumber", equalTo(REDACTED_CC));
    }
}
