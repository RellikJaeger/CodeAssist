/*
 * Copyright 2014 the original author or authors.
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

package org.gradle.api.internal.artifacts.component;

import org.gradle.internal.component.external.model.DefaultModuleComponentIdentifier;
import org.gradle.internal.component.local.model.DefaultProjectComponentSelector;

import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.component.ProjectComponentIdentifier;
import org.gradle.api.artifacts.component.ProjectComponentSelector;
import org.gradle.api.internal.artifacts.DefaultModuleIdentifier;
import org.gradle.api.internal.artifacts.Module;
import org.gradle.internal.build.BuildState;
import org.gradle.util.Path;

public class DefaultComponentIdentifierFactory implements ComponentIdentifierFactory {
    private final BuildState currentBuild;

    public DefaultComponentIdentifierFactory(BuildState currentBuild) {
        this.currentBuild = currentBuild;
    }

    @Override
    public ComponentIdentifier createComponentIdentifier(Module module) {
        ProjectComponentIdentifier projectId = module.getProjectId();

        if (projectId != null) {
            return projectId;
        }

        return new DefaultModuleComponentIdentifier(DefaultModuleIdentifier.newId(module.getGroup(), module.getName()), module.getVersion());
    }

    @Override
    public ProjectComponentSelector createProjectComponentSelector(String projectPath) {
        return DefaultProjectComponentSelector
                .newSelector(currentBuild.getProjects().getProject(Path.path(projectPath)).getComponentIdentifier());
    }

    @Override
    public ProjectComponentIdentifier createProjectComponentIdentifier(ProjectComponentSelector selector) {
        return ((DefaultProjectComponentSelector) selector).toIdentifier();
    }
}