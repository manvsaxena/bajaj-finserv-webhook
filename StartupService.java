@Service
public class StartupService {

    @PostConstruct
    public void onStartup() {
        RestTemplate restTemplate = new RestTemplate();

        // Step 1: Generate Webhook
        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook";
        Map<String, String> request = Map.of(
            "name", "John Doe",
            "regNo", "REG12347",
            "email", "john@example.com"
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        String webhook = (String) response.getBody().get("webhook");
        String accessToken = (String) response.getBody().get("accessToken");
        Map data = (Map) response.getBody().get("data");

        List<Map<String, Object>> users = (List<Map<String, Object>>) data.get("users");

        // Step 2: Determine Question
        String regNo = "REG12347";
        int lastDigit = Integer.parseInt(regNo.replaceAll("\\D", ""));
        String outcome;

        if (lastDigit % 2 == 1) {
            outcome = solveMutualFollowers(users);
        } else {
            outcome = solveNthLevel(data);
        }

        // Step 3: Send Result with Retry
        postWithRetry(webhook, accessToken, regNo, outcome);
    }

    private String solveMutualFollowers(List<Map<String, Object>> users) {
        Set<List<Integer>> mutualPairs = new HashSet<>();
        Map<Integer, List<Integer>> followMap = new HashMap<>();

        for (Map<String, Object> user : users) {
            followMap.put((Integer) user.get("id"), (List<Integer>) user.get("follows"));
        }

        for (Map.Entry<Integer, List<Integer>> entry : followMap.entrySet()) {
            Integer userId = entry.getKey();
            for (Integer followedId : entry.getValue()) {
                if (followMap.containsKey(followedId) && followMap.get(followedId).contains(userId)) {
                    List<Integer> pair = List.of(Math.min(userId, followedId), Math.max(userId, followedId));
                    mutualPairs.add(pair);
                }
            }
        }

        return new Gson().toJson(Map.of("regNo", "REG12347", "outcome", new ArrayList<>(mutualPairs)));
    }

    private String solveNthLevel(Map data) {
        Map input = (Map) data.get("users");
        int n = (int) input.get("n");
        int findId = (int) input.get("findId");
        List<Map<String, Object>> users = (List<Map<String, Object>>) input.get("users");

        Map<Integer, List<Integer>> graph = new HashMap<>();
        for (Map<String, Object> user : users) {
            graph.put((Integer) user.get("id"), (List<Integer>) user.get("follows"));
        }

        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        queue.offer(findId);
        visited.add(findId);

        while (n-- > 0) {
            int size = queue.size();
            while (size-- > 0) {
                int current = queue.poll();
                for (int neighbor : graph.getOrDefault(current, List.of())) {
                    if (!visited.contains(neighbor)) {
                        queue.offer(neighbor);
                        visited.add(neighbor);
                    }
                }
            }
        }

        return new Gson().toJson(Map.of("regNo", "REG12347", "outcome", new ArrayList<>(queue)));
    }

    @Retryable(value = Exception.class, maxAttempts = 4, backoff = @Backoff(delay = 1000))
    private void postWithRetry(String url, String token, String regNo, String bodyJson) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<String> entity = new HttpEntity<>(bodyJson, headers);
        new RestTemplate().postForEntity(url, entity, String.class);
    }
}
