package mk.ukim.finki.wp.web;

import mk.ukim.finki.wp.model.FoodItem;
import mk.ukim.finki.wp.model.Vote;
import mk.ukim.finki.wp.model.VoteType;
import mk.ukim.finki.wp.service.FoodItemService;
import mk.ukim.finki.wp.service.VoteService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/items")
public class FoodItemController {
    private final FoodItemService foodItemService;
    private final VoteService voteService;

    public FoodItemController(FoodItemService foodItemService, VoteService voteService) {
        this.foodItemService = foodItemService;
        this.voteService = voteService;
    }

    @GetMapping("/all")
    public String getAllFoodItems(@RequestParam(value = "searchTerm", required = false) String searchTerm, Model model) {
        List<FoodItem> foodItems;
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            foodItems = foodItemService.searchItems(searchTerm);
            model.addAttribute("searchTerm", searchTerm);
        } else {
            foodItems = foodItemService.findAll();
        }

        Map<Integer, Map<VoteType, Long>> voteCounts = new HashMap<>();
        Map<Integer, String> hasVoted = new HashMap<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        for (FoodItem item : foodItems) {
            // Calculate vote counts for each item
            Map<VoteType, Long> counts = item.getVotes().stream()
                    .collect(Collectors.groupingBy(Vote::getVoteType, Collectors.counting()));
            voteCounts.put(item.getId(), counts);

            // Check if the current user has voted for this item
            if (authentication != null && authentication.isAuthenticated()) {
                String vote = voteService.checkVote(item.getName());
                hasVoted.put(item.getId(), vote != null ? vote : null);
            } else {
                hasVoted.put(item.getId(), null);
            }
        }

        if (authentication != null && authentication.isAuthenticated()) {
            for (var authority : authentication.getAuthorities()) {
                System.out.println(authority.getAuthority()); // e.g., "ROLE_USER", "ROLE_ADMIN"
            }
        }

        model.addAttribute("foodItems", foodItems);
        model.addAttribute("voteCounts", voteCounts);
        model.addAttribute("hasVoted", hasVoted);

        return "food-items";
    }

    @GetMapping("/{id}")
    public String getFoodItemById(@PathVariable String id, Model model) {
        return foodItemService.findById(Integer.parseInt(id))
                .map(foodItem -> {
                    Map<VoteType, Long> counts = foodItem.getVotes().stream()
                            .collect(Collectors.groupingBy(Vote::getVoteType, Collectors.counting()));
                    long totalVotes = counts.values().stream().mapToLong(Long::longValue).sum(); // Fixed totalVotes

                    String hasVoted = voteService.checkVote(foodItem.getName());

                    model.addAttribute("foodItem", foodItem);
                    model.addAttribute("voteCounts", counts);
                    model.addAttribute("totalVotes", totalVotes);
                    model.addAttribute("hasVoted", hasVoted);

                    return "food-item-details";
                })
                .orElseGet(() -> {
                    model.addAttribute("error", "Food item not found");
                    return "food-item-details";
                });
    }

    @PostMapping("/{id}/vote")
    public String submitVote(@PathVariable String id, @RequestParam("voteType") String voteType, Model model) {
        try {
            int foodItemId = Integer.parseInt(id);
            Optional<FoodItem> foodItemOpt = foodItemService.findById(foodItemId);
            if (foodItemOpt.isPresent()) {
                FoodItem foodItem = foodItemOpt.get();
                VoteType voteTypeEnum = VoteType.valueOf(voteType);
                voteService.addVote(voteTypeEnum, foodItem.getName());
                return "redirect:/items/" + id;
            } else {
                model.addAttribute("error", "Food item not found");
                return "food-item-details";
            }
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Invalid request");
            return "food-item-details";
        }
    }


    @PostMapping("/{id}/allVote")
    public String submitAllVote(@PathVariable String id, @RequestParam("voteType") String voteType, Model model) {
        try {
            int foodItemId = Integer.parseInt(id);
            Optional<FoodItem> foodItemOpt = foodItemService.findById(foodItemId);
            if (foodItemOpt.isPresent()) {
                FoodItem foodItem = foodItemOpt.get();
                VoteType voteTypeEnum = VoteType.valueOf(voteType);
                voteService.addVote(voteTypeEnum, foodItem.getName());
                return "redirect:/items/all";
            } else {
                model.addAttribute("error", "Food item not found");
                return "food-item-details";
            }
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Invalid request");
            return "food-item-details";
        }
    }

    @GetMapping("/precision")
    public String showPrecision(Model model) {
        Map<String, Double> precisionMap = voteService.getOverallPrecision();
        model.addAttribute("precisionOur", precisionMap.get("OUR"));
        model.addAttribute("precisionFoodkg", precisionMap.get("FOODKG"));
        return "precision";
    }

    @GetMapping("/statistics")
    public String showStatistics(Model model) {
        Map<String, Double> distribution = voteService.getVoteDistribution();
        Map<String, Double> precision = voteService.getOverallPrecision2();
        Map<String, Long> tpfp = voteService.getTrueFalsePositives();
        System.out.println("Distribution: " + distribution);
        System.out.println("Precision: " + precision);
        System.out.println("TP/FP before addAttribute: " + tpfp);
        if (tpfp == null) {
            tpfp = new HashMap<>();
            System.out.println("TP/FP was null, set to empty map");
        }
        model.addAttribute("distribution", distribution);
        model.addAttribute("precision", precision);
        model.addAttribute("tpfp", tpfp);
        System.out.println("TP/FP after addAttribute: " + model.getAttribute("tpfp"));
        return "statistics";
    }
}