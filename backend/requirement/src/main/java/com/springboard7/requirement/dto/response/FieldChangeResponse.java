package com.springboard7.requirement.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldChangeResponse {

    private String field;

    private String oldValue;

    private String newValue;

}