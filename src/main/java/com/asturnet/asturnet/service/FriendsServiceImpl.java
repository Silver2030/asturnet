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
        // CAMBIO: Usamos findByUserAndFriend invirtiendo los parámetros
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
        Optional<Friends> friendsOptional = friendsRepository.findByUserAndFriendAndStatus(requester, acceptor, FriendshipStatus.PENDING); // CAMBIO: Usamos el método específico con status

        if (friendsOptional.isEmpty()) {
            throw new RuntimeException("Solicitud de amistad no encontrada o no está pendiente.");
        }

        Friends friends = friendsOptional.get();

        // No es necesario verificar status aquí si el método findByUserAndFriendAndStatus ya lo hizo
        // Asegurarse de que el 'acceptor' es de hecho el destinatario de la solicitud (ya lo hace el findByUserAndFriend)
        // if (!friends.getFriend().getId().equals(acceptor.getId())) {
        //     throw new RuntimeException("No tienes permiso para aceptar esta solicitud.");
        // }

        friends.setStatus(FriendshipStatus.ACCEPTED);
        return friendsRepository.save(friends);
    }

    @Override
    @Transactional
    public void declineFriendRequest(User decliner, User requester) {
        // El 'decliner' es el 'friend' en la relación PENDING, y el 'requester' es el 'user'.
        Optional<Friends> friendsOptional = friendsRepository.findByUserAndFriendAndStatus(requester, decliner, FriendshipStatus.PENDING); // CAMBIO: Usamos el método específico con status

        if (friendsOptional.isEmpty()) {
            throw new RuntimeException("Solicitud de amistad no encontrada o no está pendiente.");
        }

        Friends friends = friendsOptional.get();

        // Asegurarse de que el 'decliner' es de hecho el destinatario de la solicitud
        // if (!friends.getFriend().getId().equals(decliner.getId())) {
        //     throw new RuntimeException("No tienes permiso para rechazar esta solicitud.");
        // }

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

    @Override
    public FriendshipStatus getFriendshipStatus(User currentUser, User otherUser) {
        if (currentUser.getId().equals(otherUser.getId())) {
            return null; // O un estado especial como SELF, si lo definieras
        }

        Optional<Friends> friendsOptional = findFriendsBetween(currentUser, otherUser);

        if (friendsOptional.isPresent()) {
            Friends friends = friendsOptional.get();
            // La lógica aquí ya es más simple porque findFriendsBetween devuelve la relación sin importar la dirección
            return friends.getStatus(); // Retorna el estado actual de la relación encontrada
        }
        return null; // No hay relación de amistad
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
            .map(Friends::getFriend) // Mapear a la otra parte de la amistad
            .collect(Collectors.toSet());

        acceptedFriendsAsFriend.stream()
            .map(Friends::getUser) // Mapear a la otra parte de la amistad
            .forEach(uniqueFriends::add); // Añadir todos los usuarios únicos de la segunda lista al set

        return new ArrayList<>(uniqueFriends); // Convertir el Set a una List
    }
}