package com.asturnet.asturnet.service; // ¡Ojo! Este es el .service, no .service.impl

import com.asturnet.asturnet.model.Friends;
import com.asturnet.asturnet.model.FriendshipStatus;
import com.asturnet.asturnet.model.User;
import com.asturnet.asturnet.repository.FriendsRepository;
import com.asturnet.asturnet.service.FriendsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FriendsServiceImpl implements FriendsService {

    private final FriendsRepository friendsRepository;

    public FriendsServiceImpl(FriendsRepository friendsRepository) {
        this.friendsRepository = friendsRepository;
    }

    // Helper para encontrar una amistad entre dos usuarios, sin importar el orden
    @Override
    public Optional<Friends> findFriendsBetween(User user1, User user2) {
        Optional<Friends> friends = friendsRepository.findByUserAndFriend(user1, user2);
        if (friends.isPresent()) {
            return friends;
        }
        return friendsRepository.findByUserAndFriend(user2, user1);
    }

    @Override
    @Transactional
    public Friends sendFriendRequest(User sender, User receiver) {
        if (sender.getId().equals(receiver.getId())) {
            throw new RuntimeException("No puedes enviarte una solicitud de amistad a ti mismo.");
        }

        Optional<Friends> existingFriends = findFriendsBetween(sender, receiver);

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
            throw new RuntimeException("No se encontró una solicitud de amistad pendiente para cancelar de " + sender.getUsername() + " a " + receiver.getUsername());
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
        Optional<Friends> friendsOptional = findFriendsBetween(user1, user2);

        if (friendsOptional.isEmpty()) {
            throw new RuntimeException("No existe una amistad entre estos usuarios.");
        }

        Friends friends = friendsOptional.get();

        if (friends.getStatus() != FriendshipStatus.ACCEPTED) {
            throw new RuntimeException("Los usuarios no son amigos actualmente.");
        }

        friendsRepository.delete(friends);
    }

    @Override // <--- Asegúrate de que esta anotación @Override está presente
    public FriendshipStatus getFriendshipStatus(User user1, User user2) {
        if (user1 == null || user2 == null) {
            return FriendshipStatus.NOT_FRIENDS;
        }

        if (user1.getId().equals(user2.getId())) {
            return FriendshipStatus.ACCEPTED;
        }

        // *** ¡CAMBIO CRUCIAL AQUÍ! ***
        // Buscar si user1 envió solicitud a user2 (user es user1, friend es user2)
        Optional<Friends> request1to2 = friendsRepository.findByUserAndFriend(user1, user2);
        if (request1to2.isPresent()) {
            return request1to2.get().getStatus();
        }

        // *** ¡CAMBIO CRUCIAL AQUÍ! ***
        // Buscar si user2 envió solicitud a user1 (user es user2, friend es user1)
        Optional<Friends> request2to1 = friendsRepository.findByUserAndFriend(user2, user1);
        if (request2to1.isPresent()) {
            return request2to1.get().getStatus();
        }

        return FriendshipStatus.NOT_FRIENDS;
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
        List<Friends> friendshipsAsUser = friendsRepository.findByUserAndStatus(user, FriendshipStatus.ACCEPTED);
        List<Friends> friendshipsAsFriend = friendsRepository.findByFriendAndStatus(user, FriendshipStatus.ACCEPTED);

        Set<User> friends = friendshipsAsUser.stream()
            .map(Friends::getFriend)
            .collect(Collectors.toSet());

        friendshipsAsFriend.stream()
            .map(Friends::getUser)
            .forEach(friends::add);

        return new ArrayList<>(friends); // Convertir el Set (para eliminar duplicados) de nuevo a List
    }

    @Override
    // Este método ya existe en la interfaz y su implementación debería delegar al repositorio directamente.
    public Optional<Friends> findByUserAndFriend(User user, User friend) {
        return friendsRepository.findByUserAndFriend(user, friend);
    }
}