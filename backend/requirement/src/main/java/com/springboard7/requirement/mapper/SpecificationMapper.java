package com.springboard7.requirement.mapper;

import com.springboard7.requirement.dto.response.SpecificationResponse;
import com.springboard7.requirement.dto.response.SpecificationVersionResponse;
import com.springboard7.requirement.entity.Specification;
import com.springboard7.requirement.entity.SpecificationVersion;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SpecificationMapper {

    SpecificationResponse toResponse(Specification specification);


    List<SpecificationResponse> toResponse(List<Specification> specifications);

    SpecificationVersionResponse toResponse(
            SpecificationVersion version
    );
}