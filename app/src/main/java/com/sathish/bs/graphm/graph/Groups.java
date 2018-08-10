
package com.sathish.bs.graphm.graph;


public class Groups {

    private ZgClosure zgClosure;
    private ZgFaded zgFaded;
    private ZgSelected zgSelected;
    private ZgSelectedIn zgSelectedIn;
    private ZgPressed zgPressed;
    private ZgClosureIn zgClosureIn;
    private ZgFadedIn zgFadedIn;
    private ZgInvalid zgInvalid;
    private ZgNew zgNew;
    private ZgHighlight zgHighlight;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Groups() {
    }

    /**
     * 
     * @param zgClosureIn
     * @param zgHighlight
     * @param zgFadedIn
     * @param zgPressed
     * @param zgClosure
     * @param zgSelected
     * @param zgSelectedIn
     * @param zgFaded
     * @param zgNew
     * @param zgInvalid
     */
    public Groups(ZgClosure zgClosure, ZgFaded zgFaded, ZgSelected zgSelected, ZgSelectedIn zgSelectedIn, ZgPressed zgPressed, ZgClosureIn zgClosureIn, ZgFadedIn zgFadedIn, ZgInvalid zgInvalid, ZgNew zgNew, ZgHighlight zgHighlight) {
        super();
        this.zgClosure = zgClosure;
        this.zgFaded = zgFaded;
        this.zgSelected = zgSelected;
        this.zgSelectedIn = zgSelectedIn;
        this.zgPressed = zgPressed;
        this.zgClosureIn = zgClosureIn;
        this.zgFadedIn = zgFadedIn;
        this.zgInvalid = zgInvalid;
        this.zgNew = zgNew;
        this.zgHighlight = zgHighlight;
    }

    public ZgClosure getZgClosure() {
        return zgClosure;
    }

    public void setZgClosure(ZgClosure zgClosure) {
        this.zgClosure = zgClosure;
    }

    public Groups withZgClosure(ZgClosure zgClosure) {
        this.zgClosure = zgClosure;
        return this;
    }

    public ZgFaded getZgFaded() {
        return zgFaded;
    }

    public void setZgFaded(ZgFaded zgFaded) {
        this.zgFaded = zgFaded;
    }

    public Groups withZgFaded(ZgFaded zgFaded) {
        this.zgFaded = zgFaded;
        return this;
    }

    public ZgSelected getZgSelected() {
        return zgSelected;
    }

    public void setZgSelected(ZgSelected zgSelected) {
        this.zgSelected = zgSelected;
    }

    public Groups withZgSelected(ZgSelected zgSelected) {
        this.zgSelected = zgSelected;
        return this;
    }

    public ZgSelectedIn getZgSelectedIn() {
        return zgSelectedIn;
    }

    public void setZgSelectedIn(ZgSelectedIn zgSelectedIn) {
        this.zgSelectedIn = zgSelectedIn;
    }

    public Groups withZgSelectedIn(ZgSelectedIn zgSelectedIn) {
        this.zgSelectedIn = zgSelectedIn;
        return this;
    }

    public ZgPressed getZgPressed() {
        return zgPressed;
    }

    public void setZgPressed(ZgPressed zgPressed) {
        this.zgPressed = zgPressed;
    }

    public Groups withZgPressed(ZgPressed zgPressed) {
        this.zgPressed = zgPressed;
        return this;
    }

    public ZgClosureIn getZgClosureIn() {
        return zgClosureIn;
    }

    public void setZgClosureIn(ZgClosureIn zgClosureIn) {
        this.zgClosureIn = zgClosureIn;
    }

    public Groups withZgClosureIn(ZgClosureIn zgClosureIn) {
        this.zgClosureIn = zgClosureIn;
        return this;
    }

    public ZgFadedIn getZgFadedIn() {
        return zgFadedIn;
    }

    public void setZgFadedIn(ZgFadedIn zgFadedIn) {
        this.zgFadedIn = zgFadedIn;
    }

    public Groups withZgFadedIn(ZgFadedIn zgFadedIn) {
        this.zgFadedIn = zgFadedIn;
        return this;
    }

    public ZgInvalid getZgInvalid() {
        return zgInvalid;
    }

    public void setZgInvalid(ZgInvalid zgInvalid) {
        this.zgInvalid = zgInvalid;
    }

    public Groups withZgInvalid(ZgInvalid zgInvalid) {
        this.zgInvalid = zgInvalid;
        return this;
    }

    public ZgNew getZgNew() {
        return zgNew;
    }

    public void setZgNew(ZgNew zgNew) {
        this.zgNew = zgNew;
    }

    public Groups withZgNew(ZgNew zgNew) {
        this.zgNew = zgNew;
        return this;
    }

    public ZgHighlight getZgHighlight() {
        return zgHighlight;
    }

    public void setZgHighlight(ZgHighlight zgHighlight) {
        this.zgHighlight = zgHighlight;
    }

    public Groups withZgHighlight(ZgHighlight zgHighlight) {
        this.zgHighlight = zgHighlight;
        return this;
    }

}
