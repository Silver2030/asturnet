package com.asturnet.asturnet.repository;

import com.asturnet.asturnet.model.Friends;
import com.asturnet.asturnet.model.FriendshipStatus;
import com.asturnet.asturnet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; 
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendsRepository extends JpaRepository<Friends, Long> {

    Optional<Friends> findByUserAndFriend(User user, User friend);

    @Query("SELECT f FROM Friends f WHERE (f.user = :user1 AND f.friend = :user2) OR (f.user = :user2 AND f.friend = :user1)")
    Optional<Friends> findByUsersInAnyOrder(User user1, User user2);

    Optional<Friends> findByUserAndFriendAndStatus(User user, User friend, FriendshipStatus status);

    List<Friends> findByUserAndStatus(User user, FriendshipStatus status);
    List<Friends> findByFriendAndStatus(User friend, FriendshipStatus status);
}