package com.clara.ops.challenge.document_management_service_challenge.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentSearchFilters {
  private String user;
  private String name;
  private List<String> tags;
}
