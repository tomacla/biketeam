package info.tomacla.biketeam.web.admin.archive;

import info.tomacla.biketeam.service.ArchiveService;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
@RequestMapping(value = "/admin")
public class AdminArchiveController extends AbstractController {

    @Autowired
    private ArchiveService archiveService;

    @GetMapping(value = "/archives")
    public String listArchives(@RequestParam(value = "archive", required = false) String archive,
                               Principal principal, Model model) {

        if (archive != null) {
            archiveService.importArchive(archive);
        }

        addGlobalValues(principal, model, "Administration - Archives", null);
        model.addAttribute("archives", archiveService.listArchives());

        return "admin_archives";
    }


}
