package com.sha.ecommerce_backend.dto;

import lombok.Data;

@Data
public class GetUserDto {
    private String id;
    private String userName;
    private String email;
    private String phoneNumber;
    private String address;
    private String studentId;
    private float reputation;
    private float balance;
    private boolean isAdmin;
}
