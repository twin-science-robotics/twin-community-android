package com.google.blockly.model;

import java.util.List;

/**
 * Created by mertselcukdemir on 20.11.2018
 * Copyright (c) 2018 YGA to present
 * All rights reserved.
 */
public class ToolboxModel {
    private List<Integer> activeColors;
    private List<Integer> activeIcons;
    private List<Integer> passiveIcons;

    public ToolboxModel(List<Integer> activeColors, List<Integer> activeIcons, List<Integer> passiveIcons) {
        this.activeColors = activeColors;
        this.activeIcons = activeIcons;
        this.passiveIcons = passiveIcons;
    }

    public List<Integer> getActiveColors() {
        return activeColors;
    }

    public void setActiveColors(List<Integer> activeColors) {
        this.activeColors = activeColors;
    }

    public List<Integer> getActiveIcons() {
        return activeIcons;
    }

    public void setActiveIcons(List<Integer> activeIcons) {
        this.activeIcons = activeIcons;
    }

    public List<Integer> getPassiveIcons() {
        return passiveIcons;
    }

    public void setPassiveIcons(List<Integer> passiveIcons) {
        this.passiveIcons = passiveIcons;
    }

}
