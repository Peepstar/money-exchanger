package org.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CurrencyDTO(@JsonProperty("base_code") String baseCode,
                          @JsonProperty("target_code") String targetCode,
                          @JsonProperty("conversion_rate") String conversionRate,
                          @JsonProperty("conversion_result") String conversionResult,
                          @JsonProperty("time_last_update_utc") String timeLastUpdate){
}
