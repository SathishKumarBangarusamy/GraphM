
package com.sathish.bs.graphm.graph;


public class Vertex {

    private String value;
    private String displayValue;
    private Position position;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Vertex() {
    }

    /**
     * 
     * @param position
     * @param value
     * @param displayValue
     */
    public Vertex(String value, String displayValue, Position position) {
        super();
        this.value = value;
        this.displayValue = displayValue;
        this.position = position;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Vertex withValue(String value) {
        this.value = value;
        return this;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public void setDisplayValue(String displayValue) {
        this.displayValue = displayValue;
    }

    public Vertex withDisplayValue(String displayValue) {
        this.displayValue = displayValue;
        return this;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Vertex withPosition(Position position) {
        this.position = position;
        return this;
    }

}
