package com.mycompany.evmc.mapper;

import com.mycompany.evmc.dto.EmployeeDto;
import com.mycompany.evmc.model.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    EmployeeMapper INSTANCE = Mappers.getMapper(EmployeeMapper.class);

    // If you just need a simple conversion
    EmployeeDto toDto(Employee employee);

    // ✅ Let MapStruct map automatically, no need to force timezone/manager
    Employee toEntity(EmployeeDto dto);
}
