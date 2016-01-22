package mongoinserts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.lang.Long.MAX_VALUE;
import static java.util.Spliterators.spliterator;

/**
 * Support for converting streams of E into streams of List<E>, i.e. batching
 *
 * Created by David Soroko on 22/01/2016.
 *
 */
public class BatchingSpliterator<E> implements Spliterator<List<E>> /* extends AbstractSpliterator<List<E>> */ {
    private final Spliterator<E> delegate;
    private int characteristics;
    private final int batchSize;
    private final int splitSize;
    private long estimatedSize;


    BatchingSpliterator(Spliterator<E> delegate, int batchSize, int splitSize) {

        setCharacteristics(delegate.characteristics());

        this.delegate = delegate;
        this.batchSize = batchSize;
        this.splitSize = splitSize;

        long est = delegate.estimateSize();
        estimatedSize = est == MAX_VALUE ? MAX_VALUE : est / batchSize + (est % batchSize > 0 ? 1 : 0);
    }

    @Override
    public boolean tryAdvance(Consumer<? super List<E>> consumer) {
        final ArrayList<E> batch = new ArrayList<>(batchSize);

        while (delegate.tryAdvance(batch::add) && batch.size() < batchSize) ;

        if (batch.isEmpty()) {
            return false;
        }
        consumer.accept(batch);
        return true;
    }

    @Override
    public Spliterator<List<E>> trySplit() {
        final HoldingConsumer<List<E>> holder = new HoldingConsumer<>();
        if (!tryAdvance(holder)) return null;
        final Object[] a = new Object[splitSize];
        int j = 0;

        do a[j] = holder.value; while (++j < splitSize && tryAdvance(holder));

        if (estimatedSize != Long.MAX_VALUE) {
            estimatedSize -= j;
        }
        return spliterator(a, 0, j, characteristics());
    }


    @Override
    public long estimateSize() {
        return estimatedSize;
    }

    @Override
    public int characteristics() {
        return characteristics;
    }

    private void setCharacteristics(int incoming) {
        incoming = ((incoming & Spliterator.SIZED) != 0)
                ? incoming | Spliterator.SUBSIZED
                : incoming;
        this.characteristics = incoming | Spliterator.NONNULL;
    }

    private static final class HoldingConsumer<T> implements Consumer<T> {
        Object value;

        @Override
        public void accept(T value) {
            this.value = value;
        }
    }

    // #######################################
    public static class Builder<E> {
        private Spliterator<E> spliterator;
        private int batchSize;
        private int splitSize = 1;

        public Builder wrap(Stream<E> stream) {
            if (stream == null) {
                throw new IllegalArgumentException("stream can't be null");
            }
            this.spliterator = stream.spliterator();
            return this;
        }

        public Builder wrap(Collection<E> collection) {
            if (collection == null) {
                throw new IllegalArgumentException("stream can't be null");
            }
            this.spliterator = collection.stream().spliterator();
            return this;
        }

        public Builder batchSize(int batchSize) {
            if (batchSize <= 0) {
                throw new IllegalArgumentException("Batch size must be > 0, was: " + batchSize);
            }
            this.batchSize = batchSize;
            return this;
        }

        public Builder splitSize(int splitSize) {
            if (splitSize < 1) {
                throw new IllegalArgumentException("Split size must be >= 0, was: " + splitSize);
            }
            this.splitSize = splitSize;
            return this;
        }

        public Stream<List<E>> stream() {
            BatchingSpliterator split = new BatchingSpliterator<>(spliterator, batchSize, splitSize);
            return StreamSupport.stream(split, false);
        }

    }
}
