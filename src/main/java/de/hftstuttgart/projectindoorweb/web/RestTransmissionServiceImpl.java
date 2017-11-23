package de.hftstuttgart.projectindoorweb.web;

import de.hftstuttgart.projectindoorweb.application.internal.AssertParam;
import de.hftstuttgart.projectindoorweb.inputHandler.PreProcessingServiceComponent;
import de.hftstuttgart.projectindoorweb.persistence.PersistencyServiceComponent;
import de.hftstuttgart.projectindoorweb.persistence.entities.Parameter;
import de.hftstuttgart.projectindoorweb.persistence.entities.Project;
import de.hftstuttgart.projectindoorweb.persistence.entities.RadioMap;
import de.hftstuttgart.projectindoorweb.web.internal.CalculatedPosition;
import de.hftstuttgart.projectindoorweb.web.internal.ProjectElement;
import de.hftstuttgart.projectindoorweb.web.internal.ProjectParameter;

import java.io.File;
import java.util.*;

public class RestTransmissionServiceImpl implements RestTransmissionService {






    @Override
    public boolean generateRadioMap(String projectIdentifier, String buildingIdentifier, List<File> radioMapFiles) {

        if (projectIdentifier == null || projectIdentifier.isEmpty()
                || buildingIdentifier == null || buildingIdentifier.isEmpty()
                || radioMapFiles == null || radioMapFiles.isEmpty()) {
            return false;
        }

        File[] radioMapFileArray = radioMapFiles.toArray(new File[radioMapFiles.size()]);
        List<RadioMap> generatedRadioMaps = PreProcessingServiceComponent.getPreProcessingService().generateRadioMap(radioMapFileArray);

        return true;
    }

    @Override
    public boolean generatePositionResults(List<File> evaluationFiles) {

        if (evaluationFiles == null || evaluationFiles.size() == 0) {
            return false;
        }

        File[] evaluationFileArray = evaluationFiles.toArray(new File[evaluationFiles.size()]);


        return false;
    }

    @Override
    public CalculatedPosition getPositionForWifiReading(String wifiReading) {

        if (AssertParam.isNullOrEmpty(wifiReading)) {
            return createEmptyCalculatedPosition();
        }

        return new CalculatedPosition(0, 0, 0, false, ""); //TODO implement when ready
    }

    @Override
    public List<CalculatedPosition> getPositionResultsForIdentifier(String positionIdentifier) {

        List<CalculatedPosition> result = new ArrayList<>();

        if (AssertParam.isNullOrEmpty(positionIdentifier)) {
            return result;
        }

        return result;//TODO implement when ready
    }

    @Override
    public long saveNewProject(Set<ProjectParameter> projectParameterSet, String projectName, String algorithmType) {

        if (projectParameterSet == null || projectParameterSet.size() == 0 || AssertParam.isNullOrEmpty(projectName) || AssertParam.isNullOrEmpty(algorithmType)) {
            return -1;
        }

        long generatedProjectId = PersistencyServiceComponent.getPersistencyService()
                .createNewProject(projectName, algorithmType, projectParameterSet);

        return generatedProjectId;
    }

    @Override
    public boolean saveCurrentProject(String projectName, Set<ProjectParameter> projectParameterSet, String projectIdentifier, String algorithmType) {


        if (projectName == null || projectParameterSet == null ||
                projectParameterSet.size() == 0 || AssertParam.isNullOrEmpty(projectIdentifier)
                || AssertParam.isNullOrEmpty(algorithmType)) {
            return false;
        }

        boolean updateSuccess = false;
        try {
            long projectId = Long.parseLong(projectIdentifier);
            updateSuccess = PersistencyServiceComponent.getPersistencyService().updateProject(projectId, projectName, algorithmType, projectParameterSet);
        } catch (NumberFormatException ex) {
            updateSuccess = false;
        } finally {
            return updateSuccess;
        }

    }

    @Override
    public boolean deleteSelectedProject(String projectIdentifier) {

        if (AssertParam.isNullOrEmpty(projectIdentifier)) {
            return false;
        }

        boolean deletionSuccess = false;
        try {
            long projectId = Long.parseLong(projectIdentifier);
            deletionSuccess = PersistencyServiceComponent.getPersistencyService().deleteProject(projectId);
        } catch (NumberFormatException ex) {
            deletionSuccess = false;
        } finally {
            return deletionSuccess;
        }

    }

    @Override
    public ProjectElement loadSelectedProject(String projectIdentifier) {
        if (AssertParam.isNullOrEmpty(projectIdentifier)) {
            return createEmptyProjectElement();
        }

        Project project = null;
        try {
            long projectId = Long.parseLong(projectIdentifier);
            project = PersistencyServiceComponent.getPersistencyService().getProjectById(projectId);
        } catch (NumberFormatException ex) {
            project = null;
        }

        ProjectElement element;
        if (project != null) {
            element = new ProjectElement(project.getProjectName(), String.valueOf(project.getId()),
                    getProjectParametersFromInternalEntity(project.getProjectParameters()));
        } else {
            element = createEmptyProjectElement();
        }

        return element;
    }

    @Override
    public List<ProjectElement> getAllProjects() {

        List<Project> projects = PersistencyServiceComponent.getPersistencyService().getAllProjects();

        return convertToProjectElements(projects);

    }

    private CalculatedPosition createEmptyCalculatedPosition() {
        return new CalculatedPosition(0, 0, 0, false, "");
    }

    private ProjectElement createEmptyProjectElement() {
        return new ProjectElement("", "", new HashSet<>());
    }

    private List<ProjectElement> convertToProjectElements(List<Project> projects) {

        List<ProjectElement> result = new ArrayList<>(projects.size());

        for (Project project :
                projects) {
            result.add(new ProjectElement(project.getProjectName(), String.valueOf(project.getId()),
                    getProjectParametersFromInternalEntity(project.getProjectParameters())));
        }

        return result;


    }

    private Set<ProjectParameter> getProjectParametersFromInternalEntity(List<Parameter> parameters) {

        Set<ProjectParameter> projectParameters = new LinkedHashSet<>();

        for (Parameter parameter :
                parameters) {
            projectParameters.add(new ProjectParameter(parameter.getParameterName(), parameter.getParamenterValue()));
        }

        return projectParameters;


    }


}
