package mk.ukim.finki.wp.service;

import mk.ukim.finki.wp.model.FoodItem;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FoodItemService {
    List<FoodItem> findAll();
    Optional<FoodItem> findById(Integer id);
    String updateVotes(MultipartFile file);

    List<FoodItem> searchItems(String searchTerm);
}