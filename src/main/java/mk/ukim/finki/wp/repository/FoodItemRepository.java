package mk.ukim.finki.wp.repository;

import mk.ukim.finki.wp.model.FoodItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FoodItemRepository extends JpaRepository<FoodItem, Integer> {
    Optional<FoodItem> findByName(String name);

    List<FoodItem> findByNameContainingIgnoreCase(String searchTerm);
}