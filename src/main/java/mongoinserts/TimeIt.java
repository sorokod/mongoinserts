package mongoinserts;

import java.util.concurrent.TimeUnit;

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
        return timeIt(block, TimeUnit.MILLISECONDS);
    }


    public static long timeIt(CodeBlock block, TimeUnit timeUnit) throws Exception {
        long start = System.nanoTime();
        block.invoke();
        long stop = System.nanoTime();
        return timeUnit.convert(stop - start, TimeUnit.NANOSECONDS);
    }


    public static double tps(long millis, long calls) {
        return calls * ((double) 1_000 / millis);
    }
}
