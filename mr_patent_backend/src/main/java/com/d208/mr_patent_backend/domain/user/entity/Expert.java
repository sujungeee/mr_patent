package com.d208.mr_patent_backend.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "expert")
@Getter
@Setter
@NoArgsConstructor
public class Expert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expert_id", columnDefinition = "INT UNSIGNED")
    private Integer expertId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expert_name", length = 25, nullable = false)
    private String expertName;

    @Column(name = "expert_identification", length = 15, nullable = false)
    private String expertIdentification = "0";

    @Column(name = "expert_description", length = 500, nullable = false)
    private String expertDescription;

    @Column(name = "expert_address", length = 30, nullable = false)
    private String expertAddress;

    @Column(name = "expert_phone", length = 15, nullable = false)
    private String expertPhone;

    @Column(name = "expert_get_date", nullable = false)
    private LocalDate expertGetDate;

    @Column(name = "expert_license", length = 255, nullable = false)
    private String expertLicense;

    @Column(name = "expert_license_number", length = 30, nullable = false)
    private String expertLicenseNumber;

    @Column(name = "expert_status", nullable = false)
    private Integer expertStatus = 0;  // 기본값: 미승인

    @Column(name = "expert_created_at", nullable = false)
    private LocalDateTime expertCreatedAt;

    @Column(name = "expert_updated_at")
    private LocalDateTime expertUpdatedAt;

    @PrePersist
    public void prePersist() {
        this.expertCreatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.expertUpdatedAt = LocalDateTime.now();
    }
}