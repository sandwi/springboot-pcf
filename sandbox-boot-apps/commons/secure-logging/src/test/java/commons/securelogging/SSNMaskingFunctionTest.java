package commons.securelogging;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SSNMaskingFunctionTest {

    private SSNMaskingFunction subject;

    @Before
    public void setup() {
        subject = new SSNMaskingFunction();
    }

    @Test
    public void testNumberWithHyphensRedaction() {
        Object masked = subject.apply("111-22-3333");

        Assert.assertNotNull(masked);
        Assert.assertEquals("*****3333", masked);
    }

    @Test
    public void testNumberWithSpacesRedaction() {
        Object masked = subject.apply("111-22-3333");

        Assert.assertNotNull(masked);
        Assert.assertEquals("*****3333", masked);
    }

    @Test
    public void testNumberWithoutSpacesOrHyphensRedaction() {
        Object masked = subject.apply("111223333");

        Assert.assertNotNull(masked);
        Assert.assertEquals("*****3333", masked);
    }

    @Test
    public void testNull() {
        Object masked = subject.apply(null);
        Assert.assertNull(masked);
    }
}
