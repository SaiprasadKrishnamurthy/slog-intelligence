package com.sai.slog.intelligence.rest;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sai.slog.intelligence.domain.ExceptionCorrelation;
import com.sai.slog.intelligence.domain.Log;
import com.sai.slog.intelligence.domain.LogCorrelation;
import com.sai.slog.intelligence.repository.LogCorrelationRepository;
import io.swagger.annotations.ApiOperation;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * Created by saipkri on 23/02/17.
 */
@RestController
public class ExceptionCorrelationResource {

    private final SessionFactory sessionFactory;
    private final LogCorrelationRepository logCorrelationRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final String logSearchEndpoint;

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Inject
    public ExceptionCorrelationResource(final SessionFactory sessionFactory, final LogCorrelationRepository logCorrelationRepository, @Value("${log.search.endpoint}") final String logSearchEndpoint) {
        this.sessionFactory = sessionFactory;
        this.logCorrelationRepository = logCorrelationRepository;
        this.logSearchEndpoint = logSearchEndpoint;
    }


    @ApiOperation("Finds exception correlations.")
    @CrossOrigin(methods = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.OPTIONS, RequestMethod.GET})
    @RequestMapping(value = "/exceptioncorrelations", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> exceptionCorrelations(@RequestParam("customer") final String customer, @RequestParam("exceptionMessage") final String exceptionMessage, @RequestParam(value = "startTimeInMillis") long startTimeInMillis, @RequestParam(value = "endTimeInMillis") long endTimeInMillis) {
        int depth = 1;
        List<LogCorrelation> results = logCorrelationRepository.findByLevel(exceptionMessage, depth);
        final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Map<String, Object[]> filesGrouping = new LinkedHashMap<>();

        while (!results.isEmpty()) {
            for (LogCorrelation lc : results) {
                // Perform a search in ES to see if the error is present within the time window.
                List<Log> logs = new ArrayList<>();
                Map<String, String> criteria = new HashMap<>();
                criteria.put("customer", customer);
                criteria.put("noSortByTimestamp", "true");
                criteria.put("fromDate", DATE_FORMAT.format(new Date(startTimeInMillis)));
                criteria.put("toDate", DATE_FORMAT.format(new Date(endTimeInMillis + 5000L)));
                criteria.put("fromPage", "0");
                criteria.put("toPage", "1");
                criteria.put("freeTextSearch", lc.getLogMessage().trim());
                List response = restTemplate.postForObject(logSearchEndpoint, criteria, List.class);
                response.forEach(res -> logs.add(objectMapper.convertValue(res, Log.class)));
                logs.stream()
                        .forEach(log -> {
                            if (!filesGrouping.containsKey(log.getPath())) {
                                List<Log> _logs = new ArrayList<>();
                                _logs.add(log);
                                filesGrouping.put(lc.getDescription(), new Object[]{_logs, lc});
                            } else {
                                ((List<Log>) filesGrouping.get(log.getPath())[0]).add(log);
                            }
                        });
            }
            results = logCorrelationRepository.findByLevel(exceptionMessage, ++depth);
        }

        List<ExceptionCorrelation> response = filesGrouping.entrySet().stream().map(entry -> {
            List<Log> logs = (List<Log>) entry.getValue()[0];
            LogCorrelation lc = (LogCorrelation) entry.getValue()[1];
            return new ExceptionCorrelation(entry.getKey(), lc.getDescription(), logs);
        }).collect(toList());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ApiOperation("Finds detrimental exceptions")
    @CrossOrigin(methods = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.OPTIONS, RequestMethod.GET})
    @RequestMapping(value = "/detrimentalexceptions", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> detrimentalExceptions(@RequestParam("customer") final String customer, @RequestParam(value = "minutesBackFromNow") int minutesBackFromNow) {
        List<LogCorrelation> results = logCorrelationRepository.detrimentalExceptions();
        Date minutesUpto = new Date(System.currentTimeMillis() - (minutesBackFromNow * 60 * 1000));
        final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Map<String, Object[]> filesGrouping = new LinkedHashMap<>();

        System.out.println(results);
        for (LogCorrelation lc : results) {
            // Perform a search in ES to see if the error is present within the time window.
            List<Log> logs = new ArrayList<>();
            Map<String, String> criteria = new HashMap<>();
            criteria.put("customer", customer);
            criteria.put("noSortByTimestamp", "true");
            criteria.put("fromDate", DATE_FORMAT.format(new Date(minutesUpto.getTime())));
            criteria.put("toDate", DATE_FORMAT.format(new Date()));
            criteria.put("fromPage", "0");
            criteria.put("toPage", "1");
            criteria.put("freeTextSearch", lc.getLogMessage().trim());
            List response = restTemplate.postForObject(logSearchEndpoint, criteria, List.class);
            response.forEach(res -> logs.add(objectMapper.convertValue(res, Log.class)));
            logs.stream()
                    .forEach(log -> {
                        if (!filesGrouping.containsKey(log.getPath())) {
                            List<Log> _logs = new ArrayList<>();
                            _logs.add(log);
                            filesGrouping.put(lc.getDescription(), new Object[]{_logs, lc});
                        } else {
                            ((List<Log>) filesGrouping.get(log.getPath())[0]).add(log);
                        }
                    });
        }

        List<ExceptionCorrelation> response = filesGrouping.entrySet().stream().map(entry -> {
            List<Log> logs = (List<Log>) entry.getValue()[0];
            LogCorrelation lc = (LogCorrelation) entry.getValue()[1];
            return new ExceptionCorrelation(entry.getKey(), lc.getDescription(), logs);
        }).collect(toList());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
