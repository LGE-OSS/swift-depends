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

import static org.junit.Assert.assertEquals;

import com.lge.swiftdepends.domain.Component;

import org.junit.Test;

public class ExternalComponentManagerTest {
    
    @Test
    public void testMakeModule(){
        ExternalComponentManager ecm = new ExternalComponentManager();
        
        final String componentName = "test";

        Component component = ecm.makeModule(componentName);
        assertEquals(componentName, component.getName());
    }

    @Test
    public void testMakeElement(){
        ExternalComponentManager ecm = new ExternalComponentManager();
        
        final String moduleName = "module";
        final String elementName = "module.element";

        ecm.makeElement(elementName);
        Component moduleComponent = ecm.makeModule(moduleName);

        assertEquals(elementName, moduleComponent.getComponents().get(0).getName());
    }
}
