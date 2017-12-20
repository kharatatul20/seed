/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.seed.undertow.internal;

import static org.seedstack.shed.ClassLoaders.findMostCompleteClassLoader;

import io.undertow.server.DefaultByteBufferPool;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainerInitializerInfo;
import io.undertow.servlet.util.ImmediateInstanceHandle;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import javax.servlet.ServletContainerInitializer;
import org.seedstack.seed.undertow.UndertowConfig;
import org.seedstack.seed.web.WebConfig;
import org.xnio.XnioWorker;

class DeploymentManagerFactory {
    private final ClassLoader mostCompleteClassLoader = findMostCompleteClassLoader(DeploymentManagerFactory.class);
    private final XnioWorker xnioWorker;
    private final WebConfig.ServerConfig serverConfig;
    private final UndertowConfig undertowConfig;
    private final Map<String, String> initParameters;
    private final String applicationId;

    DeploymentManagerFactory(XnioWorker xnioWorker, WebConfig.ServerConfig serverConfig, UndertowConfig undertowConfig,
            Map<String, String> initParameters, String applicationId) {
        this.xnioWorker = xnioWorker;
        this.serverConfig = serverConfig;
        this.undertowConfig = undertowConfig;
        this.initParameters = initParameters;
        this.applicationId = applicationId;
    }

    DeploymentManager createDeploymentManager() {
        DeploymentInfo servletBuilder = configureDeploymentInfo(serverConfig.getContextPath());
        return Servlets.defaultContainer().addDeployment(servletBuilder);
    }

    private DeploymentInfo configureDeploymentInfo(String contextPath) {
        DeploymentInfo deploymentInfo = Servlets.deployment()
                .setEagerFilterInit(true)
                .setClassLoader(mostCompleteClassLoader)
                .setDeploymentName(applicationId)
                .addServletContextAttribute(
                        WebSocketDeploymentInfo.ATTRIBUTE_NAME,
                        new WebSocketDeploymentInfo()
                                .setBuffers(new DefaultByteBufferPool(
                                        undertowConfig.isDirectBuffers(),
                                        undertowConfig.getBufferSize()))
                                .setWorker(xnioWorker)
                )
                .setContextPath(contextPath);

        for (Map.Entry<String, String> initParameter : initParameters.entrySet()) {
            deploymentInfo.addInitParameter(initParameter.getKey(), initParameter.getValue());
        }

        for (ServletContainerInitializer servletContainerInitializer : loadServletContainerInitializers()) {
            deploymentInfo.addServletContainerInitalizer(
                    createServletContainerInitializerInfo(servletContainerInitializer));
        }

        return deploymentInfo;
    }

    private <T extends ServletContainerInitializer> ServletContainerInitializerInfo
    createServletContainerInitializerInfo(
            final T servletContainerInitializer) {
        return new ServletContainerInitializerInfo(servletContainerInitializer.getClass(),
                () -> new ImmediateInstanceHandle<>(servletContainerInitializer), null);
    }

    private Set<ServletContainerInitializer> loadServletContainerInitializers() {
        Set<ServletContainerInitializer> servletContainerInitializers = new HashSet<>();
        for (ServletContainerInitializer servletContainerInitializer : ServiceLoader.load(
                ServletContainerInitializer.class, mostCompleteClassLoader)) {
            servletContainerInitializers.add(servletContainerInitializer);
        }
        return servletContainerInitializers;
    }
}
