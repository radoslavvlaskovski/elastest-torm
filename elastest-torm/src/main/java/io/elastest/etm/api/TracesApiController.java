package io.elastest.etm.api;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.elastest.etm.model.AggregationTree;
import io.elastest.etm.model.LogAnalyzerQuery;
import io.elastest.etm.model.MonitoringQuery;
import io.elastest.etm.service.MonitoringServiceInterface;
import io.elastest.etm.service.TracesService;
import io.swagger.annotations.ApiParam;

@Controller
public class TracesApiController implements TracesApi {
    public final Logger logger = getLogger(lookup().lookupClass());

    private TracesService tracesService;
    private MonitoringServiceInterface monitoringService;

    public TracesApiController(TracesService tracesService,
            MonitoringServiceInterface monitoringService) {
        this.tracesService = tracesService;
        this.monitoringService = monitoringService;
    }

    public ResponseEntity<Map<String, Object>> processTrace(
            @Valid @RequestBody Map<String, Object> data) {
        try {
            this.tracesService.processHttpTrace(data);
            return new ResponseEntity<Map<String, Object>>(data, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<Map<String, Object>>(data,
                    HttpStatus.NOT_ACCEPTABLE);
        }

    }

    public ResponseEntity<List<Map<String, Object>>> searchAllByTerms(
            @ApiParam(value = "Search Request configuration", required = true) @Valid @RequestBody MonitoringQuery body)
            throws Exception {
        return new ResponseEntity<List<Map<String, Object>>>(
                monitoringService.searchAllByTerms(body), HttpStatus.OK);
    }

    /* ****************************************** */
    /* ****************** Logs ****************** */
    /* ****************************************** */

    public ResponseEntity<List<Map<String, Object>>> searchAllLogs(
            @ApiParam(value = "Search Request configuration", required = true) @Valid @RequestBody MonitoringQuery body)
            throws Exception {
        return new ResponseEntity<List<Map<String, Object>>>(
                monitoringService.searchAllLogs(body), HttpStatus.OK);
    }

    public ResponseEntity<List<Map<String, Object>>> searchPreviousLogs(
            @ApiParam(value = "Search Request configuration", required = true) @Valid @RequestBody MonitoringQuery body)
            throws Exception {
        return new ResponseEntity<List<Map<String, Object>>>(
                monitoringService.getPreviousLogsFromTimestamp(body),
                HttpStatus.OK);
    }

    public ResponseEntity<List<Map<String, Object>>> searchLastLogs(
            @ApiParam(value = "Number of logs to get.", required = true) @PathVariable("size") int size,
            @ApiParam(value = "Search Request configuration", required = true) @Valid @RequestBody MonitoringQuery body)
            throws Exception {
        return new ResponseEntity<List<Map<String, Object>>>(
                monitoringService.getLastLogs(body, size), HttpStatus.OK);
    }

    public ResponseEntity<List<AggregationTree>> searchLogsAggregationTree(
            @ApiParam(value = "Search Request configuration", required = true) @Valid @RequestBody MonitoringQuery body)
            throws Exception {
        return new ResponseEntity<List<AggregationTree>>(
                monitoringService.searchLogsTree(body),
                HttpStatus.OK);
    }

    public ResponseEntity<List<AggregationTree>> searchLogsLevelsTree(
            @ApiParam(value = "Search Request configuration", required = true) @Valid @RequestBody MonitoringQuery body)
            throws Exception {
        return new ResponseEntity<List<AggregationTree>>(
                monitoringService.searchLogsLevelsTree(body),
                HttpStatus.OK);
    }

    /* ***************************************** */
    /* **************** Metrics **************** */
    /* ***************************************** */

    public ResponseEntity<List<Map<String, Object>>> searchAllMetrics(
            @ApiParam(value = "Search Request configuration", required = true) @Valid @RequestBody MonitoringQuery body)
            throws Exception {
        return new ResponseEntity<List<Map<String, Object>>>(
                monitoringService.searchAllMetrics(body), HttpStatus.OK);
    }

    public ResponseEntity<List<Map<String, Object>>> searchPreviousMetrics(
            @ApiParam(value = "Search Request configuration", required = true) @Valid @RequestBody MonitoringQuery body)
            throws Exception {
        return new ResponseEntity<List<Map<String, Object>>>(
                monitoringService.getPreviousMetricsFromTimestamp(body),
                HttpStatus.OK);
    }

    public ResponseEntity<List<Map<String, Object>>> searchLastMetrics(
            @ApiParam(value = "Number of Metrics to get.", required = true) @PathVariable("size") int size,
            @ApiParam(value = "Search Request configuration", required = true) @Valid @RequestBody MonitoringQuery body)
            throws Exception {
        return new ResponseEntity<List<Map<String, Object>>>(
                monitoringService.getLastMetrics(body, size), HttpStatus.OK);
    }

    public ResponseEntity<List<AggregationTree>> searchMetricsAggregationTree(
            @ApiParam(value = "Search Request configuration", required = true) @Valid @RequestBody MonitoringQuery body)
            throws Exception {
        return new ResponseEntity<List<AggregationTree>>(
                monitoringService.searchMetricsTree(body), HttpStatus.OK);
    }

    /* ********************************************* */
    /* **************** LogAnalyzer **************** */
    /* ********************************************* */

    public ResponseEntity<List<Map<String, Object>>> searchLogAnalyzerQuery(
            @ApiParam(value = "Search Request configuration", required = true) @Valid @RequestBody LogAnalyzerQuery body)
            throws Exception {
        return new ResponseEntity<List<Map<String, Object>>>(
                monitoringService.searchLogAnalyzerQuery(body), HttpStatus.OK);
    }
}
