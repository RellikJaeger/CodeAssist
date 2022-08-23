package org.gradle.composite.internal;

import org.gradle.api.artifacts.component.BuildIdentifier;
import org.gradle.initialization.BuildCancellationToken;
import org.gradle.api.internal.BuildDefinition;
import org.gradle.api.internal.StartParameterInternal;
import org.gradle.internal.classpath.ClassPath;
import org.gradle.internal.time.Time;
import org.gradle.util.Path;
import org.gradle.initialization.BuildRequestMetaData;
import org.gradle.initialization.DefaultBuildRequestMetaData;
import org.gradle.initialization.NoOpBuildEventConsumer;
import org.gradle.internal.build.BuildLayoutValidator;
import org.gradle.internal.build.BuildState;
import org.gradle.internal.build.NestedBuildTree;
import org.gradle.internal.buildtree.BuildTreeLifecycleController;
import org.gradle.internal.buildtree.BuildTreeModelControllerServices;
import org.gradle.internal.buildtree.BuildTreeState;
import org.gradle.internal.service.scopes.GradleUserHomeScopeServiceRegistry;
import org.gradle.internal.session.BuildSessionState;
import org.gradle.internal.session.state.CrossBuildSessionState;

import java.util.function.Function;

public class DefaultNestedBuildTree implements NestedBuildTree {
    private final BuildDefinition buildDefinition;
    private final BuildIdentifier buildIdentifier;
    private final Path identityPath;
    private final BuildState owner;
    private final GradleUserHomeScopeServiceRegistry userHomeDirServiceRegistry;
    private final CrossBuildSessionState crossBuildSessionState;
    private final BuildCancellationToken buildCancellationToken;

    public DefaultNestedBuildTree(
            BuildDefinition buildDefinition,
            BuildIdentifier buildIdentifier,
            Path identityPath,
            BuildState owner,
            GradleUserHomeScopeServiceRegistry userHomeDirServiceRegistry,
            CrossBuildSessionState crossBuildSessionState,
            BuildCancellationToken buildCancellationToken
    ) {
        this.buildDefinition = buildDefinition;
        this.buildIdentifier = buildIdentifier;
        this.identityPath = identityPath;
        this.owner = owner;
        this.userHomeDirServiceRegistry = userHomeDirServiceRegistry;
        this.crossBuildSessionState = crossBuildSessionState;
        this.buildCancellationToken = buildCancellationToken;
    }

    @Override
    public <T> T run(Function<? super BuildTreeLifecycleController, T> buildAction) {
        StartParameterInternal startParameter = buildDefinition.getStartParameter();
        BuildRequestMetaData buildRequestMetaData = new DefaultBuildRequestMetaData(Time.currentTimeMillis());
        try (BuildSessionState session = new BuildSessionState(userHomeDirServiceRegistry,
                crossBuildSessionState, startParameter, buildRequestMetaData, ClassPath.EMPTY,
                buildCancellationToken, buildRequestMetaData.getClient(),
                new NoOpBuildEventConsumer())) {
            session.getServices().get(BuildLayoutValidator.class).validate(startParameter);
            BuildTreeModelControllerServices.Supplier modelServices =
                    session.getServices().get(BuildTreeModelControllerServices.class)
                            .servicesForNestedBuildTree(startParameter);
            try (BuildTreeState buildTree = new BuildTreeState(session.getServices(),
                    modelServices)) {
                RootOfNestedBuildTree rootBuild =
                        new RootOfNestedBuildTree(buildDefinition, buildIdentifier, identityPath,
                                owner, buildTree);
                rootBuild.attach();
                return rootBuild.run(buildAction);
            }
        }
    }
}