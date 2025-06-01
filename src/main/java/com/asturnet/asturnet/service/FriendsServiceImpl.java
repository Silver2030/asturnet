package com.asturnet.asturnet.service;

import com.asturnet.asturnet.model.Friends;
import com.asturnet.asturnet.model.FriendshipStatus;
import com.asturnet.asturnet.model.User;
import com.asturnet.asturnet.repository.FriendsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FriendsServiceImpl implements FriendsService {

    private final FriendsRepository friendsRepository;

    public FriendsServiceImpl(FriendsRepository friendsRepository) {
        this.friendsRepository = friendsRepository;
    }

    @Override
    @Transactional
    public Friends sendFriendRequest(User sender, User receiver) {
        if (sender.getId().equals(receiver.getId())) {
            throw new RuntimeException("No puedes enviarte una solicitud de amistad a ti mismo.");
        }

        Optional<Friends> existingFriends = friendsRepository.findByUsersInAnyOrder(sender, receiver);

        if (existingFriends.isPresent()) {
            Friends fs = existingFriends.get();

            if (fs.getStatus() == FriendshipStatus.PENDING) {
                if (fs.getUser().getId().equals(sender.getId())) {
                    throw new RuntimeException("Ya has enviado una solicitud de amistad a este usuario.");
                } else {
                    fs.setStatus(FriendshipStatus.ACCEPTED);
                    return friendsRepository.save(fs);
                }
            } else if (fs.getStatus() == FriendshipStatus.ACCEPTED) {
                throw new RuntimeException("Ya sois amigos.");
            } else if (fs.getStatus() == FriendshipStatus.REJECTED) {
                friendsRepository.delete(fs);
            }
        }

        Friends newFriends = new Friends();
        newFriends.setUser(sender);
        newFriends.setFriend(receiver);
        newFriends.setStatus(FriendshipStatus.PENDING);
        return friendsRepository.save(newFriends);
    }

    @Override
    @Transactional
    public void cancelFriendRequest(User sender, User receiver) {
        Optional<Friends> pendingRequest = friendsRepository.findByUserAndFriendAndStatus(sender, receiver, FriendshipStatus.PENDING);

        if (pendingRequest.isPresent()) {
            friendsRepository.delete(pendingRequest.get());
        } else {
            throw new RuntimeException("No se encontró una solicitud de amistad pendiente enviada por " + sender.getUsername() + " a " + receiver.getUsername() + " para cancelar.");
        }
    }

    @Override
    @Transactional
    public Friends acceptFriendRequest(User acceptor, User requester) {
        Optional<Friends> friendsOptional = friendsRepository.findByUserAndFriendAndStatus(requester, acceptor, FriendshipStatus.PENDING);

        if (friendsOptional.isEmpty()) {
            throw new RuntimeException("Solicitud de amistad no encontrada o no está pendiente.");
        }

        Friends friends = friendsOptional.get();
        friends.setStatus(FriendshipStatus.ACCEPTED);
        return friendsRepository.save(friends);
    }

    @Override
    @Transactional
    public void declineFriendRequest(User decliner, User requester) {
        Optional<Friends> friendsOptional = friendsRepository.findByUserAndFriendAndStatus(requester, decliner, FriendshipStatus.PENDING);

        if (friendsOptional.isEmpty()) {
            throw new RuntimeException("Solicitud de amistad no encontrada o no está pendiente.");
        }

        Friends friends = friendsOptional.get();
        friendsRepository.delete(friends);
    }

    @Override
    @Transactional
    public void removeFriend(User user1, User user2) {
        Optional<Friends> friendsOptional = friendsRepository.findByUsersInAnyOrder(user1, user2);

        if (friendsOptional.isEmpty()) {
            throw new RuntimeException("No existe una amistad entre estos usuarios.");
        }

        Friends friends = friendsOptional.get();

        if (friends.getStatus() != FriendshipStatus.ACCEPTED) {
            throw new RuntimeException("Los usuarios no son amigos actualmente.");
        }

        friendsRepository.delete(friends);
    }

    @Override
    public FriendshipStatus getFriendshipStatus(User user1, User user2) {
        if (user1 == null || user2 == null) {
            return FriendshipStatus.NOT_FRIENDS;
        }
        if (user1.getId().equals(user2.getId())) {
            return FriendshipStatus.NOT_FRIENDS; 
        }

        Optional<Friends> friendship = friendsRepository.findByUsersInAnyOrder(user1, user2);

        if (friendship.isPresent()) {
            return friendship.get().getStatus();
        } else {
            return FriendshipStatus.NOT_FRIENDS;
        }
    }

    @Override
    public List<Friends> getPendingFriendRequests(User user) {
        return friendsRepository.findByFriendAndStatus(user, FriendshipStatus.PENDING);
    }

    @Override
    public List<User> getFriends(User user) {
        List<Friends> acceptedFriendsAsUser = friendsRepository.findByUserAndStatus(user, FriendshipStatus.ACCEPTED);
        List<Friends> acceptedFriendsAsFriend = friendsRepository.findByFriendAndStatus(user, FriendshipStatus.ACCEPTED);

        Set<User> uniqueFriends = acceptedFriendsAsUser.stream()
            .map(Friends::getFriend) 
            .collect(Collectors.toSet());

        acceptedFriendsAsFriend.stream()
            .map(Friends::getUser) 
            .forEach(uniqueFriends::add); 

        return new ArrayList<>(uniqueFriends); 
    }

    @Override
    public List<User> getAcceptedFriends(User user) {
        return getFriends(user); 
    }

    @Override
    public Optional<Friends> findByUserAndFriend(User user, User friend) {
        return friendsRepository.findByUserAndFriend(user, friend);
    }
}