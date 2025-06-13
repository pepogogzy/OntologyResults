package mk.ukim.finki.wp.service;

import mk.ukim.finki.wp.model.FoodItem;
import mk.ukim.finki.wp.model.Vote;
import mk.ukim.finki.wp.model.VoteType;

import java.util.Map;

//vizuelizacija spored user ili timestamp bi bila tuka
public interface VoteService {

    Vote addVote(VoteType voteType, String name);
    String checkVote(String name);
    long countVotesByTypeForFoodItem(FoodItem foodItem, VoteType voteType);


    Map<String, Double> getVoteDistribution();

    Map<String, Double> getOverallPrecision();

    Map<String, Double> getOverallPrecision2();

    Map<String, Long> getTrueFalsePositives();
}
