package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.model.Document;
import com.clara.ops.challenge.document_management_service_challenge.model.DocumentDownloadUrl;
import com.clara.ops.challenge.document_management_service_challenge.repository.DocumentRepository;
import io.micrometer.common.util.StringUtils;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/** Document Service */
@Service
public class DocumentService {
  @Autowired private DocumentRepository documentRepository;

  @Autowired private MinioClient minioClient;

  private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50 MB

  /**
   * @param inputStream
   * @param username
   * @param documentName
   * @param tagNames
   * @param fileSize
   * @param fileType
   * @return
   * @throws Exception
   */
  @Async
  public void uploadDocument(
      InputStream inputStream,
      String username,
      String documentName,
      List<String> tagNames,
      long fileSize,
      String fileType)
      throws Exception {
    // Validate file size
    if (fileSize > MAX_FILE_SIZE) {
      throw new IllegalArgumentException("File size exceeds the maximum limit of 50MB.");
    }

    // Prepare the MinIO path
    String minioPath = username + "/" + documentName;

    // Upload the file to MinIO
    try (BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
      minioClient.putObject(
          PutObjectArgs.builder().bucket("document-bucket").object(minioPath).stream(
                  bufferedInputStream, fileSize, -1)
              .contentType(fileType)
              .build());
    }

    // Create the document entity
    Document document = new Document();
    document.setUsername(username);
    document.setName(documentName);
    document.setMinioPath(minioPath);
    document.setFileSize(fileSize);
    document.setFileType(fileType);
    document.setCreatedAt(LocalDateTime.now());

    // Set tags as a comma-separated string
    if (tagNames != null && !tagNames.isEmpty()) {
      String tagsString = String.join(",", tagNames); // Join tags with a comma
      document.setTags(tagsString); // Set the tags field
    }

    // Save the document
    documentRepository.save(document);
  }

  /**
   * @param user
   * @param documentName
   * @param tags
   * @param page
   * @param size
   * @return
   */
  public List<Document> searchDocuments(
      String user, String documentName, List<String> tags, int page, int size) {
    if (StringUtils.isBlank(user) && StringUtils.isBlank(documentName) && tags == null) {
      return documentRepository.findAll();
    }
    return documentRepository.findByUsernameAndNameAndTagsOrderByCreatedAtDesc(
        user, documentName, String.join(",", tags));
  }

  /**
   * @param documentId
   * @return
   * @throws Exception
   */
  public DocumentDownloadUrl generateDownloadUrl(Long documentId) throws Exception {
    Document document =
        documentRepository
            .findById(documentId)
            .orElseThrow(() -> new RuntimeException("Document not found"));
    return DocumentDownloadUrl.builder()
        .url(
            minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .bucket("document-bucket")
                    .object(document.getMinioPath())
                    .expiry(1, TimeUnit.HOURS)
                    .method(Method.GET)
                    .build()))
        .build();
  }
}
