package com.kifeito.user.Entity;

import jakarta.persistence.*;

@Entity
@Table(name ="T_KFT_USER")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String name;

    @Column(nullable = false, unique = true)
    String email;

    @Column(nullable = false)
    String password;

}
