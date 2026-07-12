package com.forgemind.modules.git.service;

import com.forgemind.modules.git.dto.request.CodeSearchRequest;
import com.forgemind.modules.git.dto.response.CodeSearchResponse;
import java.util.List;

public interface CodeSearchService {
  List<CodeSearchResponse> search(CodeSearchRequest request);
}
