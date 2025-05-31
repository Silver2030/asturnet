package com.asturnet.asturnet.service;

import com.asturnet.asturnet.model.Friends; // <-- ¡Importa la clase Friends!
import com.asturnet.asturnet.model.FriendshipStatus;
import com.asturnet.asturnet.model.User;
import java.util.List;
import java.util.Optional;

public interface FriendsService {
    // Enviar una solicitud de amistad
    Friends sendFriendRequest(User sender, User receiver);

    // Aceptar una solicitud de amistad
    Friends acceptFriendRequest(User acceptor, User requester);

    // Rechazar una solicitud de amistad
    void declineFriendRequest(User decliner, User requester);

    // Eliminar una amistad
    void removeFriend(User user1, User user2);

    // Obtener el estado de la amistad entre dos usuarios
    FriendshipStatus getFriendshipStatus(User currentUser, User otherUser);

    // Obtener todas las solicitudes de amistad pendientes recibidas por un usuario
    List<Friends> getPendingFriendRequests(User user);

    // Obtener la lista de amigos de un usuario
    List<User> getFriends(User user);

    // Obtener la relación de amistad directa o inversa entre dos usuarios
    Optional<Friends> findFriendsBetween(User user1, User user2);

    // CANCEL FRIEND REQUEST: Añadido este método
    void cancelFriendRequest(User sender, User receiver);

    List<User> getAcceptedFriends(User user);
}