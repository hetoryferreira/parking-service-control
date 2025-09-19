package com.estapar.parking.application.webhook;
import com.estapar.parking.application.request.EventTypeRequest;
import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebhookQualifier {
    EventTypeRequest value();
}