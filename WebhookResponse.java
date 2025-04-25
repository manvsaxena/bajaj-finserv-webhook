import com.google.gson.annotations.SerializedName;
import lombok.Data;
import java.util.Map;

@Data
public class WebhookResponse {
    private String webhook;
    
    @SerializedName("accessToken")
    private String accessToken;
    
    private Map<String, Object> data;
} 