package com.tw.fake_spring_boot_scaffolder.service;

import com.tw.fake_spring_boot_scaffolder.model.ProjectRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
public class ProjectGeneratorService {

    private final RestTemplate restTemplate = new RestTemplate();

    // The custom properties you want to inject
    private static final String CUSTOM_PROPERTIES = """
            # Database connection settings
            spring.datasource.url=jdbc:postgresql://localhost:5432/mydatabase
            spring.datasource.username=postgres
            spring.datasource.password=mysecretpassword
            spring.datasource.driver-class-name=org.postgresql.Driver
            
            # JPA and Hibernate settings
            spring.jpa.hibernate.ddl-auto=update
            spring.jpa.show-sql=true
            spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
            
            # Optional: Change the server port
            # server.port=8081
            """;

    public byte[] generateProject(ProjectRequest request) throws IOException {
        // 1. Build the URL for start.spring.io
        // Omitting 'bootVersion' automatically fetches the latest stable Spring Boot version
        String url = UriComponentsBuilder.fromUriString("https://start.spring.io/starter.zip")
                .queryParam("type", "maven-project")
                .queryParam("language", "java")
                .queryParam("groupId", request.groupId())
                .queryParam("artifactId", request.artifactId())
                .queryParam("name", request.appName())
                .queryParam("javaVersion", request.javaVersion())
                .queryParam("dependencies", "web,data-jpa,postgresql") // Default dependencies
                .toUriString();

        // 2. Fetch the ZIP file from Spring Initializr
        byte[] originalZip = restTemplate.getForObject(url, byte[].class);

        if (originalZip == null) {
            throw new RuntimeException("Failed to download project from start.spring.io");
        }

        // 3. Modify the ZIP to inject custom application.properties
        return injectCustomProperties(originalZip);
    }

    private byte[] injectCustomProperties(byte[] originalZip) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(originalZip));
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                // Create a new entry with the same name
                zos.putNextEntry(new ZipEntry(entry.getName()));

                // If it's the application.properties file, overwrite it with our custom string
                if (entry.getName().endsWith("application.properties")) {
                    zos.write(CUSTOM_PROPERTIES.getBytes());
                } else {
                    // Otherwise, just copy the original file content exactly as is
                    zis.transferTo(zos);
                }
                zos.closeEntry();
            }
        }
        return baos.toByteArray();
    }
}
