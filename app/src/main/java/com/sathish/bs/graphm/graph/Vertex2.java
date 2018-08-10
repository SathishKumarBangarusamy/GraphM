
package com.sathish.bs.graphm.graph;


public class Vertex2 {

    private String value;
    private String name;
    private String symbol;
    private String alias;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Vertex2() {
    }

    /**
     * 
     * @param symbol
     * @param alias
     * @param name
     * @param value
     */
    public Vertex2(String value, String name, String symbol, String alias) {
        super();
        this.value = value;
        this.name = name;
        this.symbol = symbol;
        this.alias = alias;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Vertex2 withValue(String value) {
        this.value = value;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Vertex2 withName(String name) {
        this.name = name;
        return this;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Vertex2 withSymbol(String symbol) {
        this.symbol = symbol;
        return this;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Vertex2 withAlias(String alias) {
        this.alias = alias;
        return this;
    }

}