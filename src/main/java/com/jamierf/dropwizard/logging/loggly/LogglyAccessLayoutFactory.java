package com.jamierf.dropwizard.logging.loggly;

import ch.qos.logback.access.spi.IAccessEvent;
import com.jamierf.dropwizard.logging.loggly.LogglyAppenderFactory;
import com.jamierf.dropwizard.logging.loggly.LogglyLayoutFactory;
import net.logstash.logback.composite.FormattedTimestampJsonProvider;
import net.logstash.logback.layout.CompositeJsonLayout;
import net.logstash.logback.layout.LogstashAccessLayout;

import java.util.Map;

import static com.jamierf.dropwizard.logging.loggly.AppenderFactoryHelper.getCustomFieldsFromMap;

public class LogglyAccessLayoutFactory implements LogglyLayoutFactory<IAccessEvent> {

    @Override
    public CompositeJsonLayout<IAccessEvent> createLayout(Map<String, String> customFields) {
        LogstashAccessLayout layout = new LogstashAccessLayout();
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
