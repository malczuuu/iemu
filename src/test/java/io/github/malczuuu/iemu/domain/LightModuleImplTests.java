package io.github.malczuuu.iemu.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.malczuuu.iemu.mock.TimerTaskMock;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.xml.ws.Holder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LightModuleImplTests {

  private final List<TimerTask> tasks = new ArrayList<>();
  private final Timer timer = mock(Timer.class);

  private final LightModuleImpl lightModule = new LightModuleImpl(false, timer);

  @BeforeEach
  void beforeEach() {
    doAnswer(i -> tasks.add(i.getArgumentAt(0, TimerTask.class)))
        .when(timer)
        .schedule(any(TimerTask.class), anyLong(), anyLong());
  }

  @Test
  void shouldOnTimeBeUpdatedAfterOnWasSetToTrue() {
    lightModule.setOn(true);
    tasks.forEach(TimerTaskMock::run);

    assertTrue(lightModule.isOn());
    assertEquals(1, lightModule.getOnTime());
  }

  @Test
  void shouldOnTimeNotBeUpdatedAfterOnWasSetToFalse() {
    lightModule.setOn(true);
    tasks.forEach(TimerTaskMock::run);
    tasks.forEach(TimerTaskMock::run);
    lightModule.setOn(false);
    tasks.forEach(TimerTaskMock::run);
    tasks.forEach(TimerTaskMock::run);
    tasks.forEach(TimerTaskMock::run);

    assertFalse(lightModule.isOn());
    assertEquals(2, lightModule.getOnTime());
  }

  @Test
  void shouldBeNotifiedAfterOnWasChanged() {
    Holder<Boolean> holder = new Holder<>(false);
    lightModule.subscribeOnStateChange(b -> holder.value = b);

    lightModule.setOn(true);

    assertTrue(holder.value);
  }

  @Test
  void shouldBeNotifiedAfterOnTimeChanged() {
    Holder<Long> holder = new Holder<>(-1L);
    lightModule.subscribeOnTimeCounterChange(b -> holder.value = b);

    lightModule.setOn(true);
    tasks.forEach(TimerTaskMock::run);

    assertEquals(1L, holder.value.longValue());
  }

  @Test
  void shouldBeNotifiedAfterDimmerChanged() {
    Holder<Integer> holder = new Holder<>(-1);
    lightModule.subscribeOnDimmerChange(b -> holder.value = b);

    lightModule.setDimmer(45);

    assertEquals(45, holder.value.intValue());
  }

  @Test
  void shouldDimmerBeSetToZeroIfNegativeUsed() {
    lightModule.setDimmer(-1);

    assertEquals(0, lightModule.getDimmer());
  }

  @Test
  void shouldDimmerBeSetTo100IfBiggerThan100Used() {
    lightModule.setDimmer(101);

    assertEquals(100, lightModule.getDimmer());
  }

  @Test
  void shouldTimerBeCanceledAndPurgedOnShutdown() {
    Holder<Boolean> canceled = new Holder<>(false);
    Holder<Boolean> purged = new Holder<>(false);
    doAnswer(i -> canceled.value = true).when(timer).cancel();
    when(timer.purge())
        .thenAnswer(
            i -> {
              purged.value = true;
              return tasks.size();
            });
    lightModule.setOn(true);
    tasks.forEach(TimerTaskMock::run);
    tasks.forEach(TimerTaskMock::run);
    tasks.forEach(TimerTaskMock::run);
    lightModule.setOn(false);
    tasks.forEach(TimerTaskMock::run);
    lightModule.setOn(true);

    lightModule.shutdown();

    assertTrue(canceled.value);
    assertTrue(purged.value);
    assertTrue(tasks.size() > 0);
    tasks.forEach(TimerTaskMock::isCanceled);
  }

  // TODO: test if subscriptions were cancelled
}
