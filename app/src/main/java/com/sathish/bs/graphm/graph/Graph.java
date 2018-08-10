
package com.sathish.bs.graphm.graph;

import java.util.ArrayList;
import java.util.List;

public class Graph {

    private List<Vertex> vertices = new ArrayList<>();
    private List<Edge> edges = new ArrayList<>();
    private Style style;
    private String graphKey;
    private List<String> edgeDragger = null;
    private Integer linkDistance;
    private Double graphScale;
    private List<Double> graphTranslate = null;
    private String theme;
    private Boolean zipAllEdges;
    private Boolean animation;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Graph() {
    }

    /**
     * 
     * @param graphKey
     * @param animation
     * @param edgeDragger
     * @param style
     * @param edges
     * @param theme
     * @param vertices
     * @param zipAllEdges
     * @param graphScale
     * @param linkDistance
     * @param graphTranslate
     */
    public Graph(List<Vertex> vertices, List<Edge> edges, Style style, String graphKey, List<String> edgeDragger, Integer linkDistance, Double graphScale, List<Double> graphTranslate, String theme, Boolean zipAllEdges, Boolean animation) {
        super();
        this.vertices = vertices;
        this.edges = edges;
        this.style = style;
        this.graphKey = graphKey;
        this.edgeDragger = edgeDragger;
        this.linkDistance = linkDistance;
        this.graphScale = graphScale;
        this.graphTranslate = graphTranslate;
        this.theme = theme;
        this.zipAllEdges = zipAllEdges;
        this.animation = animation;
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    public void setVertices(List<Vertex> vertices) {
        this.vertices = vertices;
    }

    public Graph withVertices(List<Vertex> vertices) {
        this.vertices = vertices;
        return this;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void setEdges(List<Edge> edges) {
        this.edges = edges;
    }

    public Graph withEdges(List<Edge> edges) {
        this.edges = edges;
        return this;
    }

    public Style getStyle() {
        return style;
    }

    public void setStyle(Style style) {
        this.style = style;
    }

    public Graph withStyle(Style style) {
        this.style = style;
        return this;
    }

    public String getGraphKey() {
        return graphKey;
    }

    public void setGraphKey(String graphKey) {
        this.graphKey = graphKey;
    }

    public Graph withGraphKey(String graphKey) {
        this.graphKey = graphKey;
        return this;
    }

    public List<String> getEdgeDragger() {
        return edgeDragger;
    }

    public void setEdgeDragger(List<String> edgeDragger) {
        this.edgeDragger = edgeDragger;
    }

    public Graph withEdgeDragger(List<String> edgeDragger) {
        this.edgeDragger = edgeDragger;
        return this;
    }

    public Integer getLinkDistance() {
        return linkDistance;
    }

    public void setLinkDistance(Integer linkDistance) {
        this.linkDistance = linkDistance;
    }

    public Graph withLinkDistance(Integer linkDistance) {
        this.linkDistance = linkDistance;
        return this;
    }

    public Double getGraphScale() {
        return graphScale;
    }

    public void setGraphScale(Double graphScale) {
        this.graphScale = graphScale;
    }

    public Graph withGraphScale(Double graphScale) {
        this.graphScale = graphScale;
        return this;
    }

    public List<Double> getGraphTranslate() {
        return graphTranslate;
    }

    public void setGraphTranslate(List<Double> graphTranslate) {
        this.graphTranslate = graphTranslate;
    }

    public Graph withGraphTranslate(List<Double> graphTranslate) {
        this.graphTranslate = graphTranslate;
        return this;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public Graph withTheme(String theme) {
        this.theme = theme;
        return this;
    }

    public Boolean getZipAllEdges() {
        return zipAllEdges;
    }

    public void setZipAllEdges(Boolean zipAllEdges) {
        this.zipAllEdges = zipAllEdges;
    }

    public Graph withZipAllEdges(Boolean zipAllEdges) {
        this.zipAllEdges = zipAllEdges;
        return this;
    }

    public Boolean getAnimation() {
        return animation;
    }

    public void setAnimation(Boolean animation) {
        this.animation = animation;
    }

    public Graph withAnimation(Boolean animation) {
        this.animation = animation;
        return this;
    }

}
