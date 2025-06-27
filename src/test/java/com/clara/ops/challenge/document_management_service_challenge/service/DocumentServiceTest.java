package com.clara.ops.challenge.document_management_service_challenge.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.clara.ops.challenge.document_management_service_challenge.model.Document;
import com.clara.ops.challenge.document_management_service_challenge.model.DocumentDownloadUrl;
import com.clara.ops.challenge.document_management_service_challenge.repository.DocumentRepository;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DocumentServiceTest {

  @Mock private DocumentRepository documentRepository;

  @Mock private MinioClient minioClient;

  @InjectMocks private DocumentService documentService;

  private Document document;

  @BeforeEach
  public void setUp() {
    document = new Document();
    document.setId(1L);
    document.setUsername("user1");
    document.setName("test.pdf");
    document.setMinioPath("user1/test.pdf");
    document.setFileSize(1024L);
    document.setFileType("application/pdf");
    document.setCreatedAt(LocalDateTime.now());
    document.setTags("pdf,document");
  }

  @Test
  public void testUploadDocument() throws Exception {
    // Arrange
    InputStream inputStream = new ByteArrayInputStream(new byte[1024]); // Simulate a 1KB PDF file
    List<String> tags = Arrays.asList("pdf", "document");

    when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);
    when(documentRepository.save(any(Document.class))).thenReturn(document);

    // Act
    documentService.uploadDocument(
        inputStream, "user1", "test.pdf", tags, 1024L, "application/pdf");

    // Assert
    verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
    verify(documentRepository, times(1)).save(any(Document.class));
  }

  @Test
  public void testSearchDocuments() {
    // Arrange
    List<Document> documents = Arrays.asList(document);
    List<String> tags = new ArrayList<>();
    tags.add("pdf");
    tags.add("document");

    when(documentRepository.findByUsernameAndNameAndTagsOrderByCreatedAtDesc("user1", "test.pdf", String.join(",", tags))).thenReturn(documents);

    // Act
    List<Document> result = documentService.searchDocuments("user1", "test.pdf", tags, 0, 10);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(document, result.get(0));
    verify(documentRepository, times(1)).findByUsernameAndNameAndTagsOrderByCreatedAtDesc("user1", "test.pdf", String.join(",", tags));
  }

  @Test
  public void testGenerateDownloadUrl() throws Exception {
    // Arrange
    when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
    when(minioClient.getPresignedObjectUrl(any())).thenReturn("http://minio-url/test.pdf");

    // Act
    DocumentDownloadUrl downloadUrl = documentService.generateDownloadUrl(1L);

    // Assert
    assertEquals("http://minio-url/test.pdf", downloadUrl.getUrl());
    verify(documentRepository, times(1)).findById(1L);
    verify(minioClient, times(1)).getPresignedObjectUrl(any());
  }

  @Test
  public void testUploadDocument_FileSizeExceedsLimit() {
    // Arrange
    InputStream inputStream =
        new ByteArrayInputStream(new byte[1024 * 1024 * 60]); // Simulate a 60MB PDF file
    List<String> tags = Arrays.asList("pdf", "document");

    // Act & Assert
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              documentService.uploadDocument(
                  inputStream, "user1", "test.pdf", tags, 1024 * 1024 * 60, "application/pdf");
            });

    assertEquals("File size exceeds the maximum limit of 50MB.", exception.getMessage());
  }
}
