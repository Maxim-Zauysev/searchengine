package searchengine.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BadRequest {
    boolean gotResult;
    String error;

    public BadRequest(boolean gotResult, String error) {
        this.gotResult = gotResult;
        this.error = error;
    }
}
