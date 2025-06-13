package mk.ukim.finki.wp.service.impl;

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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FoodItemServiceImpl implements FoodItemService {
    private final FoodItemRepository foodItemRepository;
    private final VoteRepository voteRepository;
    private final VoteService voteService;

    public FoodItemServiceImpl(FoodItemRepository foodItemRepository, VoteRepository voteRepository, VoteService voteService) {
        this.foodItemRepository = foodItemRepository;
        this.voteRepository = voteRepository;
        this.voteService = voteService;
    }

    @Override
    public List<FoodItem> findAll() {
        return foodItemRepository.findAll();
    }

    @Override
    public Optional<FoodItem> findById(Integer id) {
        return foodItemRepository.findById(id);
    }

    @Override
    public String updateVotes(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.Builder.create()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setDelimiter('\t')
                     .build())) {

            for (CSVRecord record : csvParser) {
                String name = record.get("Name").trim();
                String ourClass = record.get("Our USDA Class Name").trim();
                String foodKGClass = record.get("FoodKG USDA Class Name").trim();
                int ourVoteCount = Integer.parseInt(record.get("Our Mapping votes").trim());
                int theirVoteCount = Integer.parseInt(record.get("FoodKG Mapping votes").trim());

                FoodItem foodItem = foodItemRepository.findByName(name)
                        .orElseGet(() -> {
                            FoodItem item = new FoodItem();
                            item.setName(name);
                            item.setOurClass(ourClass);
                            item.setFoodKGClass(foodKGClass);
                            return foodItemRepository.save(item);
                        });

                Map<VoteType, Long> currentVotes = foodItem.getVotes().stream()
                        .collect(Collectors.groupingBy(Vote::getVoteType, Collectors.counting()));

                long currentOur = currentVotes.getOrDefault(VoteType.OUR, 0L);
                long currentTheir = currentVotes.getOrDefault(VoteType.FOODKG, 0L);

                //System.out.println(name + " " + ourVoteCount);

                if (currentOur == ourVoteCount && currentTheir == theirVoteCount) {
                    continue;
                }

                foodItem.getVotes().clear(); // orphanRemoval = true will delete old votes

                for (int i = 0; i < ourVoteCount; i++) {
                    foodItem.getVotes().add(new Vote(VoteType.OUR, foodItem));
                }
                for (int i = 0; i < theirVoteCount; i++) {
                    foodItem.getVotes().add(new Vote(VoteType.FOODKG, foodItem));
                }

                foodItemRepository.save(foodItem);
                //System.out.println("Votes in foodItem: " + foodItem.getVotes().size());
                //System.out.println("Saved foodItem ID: " + foodItem.getId());
            }

            return "Upload successful!";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    @Override
    public List<FoodItem> searchItems(String searchTerm) {
        return foodItemRepository.findByNameContainingIgnoreCase(searchTerm);
    }


}