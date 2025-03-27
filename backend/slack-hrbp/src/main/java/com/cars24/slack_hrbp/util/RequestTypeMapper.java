package com.cars24.slack_hrbp.util;
import java.util.HashMap;
import java.util.Map;


import java.util.Collections;

public final class RequestTypeMapper {

    private static final Map<String, String> REQUEST_TYPE_MAP = createTypeMap();

    // Private constructor to prevent instantiation
    private RequestTypeMapper() {}

    /**
     * Gets the standardized code for a request type
     * @param requestType The input request type string
     * @return Single-character code or empty string if not found
     */
    public static String getRequestTypeCode(String requestType) {
        if (requestType == null) {
            return "";
        }
        return REQUEST_TYPE_MAP.getOrDefault(requestType.trim(), "");
    }

    private static Map<String, String> createTypeMap() {
        Map<String, String> map = new HashMap<>();

        // Planned Leave types
        map.put("Planned Leave", "P");
        map.put("Planned Leave (First Half)", "P**");
        map.put("Planned Leave (Second Half)", "P*");

        // Unplanned Leave types
        map.put("Unplanned Leave", "U");
        map.put("UnPlanned Leave", "U"); // Handle typo

        // Other types
        map.put("Sick Leave", "S");
        map.put("Work From Home", "W");
        map.put("WFH", "W"); // Abbreviation
        map.put("Travelling to HQ", "T");
        map.put("Holiday", "H");
        map.put("Elections", "E");
        map.put("Joined", "J");

        return Collections.unmodifiableMap(map);
    }

    // Helper methods for common checks
    public static boolean isWorkFromHome(String requestType) {
        return "W".equals(getRequestTypeCode(requestType));
    }

    public static boolean isLeave(String requestType) {
        String code = getRequestTypeCode(requestType);
        return code.startsWith("P") || "U".equals(code) || "S".equals(code);
    }
}