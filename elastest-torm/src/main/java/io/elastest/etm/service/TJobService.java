package io.elastest.etm.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.annotation.PreDestroy;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.http.HTTPException;

import org.apache.maven.plugins.surefire.report.ReportTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.xml.sax.SAXException;

import io.elastest.etm.api.model.ExternalJob;
import io.elastest.etm.dao.TJobExecRepository;
import io.elastest.etm.dao.TJobRepository;
import io.elastest.etm.model.EimMonitoringConfig.BeatsStatusEnum;
import io.elastest.etm.model.Enums.MonitoringStorageType;
import io.elastest.etm.model.MultiConfig;
import io.elastest.etm.model.Parameter;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.TJob;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.TJobExecution.ResultEnum;
import io.elastest.etm.model.TJobExecution.TypeEnum;
import io.elastest.etm.model.TJobExecutionFile;
import io.elastest.etm.utils.TestResultParser;
import io.elastest.etm.utils.UtilsService;

@Service
public class TJobService {
    private static final Logger logger = LoggerFactory
            .getLogger(TJobService.class);

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${registry.contextPath}")
    private String registryContextPath;

    private final TJobRepository tJobRepo;
    private final TJobExecRepository tJobExecRepositoryImpl;
    public final TJobExecOrchestratorService tJobExecOrchestratorService;
    private final EsmService esmService;
    private DatabaseSessionManager dbmanager;
    private UtilsService utilsService;

    Map<String, Future<Void>> asyncExecs = new HashMap<String, Future<Void>>();

    public TJobService(TJobRepository tJobRepo,
            TJobExecRepository tJobExecRepositoryImpl,
            TJobExecOrchestratorService epmIntegrationService,
            EsmService esmService, DatabaseSessionManager dbmanager,
            UtilsService utilsService) {
        super();
        this.tJobRepo = tJobRepo;
        this.tJobExecRepositoryImpl = tJobExecRepositoryImpl;
        this.tJobExecOrchestratorService = epmIntegrationService;
        this.esmService = esmService;
        this.dbmanager = dbmanager;
        this.utilsService = utilsService;
    }

    @PreDestroy
    private void preDestroy() {
        dbmanager.bindSession();
        this.stopAllRunningTJobs();
        dbmanager.unbindSession();
    }

    public void stopAllRunningTJobs() {
        logger.info("Stopping non-finished TJobExecutions ({} total)",
                asyncExecs.size());
        Map<String, Future<Void>> copyOfAsyncExecs = new HashMap<String, Future<Void>>(
                asyncExecs);
        for (HashMap.Entry<String, Future<Void>> currentExecMap : copyOfAsyncExecs
                .entrySet()) {
            Long currentExecId = getTJobExecByMapName(currentExecMap.getKey());
            logger.info("Stopping TJobExecution with id {}", currentExecId);
            this.stopTJobExec(currentExecId);
        }

        logger.info("End Stopping non-finished TJobExecutions");
    }

    public TJob createTJob(TJob tjob) {
        return tJobRepo.save(tjob);
    }

    public void deleteTJob(Long tJobId) {
        TJob tJob = tJobRepo.findById(tJobId).get();
        tJobRepo.delete(tJob);
    }

    public List<TJob> getAllTJobs() {
        return tJobRepo.findAll();
    }

    public String getMapNameByTJobExec(TJobExecution tJobExec) {
        return tJobExec.getTjob().getId() + "_" + tJobExec.getId();
    }

    public Long getTJobExecByMapName(String mapName) {
        return Long.parseLong(mapName.split("_")[1]);
    }

    /* *** Execute TJob *** */

