package com.recruitmentplatform.auth.event;

import com.recruitmentplatform.auth.enums.AccountRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredPayload {
    private UUID userId;
    private String email;
    private AccountRole role;
    private String firstName;
    private String lastName;
}
