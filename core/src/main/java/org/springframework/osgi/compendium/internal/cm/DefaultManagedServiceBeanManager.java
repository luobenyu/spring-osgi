/*
 * Copyright 2006-2008 the original author or authors.
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

package org.springframework.osgi.compendium.internal.cm;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.core.CollectionFactory;
import org.springframework.util.Assert;

/**
 * Default implementation for {@link ManagedServiceBeanManager}.
 * 
 * @author Costin Leau
 * 
 */
public class DefaultManagedServiceBeanManager implements ManagedServiceBeanManager {

	private interface UpdateCallback {

		void update(Map properties);
	}

	private class BeanManagedUpdate implements UpdateCallback {

		private final String methodName;
		// class cache = keeps track of method adapters for each given class
		// the cache becomes useful when dealing with FactoryBean which can returns
		// different class types on each invocation
		private final Map classCache = new WeakHashMap(2);


		public BeanManagedUpdate(String methodName) {
			this.methodName = methodName;
		}

		public void update(Map properties) {
			for (Iterator iterator = instanceRegistry.values().iterator(); iterator.hasNext();) {
				Object instance = iterator.next();
				getUpdateMethod(instance).invoke(instance, properties);
			}
		}

		/**
		 * Returns a (lazily created) method adapter that invokes a predefined
		 * method on the given instance.
		 * 
		 * @param instance object instance
		 * @return method update method adapter
		 */
		private UpdateMethodAdapter getUpdateMethod(Object instance) {
			WeakReference adapterReference = (WeakReference) classCache.get(instance.getClass());
			if (adapterReference != null) {
				return (UpdateMethodAdapter) adapterReference.get();
			}
			UpdateMethodAdapter adapter = new UpdateMethodAdapter(methodName);
			classCache.put(instance.getClass(), new WeakReference(adapter));
			return adapter;
		}
	}

	private class ContainerManagedUpdate implements UpdateCallback {

		public void update(Map properties) {
			for (Iterator iterator = instanceRegistry.values().iterator(); iterator.hasNext();) {
				Object instance = iterator.next();
				synchronized (instance) {
					injectConfigurationAdminInfo(instance, properties);
				}
			}
		}
	}


	private final Map instanceRegistry = CollectionFactory.createConcurrentMap(8);
	private final UpdateCallback updateCallback;
	private final ConfigurationAdminManager cam;


	public DefaultManagedServiceBeanManager(UpdateStrategy updateStrategy, String methodName,
			ConfigurationAdminManager cam) {

		if (UpdateStrategy.BEAN_MANAGED.equals(updateStrategy)) {
			Assert.hasText(methodName, "method name required when using 'bean-managed' strategy");
			updateCallback = new BeanManagedUpdate(methodName);
		}
		else if (UpdateStrategy.CONTAINER_MANAGED.equals(updateStrategy)) {
			updateCallback = new ContainerManagedUpdate();
		}
		else
			updateCallback = null;

		this.cam = cam;
	}

	public Object register(Object bean) {
		instanceRegistry.put(new Integer(System.identityHashCode(bean)), bean);
		injectConfigurationAdminInfo(bean, cam.getConfiguration());
		return bean;
	}

	public void unregister(Object bean) {
		instanceRegistry.remove(new Integer(System.identityHashCode(bean)));
	}

	public void updated(Map properties) {
		if (updateCallback != null) {
			updateCallback.update(properties);
		}
	}

	/**
	 * Injects the information found inside the Configuration Admin to the given
	 * object instance.
	 * 
	 * @param bean bean instance to configure
	 */
	static void injectConfigurationAdminInfo(Object instance, Map properties) {
		if (properties != null && !properties.isEmpty()) {
			BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(instance);
			for (Iterator iterator = properties.entrySet().iterator(); iterator.hasNext();) {
				Map.Entry entry = (Map.Entry) iterator.next();
				String propertyName = (String) entry.getKey();

				if (beanWrapper.isWritableProperty(propertyName)) {
					beanWrapper.setPropertyValue(propertyName, entry.getValue());
				}
			}
		}
	}
}