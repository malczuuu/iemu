package io.github.malczuuu.iemu.domain;

import java.time.Instant;
import java.util.function.Consumer;

public interface CurrentTimeModule {

  void setCurrentTime(Instant currentTime);

  void setTimeZone(String timeZone);

  void setUTCOffset(String utcOffset);

  Instant getCurrentTime();

  String getTimeZone();

  String getUTCOffset();

  void shutdown();

  void subscribeOnCurrentTimeChange(Consumer<Instant> consumer);
}
