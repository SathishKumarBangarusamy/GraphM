
package com.sathish.bs.graphm.graph;


public class Vertex_ {

    private Inner inner;
    private Outer outer;
    private Label label;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Vertex_() {
    }

    /**
     * 
     * @param outer
     * @param inner
     * @param label
     */
    public Vertex_(Inner inner, Outer outer, Label label) {
        super();
        this.inner = inner;
        this.outer = outer;
        this.label = label;
    }

    public Inner getInner() {
        return inner;
    }

    public void setInner(Inner inner) {
        this.inner = inner;
    }

    public Vertex_ withInner(Inner inner) {
        this.inner = inner;
        return this;
    }

    public Outer getOuter() {
        return outer;
    }

    public void setOuter(Outer outer) {
        this.outer = outer;
    }

    public Vertex_ withOuter(Outer outer) {
        this.outer = outer;
        return this;
    }

    public Label getLabel() {
        return label;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    public Vertex_ withLabel(Label label) {
        this.label = label;
        return this;
    }

}
