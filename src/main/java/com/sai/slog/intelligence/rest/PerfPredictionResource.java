package com.sai.slog.intelligence.rest;

import com.sai.slog.intelligence.model.PerfPredictionRequest;
import com.sai.slog.intelligence.util.MultiRegression;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by saipkri on 23/02/17..
 */
@RestController
public class PerfPredictionResource {

    @ApiOperation("Predicts a perf metric")
    @CrossOrigin(methods = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.OPTIONS, RequestMethod.GET})
    @RequestMapping(value = "/perfmetric", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> predict(@RequestBody final PerfPredictionRequest request) {
        double result = MultiRegression.predict(request);
        Map<String, Double> res = new HashMap<>();
        res.put("result", result);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }
}