    public TJobExecution executeTJob(Long tJobId,
            List<Parameter> tJobParameters, List<Parameter> sutParameters,
            List<MultiConfig> multiConfigs) throws HttpClientErrorException {
        TJob tJob = tJobRepo.findById(tJobId).get();

        SutSpecification sut = tJob.getSut();
        // Checks if has Sut instrumented by ElasTest and beats status is
        // activating yet
        if (sut != null && sut.isInstrumentedByElastest()
                && sut.getEimMonitoringConfig() != null
                && sut.getEimMonitoringConfig()
                        .getBeatsStatus() == BeatsStatusEnum.ACTIVATING) {
            throw new HttpClientErrorException(HttpStatus.ACCEPTED);
        }

        TJobExecution tJobExec = new TJobExecution();

        if (utilsService.isElastestMini()) {
            tJobExec.setMonitoringStorageType(MonitoringStorageType.MYSQL);
        } else {
            tJobExec.setMonitoringStorageType(
                    MonitoringStorageType.ELASTICSEARCH);
        }

        tJobExec.setStartDate(new Date());
        if (tJob.getSut() != null && sutParameters != null
                && !sutParameters.isEmpty()) {
            tJob.getSut().setParameters(sutParameters);
        }
        tJobExec.setTjob(tJob);
        if (tJobParameters != null && !tJobParameters.isEmpty()) {
            tJobExec.setParameters(tJobParameters);
        }

        tJobExec.setType(TypeEnum.SIMPLE);
        if (multiConfigs != null && multiConfigs.size() > 0 && tJob.isMulti()) {
            tJobExec.setMultiConfigurations(multiConfigs);
            tJobExec.setType(TypeEnum.PARENT);
        }

        tJobExec = tJobExecRepositoryImpl.save(tJobExec);

        // After first save, get real Id
        tJobExec.generateMonitoringIndex();
        tJobExec = tJobExecRepositoryImpl.save(tJobExec);

        Future<Void> asyncExec;
        if (!tJob.isExternal()) {
            asyncExec = tJobExecOrchestratorService.executeTJob(tJobExec,
                    tJob.getSelectedServices());
            asyncExecs.put(getMapNameByTJobExec(tJobExec), asyncExec);
        } else {
            tJobExecOrchestratorService.executeExternalJob(tJobExec, false);
        }

        return tJobExec;
    }

    /* *** Execute Jenkins TJob *** */

    public TJobExecution executeTJob(ExternalJob externalJob, Long tJobId,
            List<Parameter> parameters, List<Parameter> sutParameters)
            throws HttpClientErrorException {

        // TODO MultiTJob
        TJob tJob = tJobRepo.findById(tJobId).get();

        SutSpecification sut = tJob.getSut();
        // Checks if has sut instrumented by elastest and beats status is
        // activating yet
        if (sut != null && sut.isInstrumentedByElastest()
                && sut.getEimMonitoringConfig() != null
                && sut.getEimMonitoringConfig()
                        .getBeatsStatus() == BeatsStatusEnum.ACTIVATING) {
            throw new HttpClientErrorException(HttpStatus.ACCEPTED);
        }

        TJobExecution tJobExec = new TJobExecution();

        if (utilsService.isElastestMini()) {
            tJobExec.setMonitoringStorageType(MonitoringStorageType.MYSQL);
        } else {
            tJobExec.setMonitoringStorageType(
                    MonitoringStorageType.ELASTICSEARCH);
        }

        tJobExec.setStartDate(new Date());
        if (tJob.getSut() != null && sutParameters != null
                && !sutParameters.isEmpty()) {
            tJob.getSut().setParameters(sutParameters);
        }
        tJobExec.setTjob(tJob);
        if (parameters != null && !parameters.isEmpty()) {
            tJobExec.setParameters(parameters);
        }
        if (externalJob.getBuildUrl() != null
                && !externalJob.getBuildUrl().isEmpty()) {
            tJobExec.getExternalUrls().put("jenkins-build-url",
                    externalJob.getBuildUrl());
        }
        tJobExec = tJobExecRepositoryImpl.save(tJobExec);

        // After first save, get real Id
        tJobExec.generateMonitoringIndex();
        tJobExec = tJobExecRepositoryImpl.save(tJobExec);

        tJobExecOrchestratorService.executeExternalJob(tJobExec,
                externalJob.isFromIntegratedJenkins());
        return tJobExec;
    }

