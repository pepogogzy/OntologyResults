package mk.ukim.finki.wp.service.impl;

import mk.ukim.finki.wp.model.FoodItem;
import mk.ukim.finki.wp.model.User;
import mk.ukim.finki.wp.model.Vote;
import mk.ukim.finki.wp.model.VoteType;
import mk.ukim.finki.wp.repository.FoodItemRepository;
import mk.ukim.finki.wp.repository.VoteRepository;
import mk.ukim.finki.wp.service.UserService;
import mk.ukim.finki.wp.service.VoteService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VoteServiceImpl implements VoteService {

    private final VoteRepository voteRepository;
    private final FoodItemRepository foodItemRepository;
    private final UserService userService;

    public VoteServiceImpl(VoteRepository voteRepository, FoodItemRepository foodItemRepository, UserService userService) {
        this.voteRepository = voteRepository;
        this.foodItemRepository = foodItemRepository;
        this.userService = userService;
    }

    @Override
    public Vote addVote(VoteType voteType, String name) {
        FoodItem foodItem = foodItemRepository.findByName(name).orElseThrow(); //ne bi trebalo da bide vo else
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Vote vote = new Vote(voteType, foodItem);

        if (authentication != null && authentication.isAuthenticated()) {
            String username;
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                username = ((UserDetails) principal).getUsername();
            } else {
                username = principal.toString();
            }

            User user = userService.findByUsername(username);

            if(voteRepository.findByUserAndFoodItem(user, foodItem).isEmpty()) {
                vote.setUser(user);
                return voteRepository.save(vote);
            }

            // Set the user to the vote
            //vote.setUser(user);

            // Save vote (assuming you have a voteService)
            //return voteRepository.save(vote);
        }
        return vote;//voteRepository.save(vote);
    }

    @Override
    public String checkVote(String name) {
        FoodItem foodItem = foodItemRepository.findByName(name).orElseThrow();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //Vote vote = new Vote(voteType, foodItem);

        if (authentication != null && authentication.isAuthenticated()) {
            String username;
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                username = ((UserDetails) principal).getUsername();
            } else {
                username = principal.toString();
            }

            User user = userService.findByUsername(username);
            if(voteRepository.findByUserAndFoodItem(user, foodItem).isPresent()) { // ako ima glasano
                return voteRepository.findByUserAndFoodItem(user, foodItem).orElseThrow().getVoteType().toString();
            }
        }
        return "";
    }

    @Override
    public long countVotesByTypeForFoodItem(FoodItem foodItem, VoteType voteType) {
        return foodItem.getVotes().stream()
                .filter(vote -> vote.getVoteType() == voteType)
                .count();
    }
    @Override
    public Map<String, Double> getVoteDistribution() {
        List<FoodItem> allFoodItems = foodItemRepository.findAll();
        List<Vote> allVotes = voteRepository.findAll();
        long totalItems = allFoodItems.size();
        if (totalItems == 0) {
            return Map.of("OUR", 0.0, "BOTH", 0.0, "NONE", 0.0, "FOODKG", 0.0);
        }

        Map<VoteType, Long> categoryCounts = new HashMap<>();
        categoryCounts.put(VoteType.OUR, 0L);
        categoryCounts.put(VoteType.FOODKG, 0L);
        categoryCounts.put(VoteType.BOTH, 0L);
        categoryCounts.put(VoteType.NONE, 0L);

        for (FoodItem foodItem : allFoodItems) {
            VoteType majority = getMajorityVoteType(foodItem);
            categoryCounts.put(majority, categoryCounts.getOrDefault(majority, 0L) + 1);
        }
        System.out.println(categoryCounts);

        double percentOur = (double) categoryCounts.getOrDefault(VoteType.OUR, 0L) / totalItems * 100;
        double percentBoth = (double) categoryCounts.getOrDefault(VoteType.BOTH, 0L) / totalItems * 100;
        double percentNone = (double) categoryCounts.getOrDefault(VoteType.NONE, 0L) / totalItems * 100;
        double percentFoodkg = (double) categoryCounts.getOrDefault(VoteType.FOODKG, 0L) / totalItems * 100;

        Map<String, Double> distribution = new HashMap<>();
        distribution.put("OUR", percentOur);
        distribution.put("BOTH", percentBoth);
        distribution.put("NONE", percentNone);
        distribution.put("FOODKG", percentFoodkg);
        return distribution;
    }

    private VoteType getMajorityVoteType(FoodItem foodItem) {
        List<Vote> votes = voteRepository.findByFoodItem(foodItem);
        if (votes.isEmpty()) {
            return VoteType.NONE;
        }
        Map<VoteType, Long> counts = votes.stream()
                .collect(Collectors.groupingBy(Vote::getVoteType, Collectors.counting()));
        long maxCount = counts.values().stream().mapToLong(Long::longValue).max().orElse(0);
        List<VoteType> maxTypes = counts.entrySet().stream()
                .filter(e -> e.getValue() == maxCount)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        if (maxTypes.size() > 1) {
            return VoteType.NONE;
        } else {
            return maxTypes.get(0);
        }
    }
    @Override
    public Map<String, Double> getOverallPrecision() {
        List<Vote> votes = voteRepository.findAll();
        Map<FoodItem, Map<VoteType, Long>> voteCounts = votes.stream()
                .filter(v -> v.getVoteType() == VoteType.OUR || v.getVoteType() == VoteType.FOODKG)
                .collect(Collectors.groupingBy(
                        Vote::getFood_item,
                        Collectors.groupingBy(Vote::getVoteType, Collectors.counting())
                ));

        int tpOur = 0, fpOur = 0;
        int tpTheir = 0, fpTheir = 0;

        for (Vote vote : votes) {
            if (vote.getFood_item() == null || (vote.getVoteType() != VoteType.OUR && vote.getVoteType() != VoteType.FOODKG)) {
                continue;
            }
            Map<VoteType, Long> counts = voteCounts.getOrDefault(vote.getFood_item(), Map.of());
            VoteType majority = counts.isEmpty() ? VoteType.NONE :
                    counts.entrySet().stream()
                            .filter(e -> e.getValue().equals(Collections.max(counts.values())))
                            .count() > 1 ? VoteType.NONE :
                            Collections.max(counts.entrySet(), Map.Entry.comparingByValue()).getKey();

            if (vote.getVoteType() == VoteType.OUR) {
                if (majority == VoteType.OUR) tpOur++;
                else fpOur++;
            } else if (vote.getVoteType() == VoteType.FOODKG) {
                if (majority == VoteType.FOODKG) tpTheir++;
                else fpTheir++;
            }
        }

        double precisionOur = (tpOur + fpOur) == 0 ? 0 : (double) tpOur / (tpOur + fpOur);
        double precisionTheir = (tpTheir + fpTheir) == 0 ? 0 : (double) tpTheir / (tpTheir + fpTheir);

        Map<String, Double> precisionMap = new HashMap<>();
        precisionMap.put("OUR", precisionOur);
        precisionMap.put("FOODKG", precisionTheir);
        return precisionMap;
    }
    @Override
    public Map<String, Double> getOverallPrecision2() {
        Map<String, Double> distribution = getVoteDistribution();
        double precisionOur = (distribution.getOrDefault("OUR", 0.0) + distribution.getOrDefault("BOTH", 0.0)) / 100.0;
        double precisionFoodkg = (distribution.getOrDefault("FOODKG", 0.0) + distribution.getOrDefault("BOTH", 0.0)) / 100.0;
        Map<String, Double> precisionMap = new HashMap<>();
        precisionMap.put("OUR", precisionOur);
        precisionMap.put("FOODKG", precisionFoodkg);
        return precisionMap;
    }

    @Override
    public Map<String, Long> getTrueFalsePositives() {
        List<FoodItem> allFoodItems = foodItemRepository.findAll();
        long totalItems = allFoodItems.size();
        if (totalItems == 0) {
            return Map.of("TP_OUR", 0L, "FP_OUR", 0L, "TP_FOODKG", 0L, "FP_FOODKG", 0L);
        }

        long tpOur = 0, fpOur = 0, tpFoodkg = 0, fpFoodkg = 0;

        for (FoodItem foodItem : allFoodItems) {
            VoteType majority = getMajorityVoteType(foodItem);
            if (majority == VoteType.OUR || majority == VoteType.BOTH) {
                tpOur++;
            } else if (majority == VoteType.FOODKG || majority == VoteType.NONE) {
                fpOur++;
            }
            if (majority == VoteType.FOODKG || majority == VoteType.BOTH) {
                tpFoodkg++;
            } else if (majority == VoteType.OUR || majority == VoteType.NONE) {
                fpFoodkg++;
            }
        }

        Map<String, Long> result = new HashMap<>();
        result.put("TP_OUR", tpOur);
        result.put("FP_OUR", fpOur);
        result.put("TP_FOODKG", tpFoodkg);
        result.put("FP_FOODKG", fpFoodkg);
        return result;
    }

}
