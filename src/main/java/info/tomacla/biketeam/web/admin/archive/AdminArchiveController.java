package info.tomacla.biketeam.web.admin.archive;

import info.tomacla.biketeam.common.FileExtension;
import info.tomacla.biketeam.domain.global.SiteConfiguration;
import info.tomacla.biketeam.domain.global.SiteDescription;
import info.tomacla.biketeam.domain.global.SiteIntegration;
import info.tomacla.biketeam.service.ArchiveService;
import info.tomacla.biketeam.service.FacebookService;
import info.tomacla.biketeam.service.FileService;
import info.tomacla.biketeam.service.MapService;
import info.tomacla.biketeam.web.AbstractController;
import info.tomacla.biketeam.web.admin.configuration.EditSiteConfigurationForm;
import info.tomacla.biketeam.web.admin.configuration.EditSiteDescriptionForm;
import info.tomacla.biketeam.web.admin.configuration.EditSiteIntegrationForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "/admin")
public class AdminArchiveController extends AbstractController {

    @Autowired
    private ArchiveService archiveService;

    @GetMapping(value = "/archives")
    public String listArchives(@RequestParam(value = "archive", required = false) String archive,
            Principal principal, Model model) {

        if(archive != null) {
            archiveService.importArchive(archive);
        }

        addGlobalValues(principal, model, "Administration - Archives");
        model.addAttribute("archives", archiveService.listArchives());

        return "admin_archives";
    }


}
