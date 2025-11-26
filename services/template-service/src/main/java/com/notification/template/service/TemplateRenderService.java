package com.notification.template.service;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.notification.template.exception.TemplateRenderException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

@Slf4j
@Service
public class TemplateRenderService {

    private final MustacheFactory mustacheFactory = new DefaultMustacheFactory();

    public String render(String templateContent, Map<String, String> variables) {
        try {
            log.debug("Rendering template with variables: {}", variables.keySet());

            Mustache mustache = mustacheFactory.compile(new StringReader(templateContent), "template");

            StringWriter writer = new StringWriter();
            mustache.execute(writer, variables);
            writer.flush();

            String result = writer.toString();
            log.debug("Template rendered successfully");

            return result;
        } catch (Exception e) {
            log.error("Error rendering template: {}", e.getMessage(), e);
            throw new TemplateRenderException("Failed to render template", e);
        }
    }
}

