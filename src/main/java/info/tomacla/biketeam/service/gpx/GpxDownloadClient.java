package info.tomacla.biketeam.service.gpx;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;

public class GpxDownloadClient {

    public static void downloadGPX(File targetFile, String gpxUrl) {

        try {
            RestTemplate template = new RestTemplate(requestFactory());
            template.execute(gpxUrl, HttpMethod.GET, null, (clientHttpResponse) -> StreamUtils.copy(clientHttpResponse.getBody(), new FileOutputStream(targetFile)));
        } catch (Exception e) {
            throw new RuntimeException("Unable to download GPX", e);
        }

    }

    // FIXME disable https
    private static ClientHttpRequestFactory requestFactory() {

        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .setExpectContinueEnabled(true)
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCookieStore(new BasicCookieStore())
                .setDefaultRequestConfig(defaultRequestConfig)
                .setSSLHostnameVerifier(new NoopHostnameVerifier())
                .build();

        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }
}
