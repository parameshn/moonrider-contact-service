package com.moonrider.zamazon.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdentifyRequest {

    private String email;

    @JsonProperty("phoneNumber")
    private String phoneNumber;

}
