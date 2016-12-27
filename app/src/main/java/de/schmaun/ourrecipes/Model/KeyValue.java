package de.schmaun.ourrecipes.Model;

public class KeyValue {

    private long id;
    private String key;
    private String value;

    public KeyValue(String key) {
        this.key = key;
    }

    public KeyValue(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String toString() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
