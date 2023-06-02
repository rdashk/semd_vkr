package ru.isu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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

    /*@GetMapping("/home")
    public String home(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
        model.addAttribute("name", name);
        return "home";
        //return "redirect:/appointments";
    }*/

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
}
