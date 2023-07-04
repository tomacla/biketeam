package info.tomacla.biketeam.web.admin.images;

import info.tomacla.biketeam.common.file.FileRepositories;
import info.tomacla.biketeam.service.file.FileService;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping(value = "/admin/images")
public class AdminImagesController extends AbstractController {

    @Autowired
    private FileService fileService;

    @GetMapping
    public String getLogo(@ModelAttribute("error") String error,
                          Principal principal, Model model) {

        addGlobalValues(principal, model, "Administration - Images", null);
        return "admin_images";
    }

    @PostMapping(value = "/home")
    public String updateHomeLogo(Principal principal, Model model,
                                 RedirectAttributes attributes,
                                 @RequestParam("file") MultipartFile file) {


        try {

            fileService.storeFile(file.getInputStream(),
                    FileRepositories.MISC_IMAGES,
                    "home.png"
            );

            addGlobalValues(principal, model, "Administration - Images", null);
            return "admin_images";

        } catch (Exception e) {
            addGlobalValues(principal, model, "Administration - Images", null);
            attributes.addFlashAttribute("error", e.getMessage());
            return "admin_images";
        }

    }

    @PostMapping(value = "/favicon")
    public String updateFaviconLogo(Principal principal, Model model,
                                    RedirectAttributes attributes,
                                    @RequestParam("file") MultipartFile file) {


        try {

            fileService.storeFile(file.getInputStream(),
                    FileRepositories.MISC_IMAGES,
                    "favicon.png"
            );

            addGlobalValues(principal, model, "Administration - Images", null);
            return "admin_images";

        } catch (Exception e) {
            addGlobalValues(principal, model, "Administration - Images", null);
            attributes.addFlashAttribute("error", e.getMessage());
            return "admin_images";
        }

    }

    @PostMapping(value = "/logo")
    public String updateNavbarLogo(Principal principal, Model model,
                                   RedirectAttributes attributes,
                                   @RequestParam("file") MultipartFile file) {


        try {

            fileService.storeFile(file.getInputStream(),
                    FileRepositories.MISC_IMAGES,
                    "logo.png"
            );

            addGlobalValues(principal, model, "Administration - Images", null);
            return "admin_images";

        } catch (Exception e) {
            addGlobalValues(principal, model, "Administration - Images", null);
            attributes.addFlashAttribute("error", e.getMessage());
            return "admin_images";
        }

    }


}
