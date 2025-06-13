package mk.ukim.finki.wp.web;

import mk.ukim.finki.wp.model.FoodItem;
import mk.ukim.finki.wp.model.Vote;
import mk.ukim.finki.wp.model.VoteType;
import mk.ukim.finki.wp.repository.FoodItemRepository;
import mk.ukim.finki.wp.repository.VoteRepository;
import mk.ukim.finki.wp.service.FoodItemService;
import mk.ukim.finki.wp.service.VoteService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@Controller
public class UploadController {

    //koristime csvParser tuka
    private final FoodItemService foodItemService;
    private final VoteService voteService;

    public UploadController(FoodItemService foodItemService, VoteService voteService) {
        this.foodItemService = foodItemService;
        this.voteService = voteService;
    }

    //has role admin
    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, Model model) {
        String message = foodItemService.updateVotes(file);
        model.addAttribute("message", message);
        return "upload";
    }

    @GetMapping("/upload")
    public String showUploadForm() {
        return "upload";
    }

}
