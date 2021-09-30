/*******************************************************************************
 *
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: GPL-2.0
 *
 * Contributors:
 *     LG Electronics Inc. - Byunghun Jeong, Yangmi Shin, Sunjae Baek
 *     
 *******************************************************************************/
package com.lge.swiftdepends.infrastructure.parser.dependency;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.lge.swiftdepends.domain.Component;
import com.lge.swiftdepends.domain.ComponentType;
import com.lge.swiftdepends.domain.Element;
import com.lge.swiftdepends.domain.Module;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExternalComponentManager {
    private final String fileSyntax = ".(file)";
    private HashMap<String, Component> components = new HashMap<>();
    private static final Logger logger = LogManager.getLogger(ExternalComponentManager.class);
    
    public Component makeModule(String componentName){
        if (components.get(componentName) == null){
            Component module = new Module(componentName, ComponentType.EXTERNAL);
            components.put(componentName, module);
            return module;
        }
        else {
            return components.get(componentName);
        }
    }

    public Component makeElement(String elementName){
        elementName = elementName.replace(fileSyntax, "");
        String moduleName ="";
        try{
            moduleName = elementName.split("\\.")[0];
        }
        catch(Exception e){
            logger.error(elementName);
            return null;
        }

        Component moduleComponent;
        if (components.get(moduleName) == null){
            moduleComponent = makeModule(moduleName);
        } 
        else {
            moduleComponent = components.get(moduleName);
        }

        for (Component comp : moduleComponent.getComponents()) {
            if (comp.getName().equals(elementName)){
                return comp;
            }
        }

        Component elementComponent = new Element(elementName, ComponentType.EXTERNAL);
        moduleComponent.add(elementComponent);

        return elementComponent;
    }

    private void sortComponent(){
        for (Component component : this.components.values()) {
            if( component == null){
                logger.error("Component is null");
            }else{
                try {
                    component.setComponents(component.getComponents()
                                                .stream()
                                                .sorted(Comparator.comparing(Component::getName))
                                                .collect(Collectors.toList()));
                } catch (Exception e) {
                    logger.error("internal ECM sort fail");
                }
            }
        }
    }

    public List<Component> getComponents(){
        sortComponent();

        try {
            return this.components.values().stream()
                                        .sorted(Comparator.comparing(Component::getName))
                                        .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("ECM sort fail");
            return new ArrayList<>(components.values()); 
        }
        
    }
}
