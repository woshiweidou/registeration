package com.mvc;

import java.util.HashMap;

public class ModelAndView {
    private String viewName;
    private HashMap<String, Object> attributeMap = new HashMap();

    public ModelAndView() {
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public void addObject(String key, Object value) {
        this.attributeMap.put(key, value);
    }

    String getViewName() {
        return this.viewName;
    }

    Object getObject(String key) {
        return this.attributeMap.get(key);
    }

    HashMap<String, Object> getAttributeMap() {
        return this.attributeMap;
    }
}