    public TJobExecution stopTJobExec(Long tJobExecId) {
        TJobExecution tJobExec = tJobExecRepositoryImpl.findById(tJobExecId)
                .get();
        String mapKey = getMapNameByTJobExec(tJobExec);

        if (tJobExec.isMultiExecutionChild()) {
            return stopTJobExec(tJobExec.getExecParent().getId());
        }

        Future<Void> asyncExec = asyncExecs.get(mapKey);

        boolean cancelExecuted = false;

        try {
            cancelExecuted = asyncExec.cancel(true);
            // If is not cancelled, stop async Exec and stop services
            if (cancelExecuted) {
                logger.info("Forcing Execution Stop");
                try {
                    tJobExec = tJobExecOrchestratorService
                            .forceEndExecution(tJobExec);
                    logger.info("Execution Stopped Successfully!");
                } catch (Exception e) {
                    logger.error("Error on forcing Execution stop");
                }
            } else { // If is already finished, gets TJobExec
                tJobExec = tJobExecRepositoryImpl.findById(tJobExecId).get();
            }
            asyncExecs.remove(mapKey);
        } catch (Exception e) {
            logger.info("Error during forcing stop", e);
        }
        return tJobExec;
    }

    public void endExternalTJobExecution(long tJobExecId, int result,
            List<String> testResultsReportsAsString) {
        logger.info("Finishing the external TJob.");
        TJobExecution tJobExec = this.getTJobExecById(tJobExecId);

        // Parsing test results
        List<ReportTestSuite> testResultsReports = new ArrayList<>();
        TestResultParser testResultParser = new TestResultParser();
        if (testResultsReportsAsString != null) {
            for (String testSuite : testResultsReportsAsString) {
                try {
                    testResultsReports.add(testResultParser
                            .testSuiteStringToReportTestSuite(testSuite));
                } catch (ParserConfigurationException | SAXException
                        | IOException e) {
                    // TODO Create a manual TestSuite with an error message
                    logger.error("Error on parse testSuite {}", e);
                }
            }

            tJobExecOrchestratorService.saveTestResults(testResultsReports,
                    tJobExec);
        }
        try {
            tJobExecOrchestratorService.deprovisionServices(tJobExec);
        } catch (Exception e) {
            logger.error(
                    "Exception during desprovisioning of the TSS associated with an External TJob.");
        }
        if (tJobExec.isWithSut()) {
            try {
                DockerExecution dockerExec = new DockerExecution(tJobExec);
                dockerExec.setSutExec(tJobExec.getSutExecution());
                tJobExecOrchestratorService.endDockbeatExec(dockerExec, true);
                tJobExecOrchestratorService.endSutExec(dockerExec, false);
                tJobExecOrchestratorService
                        .stopManageSutByExternalElasticsearch(
                                dockerExec.getTJobExec());
            } catch (Exception e) {
                logger.error(
                        "Error desprovisioning a SUT used in a TJob run from Jenkins.");
            }
        }
        
        tJobExec.setResult(ResultEnum.values()[result]);
        tJobExec.setResultMsg("Finished: " + tJobExec.getResult());
        tJobExec.setEndDate(new Date());
        tJobExecRepositoryImpl.save(tJobExec);
    }

    public void deleteTJobExec(Long tJobExecId) {
        TJobExecution tJobExec = tJobExecRepositoryImpl.findById(tJobExecId)
                .get();
        tJobExecRepositoryImpl.delete(tJobExec);
    }

    public TJob getTJobById(Long tJobId) {
        return tJobRepo.findById(tJobId).get();
    }

    public TJob getTJobByName(String name) {
        return tJobRepo.findByName(name);
    }

    public List<TJobExecution> getAllTJobExecs() {
        return tJobExecRepositoryImpl.findAll();
    }

    public List<TJobExecution> getLastNTJobExecs(Long number) {
        Pageable lastN = PageRequest.of(0, number.intValue(), Direction.DESC,
                "id");
        return tJobExecRepositoryImpl.findWithPageable(lastN);
    }

    public List<TJobExecution> getLastNTJobExecsWithoutChilds(Long number) {
        Pageable lastN = PageRequest.of(0, number.intValue(), Direction.DESC,
                "id");
        return tJobExecRepositoryImpl.findWithPageableWithoutChilds(lastN);
    }

    public List<TJobExecution> getAllRunningTJobExecs() {
        return this.tJobExecRepositoryImpl
                .findByResults(ResultEnum.getNotFinishedOrExecutedResultList());
    }

    public List<TJobExecution> getAllRunningTJobExecsWithoutChilds() {
        return this.tJobExecRepositoryImpl.findByResultsWithoutChilds(
                ResultEnum.getNotFinishedOrExecutedResultList());
    }

