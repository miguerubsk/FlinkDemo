package org.myorg.quickstart.triggers;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.triggers.TriggerResult;
import org.apache.flink.streaming.api.windowing.windows.Window;

/**
 * Disparador personalizado que se activa cuando:
 * 1. Se alcanza un número determinado de elementos (count)
 * 2. Pasa un tiempo determinado (timeout)
 */
public class CountOrTimeoutTrigger<W extends Window> extends Trigger<Object, W> {
    private final long maxCount;
    private final long timeoutMs;

    private final ValueStateDescriptor<Long> countDescriptor = new ValueStateDescriptor<>("count", Types.LONG);
    private final ValueStateDescriptor<Long> timerDescriptor = new ValueStateDescriptor<>("timer", Types.LONG);

    public CountOrTimeoutTrigger(long maxCount, long timeoutMs) {
        this.maxCount = maxCount;
        this.timeoutMs = timeoutMs;
    }

    @Override
    public TriggerResult onElement(Object element, long timestamp, W window, TriggerContext ctx) throws Exception {
        ValueState<Long> countState = ctx.getPartitionedState(countDescriptor);
        ValueState<Long> timerState = ctx.getPartitionedState(timerDescriptor);

        Long currentCount = countState.value();
        if (currentCount == null) {
            currentCount = 0L;
        }
        currentCount++;
        countState.update(currentCount);

        // Si es el primer elemento, configuramos el timer de timeout
        if (currentCount == 1) {
            long nextTimer = ctx.getCurrentProcessingTime() + timeoutMs;
            ctx.registerProcessingTimeTimer(nextTimer);
            timerState.update(nextTimer);
        }

        if (currentCount >= maxCount) {
            return fireAndPurge(ctx, countState, timerState);
        }

        return TriggerResult.CONTINUE;
    }

    @Override
    public TriggerResult onProcessingTime(long time, W window, TriggerContext ctx) throws Exception {
        ValueState<Long> countState = ctx.getPartitionedState(countDescriptor);
        ValueState<Long> timerState = ctx.getPartitionedState(timerDescriptor);

        // El timer ha saltado por tiempo
        return fireAndPurge(ctx, countState, timerState);
    }

    @Override
    public TriggerResult onEventTime(long time, W window, TriggerContext ctx) throws Exception {
        return TriggerResult.CONTINUE;
    }

    @Override
    public void clear(W window, TriggerContext ctx) throws Exception {
        ctx.getPartitionedState(countDescriptor).clear();
        Long timer = ctx.getPartitionedState(timerDescriptor).value();
        if (timer != null) {
            ctx.deleteProcessingTimeTimer(timer);
        }
        ctx.getPartitionedState(timerDescriptor).clear();
    }

    private TriggerResult fireAndPurge(TriggerContext ctx, ValueState<Long> countState, ValueState<Long> timerState)
            throws Exception {
        countState.clear();
        Long timer = timerState.value();
        if (timer != null) {
            ctx.deleteProcessingTimeTimer(timer);
        }
        timerState.clear();
        return TriggerResult.FIRE_AND_PURGE;
    }

    public static <W extends Window> CountOrTimeoutTrigger<W> of(long maxCount, long timeoutMs) {
        return new CountOrTimeoutTrigger<>(maxCount, timeoutMs);
    }
}
