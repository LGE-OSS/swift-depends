/*******************************************************************************
 *
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: GPL-2.0
 *
 * Contributors:
 *     LG Electronics Inc. - Byunghun Jeong, Yangmi Shin, Sunjae Baek
 *     
 *******************************************************************************/
package com.lge.swiftdepends.infrastructure.persistence;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

import com.lge.swiftdepends.domain.Component;
import com.lge.swiftdepends.domain.Module;
import com.lge.swiftdepends.domain.Element;
import com.lge.swiftdepends.domain.ComponentRepository;
import com.lge.swiftdepends.domain.ComponentType;
import com.lge.swiftdepends.domain.Dependency;
import com.lge.swiftdepends.domain.DependencyType;


public class JsonRepository implements ComponentRepository {
	
	private static class DependencySerializer implements JsonSerializer<Dependency> {
		@Override
		public JsonElement serialize(Dependency src, Type typeOfSrc, JsonSerializationContext context) {
			if (src == null) {
				return null;
			} else {
				JsonObject dependencyJsonObj = new JsonObject();
				
				if (src.getDependsOnComponent() != null) {
					dependencyJsonObj.addProperty("toComponent_name", src.getDependsOnComponent().getName());
					dependencyJsonObj.addProperty("toComponent_path", src.getDependsOnComponent().getPath());
					dependencyJsonObj.addProperty("toComponent_type", src.getDependsOnComponent().getType().toString());
				} else {
					dependencyJsonObj.addProperty("toComponent_name", "");
					dependencyJsonObj.addProperty("toComponent_path", "");
					dependencyJsonObj.addProperty("toComponent_type", "");
				}
				
				dependencyJsonObj.addProperty("type", src.getType().toString());
				dependencyJsonObj.addProperty("location", src.getLocation());
				
				return dependencyJsonObj; 
			}
		} 
	}
	
	private static class DependencyDeserializer implements JsonDeserializer<Dependency> {
		@Override
		public Dependency deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			if (json == null || json.isJsonObject() == false) {
				return null;
			} else {
				JsonObject jsonObject = json.getAsJsonObject();
				
				String toComponentName = getValue(jsonObject, "toComponent_name");
				String toComponentPath = getValue(jsonObject, "toComponent_path");
				String toComponentType = getValue(jsonObject, "toComponent_type");
				String type = getValue(jsonObject, "type");
				String location = getValue(jsonObject, "location");
				
				return new Dependency(new Element(toComponentName, ComponentType.valueOf(toComponentType), toComponentPath), 
										DependencyType.valueOf(type), location);
			}
		}
		
		private String getValue(JsonObject jsonObject, String key) {
			JsonElement element = jsonObject.get(key);
			if (element.isJsonPrimitive()) {
				return element.getAsJsonPrimitive().getAsString();
			} else if (element.isJsonNull()){
				return null;
			} else {
				return "";
			}
		}
	}
	
	private Path jsonFilePath = null;
	private RuntimeTypeAdapterFactory<Component> runtimeTypeAdapterFactory = null;
	private Gson gson = null;
	private ComponentHashMapCreator componentHashMapCreator = null; 
	
	public JsonRepository(Path jsonFilePath) {	
		super();
		this.jsonFilePath = jsonFilePath;
		this.runtimeTypeAdapterFactory = RuntimeTypeAdapterFactory
										    .of(Component.class, "Component")
										    .registerSubtype(Module.class, "Module")
										    .registerSubtype(Element.class, "Element");
		this.gson = new GsonBuilder()
				.setPrettyPrinting()
				.serializeNulls()
				.registerTypeAdapterFactory(runtimeTypeAdapterFactory)
				.registerTypeAdapter(Dependency.class, new DependencySerializer())
				.registerTypeAdapter(Dependency.class, new DependencyDeserializer())
				.create();
		componentHashMapCreator = new ComponentHashMapCreator();
	}
	
	private void setParentComponent(Component component) {
		try {
			if (component.getComponents().size() == 0) {
				return;
			} else {
				component.getComponents().forEach(child->{
					child.setParent(component);
					setParentComponent(child);
				});
			}
		}
		catch(UnsupportedOperationException e) {
			return;
		}
	}
	
	private void setDependencyComponent(Component component) {
		if (component.getDependencyStrengh() != 0) {
			component.getDependencies().forEach(dependency->{
				Component replaceComponent = componentHashMapCreator.findComponent(dependency.getDependsOnComponent());
				if (replaceComponent != null) {
					dependency.setDependsOnComponent(replaceComponent);
				}
				else {
					System.out.println("No component to replace was found!");
				}
			});
		}
		
		try {
			if (component.getComponents().size() == 0) {
				return;
			} else{
				component.getComponents().forEach(child->{setDependencyComponent(child);});
			}
		}
		catch(UnsupportedOperationException e) {
			return;
		}
	}

	@Override
	public boolean create(List<Component> components) {
		boolean result;
		BufferedWriter bw = null;
		String jsonString = gson.toJson(components);
		
		try {
			if(Files.exists(jsonFilePath))
				Files.delete(jsonFilePath);
			
			bw = Files.newBufferedWriter(jsonFilePath, Charset.forName("UTF-8"),StandardOpenOption.CREATE_NEW);
			bw.write(jsonString);
			result = true;
		} catch(Exception e) {
			result = false;
			e.printStackTrace();
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					result = false;
					e.printStackTrace();
				}
			}
		}
		
		return result;
	}
	
	@Override
	public List<Component> read() {
		List<Component> components = null;
		
		try {
			components = gson.fromJson(Files.readString(jsonFilePath, StandardCharsets.UTF_8), 
										new TypeToken<ArrayList<Module>>(){}.getType());
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (components != null) {
			// set parent component
			components.forEach(component->{setParentComponent(component);});
			// generate component HashMap
			components.forEach(component->{component.accecpt(componentHashMapCreator);});
			// set dependency component
			components.forEach(component->{setDependencyComponent(component);});
		}
		
		return components;
	}	
}
