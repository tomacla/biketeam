package info.tomacla.biketeam.web;

import info.tomacla.biketeam.domain.team.Team;
import org.springframework.web.servlet.view.RedirectView;

public class ViewHandler {

    public String redirect(Team team, String suffix) {
        return createRedirect(team, suffix);
    }

    public RedirectView redirectView(Team team, String suffix) {
        return createRedirectView(team, suffix);
    }

    protected RedirectView createRedirectView(Team team, String suffix) {
        return new RedirectView(getViewName(team, suffix));
    }

    protected String createRedirect(Team team, String suffix) {
        return "redirect:" + getViewName(team, suffix);
    }

    protected String getViewName(Team team, String suffix) {
        if (team.getConfiguration().isDomainConfigured()) {
            return suffix;
        }
        return "/" + team.getId() + suffix;
    }

}
