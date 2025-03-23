package com.cars24.slack_hrbp.data.dao;

import com.cars24.slack_hrbp.data.response.GraphResponse;
import org.springframework.web.bind.annotation.RequestParam;

public interface GraphDao {
    public GraphResponse getGraph(@RequestParam String userid , String month);
}
