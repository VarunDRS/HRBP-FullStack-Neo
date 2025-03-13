package com.cars24.slack_hrbp.controller;


import com.cars24.slack_hrbp.data.response.GraphResponse;
import com.cars24.slack_hrbp.service.impl.GraphServicesImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/graph")
@RequiredArgsConstructor
@Slf4j
public class GraphsController {

    private final GraphServicesImpl graphsServices;

    @PreAuthorize("hasAnyRole('HR', 'MANAGER')")
    @GetMapping()
    public ResponseEntity<GraphResponse> getGraph(@RequestParam String userid , String month){
        GraphResponse resp = graphsServices.getGraph(userid,month);
        return ResponseEntity.ok().body(resp);
    }

}
