package com.hysk.canting.utils;

import lombok.Data;

@Data
public class JsonResult {
    private int code;
    private String message;
    private Object data;
    private boolean success;

    public JsonResult(int code, String message, Object data, boolean success) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.success = success;
    }

    public static JsonResult success(Object data) {
        return new JsonResult(200, "操作成功", data, true);
    }

    public static JsonResult success() {
        return success(null);
    }

    public static JsonResult error(String message) {
        return new JsonResult(500, message, null, false);
    }

    public static JsonResult error(int code, String message) {
        return new JsonResult(code, message, null, false);
    }
} 