package com.example.manageruser.Repository;

import com.example.manageruser.Model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SearchRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE CONCAT(u.username, ' ', u.email) LIKE %?1% AND u != ?2")
    Page<User> search(String keyword, User currentUser, Pageable pageable);


    @Query("SELECT u FROM User u WHERE LOWER(u.address) LIKE LOWER(CONCAT('%', ?1, '%')) AND u != ?2")
    Page<User> findFriendsByAddress(String address, User currentUser, Pageable pageable);


    User findUserByUsername(String username);
}
