package ru.isu.controller;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.isu.model.Answer;
import ru.isu.model.TableItem;
import ru.isu.service.SenderToRabbitMQ;

import java.util.ArrayList;
import java.util.List;

import static ru.isu.model.RabbitQueue.WEB_MESSAGE;

@Component
@Controller
public class PageController {
    List<TableItem> semds;

    private final SenderToRabbitMQ sender;

    public PageController(SenderToRabbitMQ sender) {
        this.sender = sender;
    }

    /*@GetMapping("/home")
    public String home(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
        model.addAttribute("name", name);
        return "home";
        //return "redirect:/appointments";
    }*/

    @GetMapping("/home")
    public String start(Model model) {
        sender.send(WEB_MESSAGE, new Answer(model.toString(),"/listSEMD"));
        model.addAttribute("name", "Dasha");
        return "home";
        //return "redirect:/appointments";
    }

    @GetMapping("/all_semds")
    public String allSemds(Model model) {
        model.addAttribute("semds", semds);
        return "all_semds";
    }

    public void setAllSemds(String message) {
        var arr = message.split("!");
        this.semds = new ArrayList<>();
        for (var item: arr) {
            this.semds.add(new TableItem(item.split("\\}")));
        }
    }
}
