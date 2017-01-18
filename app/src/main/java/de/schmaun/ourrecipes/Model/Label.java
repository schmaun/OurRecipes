package de.schmaun.ourrecipes.Model;

import java.io.Serializable;

public class Label implements Serializable {

    private long id;
    private String name;

    public Label(){}
    public Label(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() { return name; }
}
