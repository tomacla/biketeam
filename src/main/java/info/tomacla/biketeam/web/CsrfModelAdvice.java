package info.tomacla.biketeam.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Expose le jeton CSRF aux templates FreeMarker sous le nom {@code _csrf}.
 * <p>
 * On n'active surtout pas spring.freemarker.expose-request-attributes : AbstractTemplateView
 * leve une ServletException des qu'un attribut de requete et un attribut de modele portent le
 * meme nom. Le jeton vaut null sur les requetes non protegees : les templates utilisent donc
 * {@code <#if _csrf??>}.
 */
@ControllerAdvice
public class CsrfModelAdvice {

    @ModelAttribute("_csrf")
    public CsrfToken csrfToken(HttpServletRequest request) {
        return (CsrfToken) request.getAttribute(CsrfToken.class.getName());
    }

}
