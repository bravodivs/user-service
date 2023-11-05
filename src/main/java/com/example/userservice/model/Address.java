package com.example.userservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "addresses")
@Getter
@Setter
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JdbcTypeCode(SqlTypes.LONGNVARCHAR)
    @Column(columnDefinition = "VARCHAR(36)")
    @Schema(hidden = true)
    private UUID id;

    @NotNull
    @Column(nullable = false)
    private Integer pincode;

    @NotNull
    @NotEmpty
    @Column(nullable = false)
    private String city;

    @NotNull
    @NotEmpty
    @Column(nullable = false)
    private String state;

    @NotNull
    @NotEmpty
    @Column(nullable = false)
    private String addressLine1;

    @NotNull
    @NotEmpty
    @Column(nullable = false)
    private String addressLine2;

    @Column(columnDefinition = "varchar(20) default 'INDIA'")
    private String country;

    @Override
    public String toString() {
        return "Addresses{" +
                "Id=" + id +
                ", pincode=" + pincode +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", addressLine1='" + addressLine1 + '\'' +
                ", addressLine2='" + addressLine2 + '\'' +
                ", country='" + country + '\'' +
                '}';
    }
}
