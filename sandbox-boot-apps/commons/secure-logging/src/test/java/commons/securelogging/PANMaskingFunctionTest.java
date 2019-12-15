package commons.securelogging;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PANMaskingFunctionTest {

    private PANMaskingFunction subject;

    @Before
    public void setup() {
        subject = new PANMaskingFunction();
    }

    @Test
    public void testNumberWithHyphensRedaction() {
        Object masked = subject.apply("1111-2222-3333-4444");

        Assert.assertNotNull(masked);
        Assert.assertEquals("111122******4444", masked);
    }

    @Test
    public void testNumberWithSpacesRedaction() {
        Object masked = subject.apply("1111 2222 3333 4444");

        Assert.assertNotNull(masked);
        Assert.assertEquals("111122******4444", masked);
    }

    @Test
    public void testNumberWithoutSpacesOrHyphensRedaction() {
        Object masked = subject.apply("1111222233334444");

        Assert.assertNotNull(masked);
        Assert.assertEquals("111122******4444", masked);
    }

    @Test
    public void testNull() {
        Object masked = subject.apply(null);
        Assert.assertNull(masked);
    }
}
