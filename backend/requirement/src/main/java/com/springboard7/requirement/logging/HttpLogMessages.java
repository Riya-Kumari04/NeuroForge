package com.springboard7.requirement.logging;

public final class HttpLogMessages {

    private HttpLogMessages() {}

    public static final String GENERATE_SPECIFICATION =
            "POST /api/v1/specifications/{}/generate";

    public static final String GET_ALL_VERSIONS =
            "HTTP GET /api/v1/specifications/{}/versions";

    public static final String GET_VERSION =
            "HTTP GET /api/v1/specifications/{}/versions/{}";

    public static final String GET_LATEST_VERSION =
            "HTTP GET /api/v1/specifications/{}/latest";

    public static final String CREATE_SPECIFICATION =
            "HTTP POST /api/v1/specifications";

    public static final String UPDATE_SPECIFICATION =
            "HTTP PUT /api/v1/specifications/{}";

    public static final String DELETE_SPECIFICATION =
            "HTTP DELETE /api/v1/specifications/{}";

    public static final String GET_SPECIFICATION =
            "HTTP GET /api/v1/specifications/{}";

    public static final String SEARCH_SPECIFICATIONS =
            "HTTP GET /api/v1/specifications";

    public static final String SUBMIT_VERSION =
            "HTTP POST /api/v1/specifications/{}/versions/{}/submit";

    public static final String APPROVE_VERSION =
            "HTTP POST /api/v1/specifications/{}/versions/{}/approve";

    public static final String REJECT_VERSION =
            "HTTP POST /api/v1/specifications/{}/versions/{}/reject";

    public static final String ARCHIVE_VERSION =
            "HTTP POST /api/v1/specifications/{}/versions/{}/archive";

    public static final String COMPARE_VERSIONS =
            "HTTP GET /api/v1/specifications/{}/versions/{}/compare/{}";
}