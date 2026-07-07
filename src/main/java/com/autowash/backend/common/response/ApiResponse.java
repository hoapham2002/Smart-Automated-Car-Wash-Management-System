package com.autowash.backend.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * The single response envelope used by EVERY endpoint in this project, per
 * the Definition of Done in the architecture doc:
 * "API trả đúng format { success, data } / { success, error, message }".
 *
 * Every controller across all 4+ weeks (both Person A and Person B) wraps
 * its return value in ApiResponse.success(...) - this is the class that
 * makes all of that compile.
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private String error;
    private String message;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    public static <T> ApiResponse<T> error(String errorCode, String message) {
        return new ApiResponse<>(false, null, errorCode, message);
    }
}
