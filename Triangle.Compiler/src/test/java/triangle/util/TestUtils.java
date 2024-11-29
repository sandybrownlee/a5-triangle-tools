package triangle.util;

import java.io.InputStream;

public final class TestUtils {

    // generates an ad-hoc input stream that reads from a snippet of provided source
    public static InputStream inputStreamOf(String sourceCode) {
        return new InputStream() {
            private int index = 0;

            @Override public int read() {
                if (index == sourceCode.length()) {
                    return -1;
                }

                return sourceCode.charAt(index++);
            }
        };
    }

    private TestUtils() {
        throw new IllegalStateException("Utility class");
    }

}
