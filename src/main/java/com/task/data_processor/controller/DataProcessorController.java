package com.task.data_processor.controller;

import com.task.data_processor.service.DataProcessorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DataProcessorController {

    private final DataProcessorService dataProcessorService;

    /**
     * Processes location and metadata files. Both files are mandatory.
     * Validates and processes the uploaded files, extracting relevant data for analysis.
     *
     * @param file1 The location data file to be processed (required).
     * @param file2 The metadata file containing additional details (required).
     * @return ResponseEntity containing the API response with status and processed data.
     */
    @PostMapping(value = "/process-locations", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "PROCESS LOCATION DATA",
            description = "Processes location and metadata files. Both files are mandatory.\n\n" +
                    "Endpoint Workflow:\n" +
                    "1. The server receives the location data file and metadata file as input.\n" +
                    "2. It validates the uploaded files to ensure correct format and completeness.\n" +
                    "3. The data is processed and merged for analysis.\n" +
                    "4. The processed data is returned in a ResponseEntity with HTTP status OK (200).\n",
            tags = {"PROCESSING"}
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Files processed successfully. Processed data returned.",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid files provided. Processing failed.",
                    content = @Content(
                            mediaType = "application/json"
                    )
            )
    })
    public ResponseEntity<?> processLocations(
            @Parameter(
                    name = "locationsFile",
                    description = "The location data file to be processed. This file is mandatory.",
                    required = true
            )
            @RequestParam("locationsFile") MultipartFile file1,
            @Parameter(
                    name = "metadataFile",
                    description = "The metadata file containing additional details. This file is mandatory.",
                    required = true
            )
            @RequestParam("metadataFile") MultipartFile file2) {

        return dataProcessorService.loadAndProcessData(file1, file2);
    }


}
