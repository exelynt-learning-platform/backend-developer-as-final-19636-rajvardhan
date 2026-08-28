package com.example.booking.utility;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse<T> {
    private int statusCode;
    private String msg;
    private T data;

    public static <T> BaseResponse<T> success(String msg, T data) {
        return new BaseResponse<>(200, msg, data);
    }

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(200, "Success", data);
    }

    public static <T> BaseResponse<T> error(int statusCode, String msg) {
        return new BaseResponse<>(statusCode, msg, null);
    }
}
