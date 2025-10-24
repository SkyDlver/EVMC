package com.mycompany.evmc.service.impl;

import com.mycompany.evmc.dto.*;
import com.mycompany.evmc.mapper.EmployeeMapper;
import com.mycompany.evmc.model.Employee;
import com.mycompany.evmc.model.Role;
import com.mycompany.evmc.repository.EmployeeRepository;
import com.mycompany.evmc.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeMapper employeeMapper;

    @Override
    public LoginResponse loginUser(LoginRequest loginRequest) {
        Optional<Employee> userOpt = employeeRepository.findByEmail(loginRequest.getEmail());

        if (userOpt.isEmpty()) {
            return LoginResponse.builder()
                    .message("Invalid credentials: email not found")
                    .token(null)
                    .build();
        }

        Employee employee = userOpt.get();

        // ⚠️ Here you’d check password hash — mock for now
        // TODO: integrate BCrypt or JWT logic
        if (!"password123".equals(loginRequest.getPassword())) {
            return LoginResponse.builder()
                    .message("Invalid password")
                    .token(null)
                    .build();
        }

        // Placeholder token
        String fakeJwt = "FAKE-JWT-TOKEN-" + UUID.randomUUID();

        return LoginResponse.builder()
                .message("Login successful")
                .token(fakeJwt)
                .build();
    }

    @Override
    public RegisterResponse registerUser(RegisterRequest request) {
        if (employeeRepository.findByEmail(request.getEmail()).isPresent()) {
            return new RegisterResponse("Email already registered", null);
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        Employee employee = Employee.builder()
                .employeeNumber("EMP-" + System.currentTimeMillis())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(hashedPassword)
                .role(request.getRole() != null ? request.getRole() : Role.EMPLOYEE)
                .team(request.getTeam())
                .hiredAt(request.getHiredAt())
                .build();

        Employee saved = employeeRepository.save(employee);

        return new RegisterResponse(
                "Registration successful",
                employeeMapper.toDto(saved)
        );
    }
}
