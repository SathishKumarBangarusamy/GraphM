
package com.sathish.bs.graphm.graph;


public class Position {

    private Double x;
    private Double y;
    private Double z;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Position() {
    }

    /**
     * 
     * @param z
     * @param y
     * @param x
     */
    public Position(Double x, Double y, Double z) {
        super();
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Position withX(Double x) {
        this.x = x;
        return this;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public Position withY(Double y) {
        this.y = y;
        return this;
    }

    public Double getZ() {
        return z;
    }

    public void setZ(Double z) {
        this.z = z;
    }

    public Position withZ(Double z) {
        this.z = z;
        return this;
    }

}
