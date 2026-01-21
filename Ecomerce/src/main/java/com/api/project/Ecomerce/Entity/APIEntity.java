package com.api.project.Ecomerce.Entity;

import jakarta.persistence.*;

import javax.annotation.processing.Generated;

@Entity
@Table(name = "API")
public class APIEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;



}
