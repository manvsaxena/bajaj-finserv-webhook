import com.google.gson.Gson;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class MutualFollowersService {
    private static final String REG_NO = "REG12347";

    public String findMutualFollowers(Map<String, Object> input) {
        try {
            // Extract users from input
            List<Map<String, Object>> users = (List<Map<String, Object>>) input.get("users");
            
            // Find mutual followers
            List<List<Integer>> mutualPairs = findMutualPairs(users);
            
            // Create response
            Map<String, Object> response = new HashMap<>();
            response.put("regNo", REG_NO);
            response.put("outcome", mutualPairs);
            
            return new Gson().toJson(response);
        } catch (Exception e) {
            throw new RuntimeException("Error processing mutual followers: " + e.getMessage(), e);
        }
    }

    private List<List<Integer>> findMutualPairs(List<Map<String, Object>> users) {
        // Create adjacency map for efficient lookup
        Map<Integer, Set<Integer>> followsMap = createFollowsMap(users);
        
        // Store results
        List<List<Integer>> mutualPairs = new ArrayList<>();
        Set<String> processedPairs = new HashSet<>();
        
        // Find mutual followers
        for (Map<String, Object> user : users) {
            int userId = ((Number) user.get("id")).intValue();
            List<Number> follows = (List<Number>) user.get("follows");
            
            for (Number followId : follows) {
                int followedId = followId.intValue();
                
                // Create unique pair identifier (smaller ID first)
                String pairKey = Math.min(userId, followedId) + "-" + Math.max(userId, followedId);
                
                // Skip if we've already processed this pair
                if (processedPairs.contains(pairKey)) {
                    continue;
                }
                
                // Check if there's a mutual follow
                if (followsMap.containsKey(followedId) && 
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
        
        return mutualPairs;
    }

    private Map<Integer, Set<Integer>> createFollowsMap(List<Map<String, Object>> users) {
        Map<Integer, Set<Integer>> followsMap = new HashMap<>();
        
        for (Map<String, Object> user : users) {
            int userId = ((Number) user.get("id")).intValue();
            List<Number> follows = (List<Number>) user.get("follows");
            
            // Convert follows list to Set for O(1) lookup
            Set<Integer> followSet = follows.stream()
                .map(Number::intValue)
                .collect(Collectors.toSet());
                
            followsMap.put(userId, followSet);
        }
        
        return followsMap;
    }

    // Test method to verify the implementation
    public static void main(String[] args) {
        // Create test input
        Map<String, Object> input = new HashMap<>();
        List<Map<String, Object>> users = new ArrayList<>();

        // User 1: Alice
        Map<String, Object> alice = new HashMap<>();
        alice.put("id", 1);
        alice.put("name", "Alice");
        alice.put("follows", Arrays.asList(2, 3));
        users.add(alice);

        // User 2: Bob
        Map<String, Object> bob = new HashMap<>();
        bob.put("id", 2);
        bob.put("name", "Bob");
        bob.put("follows", Arrays.asList(1));
        users.add(bob);

        // User 3: Charlie
        Map<String, Object> charlie = new HashMap<>();
        charlie.put("id", 3);
        charlie.put("name", "Charlie");
        charlie.put("follows", Arrays.asList(4));
        users.add(charlie);

        // User 4: David
        Map<String, Object> david = new HashMap<>();
        david.put("id", 4);
        david.put("name", "David");
        david.put("follows", Arrays.asList(3));
        users.add(david);

        input.put("users", users);

        // Test the solution
        MutualFollowersService service = new MutualFollowersService();
        String result = service.findMutualFollowers(input);
        System.out.println(result);
    }
} 