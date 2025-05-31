package com.asturnet.asturnet.repository;

import com.asturnet.asturnet.model.Friends;
import com.asturnet.asturnet.model.FriendshipStatus;
import com.asturnet.asturnet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendsRepository extends JpaRepository<Friends, Long> {

    // Encuentra una amistad específica donde 'user' es el remitente y 'friend' es el receptor
    Optional<Friends> findByUserAndFriend(User user, User friend);

    // Encuentra una relación específica por remitente, receptor y estado
    Optional<Friends> findByUserAndFriendAndStatus(User user, User friend, FriendshipStatus status);

    // Métodos para obtener listas de amistades basadas en el rol del usuario en la relación
    List<Friends> findByUserAndStatus(User user, FriendshipStatus status);
    List<Friends> findByFriendAndStatus(User friend, FriendshipStatus status);

    // NOTA: findByFriendAndUser(User user1, User user2) ya no es necesario si usas findByUserAndFriend(user2, user1)
    // NOTA: findByUserOrFriendAndStatus(User user, User user, FriendshipStatus status) no es un método JPA estándar para "OR" con la misma entidad en ambos lados.
    //      Lo manejaremos en el servicio combinando los resultados de findByUserAndStatus y findByFriendAndStatus.
}