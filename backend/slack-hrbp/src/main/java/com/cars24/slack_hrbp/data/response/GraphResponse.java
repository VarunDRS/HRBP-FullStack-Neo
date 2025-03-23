package com.cars24.slack_hrbp.data.response;

import lombok.Data;

import java.util.Map;

@Data
public class GraphResponse {
    private Map<String, Integer> typeCounts;
}
