package uk.ac.shef.dcs.oak.lodie.table.interpreter.smp;

/**
 * Created by zqz on 23/04/2015.
 */
public class ChangeMessage implements Comparable<ChangeMessage>{
    private double confidence;
    private String label;

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public int compareTo(ChangeMessage o) {
        return Double.valueOf(o.getConfidence()).compareTo(getConfidence());
    }
}
