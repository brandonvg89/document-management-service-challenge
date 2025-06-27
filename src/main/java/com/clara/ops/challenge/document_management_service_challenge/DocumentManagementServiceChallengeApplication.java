package com.clara.ops.challenge.document_management_service_challenge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.Async;

@SpringBootApplication
@Async
@EntityScan(basePackages = {"com.clara.ops.challenge.document_management_service_challenge.model"})
public class DocumentManagementServiceChallengeApplication {

  public static void main(String[] args) {
    SpringApplication.run(DocumentManagementServiceChallengeApplication.class, args);
  }
}
