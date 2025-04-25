import com.google.gson.Gson;
import java.util.*;

public class NthLevelFollowersService {
    private static final String REG_NO = "REG12347";
    
    public String findNthLevelFollowers(Map<String, Object> input) {
        // Extract input parameters
        int n = ((Number) ((Map<String, Object>) input.get("users")).get("n")).intValue();
        int findId = ((Number) ((Map<String, Object>) input.get("users")).get("findId")).intValue();
        List<Map<String, Object>> users = (List<Map<String, Object>>) 
            ((Map<String, Object>) input.get("users")).get("users");
        
        // Build adjacency list representation of the follow graph
        Map<Integer, List<Integer>> followGraph = buildFollowGraph(users);
        
        // Find nth level followers using BFS
        List<Integer> nthLevelFollowers = findNthLevel(followGraph, findId, n);
        
        // Sort the followers for consistent output
        Collections.sort(nthLevelFollowers);
        
        // Create result map
        Map<String, Object> result = new HashMap<>();
        result.put("regNo", REG_NO);
        result.put("outcome", nthLevelFollowers);
        
        // Convert to JSON and return
        return new Gson().toJson(result);
    }
    
    private Map<Integer, List<Integer>> buildFollowGraph(List<Map<String, Object>> users) {
        Map<Integer, List<Integer>> graph = new HashMap<>();
        
        for (Map<String, Object> user : users) {
            int userId = ((Number) user.get("id")).intValue();
            List<Number> follows = (List<Number>) user.get("follows");
            
            // Convert follows list to Integer list
            List<Integer> followsList = follows.stream()
                .map(Number::intValue)
                .collect(Collectors.toList());
                
            graph.put(userId, followsList);
        }
        
        return graph;
    }
    
    private List<Integer> findNthLevel(Map<Integer, List<Integer>> graph, int startId, int n) {
        // Track visited nodes to avoid cycles
        Set<Integer> visited = new HashSet<>();
        visited.add(startId);
        
        // Queue for BFS
        Queue<Integer> queue = new LinkedList<>();
        queue.offer(startId);
        
        // Track current level
        int currentLevel = 0;
        
        // Store nodes at the nth level
        List<Integer> nthLevelNodes = new ArrayList<>();
        
        while (!queue.isEmpty() && currentLevel <= n) {
            int levelSize = queue.size();
            
            // Process all nodes at current level
            for (int i = 0; i < levelSize; i++) {
                int currentNode = queue.poll();
                
                // If we're at the desired level, add to result
                if (currentLevel == n) {
                    nthLevelNodes.add(currentNode);
                    continue;
                }
                
                // Add unvisited neighbors to queue
                List<Integer> neighbors = graph.getOrDefault(currentNode, new ArrayList<>());
                for (int neighbor : neighbors) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        queue.offer(neighbor);
                    }
                }
            }
            
            currentLevel++;
        }
        
        // Remove the start node if it's in the result (shouldn't be)
        nthLevelNodes.remove(Integer.valueOf(startId));
        
        return nthLevelNodes;
    }
    
    // Test method
    public static void main(String[] args) {
        // Create example input
        Map<String, Object> input = new HashMap<>();
        Map<String, Object> usersMap = new HashMap<>();
        List<Map<String, Object>> users = new ArrayList<>();
        
        // Set n and findId
        usersMap.put("n", 2);
        usersMap.put("findId", 1);
        
        // Create users
        // Alice
        Map<String, Object> alice = new HashMap<>();
        alice.put("id", 1);
        alice.put("name", "Alice");
        alice.put("follows", Arrays.asList(2, 3));
        users.add(alice);
        
        // Bob
        Map<String, Object> bob = new HashMap<>();
        bob.put("id", 2);
        bob.put("name", "Bob");
        bob.put("follows", Arrays.asList(4));
        users.add(bob);
        
        // Charlie
        Map<String, Object> charlie = new HashMap<>();
        charlie.put("id", 3);
        charlie.put("name", "Charlie");
        charlie.put("follows", Arrays.asList(4, 5));
        users.add(charlie);
        
        // David
        Map<String, Object> david = new HashMap<>();
        david.put("id", 4);
        david.put("name", "David");
        david.put("follows", Arrays.asList(6));
        users.add(david);
        
        // Eva
        Map<String, Object> eva = new HashMap<>();
        eva.put("id", 5);
        eva.put("name", "Eva");
        eva.put("follows", Arrays.asList(6));
        users.add(eva);
        
        // Frank
        Map<String, Object> frank = new HashMap<>();
        frank.put("id", 6);
        frank.put("name", "Frank");
        frank.put("follows", Arrays.asList());
        users.add(frank);
        
        usersMap.put("users", users);
        input.put("users", usersMap);
        
        // Test the solution
        NthLevelFollowersService service = new NthLevelFollowersService();
        String result = service.findNthLevelFollowers(input);
        System.out.println(result);
    }
} 