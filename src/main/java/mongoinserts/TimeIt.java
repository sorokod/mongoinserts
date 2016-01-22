package mongoinserts;

import static java.lang.System.currentTimeMillis;

/**
 * Created by David Soroko on 22/01/2016.
 */
public class TimeIt {

    @FunctionalInterface
    public interface CodeBlock {
        void invoke() throws Exception;
    }

    public static long timeIt(CodeBlock block) throws Exception {
        long start = currentTimeMillis();
        block.invoke();
        return currentTimeMillis() - start;
    }


    public static double tps(long millis, long calls) {
        return calls * ((double) 1_000 / millis);
    }
}
