package info.tomacla.biketeam.service.gpx;

import jakarta.annotation.PostConstruct;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@Service
public class GpxDownloadClient {

    private RestTemplate template;

    @PostConstruct
    protected void buildRestTemplate() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        template = new RestTemplate(requestFactory());
    }

    public void downloadGPX(File targetFile, String gpxUrl) {

        try {
            template.execute(gpxUrl, HttpMethod.GET, null, (clientHttpResponse) -> StreamUtils.copy(clientHttpResponse.getBody(), new FileOutputStream(targetFile)));
        } catch (Exception e) {
            throw new RuntimeException("Unable to download GPX", e);
        }

    }

    // FIXME disable https
    private static ClientHttpRequestFactory requestFactory() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultRequestConfig(
                        RequestConfig.custom()
                                .setExpectContinueEnabled(true)
                                .build()
                )
                .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                        .setSSLSocketFactory(SSLConnectionSocketFactoryBuilder.create()
                                .setSslContext(SSLContextBuilder.create()
                                        .loadTrustMaterial(TrustAllStrategy.INSTANCE)
                                        .build())
                                .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                                .build())
                        .build())
                .build();

        return new HttpComponentsClientHttpRequestFactory(httpclient);
    }
}
