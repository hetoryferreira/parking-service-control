package com.estapar.parking.application.webhook;

import com.estapar.parking.application.request.EventTypeRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
@Slf4j
@Component
public class WebhookCommandFactory {

    private final Map<EventTypeRequest, WebhookCommand> registry =
            new EnumMap<>(EventTypeRequest.class);

    public WebhookCommandFactory(List<WebhookCommand> commands) {
        for (var cmd : commands) {
            var anno = AnnotatedElementUtils.findMergedAnnotation(cmd.getClass(), WebhookQualifier.class);
            if (anno == null) throw new IllegalStateException("Missing @WebhookQualifier on " + cmd.getClass().getName());
            var prev = registry.putIfAbsent(anno.value(), cmd);
            if (prev != null) throw new IllegalStateException("Duplicate handler for " + anno.value());
        }
        log.info("WebhookCommandFactory ready: {}", registry.keySet());
    }

    public WebhookCommand get(EventTypeRequest type) {
        var cmd = registry.get(type);
        if (cmd == null) throw new IllegalArgumentException("Unsupported event: " + type);
        return cmd;
    }
}
