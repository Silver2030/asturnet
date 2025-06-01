package com.asturnet.asturnet.controller;

import com.asturnet.asturnet.model.FriendshipStatus;
import com.asturnet.asturnet.model.Post;
import com.asturnet.asturnet.model.User;
import com.asturnet.asturnet.service.FriendsService;
import com.asturnet.asturnet.service.LikeService;
import com.asturnet.asturnet.service.PostService;
import com.asturnet.asturnet.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication; // Necesario para SecurityContextHolder
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder; // Necesario para SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority; // Necesario para ROLE_ADMIN
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    private final PostService postService;
    private final UserService userService;
    private final FriendsService friendsService;
    private final LikeService likeService;

    @Autowired
    public HomeController(PostService postService, UserService userService, FriendsService friendsService, LikeService likeService) {
        this.postService = postService;
        this.userService = userService;
        this.friendsService = friendsService;
        this.likeService = likeService;
    }

    // Método auxiliar para verificar si el usuario actual es admin
    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() &&
               authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    // Endpoint para devolver la entrada de la página
    @GetMapping("/home")
    public String home(Model model, Principal principal, @AuthenticationPrincipal UserDetails currentUserDetails) { // Añade UserDetails aquí
        List<Post> postsToShow = new ArrayList<>();
        Map<Long, Boolean> userLikesPost = new HashMap<>();
        Map<Long, Long> postLikesCount = new HashMap<>();
        Long currentUserId = null;
        User currentUser = null;
        boolean isAdmin = false; 

        if (principal != null) {
            String currentUsername = principal.getName();
            currentUser = userService.findByUsername(currentUsername);

            if (currentUser != null) {
                currentUserId = currentUser.getId();
                model.addAttribute("username", currentUser.getUsername());
                model.addAttribute("currentUser", currentUser);

                // Determinar si es administrador
                isAdmin = currentUserDetails.getAuthorities().stream() // Usamos currentUserDetails para esto
                                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                model.addAttribute("isAdmin", isAdmin); 
                
                postsToShow = postService.getHomeFeedPosts(currentUser); // Mantenemos la firma actual
            } else {
                System.out.println("Current User not found in DB for principal: " + currentUsername);
                model.addAttribute("username", "Invitado");
                model.addAttribute("isAdmin", false); 
            }
        } else {
            System.out.println("User not authenticated. Showing empty feed.");
            model.addAttribute("username", "Invitado");
            model.addAttribute("isAdmin", false); 

            model.addAttribute("posts", new ArrayList<>());
            model.addAttribute("userLikesPost", new HashMap<>());
            model.addAttribute("postLikesCount", new HashMap<>());
            model.addAttribute("userId", null);
            return "home";
        }

        for (Post post : postsToShow) {
            if (currentUser != null) {
                boolean userHasLiked = likeService.isLikedByUser(currentUser, post);
                userLikesPost.put(post.getId(), userHasLiked);
            }
            long likesCount = likeService.getLikesCountForPost(post);
            postLikesCount.put(post.getId(), likesCount);
        }

        model.addAttribute("posts", postsToShow);
        model.addAttribute("userLikesPost", userLikesPost);
        model.addAttribute("postLikesCount", postLikesCount);
        model.addAttribute("userId", currentUserId);

        return "home";
    }

    // Endpoint para devolver la lista de busqueda de usuarios
    @GetMapping("/search")
    public String searchUsers(@RequestParam(value = "query", required = false) String query, Model model, Principal principal) {
        List<User> searchResults = new ArrayList<>(); 
        
        model.addAttribute("isAdmin", isAdmin()); 

        if (query != null && !query.trim().isEmpty()) {
            searchResults = userService.searchUsers(query.trim());
            model.addAttribute("query", query);
        }

        if (principal != null) {
            String currentUsername = principal.getName();
            User currentUser = userService.findByUsername(currentUsername);
            if (currentUser != null) {
                Map<Long, FriendshipStatus> friendshipStatuses = new HashMap<>();
                for (User user : searchResults) {
                    if (!user.equals(currentUser)) {
                        FriendshipStatus status = friendsService.getFriendshipStatus(currentUser, user);
                        friendshipStatuses.put(user.getId(), status);
                    }
                }
                model.addAttribute("friendshipStatuses", friendshipStatuses);
                model.addAttribute("currentUserId", currentUser.getId());
            }
            model.addAttribute("currentUser", currentUser); 
        } else {

            model.addAttribute("currentUser", null); // Para evitar NullPointer en Thymeleaf
            model.addAttribute("currentUserId", null);
            model.addAttribute("friendshipStatuses", new HashMap<>());
        }
        
        model.addAttribute("searchResults", searchResults);
        return "search-results";
    }

    
}