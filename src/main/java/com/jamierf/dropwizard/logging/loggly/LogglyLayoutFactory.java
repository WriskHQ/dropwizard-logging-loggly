package com.jamierf.dropwizard.logging.loggly;

import ch.qos.logback.core.spi.DeferredProcessingAware;
import net.logstash.logback.composite.FormattedTimestampJsonProvider;
import net.logstash.logback.layout.CompositeJsonLayout;

import java.util.Map;

public interface LogglyLayoutFactory<E extends DeferredProcessingAware> {

    String ISO_8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    CompositeJsonLayout<E> createLayout(Map<String, String> customFields);

    default void setTimestampFormat(CompositeJsonLayout<E> layout) {
        layout.getProviders().getProviders()
                .stream()
                .filter(p -> p instanceof FormattedTimestampJsonProvider)
                .forEach(x -> ((FormattedTimestampJsonProvider) x).setPattern(ISO_8601_FORMAT));
    }
}
