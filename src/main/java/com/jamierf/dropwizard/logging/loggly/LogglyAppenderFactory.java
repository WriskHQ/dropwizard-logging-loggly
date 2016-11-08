package com.jamierf.dropwizard.logging.loggly;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Layout;
import ch.qos.logback.ext.loggly.LogglyBatchAppender;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.net.HostAndPort;
import io.dropwizard.logging.AbstractAppenderFactory;
import io.dropwizard.logging.async.AsyncAppenderFactory;
import io.dropwizard.logging.filter.LevelFilterFactory;
import io.dropwizard.logging.layout.LayoutFactory;
import net.logstash.logback.composite.FormattedTimestampJsonProvider;
import net.logstash.logback.layout.LogstashLayout;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Map;

import static com.jamierf.dropwizard.logging.loggly.AppenderFactoryHelper.getCustomFieldsFromMap;

/**
 * <p>An {@link io.dropwizard.logging.AppenderFactory} implementation which provides an appender that writes events to Loggly.</p>
 * <b>Configuration Parameters:</b>
 * <table summary="Configuration">
 * <tr>
 * <td>Name</td>
 * <td>Default</td>
 * <td>Description</td>
 * </tr>
 * <tr>
 * <td>{@code type}</td>
 * <td><b>REQUIRED</b></td>
 * <td>The appender type. Must be {@code loggly}.</td>
 * </tr>
 * <tr>
 * <td>{@code threshold}</td>
 * <td>{@code ALL}</td>
 * <td>The lowest level of events to write to the server.</td>
 * </tr>
 * <tr>
 * <td>{@code server}</td>
 * <td>{@code logs-01.loggly.com}</td>
 * <td>The Loggly server.</td>
 * </tr>
 * <tr>
 * <td>{@code token}</td>
 * <td><b>REQUIRED</b></td>
 * <td>Your Loggly customer token.</td>
 * </tr>
 * <tr>
 * <td>{@code tag}</td>
 * <td>the application name</td>
 * <td>The Loggly tag.</td>
 * </tr>
 * <tr>
 * <td>{@code logFormat}</td>
 * <td>the default format</td>
 * <td>
 * The Logback pattern with which events will be formatted. See
 * <a href="http://logback.qos.ch/manual/layouts.html#conversionWord">the Logback documentation</a>
 * for details.
 * </td>
 * </tr>
 * </table>
 *
 * @see io.dropwizard.logging.AbstractAppenderFactory
 */
@JsonTypeName("loggly")
public class LogglyAppenderFactory extends AbstractAppenderFactory<ILoggingEvent> {

    private static final String ISO_8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final String ENDPOINT_URL_TEMPLATE = "https://%s/bulk/%s/tag/%s";

    @NotNull
    @JsonProperty
    private HostAndPort server = HostAndPort.fromString("logs-01.loggly.com");

    @NotEmpty
    @JsonProperty
    private String token;

    @JsonProperty
    private String tag;

    @JsonProperty
    private Map<String, String> customFields;


    public HostAndPort getServer() {
        return server;
    }

    public void setServer(final HostAndPort server) {
        this.server = server;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(final String tag) {
        this.tag = tag;
    }

    public Map<String, String> getCustomFields() {
        return customFields;
    }

    public void setCustomFields(Map<String, String> customFields) {
        this.customFields = customFields;
    }

    protected Layout<ILoggingEvent> buildJsonLayout(LoggerContext context, LayoutFactory<ILoggingEvent> layoutFactory) {
        LogstashLayout formatter = new LogstashLayout();
        formatter.setContext(context);
        if (customFields != null) {
            formatter.setCustomFields(getCustomFieldsFromMap(customFields));
        }
        formatter.start();

        formatter.setTimeZone("UTC");
        //as per https://www.loggly.com/docs/automated-parsing/#json
        formatter.getProviders().getProviders().stream().filter(p-> p instanceof FormattedTimestampJsonProvider).forEach(x->((FormattedTimestampJsonProvider)x).setPattern(ISO_8601_FORMAT));
        formatter.getFieldNames().setTimestamp("timestamp");

        formatter.start();
        return formatter;
    }

    @Override
    public Appender<ILoggingEvent> build(LoggerContext context, String applicationName, LayoutFactory<ILoggingEvent> layoutFactory,
                                         LevelFilterFactory<ILoggingEvent> levelFilterFactory, AsyncAppenderFactory<ILoggingEvent> asyncAppenderFactory) {
        final LogglyBatchAppender<ILoggingEvent> appender = new LogglyBatchAppender<>();

        final String tagName = tag != null ? tag : applicationName;

        appender.setName("loggly-appender");
        appender.setContext(context);
        appender.setEndpointUrl(String.format(ENDPOINT_URL_TEMPLATE, server, token, tagName));
        appender.setLayout(buildJsonLayout(context, layoutFactory));
        appender.addFilter(levelFilterFactory.build(threshold));
        appender.start();

        return appender;
    }
}
