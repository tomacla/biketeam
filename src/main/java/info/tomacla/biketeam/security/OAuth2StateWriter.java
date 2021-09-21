package info.tomacla.biketeam.security;

import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Base64;

public class OAuth2StateWriter extends Base64StringKeyGenerator {

    private final StringKeyGenerator generator = new Base64StringKeyGenerator(Base64.getUrlEncoder());

    @Override
    public String generateKey() {
        HttpServletRequest currentHttpRequest = getCurrentHttpRequest();
        if (currentHttpRequest != null) {
            String referer = currentHttpRequest.getHeader("Referer");
            if (!ObjectUtils.isEmpty(referer)) {
                return generator.generateKey()
                        + OAuth2SuccessHandler.SEPARATOR
                        + referer;
            }
        }
        return generator.generateKey();
    }

    private HttpServletRequest getCurrentHttpRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) requestAttributes).getRequest();
        }
        return null;
    }
}