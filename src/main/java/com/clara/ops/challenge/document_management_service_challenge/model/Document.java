package com.clara.ops.challenge.document_management_service_challenge.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Table(name = "documents")
@Data
public class Document {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "username")
  private String username;

  @Column(name = "document_name")
  private String name;

  @Column(name = "tag")
  private String tags;

  @Column(name = "minio_path")
  private String minioPath;

  @Column(name = "file_size")
  private Long fileSize;

  @Column(name = "file_type")
  private String fileType;

  @Column(name = "created_at")
  private LocalDateTime createdAt;
}
