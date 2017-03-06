package com.sai.slog.intelligence.rest;

import com.sai.slog.intelligence.domain.LogCorrelation;
import com.sai.slog.intelligence.repository.LogCorrelationRepository;
import io.swagger.annotations.ApiOperation;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.data.neo4j.util.IterableUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

/**
 * Created by saipkri on 23/02/17.
 */
@RestController
public class LogCorrelationResource {

    private final SessionFactory sessionFactory;
    private final LogCorrelationRepository logCorrelationRepository;

    @Inject
    public LogCorrelationResource(final SessionFactory sessionFactory, final LogCorrelationRepository logCorrelationRepository) {
        this.sessionFactory = sessionFactory;
        this.logCorrelationRepository = logCorrelationRepository;
    }


    @ApiOperation("Saves a log correlation graph")
    @CrossOrigin(methods = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.OPTIONS, RequestMethod.GET})
    @RequestMapping(value = "/logcorrelation", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> saveLogcorrelation(@RequestBody final LogCorrelation logCorrelation) {
        Session session = sessionFactory.openSession();
        session.save(logCorrelation, -1);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @ApiOperation("Gets the logcorrelation by component")
    @CrossOrigin(methods = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.OPTIONS, RequestMethod.GET})
    @RequestMapping(value = "/logcorrelations", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> findAll() {
        return new ResponseEntity<>(IterableUtils.toList(logCorrelationRepository.findAll()), HttpStatus.OK);
    }

    @ApiOperation("Gets the logcorrelation by level")
    @CrossOrigin(methods = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.OPTIONS, RequestMethod.GET})
    @RequestMapping(value = "/logcorrelations/{level}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> findCorrelationsByLevel(@RequestParam(name = "logMessage") final String logMessage, @PathVariable("level") final int level) {
        return new ResponseEntity<>(logCorrelationRepository.findByLevel(logMessage.trim(), level), HttpStatus.OK);
    }
}
