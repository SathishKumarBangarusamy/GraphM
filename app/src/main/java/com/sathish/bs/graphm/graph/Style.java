
package com.sathish.bs.graphm.graph;


public class Style {

    private Groups groups;
    private Vertex_ vertex;
    private Edge_ edge;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Style() {
    }

    /**
     * 
     * @param edge
     * @param vertex
     * @param groups
     */
    public Style(Groups groups, Vertex_ vertex, Edge_ edge) {
        super();
        this.groups = groups;
        this.vertex = vertex;
        this.edge = edge;
    }

    public Groups getGroups() {
        return groups;
    }

    public void setGroups(Groups groups) {
        this.groups = groups;
    }

    public Style withGroups(Groups groups) {
        this.groups = groups;
        return this;
    }

    public Vertex_ getVertex() {
        return vertex;
    }

    public void setVertex(Vertex_ vertex) {
        this.vertex = vertex;
    }

    public Style withVertex(Vertex_ vertex) {
        this.vertex = vertex;
        return this;
    }

    public Edge_ getEdge() {
        return edge;
    }

    public void setEdge(Edge_ edge) {
        this.edge = edge;
    }

    public Style withEdge(Edge_ edge) {
        this.edge = edge;
        return this;
    }

}
