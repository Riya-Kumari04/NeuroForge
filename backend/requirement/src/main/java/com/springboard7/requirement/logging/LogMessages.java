package com.springboard7.requirement.logging;

public final class LogMessages {

    private LogMessages() {
    }
    public static final String CREATE_REQUEST =
            "Create Specification Request | title='{}'";

    public static final String FETCH_REQUEST =
            "Fetch Specification Request | id={}";

    public static final String UPDATE_REQUEST =
            "Update Specification Request | id={}";

    public static final String DELETE_REQUEST =
            "Delete Specification Request | id={}";

    public static final String SEARCH_REQUEST =
            "Search Specifications | title='{}' | status={} | page={} | size={}";

    public static final String SPEC_CREATED =
            "Specification Created | id={} | key={} | version={} | status={}";

    public static final String SPEC_UPDATED =
            "Specification Updated | id={} | version={} | status={}";

    public static final String SPEC_DELETED =
            "Specification Soft Deleted | id={}";

    public static final String SPEC_FOUND =
            "Specification Found | id={} | version={} | status={}";

    public static final String SPEC_NOT_FOUND =
            "Specification Not Found | id={}";

    public static final String DUPLICATE_TITLE =
            "Duplicate Specification Title | title='{}'";

    public static final String TITLE_VALIDATION =
            "Validating Duplicate Title | title='{}'";

    public static final String TITLE_VALIDATION_SUCCESS =
            "Title Validation Passed | title='{}'";

    public static final String BUILD_SPEC =
            "Building Specification Entity";

    public static final String BUILD_VERSION =
            "Building Initial Specification Version";

    public static final String VERSION_CREATION =
            "Creating New Version | currentVersion={} | nextVersion={}";

    public static final String SEARCH_COMPLETED =
            "Search Completed | totalElements={} | totalPages={}";

    public static final String KEY_GENERATED =
            "Generated Specification Key | key={}";

    public static final String VERSION_FETCH_REQUEST =
            "Fetch Version Request | specificationId={} | version={}";

    public static final String VERSION_LIST_REQUEST =
            "Fetch Version List Request | specificationId={}";

    public static final String LATEST_VERSION_REQUEST =
            "Fetch Latest Version Request | specificationId={}";

    public static final String VERSION_FETCH_RESPONSE =
            "Fetch Version Response | specificationId={} | version={}";

    public static final String VERSION_LIST_RESPONSE =
            "Fetch Version List Response | specificationId={} | totalVersions={}";

    public static final String LATEST_VERSION_RESPONSE =
            "Fetch Latest Version Response | specificationId={} | version={}";

    public static final String SUBMIT_REVIEW_REQUEST =
            "Submit Version For Review Request | specificationId={} | version={}";

    public static final String APPROVE_VERSION_REQUEST =
            "Approve Version Request | specificationId={} | version={}";

    public static final String REJECT_VERSION_REQUEST =
            "Reject Version Request | specificationId={} | version={}";

    public static final String ARCHIVE_VERSION_REQUEST =
            "Archive Version Request | specificationId={} | version={}";


    public static final String SUBMIT_REVIEW_RESPONSE =
            "Version Submitted For Review | specificationId={} | version={} | status={}";

    public static final String APPROVE_VERSION_RESPONSE =
            "Version Approved | specificationId={} | version={} | status={}";

    public static final String REJECT_VERSION_RESPONSE =
            "Version Rejected | specificationId={} | version={} | status={}";

    public static final String ARCHIVE_VERSION_RESPONSE =
            "Version Archived | specificationId={} | version={} | status={}";


    public static final String INVALID_WORKFLOW_STATE =
            "Invalid Workflow State | expected={} | actual={} | action={}";

    public static final String WORKFLOW_STATUS_VALIDATION =
            "Validating Workflow Status | expected={} | actual={} | action={}";

    public static final String COMPARE_VERSION_REQUEST =
            "Compare Versions Request | specificationId={} | version1={} | version2={}";

    public static final String COMPARE_VERSION_RESPONSE =
            "Compare Versions Response | specificationId={} | version1={} | version2={} | changes={}";


    public static final String GENERATE_SPECIFICATION_REQUEST =
            "Generating specification using AI | specificationId={}";

    public static final String GENERATE_SPECIFICATION_RESPONSE =
            "Specification generated successfully | specificationId={} | version={}";

    public static final String UPDATE_VERSION_REQUEST =
            "Updating Draft Version | Specification={} | Version={}";

    public static final String UPDATE_VERSION_RESPONSE =
            "Draft Version Updated | Specification={} | Version={}";
}