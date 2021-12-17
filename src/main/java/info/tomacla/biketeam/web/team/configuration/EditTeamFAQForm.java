package info.tomacla.biketeam.web.team.configuration;

import info.tomacla.biketeam.common.datatype.Strings;

public class EditTeamFAQForm {

    private String markdownPage = "";

    public static EditTeamPageFormBuilder builder() {
        return new EditTeamPageFormBuilder();
    }

    public String getMarkdownPage() {
        return markdownPage;
    }

    public void setMarkdownPage(String markdownPage) {
        this.markdownPage = Strings.requireNonBlankOrDefault(markdownPage, "");
    }

    public EditTeamPageFormParser parser() {
        return new EditTeamPageFormParser(this);
    }

    public static class EditTeamPageFormParser {

        private final EditTeamFAQForm form;

        protected EditTeamPageFormParser(EditTeamFAQForm form) {
            this.form = form;
        }

        public String getMarkdownPage() {
            return Strings.requireNonBlankOrNull(form.getMarkdownPage());
        }

    }

    public static class EditTeamPageFormBuilder {

        private final EditTeamFAQForm form;

        protected EditTeamPageFormBuilder() {
            this.form = new EditTeamFAQForm();
        }

        public EditTeamPageFormBuilder withMarkdownPage(String markdownPage) {
            form.setMarkdownPage(markdownPage);
            return this;
        }

        public EditTeamFAQForm get() {
            return form;
        }

    }

}