    public List<TJobExecution> getLastNRunningTJobExecs(Long number) {
        Pageable lastN = PageRequest.of(0, number.intValue(), Direction.DESC,
                "id");

        return tJobExecRepositoryImpl.findByResultsWithPageable(
                ResultEnum.getNotFinishedOrExecutedResultList(), lastN);
    }

    public List<TJobExecution> getLastNRunningTJobExecsWithoutChilds(
            Long number) {
        Pageable lastN = PageRequest.of(0, number.intValue(), Direction.DESC,
                "id");

        return tJobExecRepositoryImpl.findByResultsWithPageableWithoutChilds(
                ResultEnum.getNotFinishedOrExecutedResultList(), lastN);
    }

    public List<TJobExecution> getAllFinishedOrNotExecutedTJobExecs() {
        return this.tJobExecRepositoryImpl.findByResults(
                ResultEnum.getFinishedAndNotExecutedResultList());
    }

    public List<TJobExecution> getLastNFinishedOrNotExecutedTJobExecsWithoutChilds(
            Long number) {
        Pageable lastN = PageRequest.of(0, number.intValue(), Direction.DESC,
                "id");

        return tJobExecRepositoryImpl.findByResultsWithPageableWithoutChilds(
                ResultEnum.getFinishedAndNotExecutedResultList(), lastN);
    }

    public List<TJobExecution> getLastNFinishedOrNotExecutedTJobExecs(
            Long number) {
        Pageable lastN = PageRequest.of(0, number.intValue(), Direction.DESC,
                "id");

        return tJobExecRepositoryImpl.findByResultsWithPageable(
                ResultEnum.getFinishedAndNotExecutedResultList(), lastN);
    }

    public TJobExecution getTJobExecById(Long id) {
        return tJobExecRepositoryImpl.findById(id).get();
    }

    public List<TJobExecution> getTJobsExecutionsByTJobId(Long tJobId) {
        TJob tJob = tJobRepo.findById(tJobId).get();
        return getTJobsExecutionsByTJob(tJob);
    }

    public List<TJobExecution> getTJobsExecutionsByTJobIdWithoutChilds(
            Long tJobId) {
        return tJobExecRepositoryImpl.findTJobIdWithoutChilds(tJobId);
    }

    public List<TJobExecution> getTJobsExecutionsByTJob(TJob tJob) {
        return tJobExecRepositoryImpl.findByTJob(tJob);
    }

    public TJobExecution getTJobsExecution(Long tJobId, Long tJobExecId) {
        TJob tJob = tJobRepo.findById(tJobId).get();
        return tJobExecRepositoryImpl.findByIdAndTJob(tJobExecId, tJob);
    }

    public TJob modifyTJob(TJob tJob) throws RuntimeException {
        if (tJobRepo.findById(tJob.getId()) != null) {
            return tJobRepo.save(tJob);
        } else {
            throw new HTTPException(405);
        }
    }

    public List<TJobExecutionFile> getTJobExecutionFilesUrls(Long tJobId,
            Long tJobExecId) throws InterruptedException {
        return esmService.getTJobExecutionFilesUrls(tJobId, tJobExecId);
    }

    public String getFileUrl(String serviceFilePath) throws IOException {
        String urlResponse = contextPath.replaceFirst("/", "")
                + registryContextPath + "/"
                + serviceFilePath.replace("\\\\", "/");
        return urlResponse;
    }

    public void getFiles(Long tJobId, Long tJobExecId) {

    }

    public TJobExecution getChildTJobExecParent(Long tJobExecId) {
        TJobExecution tJobExec = this.tJobExecRepositoryImpl
                .findById(tJobExecId).get();
        TJobExecution parent = null;
        if (tJobExec.isMultiExecutionChild()
                && tJobExec.getExecParent() != null) {
            parent = this.tJobExecRepositoryImpl
                    .findById(tJobExec.getExecParent().getId()).get();
        }
        return parent;
    }

    public List<TJobExecution> getParentTJobExecChilds(Long tJobExecId) {
        TJobExecution tJobExec = this.tJobExecRepositoryImpl
                .findById(tJobExecId).get();
        List<TJobExecution> childs = new ArrayList<>();
        if (tJobExec.isMultiExecutionParent()
                && tJobExec.getExecChilds() != null) {
            childs = tJobExec.getExecChilds();
        }
        return childs;
    }
}