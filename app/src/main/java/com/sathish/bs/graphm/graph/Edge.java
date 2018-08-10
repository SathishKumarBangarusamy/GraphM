
package com.sathish.bs.graphm.graph;


public class Edge {

    private Vertex1 vertex1;
    private Vertex2 vertex2;
    private String value;
    private String displayValue;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Edge() {
    }

    /**
     * 
     * @param vertex1
     * @param vertex2
     * @param value
     * @param displayValue
     */
    public Edge(Vertex1 vertex1, Vertex2 vertex2, String value, String displayValue) {
        super();
        this.vertex1 = vertex1;
        this.vertex2 = vertex2;
        this.value = value;
        this.displayValue = displayValue;
    }

    public Vertex1 getVertex1() {
        return vertex1;
    }

    public void setVertex1(Vertex1 vertex1) {
        this.vertex1 = vertex1;
    }

    public Edge withVertex1(Vertex1 vertex1) {
        this.vertex1 = vertex1;
        return this;
    }

    public Vertex2 getVertex2() {
        return vertex2;
    }

    public void setVertex2(Vertex2 vertex2) {
        this.vertex2 = vertex2;
    }

    public Edge withVertex2(Vertex2 vertex2) {
        this.vertex2 = vertex2;
        return this;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Edge withValue(String value) {
        this.value = value;
        return this;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public void setDisplayValue(String displayValue) {
        this.displayValue = displayValue;
    }

    public Edge withDisplayValue(String displayValue) {
        this.displayValue = displayValue;
        return this;
    }

}
