package com.example.guestHouse.repository;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "rentals")
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "rental")
    private Set<RentalsServices> rentalsServices = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "house", nullable = false)
    private House house;

    @ManyToOne
    @JoinColumn(name = "client", nullable = false)
    private Client client;

    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal summaryPrice;

    public Rental(Long id, House house, Client client, LocalDate startDate, LocalDate endDate, BigDecimal summaryPrice) {
        this.id = id;
        this.house = house;
        this.client = client;
        this.startDate = startDate;
        this.endDate = endDate;
        this.summaryPrice = summaryPrice;
    }


    public Rental() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public House getHouse() {
        return house;
    }

    public void setHouse(House house) {
        this.house = house;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getSummaryPrice() {
        return summaryPrice;
    }

    public void setSummaryPrice(BigDecimal summaryPrice) {
        this.summaryPrice = summaryPrice;
    }
}
