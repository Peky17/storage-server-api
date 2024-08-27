package com.peky.storageserver.controllers;

import com.peky.storageserver.services.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@RestController("api/v1/storage")
@RequestMapping("/files")
public class FileController {
    private final FileService fileService;
    private final List<String> supportedImageFormats = Arrays.asList("image/svg+xml", "image/jpeg", "image/png", "image/gif");

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @Operation(summary = "Upload a file", description = "Uploads a file to the server.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(value = "/uploadFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(
            @RequestParam("file")
            @Parameter(description = "The file to upload", required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            MultipartFile file) {
        String fileName = fileService.saveFile(file);
        return ResponseEntity.ok("File uploaded successfully: " + fileName);
    }

    @Operation(summary = "Update a file", description = "Updates an existing file on the server.")
    @PutMapping(value = "/updateFile/{filename}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateFile(
            @PathVariable String filename,
            @RequestParam("file")
            @Parameter(description = "The new file to upload", required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            MultipartFile file) {
        fileService.updateFile(filename, file);
        return ResponseEntity.ok("File updated successfully: " + filename);
    }

    @Operation(summary = "Delete a file", description = "Deletes a file from the server.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File deleted successfully"),
            @ApiResponse(responseCode = "404", description = "File not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/deleteFile/{filename}")
    public ResponseEntity<String> deleteFile(@PathVariable String filename) {
        fileService.deleteFile(filename);
        return ResponseEntity.ok("File deleted successfully: " + filename);
    }

    @Operation(summary = "Get a file", description = "Retrieves a file from the server.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File retrieved successfully", content = @Content(
                    mediaType = "application/octet-stream", schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "404", description = "File not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/getFileByName/{filename}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        Resource file = fileService.loadFileAsResource(filename);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(file);
    }

    @Operation(summary = "Get an image", description = "Retrieves an image file from the server for viewing in the browser.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image retrieved successfully", content = @Content(
                    mediaType = "image/jpeg", schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "404", description = "Image not found"),
            @ApiResponse(responseCode = "415", description = "Unsupported media type"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/getImageSrc/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        Resource file = fileService.loadFileAsResource(filename);
        // Determine the file's media type
        String mediaType = fileService.getFileMediaType(filename);
        // Check if the media type is supported
        if (supportedImageFormats.contains(mediaType)) {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(mediaType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(file);
        } else {
            return ResponseEntity.status(415).body(null); // Unsupported Media Type
        }
    }
}