package com.notification.template.metrics;

import com.notification.template.dto.RenderTemplateRequest;
import com.notification.template.dto.RenderTemplateResponse;
import com.notification.template.model.Channel;
import com.notification.template.model.Template;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class MetricsService {

    private final MeterRegistry meterRegistry;

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        log.info("MetricsService initialized");
    }

    public void templateCreated(String clientId, Channel channel) {
        Counter.builder("template_created_total")
                .tag("clientId", normalize(clientId))
                .tag("channel", normalize(channel.name().toLowerCase()))
                .register(meterRegistry)
                .increment();
    }

    public void renderMetrics(RenderTemplateRequest request, Timer.Sample timerSample, boolean success) {
        Counter.builder("template_render_total")
                .tag("clientId", request.getClientId())
                .tag("channel", request.getChannel().name())
                .tag("templateCode", request.getTemplateCode())
                .tag("status", success ? "success" : "error")
                .register(meterRegistry)
                .increment();

        timerSample.stop(Timer.builder("template_render_duration_seconds")
                .tag("clientId", normalize(request.getClientId()))
                .tag("channel", normalize(request.getChannel().name()))
                .register(meterRegistry));
    }

    public void templateCache() {
        Counter.builder("template_cache_hits_total")
                .register(meterRegistry)
                .increment();
    }

    public void templateMiss() {
        Counter.builder("template_cache_misses_total")
                .register(meterRegistry)
                .increment();
    }

    public Timer.Sample getRenderTimer() {
        return Timer.start(meterRegistry);
    }

    private String normalize(String valor) {
        if (valor == null)
            return "unknown";
        return valor.toLowerCase();
    }
}


