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

    // Ya no es necesario findFriendsBetween aquí, ya que findByUsersInAnyOrder en el repositorio es bidireccional.
    // @Override
    // public Optional<Friends> findFriendsBetween(User user1, User user2) {
    //    // ... (lógica movida al repositorio)
    // }

    @Override
    @Transactional
    public Friends sendFriendRequest(User sender, User receiver) {
        if (sender.getId().equals(receiver.getId())) {
            throw new RuntimeException("No puedes enviarte una solicitud de amistad a ti mismo.");
        }

        // Utiliza el método bidireccional del repositorio para encontrar cualquier relación existente
        Optional<Friends> existingFriends = friendsRepository.findByUsersInAnyOrder(sender, receiver);

        if (existingFriends.isPresent()) {
            Friends fs = existingFriends.get();

            if (fs.getStatus() == FriendshipStatus.PENDING) {
                // Si ya hay una solicitud pendiente, verificamos quién la envió
                if (fs.getUser().getId().equals(sender.getId())) {
                    throw new RuntimeException("Ya has enviado una solicitud de amistad a este usuario.");
                } else {
                    // Si la solicitud pendiente fue enviada por el 'receiver' (ahora 'requester'), la aceptamos
                    fs.setStatus(FriendshipStatus.ACCEPTED);
                    return friendsRepository.save(fs);
                }
            } else if (fs.getStatus() == FriendshipStatus.ACCEPTED) {
                throw new RuntimeException("Ya sois amigos.");
            } else if (fs.getStatus() == FriendshipStatus.REJECTED) {
                // Si la relación estaba en estado REJECTED, la eliminamos para permitir una nueva solicitud
                friendsRepository.delete(fs);
            }
        }

        // Si no hay relación o fue rechazada y eliminada, se crea una nueva solicitud
        Friends newFriends = new Friends();
        newFriends.setUser(sender);
        newFriends.setFriend(receiver);
        newFriends.setStatus(FriendshipStatus.PENDING);
        return friendsRepository.save(newFriends);
    }

    @Override
    @Transactional
    public void cancelFriendRequest(User sender, User receiver) {
        // Busca la solicitud específica donde 'sender' es quien envió y 'receiver' es quien recibió, en estado PENDING
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
        // Busca la solicitud donde 'requester' es quien envió y 'acceptor' es quien recibió, en estado PENDING
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
        // Busca la solicitud donde 'requester' es quien envió y 'decliner' es quien recibió, en estado PENDING
        Optional<Friends> friendsOptional = friendsRepository.findByUserAndFriendAndStatus(requester, decliner, FriendshipStatus.PENDING);

        if (friendsOptional.isEmpty()) {
            throw new RuntimeException("Solicitud de amistad no encontrada o no está pendiente.");
        }

        Friends friends = friendsOptional.get();
        // Puedes cambiar el estado a REJECTED o simplemente eliminar la entrada
        friendsRepository.delete(friends); // Eliminamos la entrada para simplificar la lógica.
    }

    @Override
    @Transactional
    public void removeFriend(User user1, User user2) {
        // Utiliza el método bidireccional del repositorio para encontrar la amistad
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

        // Si se llama a este método para el propio usuario, no hay un "estado de amistad" real.
        // El controlador ya maneja la visibilidad de los botones para el propio perfil.
        // Aquí devolvemos NOT_FRIENDS porque no eres amigo de ti mismo en el contexto de la red social.
        if (user1.getId().equals(user2.getId())) {
            return FriendshipStatus.NOT_FRIENDS; // O un estado específico si tu UI lo requiere para el propio perfil.
        }

        // ¡Utiliza el método bidireccional del repositorio!
        Optional<Friends> friendship = friendsRepository.findByUsersInAnyOrder(user1, user2);

        if (friendship.isPresent()) {
            return friendship.get().getStatus();
        } else {
            // Si no se encontró ninguna relación en ninguna dirección, no son amigos.
            return FriendshipStatus.NOT_FRIENDS;
        }
    }

    @Override
    public List<Friends> getPendingFriendRequests(User user) {
        // Las solicitudes pendientes son aquellas donde 'user' es el 'friend' y el estado es PENDING
        return friendsRepository.findByFriendAndStatus(user, FriendshipStatus.PENDING);
    }

    @Override
    // Este método ahora consolida la lógica para obtener todos los amigos aceptados.
    public List<User> getFriends(User user) {
        List<Friends> acceptedFriendsAsUser = friendsRepository.findByUserAndStatus(user, FriendshipStatus.ACCEPTED);
        List<Friends> acceptedFriendsAsFriend = friendsRepository.findByFriendAndStatus(user, FriendshipStatus.ACCEPTED);

        Set<User> uniqueFriends = acceptedFriendsAsUser.stream()
            .map(Friends::getFriend) // Obtiene el "amigo" cuando el 'user' actual es el "user" de la amistad
            .collect(Collectors.toSet());

        acceptedFriendsAsFriend.stream()
            .map(Friends::getUser) // Obtiene el "usuario" cuando el 'user' actual es el "friend" de la amistad
            .forEach(uniqueFriends::add); // Añade al set, los duplicados se manejan automáticamente

        return new ArrayList<>(uniqueFriends); // Convierte el Set (para eliminar duplicados) de nuevo a List
    }

    @Override
    // Este método es idéntico a getFriends, lo he mantenido por compatibilidad pero se podría eliminar la interfaz/implementación
    // Si quieres consolidar, puedes eliminar este método de la interfaz y la implementación y usar solo getFriends(User user).
    public List<User> getAcceptedFriends(User user) {
        return getFriends(user); // Delega al método consolidado
    }

    @Override
    // Este método findByUserAndFriend en la interfaz es unidireccional y delega al repositorio.
    // Aunque findByUsersInAnyOrder es el preferido para búsquedas bidireccionales,
    // este puede ser útil para operaciones específicas si necesitas la direccionalidad.
    public Optional<Friends> findByUserAndFriend(User user, User friend) {
        return friendsRepository.findByUserAndFriend(user, friend);
    }
}