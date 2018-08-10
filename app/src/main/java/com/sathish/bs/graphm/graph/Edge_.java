
package com.sathish.bs.graphm.graph;


public class Edge_ {

    private EdgeNode edgeNode;
    private Label_ label;
    private Marker marker;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Edge_() {
    }

    /**
     * 
     * @param marker
     * @param edgeNode
     * @param label
     */
    public Edge_(EdgeNode edgeNode, Label_ label, Marker marker) {
        super();
        this.edgeNode = edgeNode;
        this.label = label;
        this.marker = marker;
    }

    public EdgeNode getEdgeNode() {
        return edgeNode;
    }

    public void setEdgeNode(EdgeNode edgeNode) {
        this.edgeNode = edgeNode;
    }

    public Edge_ withEdgeNode(EdgeNode edgeNode) {
        this.edgeNode = edgeNode;
        return this;
    }

    public Label_ getLabel() {
        return label;
    }

    public void setLabel(Label_ label) {
        this.label = label;
    }

    public Edge_ withLabel(Label_ label) {
        this.label = label;
        return this;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public Edge_ withMarker(Marker marker) {
        this.marker = marker;
        return this;
    }

}
