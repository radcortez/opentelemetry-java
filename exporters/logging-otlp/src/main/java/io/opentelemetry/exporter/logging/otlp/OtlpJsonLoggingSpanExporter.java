/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SegmentedStringWriter;
import io.opentelemetry.exporter.otlp.internal.traces.ResourceSpansMarshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link SpanExporter} which writes {@linkplain SpanData spans} to a {@link Logger} in OTLP JSON
 * format. Each log line will include a single {@code ResourceSpans}.
 */
public final class OtlpJsonLoggingSpanExporter implements SpanExporter {

  private static final Logger logger =
      Logger.getLogger(OtlpJsonLoggingSpanExporter.class.getName());

  /** Returns a new {@link OtlpJsonLoggingSpanExporter}. */
  public static SpanExporter create() {
    return new OtlpJsonLoggingSpanExporter();
  }

  private OtlpJsonLoggingSpanExporter() {}

  @Override
  public CompletableResultCode export(Collection<SpanData> spans) {
    ResourceSpansMarshaler[] allResourceSpans = ResourceSpansMarshaler.create(spans);
    for (ResourceSpansMarshaler resourceSpans : allResourceSpans) {
      SegmentedStringWriter sw =
          new SegmentedStringWriter(JsonUtil.JSON_FACTORY._getBufferRecycler());
      try (JsonGenerator gen = JsonUtil.create(sw)) {
        resourceSpans.writeJsonTo(gen);
      } catch (IOException e) {
        // Shouldn't happen in practice, just skip it.
        continue;
      }
      logger.log(Level.INFO, sw.getAndClear());
    }
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode shutdown() {
    return CompletableResultCode.ofSuccess();
  }
}
