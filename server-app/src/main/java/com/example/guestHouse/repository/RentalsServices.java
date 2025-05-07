package com.example.guestHouse.repository;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "rental_services")
public class RentalsServices {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "service", nullable = false)
    private Service service;

    @ManyToOne
    @JoinColumn(name = "rental", nullable = false)
    private Rental rental;

    @Column(name = "extime")
    private LocalDateTime exTime;

    public RentalsServices(Long id, Service service, Rental rental, LocalDateTime exTime) {
        this.id = id;
        this.service = service;
        this.rental = rental;
        this.exTime = exTime;
    }

    public RentalsServices() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public Rental getRental() {
        return rental;
    }

    public void setRental(Rental rental) {
        this.rental = rental;
    }

    public LocalDateTime getExTime() {
        return exTime;
    }

    public void setExTime(LocalDateTime exTime) {
        this.exTime = exTime;
    }
}
