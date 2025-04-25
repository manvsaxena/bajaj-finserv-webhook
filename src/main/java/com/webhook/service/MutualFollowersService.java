package com.webhook.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MutualFollowersService {
    
    private final Gson gson;
    
    public String findMutualFollowers(JsonObject data) {
        try {
            System.out.println("Processing mutual followers with data: " + data);
            
            // Extract users array from the data
            JsonArray usersArray = data.getAsJsonArray("users");
            
            // Process users
            Map<Integer, Set<Integer>> followsMap = new HashMap<>();
            
            // Build follows map
            for (JsonElement userElement : usersArray) {
                JsonObject user = userElement.getAsJsonObject();
                int userId = user.get("id").getAsInt();
                
                // Get follows array
                JsonArray followsArray = user.getAsJsonArray("follows");
                Set<Integer> followSet = new HashSet<>();
                
                // Convert follows array to Set
                for (JsonElement followElement : followsArray) {
                    followSet.add(followElement.getAsInt());
                }
                
                followsMap.put(userId, followSet);
            }
            
            // Find mutual followers
            List<List<Integer>> mutualPairs = new ArrayList<>();
            Set<String> processedPairs = new HashSet<>();
            
            for (Map.Entry<Integer, Set<Integer>> entry : followsMap.entrySet()) {
                int userId = entry.getKey();
                Set<Integer> userFollows = entry.getValue();
                
                for (Integer followedId : userFollows) {
                    String pairKey = Math.min(userId, followedId) + "-" + Math.max(userId, followedId);
                    
                    if (!processedPairs.contains(pairKey) && 
                        followsMap.containsKey(followedId) && 
                        followsMap.get(followedId).contains(userId)) {
                        
                        mutualPairs.add(Arrays.asList(
                            Math.min(userId, followedId),
                            Math.max(userId, followedId)
                        ));
                        processedPairs.add(pairKey);
                    }
                }
            }
            
            // Sort pairs for consistent output
            mutualPairs.sort((a, b) -> {
                int compare = a.get(0).compareTo(b.get(0));
                return compare != 0 ? compare : a.get(1).compareTo(b.get(1));
            });
            
            // Create result
            JsonObject result = new JsonObject();
            result.addProperty("regNo", "REG12347");
            result.add("outcome", gson.toJsonTree(mutualPairs));
            
            return result.toString();
            
        } catch (Exception e) {
            System.err.println("Error processing mutual followers: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to process mutual followers", e);
        }
    }
} 