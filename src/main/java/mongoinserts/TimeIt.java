package mongoinserts;

import java.util.concurrent.TimeUnit;


/**
 * Created by David Soroko on 22/01/2016.
 */
public class TimeIt {

    @FunctionalInterface
    public interface CodeBlock {
        void invoke() throws Exception;
    }

    public static double throughput(int numberOfInvocations, CodeBlock block) throws Exception {
        long duration = timeIt(block);
        return numberOfInvocations * (1_000.0 / duration);
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
}
