package ru.isu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.isu.model.db.SemdPackage;
import ru.isu.repository.SemdRepository;
import ru.isu.repository.UserRepository;

@Component
@Controller
public class PageController {

    @Autowired
    private SemdRepository semdRepository;

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

    @GetMapping("/all_semds/file/{code}")
    public String getFiles(@PathVariable("code") String code, Model model) {
        SemdPackage semd = semdRepository.getFileNamesByCode(code);
        //System.out.println(semd);
        model.addAttribute("semdName", semd.getName());
        model.addAttribute("code", semd.getId());
        model.addAttribute("files", semd.getFiles());
        return "files";
    }

    @GetMapping("/users")
    public String getUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "users";
    }

    @GetMapping("/deleteSemd/{code}")
    public String semdDelete(@PathVariable("code") String code) {
        semdRepository.deleteById(code);
        return "redirect:/all_semds";
    }
}
