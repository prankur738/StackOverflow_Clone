package com.springboot.stackoverflow.entity;


import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "badges")
public class Badge {
    @Column(name = "reputationRequired")
    int reputationRequired;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Column(name = "name")
    private String name;
    @Column(name = "description")
    private String description;
    @ManyToMany
    @JoinTable(name = "user_badge", joinColumns = @JoinColumn(name = "badge_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> userBadge;

    public Badge() {
    }

    public Badge(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public int getReputationRequired() {
        return reputationRequired;
    }

    public void setReputationRequired(int reputationRequired) {
        this.reputationRequired = reputationRequired;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<User> getUserBadge() {
        return userBadge;
    }

    public void setUserBadge(List<User> userBadge) {
        this.userBadge = userBadge;
    }

    public void addUsers(User theUser) {
        if (userBadge == null) {
            userBadge = new ArrayList<>();
        }

        userBadge.add(theUser);
    }

    @Override
    public String toString() {
        return "Badge{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", userBadge=" + userBadge +
                '}';
    }
}