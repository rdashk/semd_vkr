package ru.isu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.isu.repository.FileSemdRepository;
import ru.isu.repository.SemdRepository;
import ru.isu.repository.UserRepository;

@Component
@Controller
public class PageController {

    @Autowired
    private SemdRepository semdRepository;

    @Autowired
    private FileSemdRepository fileSemdRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/home")
    public String start(Model model) {
        return "home";
    }

    @GetMapping("/all_semds")
    public String allSemds(Model model) {
        model.addAttribute("semds", semdRepository.findAll());
        return "all_semds";
    }

    @GetMapping("/files")
    public String getFiles(Model model) {
        model.addAttribute("files", fileSemdRepository.findAll());
        return "files";
    }

    @GetMapping("/users")
    public String getUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "users";
    }

    @GetMapping("/deleteSemd/{code}")
    public String semdDelete(@PathVariable("code") String code) {

        System.out.println("get semd name"+semdRepository.findSemdByCode(code));
        semdRepository.deleteSemdById(code);
        // delete all files from current semd code folder
        fileSemdRepository.deleteFileSemdsByCode(code);
        return "redirect:/all_semds";
    }
}
