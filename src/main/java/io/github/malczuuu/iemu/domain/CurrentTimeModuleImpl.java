package io.github.malczuuu.iemu.domain;

import io.github.malczuuu.iemu.util.CancelAwareTimerTask;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.function.Consumer;

class CurrentTimeModuleImpl implements CurrentTimeModule {

  private final List<Consumer<Instant>> onCurrentTimeChange = new ArrayList<>();

  private long diff = 0;
  private String timeZone = TimeZone.getDefault().getID();
  private String utcOffset = new SimpleDateFormat("X").format(Calendar.getInstance().getTime());

  private final Timer timer;

  CurrentTimeModuleImpl(Timer timer) {
    this.timer = timer;
    timer.schedule(
        new CancelAwareTimerTask(
            () -> {
              Instant now = getCurrentTime();
              new ArrayList<>(onCurrentTimeChange).forEach(c -> c.accept(now));
            }),
        1000 - Instant.now().toEpochMilli() % 1000,
        1000);
  }

  @Override
  public void setCurrentTime(Instant currentTime) {
    diff = Instant.now().toEpochMilli() - currentTime.toEpochMilli();
    Instant now = getCurrentTime();
    onCurrentTimeChange.forEach(c -> c.accept(now));
  }

  @Override
  public void setTimeZone(String timeZone) {
    this.timeZone = timeZone;
  }

  @Override
  public void setUTCOffset(String utcOffset) {
    this.utcOffset = utcOffset;
  }

  @Override
  public Instant getCurrentTime() {
    return Instant.now().minusMillis(diff).truncatedTo(ChronoUnit.SECONDS);
  }

  @Override
  public String getTimeZone() {
    return timeZone;
  }

  @Override
  public String getUTCOffset() {
    return utcOffset;
  }

  @Override
  public void shutdown() {
    timer.cancel();
    timer.purge();
  }

  @Override
  public void subscribeOnCurrentTimeChange(Consumer<Instant> consumer) {
    onCurrentTimeChange.add(consumer);
  }
}
