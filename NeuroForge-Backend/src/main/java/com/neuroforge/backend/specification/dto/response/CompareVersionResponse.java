package com.neuroforge.backend.specification.dto.response;

import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompareVersionResponse {

    private Integer version1;

    private Integer version2;

    private List<FieldChangeResponse> changes;

}
