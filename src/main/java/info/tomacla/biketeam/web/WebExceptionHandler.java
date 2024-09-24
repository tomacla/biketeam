package info.tomacla.biketeam.web;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class WebExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public RedirectView defaultErrorHandler(HttpServletRequest req, Exception e, RedirectAttributes attributes) {
        attributes.addFlashAttribute("error", e.getMessage());
        return new RedirectView("/");
    }


}
