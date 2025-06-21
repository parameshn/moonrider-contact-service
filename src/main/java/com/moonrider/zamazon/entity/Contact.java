package com.moonrider.zamazon.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;



import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "contacts")
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "linked_id")
    private Long linkedId;

    @Enumerated(EnumType.STRING)
    @Column(name = "link_precedence")
    private LinkPrecedence linkPrecedence;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public Contact(String email, String phoneNumber, LinkPrecedence linkPrecedence) {
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.linkPrecedence = linkPrecedence;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum LinkPrecedence {
        PRIMARY, SECONDARY
    }

}
