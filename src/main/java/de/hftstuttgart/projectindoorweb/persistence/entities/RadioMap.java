package de.hftstuttgart.projectindoorweb.persistence.entities;

import java.util.List;

public class RadioMap extends ModelBase {


    //TODO Add 2 new coordinates for latlong

    private List<RadioMapElement> radioMapElements;


    public RadioMap(List<RadioMapElement> radioMapElements) {
        this.radioMapElements = radioMapElements;
    }


    public List<RadioMapElement> getRadioMapElements() {
        return radioMapElements;
    }

    public void setRadioMapElements(List<RadioMapElement> radioMapElements) {
        this.radioMapElements = radioMapElements;
    }
}
