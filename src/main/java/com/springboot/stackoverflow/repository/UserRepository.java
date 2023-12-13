package com.springboot.stackoverflow.repository;

import com.springboot.stackoverflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("SELECT u FROM User u WHERE " + "LOWER(u.userName) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<User> searchUser(String search);

    public User findByEmail(String email);

    public User findUserById(int id);

    User findUserByUserName(String name);

}

