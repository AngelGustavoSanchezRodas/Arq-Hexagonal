package com.navaja.navajagtbackend.services;

import com.navaja.navajagtbackend.dto.OpenGraphData;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Locale;

@Service
public class OpenGraphService {

    private static final int TIMEOUT_MILLIS = 3000;

    public OpenGraphData extract(String url) {
        try {
            Connection.Response response = Jsoup.connect(url)
                    .timeout(TIMEOUT_MILLIS)
                    .ignoreContentType(true)
                    .followRedirects(true)
                    .execute();

            String contentType = response.contentType();
            if (contentType == null || !contentType.toLowerCase(Locale.ROOT).contains("text/html")) {
                return OpenGraphData.empty();
            }

            Document document = response.parse();
            return new OpenGraphData(
                    emptyToNull(document.title()),
                    metaContent(document, "og:title"),
                    metaContent(document, "og:description"),
                    metaContent(document, "og:image")
            );
        } catch (HttpStatusException | SocketTimeoutException | UnknownHostException exception) {
            return OpenGraphData.empty();
        } catch (IOException exception) {
            return OpenGraphData.empty();
        }
    }

    private String metaContent(Document document, String property) {
        Element element = document.selectFirst("meta[property=" + property + "]");
        if (element == null) {
            return null;
        }
        return emptyToNull(element.attr("content"));
    }

    private String emptyToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}

