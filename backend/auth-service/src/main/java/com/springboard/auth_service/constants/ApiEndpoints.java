package com.springboard.auth_service.constants;

public final class ApiEndpoints {

    private ApiEndpoints() {
    }

    public static final String LOGIN = "/auth/login";

    public static final String REGISTER = "/auth/register";

    public static final String SEND_OTP = "/auth/send-otp";

    public static final String FORGOT_PASSWORD_SEND_OTP =
            "/auth/forgot-password/send-otp";

    public static final String SET_PASSWORD =
            "/auth/set-password";

    public static final String RESET_PASSWORD =
            "/auth/reset-password";

    public static final String REFRESH_TOKEN =
            "/auth/refresh-token";

}