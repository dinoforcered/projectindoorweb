package de.hftstuttgart.projectindoorweb.persistence.entities;


import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.List;

@Entity
public class WifiBlock {

    @Id
    @GeneratedValue
    private Long id;

    @ElementCollection
    private List<String> wifiLines;

    protected WifiBlock() {
    }

    public WifiBlock(List<String> wifiLines) {
        this.wifiLines = wifiLines;
    }

    public Long getId() {
        return id;
    }

    public List<String> getWifiLines() {
        return wifiLines;
    }

    public void setWifiLines(List<String> wifiLines) {
        this.wifiLines = wifiLines;
    }
}
