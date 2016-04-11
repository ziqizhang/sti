package uk.ac.shef.dcs.sti.core.algorithm.smp;

import java.util.ArrayList;
import java.util.List;

/**
 * .
 */
class ChangeMessage implements Comparable<ChangeMessage> {
    protected double confidence;
    protected List<String> labels;    //values have OR logic relations

    public ChangeMessage() {
        labels = new ArrayList<String>();
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void addLabel(String label) {
        this.labels.add(label);
    }

    @Override
    public int compareTo(ChangeMessage o) {
        return Double.valueOf(o.getConfidence()).compareTo(getConfidence());
    }
}
