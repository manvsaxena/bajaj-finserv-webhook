package com.webhook.service;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
public class NthLevelFollowersService {
    
    private final Gson gson;
    
    public String findNthLevelFollowers(Map<String, Object> data) {
        Map<String, Object> usersData = (Map<String, Object>) data.get("users");
        int n = ((Number) usersData.get("n")).intValue();
        int findId = ((Number) usersData.get("findId")).intValue();
        List<Map<String, Object>> users = (List<Map<String, Object>>) usersData.get("users");
        
        // Build graph
        Map<Integer, List<Integer>> graph = new HashMap<>();
        for (Map<String, Object> user : users) {
            int userId = ((Number) user.get("id")).intValue();
            List<Number> follows = (List<Number>) user.get("follows");
            
            List<Integer> followsList = follows.stream()
                .map(Number::intValue)
                .collect(java.util.stream.Collectors.toList());
                
            graph.put(userId, followsList);
        }
        
        // Find nth level followers using BFS
        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        queue.offer(findId);
        visited.add(findId);
        
        int currentLevel = 0;
        while (!queue.isEmpty() && currentLevel < n) {
            int levelSize = queue.size();
            
            for (int i = 0; i < levelSize; i++) {
                int currentId = queue.poll();
                
                for (Integer followId : graph.getOrDefault(currentId, new ArrayList<>())) {
                    if (!visited.contains(followId)) {
                        visited.add(followId);
                        queue.offer(followId);
                    }
                }
            }
            
            currentLevel++;
        }
        
        List<Integer> nthLevelFollowers = new ArrayList<>(queue);
        Collections.sort(nthLevelFollowers);
        
        Map<String, Object> result = Map.of(
            "regNo", "REG12347",
            "outcome", nthLevelFollowers
        );
        
        return gson.toJson(result);
    }
} 