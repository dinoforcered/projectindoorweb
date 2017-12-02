package de.hftstuttgart.projectindoorweb.web;

import de.hftstuttgart.projectindoorweb.application.internal.AssertParam;
import de.hftstuttgart.projectindoorweb.inputHandler.PreProcessingService;
import de.hftstuttgart.projectindoorweb.persistence.PersistencyService;
import de.hftstuttgart.projectindoorweb.persistence.entities.*;
import de.hftstuttgart.projectindoorweb.positionCalculator.PositionCalculatorService;
import de.hftstuttgart.projectindoorweb.web.internal.requests.building.AddNewBuilding;
import de.hftstuttgart.projectindoorweb.web.internal.requests.building.AddNewBuildingPositionAnchor;
import de.hftstuttgart.projectindoorweb.web.internal.requests.building.GetAllBuildings;
import de.hftstuttgart.projectindoorweb.web.internal.requests.positioning.*;
import de.hftstuttgart.projectindoorweb.web.internal.requests.project.*;
import de.hftstuttgart.projectindoorweb.web.internal.util.TransmissionHelper;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class RestTransmissionServiceImpl implements RestTransmissionService {

    private PersistencyService persistencyService;
    private PreProcessingService preProcessingService;
    private PositionCalculatorService positionCalculatorService;

    public RestTransmissionServiceImpl(PersistencyService persistencyService, PreProcessingService preProcessingService,
                                       PositionCalculatorService positionCalculatorService) {
        this.persistencyService = persistencyService;
        this.preProcessingService = preProcessingService;
        this.positionCalculatorService = positionCalculatorService;
    }

    @Override
    public boolean processEvaalFiles(String buildingIdentifier, boolean evaluationFiles, MultipartFile[] radioMapFiles) {

        if (buildingIdentifier == null || buildingIdentifier.isEmpty()
                || radioMapFiles == null || radioMapFiles.length == 0) {
            return false;
        }

        File[] radioMapFileArray = new File[radioMapFiles.length];

        try {
            for (int i = 0; i < radioMapFiles.length; i++) {
                radioMapFileArray[i] = TransmissionHelper.convertMultipartFileToLocalFile(radioMapFiles[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        try {
            long buildingId = Long.valueOf(buildingIdentifier);
            Building building = this.persistencyService.getBuildingById(buildingId);

            if (building != null) {
                List<EvaalFile> processedEvaalFiles = this.preProcessingService.processIntoLogFiles(building, evaluationFiles, radioMapFileArray);
                return this.persistencyService.saveEvaalFiles(processedEvaalFiles);
            } else {
                return false;
            }


        } catch (NumberFormatException ex) {
            return false;
        }


    }

    @Override
    public List<GeneratePositionResult> generatePositionResults(GenerateBatchPositionResults generateBatchPositionResults) {

        List<GeneratePositionResult> result = new ArrayList<>();

        if (generateBatchPositionResults == null) {
            return result;
        }

        try {
            long buildingId = generateBatchPositionResults.getBuildingIdentifier();
            Building building = this.persistencyService.getBuildingById(buildingId);

            EvaalFile evaluationFile = this.persistencyService.getEvaalFileForId(generateBatchPositionResults.getEvalFileIdentifier());

            Long[] radioMapFileIds = generateBatchPositionResults.getRadioMapFileIdentifiers();
            EvaalFile[] radioMapFiles = new EvaalFile[radioMapFileIds.length];


            for (int i = 0; i < radioMapFileIds.length; i++) {
                radioMapFiles[i] = this.persistencyService.getEvaalFileForId(radioMapFileIds[i]);
            }

            if (evaluationFile != null && TransmissionHelper.areRequestedFilesPresent(radioMapFiles)) {

                String projectName = String.format("AutoGenerated_%d", System.currentTimeMillis());
                String algorithmType = generateBatchPositionResults.getAlgorithmType();
                Set<SaveNewProjectParameters> saveNewProjectParamaters = generateBatchPositionResults.getSaveNewProjectParamaters();

                long projectId = this.persistencyService.createNewProject(projectName, algorithmType, saveNewProjectParamaters);
                Project autoGeneratedProject = this.persistencyService.getProjectById(projectId);

                if (autoGeneratedProject != null) {
                    List<WifiPositionResult> retrievedWifiResults =
                            (List<WifiPositionResult>) this.positionCalculatorService.
                                    calculatePositions(evaluationFile, radioMapFiles, building, generateBatchPositionResults.isWithPixelPosition());

                    result = TransmissionHelper.convertToCalculatedPositions(retrievedWifiResults);
                }

            }

        } catch (NumberFormatException ex) {
            ex.printStackTrace();

        } finally {
            return result;
        }

    }

    @Override
    public GeneratePositionResult getPositionForWifiReading(GenerateSinglePositionResult generateSinglePositionResult) {

        if (generateSinglePositionResult == null) {

            return createEmptyCalculatedPosition();
        }

        Long[] radioMapFileIds = generateSinglePositionResult.getRadioMapFileIdentifiers();
        EvaalFile[] radioMapFiles = new EvaalFile[radioMapFileIds.length];


        for (int i = 0; i < radioMapFileIds.length; i++) {
            radioMapFiles[i] = this.persistencyService.getEvaalFileForId(radioMapFileIds[i]);
        }

        if (TransmissionHelper.areRequestedFilesPresent(radioMapFiles)) {
            WifiPositionResult retrievedWifiResult = (WifiPositionResult) this.positionCalculatorService
                    .calculateSinglePosition(generateSinglePositionResult.getWifiReading(), radioMapFiles, null, false);
            return TransmissionHelper.convertToCalculatedPosition(retrievedWifiResult);
        }

        return createEmptyCalculatedPosition();

    }

    @Override
    public List<GeneratePositionResult> getPositionResultsForProjectIdentifier(String projectIdentifier) {

        List<GeneratePositionResult> result = new ArrayList<>();

        if (AssertParam.isNullOrEmpty(projectIdentifier)) {
            return result;
        }

        /*

        TODO Clarify feasability of this method! Reason:

        The calculated results not only depend on the project, but also on the eval file or wifi lines
        that were passed in for their calculations. As a result, the project ID cannot uniquely identify a set of
        calculated positions.

        */
        return result;


    }

    @Override
    public long saveNewProject(SaveNewProject saveNewProject) {

        if (saveNewProject == null) {
            return -1;
        }

        return this.persistencyService.createNewProject(saveNewProject.getProjectName(),
                saveNewProject.getAlgorithmType(),
                saveNewProject.getSaveNewProjectParametersSet());

    }

    @Override
    public boolean saveCurrentProject(SaveCurrentProject saveCurrentProject) {


        if (saveCurrentProject == null) {
            return false;
        }

        try {

            long projectId = Long.parseLong(saveCurrentProject.getProjectIdentifier());
            
            return this.persistencyService.updateProject(projectId,
                    saveCurrentProject.getProjectName(),
                    saveCurrentProject.getAlgorithmType(),
                    saveCurrentProject.getSaveNewProjectParametersSet());

        } catch (NumberFormatException ex) {
            return false;
        }

    }

    @Override
    public boolean deleteSelectedProject(String projectIdentifier) {

        if (AssertParam.isNullOrEmpty(projectIdentifier)) {
            return false;
        }

        try {
            long projectId = Long.parseLong(projectIdentifier);
            return this.persistencyService.deleteProject(projectId);
        } catch (NumberFormatException ex) {
            return false;
        }

    }

    @Override
    public LoadSelectedProject loadSelectedProject(String projectIdentifier) {

        if (AssertParam.isNullOrEmpty(projectIdentifier)) {
            return createEmptyProjectElement();
        }

        try {
            long projectId = Long.parseLong(projectIdentifier);
            Project project = this.persistencyService.getProjectById(projectId);

            if (project != null) {
                return new LoadSelectedProject(project.getProjectName(), String.valueOf(project.getId()),
                        getProjectParametersFromInternalEntity(project.getProjectParameters()));
            }

        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }

        return createEmptyProjectElement();
    }

    @Override
    public List<LoadSelectedProject> getAllProjects() {

        List<Project> projects = this.persistencyService.getAllProjects();

        return convertToProjectElements(projects);

    }

    @Override
    public List<GetAllBuildings> getAllBuildings() {
        List<Building> buildings = this.persistencyService.getAllBuildings();

        return TransmissionHelper.convertToBuildingSmallJsonWrapper(buildings);
    }

    @Override
    public List<GetAllAlgorithmTypes> getAllAlgorithmTypes() {//TODO use reflection instead if viable
        List<GetAllAlgorithmTypes> result = new ArrayList<>();

        GetAllAlgorithmTypes wifiAlgorithm = new GetAllAlgorithmTypes("WifiPositionCalculatorServiceImpl", "WIFI");

        result.add(wifiAlgorithm);

        return result;
    }

    @Override
    public List<GetEvaluationFilesForBuilding> getEvaluationFilesForBuilding(String buildingIdentifier) {

        List<GetEvaluationFilesForBuilding> result = new ArrayList<>();

        if (AssertParam.isNullOrEmpty(buildingIdentifier)) {
            return result;
        }

        try {

            long buildingId = Long.valueOf(buildingIdentifier);
            Building building = this.persistencyService.getBuildingById(buildingId);
            if (building != null) {
                List<EvaalFile> evaalFiles = this.persistencyService.getEvaluationFilesForBuilding(building);
                result = TransmissionHelper.convertToEvaluationEntries(evaalFiles);
            }


        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        } finally {
            return result;
        }


    }

    @Override
    public List<GetRadioMapFilesForBuilding> getRadioMapFilesForBuilding(String buildingIdentifier) {

        List<GetRadioMapFilesForBuilding> result = new ArrayList<>();
        if (AssertParam.isNullOrEmpty(buildingIdentifier)) {
            return result;
        }

        try {

            long buildingId = Long.valueOf(buildingIdentifier);
            Building building = this.persistencyService.getBuildingById(buildingId);
            if (building != null) {
                List<EvaalFile> evaalFiles = this.persistencyService.getRadioMapFilesForBuiling(building);
                result = TransmissionHelper.convertToRadioMapEntry(evaalFiles);
            }


        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        } finally {
            return result;
        }

    }

    @Override
    public List<GetAlgorithmParameters> getAlgorithmParameterListForAlgorithmId(String algorithmIdentifier) {
        List<GetAlgorithmParameters> result = new ArrayList<>();
        if (AssertParam.isNullOrEmpty(algorithmIdentifier)) {
            return result;
        }
        return result;//TODO implement when ready
    }

    @Override
    public boolean addNewBuilding(AddNewBuilding buildingJsonWrapper) {

        if (buildingJsonWrapper == null) {
            return false;
        }
        try {

            String buildingName = buildingJsonWrapper.getBuildingName();
            int numberOfFloors = buildingJsonWrapper.getNumberOfFloors();
            int imagePixelWidth = buildingJsonWrapper.getImagePixelWidth();
            int imagePixelHeight = buildingJsonWrapper.getImagePixelHeight();
            AddNewBuildingPositionAnchor southEastAnchor = buildingJsonWrapper.getSouthEast();
            AddNewBuildingPositionAnchor southWestAnchor = buildingJsonWrapper.getSouthWest();
            AddNewBuildingPositionAnchor northEastAnchor = buildingJsonWrapper.getNorthEast();
            AddNewBuildingPositionAnchor northWestAnchor = buildingJsonWrapper.getNorthWest();

            return this.persistencyService.addNewBuilding(buildingName, numberOfFloors, imagePixelWidth, imagePixelHeight,
                    southEastAnchor, southWestAnchor, northEastAnchor, northWestAnchor);
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private GeneratePositionResult createEmptyCalculatedPosition() {
        return new GeneratePositionResult(0, 0, 0, false, "");
    }

    private LoadSelectedProject createEmptyProjectElement() {
        return new LoadSelectedProject("", "", new HashSet<>());
    }

    private List<LoadSelectedProject> convertToProjectElements(List<Project> projects) {

        List<LoadSelectedProject> result = new ArrayList<>(projects.size());

        for (Project project :
                projects) {
            result.add(new LoadSelectedProject(project.getProjectName(), String.valueOf(project.getId()),
                    getProjectParametersFromInternalEntity(project.getProjectParameters())));
        }

        return result;


    }

    private Set<SaveNewProjectParameters> getProjectParametersFromInternalEntity(List<Parameter> parameters) {

        Set<SaveNewProjectParameters> saveNewProjectParamaters = new LinkedHashSet<>();

        for (Parameter parameter :
                parameters) {
            saveNewProjectParamaters.add(new SaveNewProjectParameters(parameter.getParameterName(), parameter.getParamenterValue()));
        }

        return saveNewProjectParamaters;


    }


}
