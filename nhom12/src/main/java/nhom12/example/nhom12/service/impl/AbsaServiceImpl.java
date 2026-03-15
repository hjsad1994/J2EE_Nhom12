package nhom12.example.nhom12.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nhom12.example.nhom12.dto.request.AbsaPredictRequest;
import nhom12.example.nhom12.dto.response.AbsaPredictResponse;
import nhom12.example.nhom12.model.ReviewAspectAnalysis;
import nhom12.example.nhom12.service.AbsaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class AbsaServiceImpl implements AbsaService {

  private final RestTemplate restTemplate;

  @Value("${absa.api-url:http://localhost:9000}")
  private String absaApiUrl;

  @Override
  public List<ReviewAspectAnalysis> analyzeComment(String comment) {
    try {
      ResponseEntity<AbsaPredictResponse> response =
          restTemplate.postForEntity(
              absaApiUrl + "/predict",
              new AbsaPredictRequest(comment),
              AbsaPredictResponse.class);

      if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
        return List.of();
      }

      List<ReviewAspectAnalysis> results = response.getBody().getResults();
      return results != null ? results : List.of();
    } catch (RestClientException ex) {
      log.warn("ABSA analysis failed: {}", ex.getMessage());
      return List.of();
    }
  }
}
