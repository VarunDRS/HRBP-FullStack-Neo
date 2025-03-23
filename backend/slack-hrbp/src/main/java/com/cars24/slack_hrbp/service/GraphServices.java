package com.cars24.slack_hrbp.service;

import com.cars24.slack_hrbp.data.response.GraphResponse;
import org.springframework.web.bind.annotation.RequestParam;

public interface GraphServices {
    public GraphResponse getGraph(@RequestParam String userid , String month);
}
