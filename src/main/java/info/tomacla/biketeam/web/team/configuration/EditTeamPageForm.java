package info.tomacla.biketeam.web.team.configuration;

import info.tomacla.biketeam.common.datatype.Strings;

public class EditTeamPageForm {

    private String markdownPage;

    public EditTeamPageForm() {
        setMarkdownPage(null);
    }

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

        private final EditTeamPageForm form;

        protected EditTeamPageFormParser(EditTeamPageForm form) {
            this.form = form;
        }

        public String getMarkdownPage() {
            return form.getMarkdownPage();
        }

    }

    public static class EditTeamPageFormBuilder {

        private final EditTeamPageForm form;

        protected EditTeamPageFormBuilder() {
            this.form = new EditTeamPageForm();
        }

        public EditTeamPageFormBuilder withMarkdownPage(String markdownPage) {
            form.setMarkdownPage(markdownPage);
            return this;
        }

        public EditTeamPageForm get() {
            return form;
        }

    }

}
