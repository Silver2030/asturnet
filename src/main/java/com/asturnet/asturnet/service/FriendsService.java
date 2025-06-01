package com.asturnet.asturnet.service;

import com.asturnet.asturnet.model.Friends; // <-- ¡Importa la clase Friends!
import com.asturnet.asturnet.model.FriendshipStatus;
import com.asturnet.asturnet.model.User;
import java.util.List;
import java.util.Optional;

public interface FriendsService {

    Friends sendFriendRequest(User sender, User receiver);

    Friends acceptFriendRequest(User acceptor, User requester);

    void declineFriendRequest(User decliner, User requester);

    void removeFriend(User user1, User user2);

    FriendshipStatus getFriendshipStatus(User currentUser, User otherUser);

    List<Friends> getPendingFriendRequests(User user);

    List<User> getFriends(User user);

    void cancelFriendRequest(User sender, User receiver);

    List<User> getAcceptedFriends(User user);

    Optional<Friends> findByUserAndFriend(User user, User friend);
}