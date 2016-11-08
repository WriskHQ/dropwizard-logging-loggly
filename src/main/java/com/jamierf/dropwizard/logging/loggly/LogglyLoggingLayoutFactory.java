package com.jamierf.dropwizard.logging.loggly;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.classic.spi.ILoggingEvent;
import net.logstash.logback.layout.CompositeJsonLayout;
import net.logstash.logback.layout.LogstashAccessLayout;
import net.logstash.logback.layout.LogstashLayout;

import java.util.Map;

import static com.jamierf.dropwizard.logging.loggly.AppenderFactoryHelper.getCustomFieldsFromMap;

public class LogglyLoggingLayoutFactory implements LogglyLayoutFactory<ILoggingEvent> {

    @Override
    public CompositeJsonLayout<ILoggingEvent> createLayout(Map<String, String> customFields) {
        LogstashLayout layout = new LogstashLayout();
        if (customFields != null) {
            layout.setCustomFields(getCustomFieldsFromMap(customFields));
        }

        //as per https://www.loggly.com/docs/automated-parsing/#json
        layout.setTimeZone("UTC");
        setTimestampFormat(layout);
        layout.getFieldNames().setTimestamp("timestamp");

        return layout;
    }


}
