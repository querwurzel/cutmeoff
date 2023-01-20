package com.wilke.cutmeoff;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Enumeration;
import java.util.concurrent.CompletableFuture;

@Controller
@RequestMapping("/")
public class Powerswitch {

    private static final Logger log = LoggerFactory.getLogger(Powerswitch.class);

    private final ApplicationContext applicationContext;

    private final Instant uptime = Instant.now();

    @Autowired
    public Powerswitch(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @GetMapping
    protected void handOverSwitch(HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.TEXT_HTML_VALUE);

        final ServletOutputStream out = response.getOutputStream();
        out.println("<!DOCTYPE html>");
        out.println("<html><head>");
        out.println("<title>Shutdown Console</title>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1'/>");
        out.println("<meta http-equiv='Content-Type' content='text/html; charset=utf-8'/>");
        out.println("<link rel='icon' type='image/svg+xml' href='/favicon.svg'>");
        out.println("<link rel='stylesheet' href='/power.css'/>");
        out.println("</head><body>");
        out.println("<form method='post' action='/'>");
        out.println("<label title='" + uptime.toString() + "'>Uptime: " + ChronoUnit.HOURS.between(uptime, Instant.now()) + "h</label>");
        out.println("<input type='submit' value='Shut down server!'>");
        out.println("</form>");
        out.println("</body></html>");
        out.flush();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    protected void cutMeOff(HttpServletRequest request) {
        final String remoteAddr = request.getHeader("X-Forwarded-For");
        final String reverseProxy = request.getRemoteAddr();
        log.info("{} cut me off after {}h.",
                remoteAddr == null ? reverseProxy : remoteAddr,
                ChronoUnit.HOURS.between(uptime, Instant.now())
        );

        final Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            log.info("{}: {}", header, request.getHeader(header));
        }

        CompletableFuture.runAsync(() -> {
            SpringApplication.exit(applicationContext, () -> 0);
            System.exit(0);
        });
    }
}
