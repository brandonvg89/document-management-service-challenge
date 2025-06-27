package com.clara.ops.challenge.document_management_service_challenge.controller;

import com.clara.ops.challenge.document_management_service_challenge.model.Document;
import com.clara.ops.challenge.document_management_service_challenge.model.DocumentDownloadUrl;
import com.clara.ops.challenge.document_management_service_challenge.model.DocumentSearchFilters;
import com.clara.ops.challenge.document_management_service_challenge.model.UploadDocument;
import com.clara.ops.challenge.document_management_service_challenge.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {
  @Autowired private DocumentService documentService;

  @Operation(summary = "Upload a PDF document")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Document uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file size or other error"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @PostMapping(
      value = "/upload",
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.ALL_VALUE)
  public ResponseEntity<String> uploadDocument(
      @Parameter(description = "PDF file to upload") @RequestParam("file") MultipartFile file,
      @Parameter(description = "Upload document is a custom class to retrieve request params")
          UploadDocument uploadDocument)
      throws Exception {

    documentService.uploadDocument(
        file.getInputStream(),
        uploadDocument.getUser(),
        uploadDocument.getName(),
        uploadDocument.getTags(),
        file.getSize(),
        "application/pdf");
    return new ResponseEntity<>("The document was uploaded successfully.", HttpStatus.CREATED);
  }

  @Operation(summary = "Search a PDF document")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Return documents successfully"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
      })
  @GetMapping("/search")
  public ResponseEntity<List<Document>> searchDocuments(
      @Parameter(description = "DocumentSearchFilters is a custom class to retrieve request params")
          @RequestBody
          DocumentSearchFilters documentSearchFilters,
      @Parameter(description = "Pagination of the document list") @RequestParam(defaultValue = "0")
          int page,
      @Parameter(description = "Size per page") @RequestParam(defaultValue = "10") int size) {
    List<Document> documents =
        documentService.searchDocuments(
            documentSearchFilters.getUser(),
            documentSearchFilters.getName(),
            documentSearchFilters.getTags(),
            page,
            size);
    return new ResponseEntity<>(documents, HttpStatus.OK);
  }

  @GetMapping("/download/{id}")
  public ResponseEntity<DocumentDownloadUrl> downloadDocument(@PathVariable Long id) {
    try {
      return new ResponseEntity<>(documentService.generateDownloadUrl(id), HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }
  }
}
