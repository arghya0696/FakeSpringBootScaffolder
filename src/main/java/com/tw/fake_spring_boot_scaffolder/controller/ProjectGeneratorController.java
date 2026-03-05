package com.tw.fake_spring_boot_scaffolder.controller;

import com.tw.fake_spring_boot_scaffolder.model.ProjectRequest;
import com.tw.fake_spring_boot_scaffolder.service.ProjectGeneratorService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/scaffold")
public class ProjectGeneratorController {

    private final ProjectGeneratorService generatorService;

    public ProjectGeneratorController(ProjectGeneratorService generatorService) {
        this.generatorService = generatorService;
    }

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generateProject(@RequestBody ProjectRequest request) {
        try {
            byte[] zipFile = generatorService.generateProject(request);

            System.out.println("generating...");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + request.artifactId() + ".zip\"")
                    .contentType(MediaType.parseMediaType("application/zip"))
                    .body(zipFile);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
