package io.zenwave360.sdk.plugins.quarkus;

import io.zenwave360.sdk.zdl.ProjectTemplates;

/**
 * Quarkus templates for {@code BackendApplicationDefaultPlugin}, selected with the {@code templates} option.
 */
public class BackendApplicationQuarkusTemplates extends ProjectTemplates {

    public BackendApplicationQuarkusTemplates() {
        setTemplatesFolder("io/zenwave360/sdk/plugins/quarkus/BackendApplicationQuarkustGenerator");
    }
}
