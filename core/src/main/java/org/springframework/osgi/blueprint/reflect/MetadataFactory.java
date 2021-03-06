/*
 * Copyright 2006-2009 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.osgi.blueprint.reflect;

import java.util.Collection;
import java.util.Set;

import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Adapter factory that translates Blueprint {@link ComponentMetadata} into Spring {@link BeanDefinition}s (and vice
 * versa).
 * 
 * @author Adrian Colyer
 * @author Costin Leau
 */
public class MetadataFactory {

	private static final BeanDefinitionFactory springFactory = new BeanDefinitionFactory();
	private static final ComponentMetadataFactory blueprintFactory = new ComponentMetadataFactory();

	public static BeanDefinition buildBeanDefinitionFor(ComponentMetadata metadata) {
		return springFactory.buildBeanDefinitionFor(metadata);
	}

	/**
	 * Inspects the given {@link BeanDefinition beanDefinition} and returns the appropriate {@link ComponentMetadata
	 * metadata} (can be one of {@link LocalComponentMetadata}, {@link ServiceExportComponentMetadata}, or
	 * {@link ServiceReferenceComponentMetadata}).
	 * 
	 * @param name bean name
	 * @param beanDefinition Spring bean definition
	 * @return an OSGi component metadata.
	 */
	public static ComponentMetadata buildComponentMetadataFor(String name, BeanDefinition beanDefinition) {
		return blueprintFactory.buildMetadata(name, beanDefinition);
	}

	/**
	 * Inspects the given {@link ConfigurableListableBeanFactory factory} and returns the appropriate OSGi 4.2 Blueprint
	 * {@link ComponentMetadata metadata}.
	 * 
	 * @param factory Spring bean factory
	 * @return collection of blueprint metadata
	 */
	public static Collection<ComponentMetadata> buildComponentMetadataFor(ConfigurableListableBeanFactory factory) {
		return blueprintFactory.buildComponentMetadataFor(factory);
	}

	/**
	 * Builds the Blueprint metadata based on the nested elements contained by the given bean definition.
	 * 
	 * @param beanDefinition
	 * @return
	 */
	static Collection<ComponentMetadata> buildNestedComponentMetadataFor(BeanDefinition beanDefinition) {
		return blueprintFactory.buildNestedMetadata(beanDefinition);
	}

	public static Set<String> filterIds(Set<String> components) {
		return blueprintFactory.filterIds(components);
	}
}