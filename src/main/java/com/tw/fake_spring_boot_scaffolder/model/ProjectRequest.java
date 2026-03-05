package com.tw.fake_spring_boot_scaffolder.model;

public record ProjectRequest(
        String appName,
        String groupId,
        String artifactId,
        String javaVersion
) {
    public ProjectRequest {
        if (javaVersion == null || javaVersion.isBlank()) {
            javaVersion = "21";
        }
    }
}
