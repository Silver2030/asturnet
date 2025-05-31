package com.asturnet.asturnet.service;

import com.asturnet.asturnet.model.Friends;
import com.asturnet.asturnet.model.FriendshipStatus;
import com.asturnet.asturnet.model.User;
import com.asturnet.asturnet.repository.FriendsRepository;
import com.asturnet.asturnet.service.FriendsService; // Importa la interfaz
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set; // Importa Set para eliminar duplicados
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
        // Intenta encontrar la relación directa (user1 -> user2)
        Optional<Friends> friends = friendsRepository.findByUserAndFriend(user1, user2);
        if (friends.isPresent()) {
            return friends;
        }
        // Si no se encuentra, intenta encontrar la relación inversa (user2 -> user1)
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
                // Si ya hay una solicitud PENDIENTE, verificar la dirección
                if (fs.getUser().getId().equals(sender.getId())) {
                    throw new RuntimeException("Ya has enviado una solicitud de amistad a este usuario.");
                } else { // Si la solicitud PENDIENTE es en la dirección inversa (receiver -> sender)
                    // Esto significa que el receiver ya le envió una solicitud al sender.
                    // Al sender intentar enviar una solicitud, en realidad está aceptando la del receiver.
                    fs.setStatus(FriendshipStatus.ACCEPTED);
                    return friendsRepository.save(fs);
                }
            } else if (fs.getStatus() == FriendshipStatus.ACCEPTED) {
                throw new RuntimeException("Ya sois amigos.");
            } else if (fs.getStatus() == FriendshipStatus.REJECTED) {
                // Si la solicitud fue rechazada, aquí puedes decidir la lógica.
                // Opción 1: Permitir reenviar, eliminando la relación rechazada y creando una nueva.
                friendsRepository.delete(fs); // Eliminar la relación rechazada
                // Luego se creará una nueva más abajo
            }
        }

        // Si no hay amistad existente o se ha eliminado una rechazada
        Friends newFriends = new Friends();
        newFriends.setUser(sender);
        newFriends.setFriend(receiver);
        newFriends.setStatus(FriendshipStatus.PENDING);
        return friendsRepository.save(newFriends);
    }

    // --- MÉTODO CANCELAR SOLICITUD DE AMISTAD (AÑADIDO Y CORREGIDO) ---
    @Override
    @Transactional
    public void cancelFriendRequest(User sender, User receiver) {
        // Debemos buscar la solicitud específica donde 'sender' es quien envió y 'receiver' quien la recibió
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
        // El 'acceptor' es el 'friend' en la relación PENDING, y el 'requester' es el 'user'.
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
        // El 'decliner' es el 'friend' en la relación PENDING, y el 'requester' es el 'user'.
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

    public FriendshipStatus getFriendshipStatus(User user1, User user2) {
        if (user1 == null || user2 == null) {
            return FriendshipStatus.NOT_FRIENDS; // O lanza una excepción, dependiendo de tu lógica de negocio
        }

        if (user1.getId().equals(user2.getId())) {
            return FriendshipStatus.ACCEPTED; // O un estado especial como SELF, si prefieres. Para posts, si eres tú, siempre es ACCEPTED
        }

        // Buscar si user1 envió solicitud a user2
        Optional<Friends> request1to2 = friendsRepository.findByRequesterAndReceiver(user1, user2);
        if (request1to2.isPresent()) {
            return request1to2.get().getStatus(); // PENDING o ACCEPTED
        }

        // Buscar si user2 envió solicitud a user1 (es decir, user1 recibió solicitud de user2)
        Optional<Friends> request2to1 = friendsRepository.findByRequesterAndReceiver(user2, user1);
        if (request2to1.isPresent()) {
            return request2to1.get().getStatus(); // PENDING o ACCEPTED
        }

        return FriendshipStatus.NOT_FRIENDS; // No hay relación de amistad
    }

    @Override
    public List<Friends> getPendingFriendRequests(User user) {
        // Las solicitudes pendientes son aquellas donde 'user' es el 'friend' y el status es PENDING
        return friendsRepository.findByFriendAndStatus(user, FriendshipStatus.PENDING);
    }

    @Override
    public List<User> getFriends(User user) {
        // Obtener amistades donde el 'user' actual es el 'user' en la relación y el estado es ACCEPTED
        List<Friends> acceptedFriendsAsUser = friendsRepository.findByUserAndStatus(user, FriendshipStatus.ACCEPTED);
        // Obtener amistades donde el 'user' actual es el 'friend' en la relación y el estado es ACCEPTED
        List<Friends> acceptedFriendsAsFriend = friendsRepository.findByFriendAndStatus(user, FriendshipStatus.ACCEPTED);

        // Usar un Set para combinar los resultados y eliminar duplicados de forma eficiente
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
        // Obtener las amistades donde el 'user' es el que envía la solicitud y el estado es ACCEPTED
        List<Friends> friendshipsAsUser = friendsRepository.findByUserAndStatus(user, FriendshipStatus.ACCEPTED);
        // Obtener las amistades donde el 'user' es el que recibe la solicitud y el estado es ACCEPTED
        List<Friends> friendshipsAsFriend = friendsRepository.findByFriendAndStatus(user, FriendshipStatus.ACCEPTED);

        // Recopilar los amigos de ambas listas, evitando duplicados
        Set<User> friends = friendshipsAsUser.stream()
                                            .map(Friends::getFriend) // El 'friend' es el amigo del 'user'
                                            .collect(Collectors.toSet());

        friendshipsAsFriend.stream()
                           .map(Friends::getUser) // El 'user' es el amigo del 'friend' (user actual)
                           .forEach(friends::add);

        // Convertir el Set (para eliminar duplicados) de nuevo a List
        return friends.stream().collect(Collectors.toList());
    }
}