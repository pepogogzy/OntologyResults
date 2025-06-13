package mk.ukim.finki.wp.model;

// ne sum siguren ali mislam ni treba ova

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    //@JoinColumn(name = "food_item_id")
    private FoodItem food_item;

    @ManyToOne
    private User user;

    // maybe custom user mapping ako treba toa da cuvame?
    //private String foodName; //ova ne treba da postoi
    //private String preferredClass;

    @Enumerated(EnumType.STRING)
    private VoteType voteType;

    public Vote(VoteType voteType, FoodItem foodItem) {
        this.voteType = voteType;
        this.food_item = foodItem;
    }

    //user, timestamp??

}
