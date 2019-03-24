package grondag.canvas.core;

import java.util.ArrayDeque;
import java.util.function.Consumer;

import grondag.canvas.buffering.DrawableDelegate;
import it.unimi.dsi.fastutil.Arrays;
import it.unimi.dsi.fastutil.Swapper;
import it.unimi.dsi.fastutil.ints.AbstractIntComparator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Accumulates and renders delegates in pipeline, buffer order.
 */
public class SolidRenderList implements Consumer<DrawableDelegate> {
    private static final ArrayDeque<SolidRenderList> POOL = new ArrayDeque<>();
    
    public static SolidRenderList claim() {
        SolidRenderList result = POOL.poll();
        if (result == null)
            result = new SolidRenderList();
        return result;
    }
    
    private final ObjectArrayList<DrawableDelegate>[] pipelineLists;

    private SolidRenderList() {
        final int size = PipelineManager.INSTANCE.pipelineCount();
        @SuppressWarnings("unchecked")
        ObjectArrayList<DrawableDelegate>[] buffers = new ObjectArrayList[size];
        for (int i = 0; i < size; i++) {
            buffers[i] = new ObjectArrayList<DrawableDelegate>();
        }
        this.pipelineLists = buffers;
    }

    public void draw() {
        for (ObjectArrayList<DrawableDelegate> list : this.pipelineLists) {
            renderListInBufferOrder(list);
        }
    }
    
    @SuppressWarnings("serial")
    private static class BufferSorter extends AbstractIntComparator implements Swapper {
        Object[] delegates;

        @Override
        public int compare(int a, int b) {
            return Integer.compare(((DrawableDelegate) delegates[a]).bufferId(),
                    ((DrawableDelegate) delegates[b]).bufferId());
        }

        @Override
        public void swap(int a, int b) {
            Object swap = delegates[a];
            delegates[a] = delegates[b];
            delegates[b] = swap;
        }
    };

    private static final ThreadLocal<BufferSorter> SORTERS = ThreadLocal.withInitial(BufferSorter::new);

    /**
     * Renders delegates in buffer order to minimize bind calls. 
     * Assumes all delegates in the list share the same pipeline.
     */
    private void renderListInBufferOrder(ObjectArrayList<DrawableDelegate> list) {
        final int limit = list.size();

        if (limit == 0)
            return;

        final Object[] delegates = list.elements();

        final BufferSorter sorter = SORTERS.get();
        sorter.delegates = delegates;
        Arrays.quickSort(0, limit, sorter, sorter);

        ((DrawableDelegate) delegates[0]).getPipeline().activate(true);

        int lastBufferId = -1;

        for (int i = 0; i < limit; i++) {
            final DrawableDelegate b = (DrawableDelegate) delegates[i];
            lastBufferId = b.bind(lastBufferId);
            b.draw();
        }
        list.clear();
    }
    
    public void release() {
        POOL.offer(this);
    }
    
    public void drawAndRelease() {
        draw();
        release();
    }
    
    @Override
    public void accept(DrawableDelegate d) {
        pipelineLists[d.getPipeline().getIndex()].add(d);
    }
}
