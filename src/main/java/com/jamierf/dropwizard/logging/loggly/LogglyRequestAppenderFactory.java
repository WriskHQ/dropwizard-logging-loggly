package com.jamierf.dropwizard.logging.loggly;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Layout;
import ch.qos.logback.ext.loggly.LogglyBatchAppender;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.filter.FilteringGeneratorDelegate;
import com.fasterxml.jackson.core.filter.TokenFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HostAndPort;
import io.dropwizard.logging.AbstractAppenderFactory;
import io.dropwizard.logging.async.AsyncAppenderFactory;
import io.dropwizard.logging.filter.LevelFilterFactory;
import io.dropwizard.logging.layout.LayoutFactory;
import net.logstash.logback.composite.GlobalCustomFieldsJsonProvider;
import net.logstash.logback.composite.JsonProviders;
import net.logstash.logback.composite.accessevent.*;
import net.logstash.logback.decorate.JsonGeneratorDecorator;
import net.logstash.logback.layout.AccessEventCompositeJsonLayout;
import net.logstash.logback.layout.LogstashAccessLayout;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

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
 * @see AbstractAppenderFactory
 */
@JsonTypeName("loggly-request")
public class LogglyRequestAppenderFactory extends AbstractAppenderFactory<IAccessEvent> {

    private static final String ENDPOINT_URL_TEMPLATE = "https://%s/bulk/%s/tag/%s";

    @NotNull
    @JsonProperty
    private HostAndPort server = HostAndPort.fromString("logs-01.loggly.com");

    @NotEmpty
    @JsonProperty
    private String token;

    @NotNull
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

    protected Layout<IAccessEvent> buildJsonLayout(LoggerContext context, LayoutFactory<IAccessEvent> layoutFactory) {
        LogstashAccessLayout formatter = new LogstashAccessLayout();
        formatter.setContext(context);

        if (customFields != null) {
            formatter.setCustomFields(getCustomFieldsFromMap(customFields));
        }
        formatter.start();
        return formatter;
    }

    public static String getCustomFieldsFromMap(Map<String, String> map) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public Appender<IAccessEvent> build(LoggerContext context, String applicationName, LayoutFactory<IAccessEvent> layoutFactory,
                                        LevelFilterFactory<IAccessEvent> levelFilterFactory, AsyncAppenderFactory<IAccessEvent> asyncAppenderFactory) {
        final LogglyBatchAppender<IAccessEvent> appender = new LogglyBatchAppender<>();

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
