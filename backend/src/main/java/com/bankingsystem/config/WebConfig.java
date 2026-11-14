package com.bankingsystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Makes {@code static/pages/index.html} the live homepage of the app.
 * <p>
 * Spring Boot only auto-serves an {@code index.html} that sits directly
 * under {@code static/} when a browser requests "/". In this project
 * index.html lives one folder deeper, at {@code static/pages/index.html},
 * so hitting {@code http://localhost:8080/} previously produced Spring's
 * Whitelabel 404 error page instead of the site.
 * <p>
 * A <b>redirect</b> (not a forward) is used on purpose: it moves the
 * browser's address bar to {@code /pages/index.html}, so every relative
 * link on that page (login.html, register.html, admin-login.html, ...)
 * keeps resolving correctly against the {@code /pages/} folder they
 * actually live in.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "/pages/index.html");
        registry.addRedirectViewController("/index.html", "/pages/index.html");
    }
}
