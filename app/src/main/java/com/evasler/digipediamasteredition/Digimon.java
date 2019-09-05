package com.evasler.digipediamasteredition;

import org.jetbrains.annotations.NotNull;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Digimon {

    @PrimaryKey
    @NotNull
    private String name;

    private String attribute;

    private String level;

    private String family;

    private String type;

    private String description;

    private String attacks;

    private String prior_forms;

    private String next_forms;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAttacks() {
        return attacks;
    }

    public void setAttacks(String attacks) {
        this.attacks = attacks;
    }

    public String getPrior_forms() {
        return prior_forms;
    }

    public void setPrior_forms(String prior_forms) {
        this.prior_forms = prior_forms;
    }

    public String getNext_forms() {
        return next_forms;
    }

    public void setNext_forms(String next_forms) {
        this.next_forms = next_forms;
    }
}
