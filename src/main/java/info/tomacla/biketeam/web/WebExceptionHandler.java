package info.tomacla.biketeam.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ControllerAdvice
public class WebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(WebExceptionHandler.class);

    @ExceptionHandler(value = Exception.class)
    public ModelAndView defaultErrorHandler(HttpServletRequest req, HttpServletResponse res, Exception e, RedirectAttributes attributes) throws Exception {

        // les exceptions qui portent deja un statut HTTP (404 d'une equipe inconnue ou d'une ressource
        // statique absente, 405...) doivent produire ce statut, et pas la redirection vers l'accueil.
        // Ce sont des erreurs du client : pas de trace ERROR avec pile d'appel, sans quoi chaque
        // passage de scanner (/.env, /.git/HEAD...) pollue les logs.
        // On relance l'exception telle quelle : ExceptionHandlerExceptionResolver rend alors la main
        // aux resolvers suivants (ResponseStatusExceptionResolver, DefaultHandlerExceptionResolver),
        // qui appliquent le statut.
        if (e instanceof ErrorResponse) {
            throw e;
        }

        // sans cela l'exception d'origine n'apparait nulle part : seul l'echec de la redirection
        // qui suit est trace, et uniquement quand la reponse est deja committee
        log.error("Error while processing {} {}", req.getMethod(), req.getRequestURI(), e);

        // exception levee pendant le rendu du template : la reponse est deja partie, un redirect
        // leverait un IllegalStateException (Cannot call sendRedirect...) a la place
        if (res.isCommitted()) {
            return new ModelAndView();
        }

        attributes.addFlashAttribute("error", e.getMessage());
        return new ModelAndView(new RedirectView("/"));
    }

}
