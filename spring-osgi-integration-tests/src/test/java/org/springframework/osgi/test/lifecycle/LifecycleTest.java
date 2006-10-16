/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.osgi.test.lifecycle;

import org.osgi.framework.Bundle;
import org.springframework.osgi.test.ConfigurableBundleCreatorTests;

/**
 * @author Hal Hildebrand
 *         Date: Oct 15, 2006
 *         Time: 5:51:36 PM
 */
public class LifecycleTest extends ConfigurableBundleCreatorTests {

	//	AMC: no need to explicitly set framework here:
	//  a) equinox is the default anyway, and 
	//  b) it will prevent testing with alternate OSGi runtimes via maven when using the profiles
	//
	//   static {
	//       System.setProperty(OSGI_FRAMEWORK_SELECTOR, EQUINOX_PLATFORM);
	//    }


    protected String getManifestLocation() {
        return "classpath:org/springframework/osgi/test/lifecycle/MANIFEST.MF";
    }


    protected String[] getBundleLocations() {
        return new String[]{
                localMavenArtifact("aopalliance.osgi", "1.0-SNAPSHOT"),
                localMavenArtifact("commons-collections.osgi", "3.2-SNAPSHOT"),
                localMavenArtifact("spring-aop", "2.1-SNAPSHOT"),
                localMavenArtifact("spring-context", "2.1-SNAPSHOT"),
                localMavenArtifact("spring-beans", "2.1-SNAPSHOT"),
                localMavenArtifact("spring-osgi-core", "1.0-SNAPSHOT"),
                localMavenArtifact("spring-jmx", "2.1-SNAPSHOT"),
                localMavenArtifact("org.springframework.osgi.test.lifecycle", "1.0-SNAPSHOT")

        };
    }


    public void testLifecycle() throws Exception {
        assertNotSame("Guinea pig has already been shutdown", "true",
                      System.getProperty("org.springframework.osgi.test.lifecycle.GuineaPig.close"));

        assertEquals("Guinea pig didn't startup", "true",
                     System.getProperty("org.springframework.osgi.test.lifecycle.GuineaPig.startUp"));
        Bundle[] bundles = getBundleContext().getBundles();
        Bundle testBundle = null;
        for (int i = 0; i < bundles.length; i++) {
            if (bundles[i].getSymbolicName().equals("org.springframework.osgi.test.lifecycle")) {
                testBundle = bundles[i];
                break;
            }
        }
        assertNotNull("Could not find the test bundle", testBundle);
        testBundle.stop();
        assertEquals("Guinea pig didn't shutdown", "true",
                     System.getProperty("org.springframework.osgi.test.lifecycle.GuineaPig.close"));
    }
}
