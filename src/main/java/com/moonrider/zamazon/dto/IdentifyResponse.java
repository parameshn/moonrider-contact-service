package com.moonrider.zamazon.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdentifyResponse {

    @JsonProperty("primaryContactId")
    private Long primaryContactId;

    private List<String> emails;

    @JsonProperty("phoneNumbers")
    private List<String> phoneNumbers;

    @JsonProperty("secondaryContactIds")
    private List<Long> secondaryContactIds;

}
