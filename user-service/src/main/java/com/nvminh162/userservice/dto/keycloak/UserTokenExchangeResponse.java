package com.nvminh162.userservice.dto.keycloak;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

// tools.jackson
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserTokenExchangeResponse {

    String accessToken;
    Long expiresIn;
    Long refreshExpiresIn;
    String refreshToken;
    String tokenType;
    String idToken;

    @JsonProperty("not-before-policy")
    Long notBeforePolicy;

    String sessionState;   
    String scope;   
}