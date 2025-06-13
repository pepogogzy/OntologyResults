package mk.ukim.finki.wp.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
public class FoodItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private String ourClass;
    private String foodKGClass;

    @OneToMany(mappedBy = "food_item", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Vote> votes = new ArrayList<>();

    public FoodItem(String name, String ourClass, String foodKGClass) {
        this.name = name;
        this.ourClass = ourClass;
        this.foodKGClass = foodKGClass;
    }

}
