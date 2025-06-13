package mk.ukim.finki.wp.repository;

import mk.ukim.finki.wp.model.FoodItem;
import mk.ukim.finki.wp.model.User;
import mk.ukim.finki.wp.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    //Optional<Vote> findByFoodName(String name);
    @Query("SELECT v.food_item.name, v.voteType, COUNT(v) " +
            "FROM Vote v GROUP BY v.food_item.name, v.voteType")
    List<Object[]> countVotesByFoodItemAndVoteType();

    @Query("SELECT v FROM Vote v WHERE v.user = :user AND v.food_item = :foodItem")
    Optional<Vote> findByUserAndFoodItem(@Param("user") User user, @Param("foodItem") FoodItem foodItem);

    @Query("SELECT v FROM Vote v WHERE v.food_item = :foodItem")
    List<Vote> findByFoodItem(@Param("foodItem") FoodItem foodItem);
}
