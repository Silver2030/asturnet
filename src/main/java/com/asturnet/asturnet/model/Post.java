package com.asturnet.asturnet.model;

import jakarta.persistence.*; 
import lombok.Data; 
import lombok.NoArgsConstructor; 
import lombok.AllArgsConstructor; 
import org.hibernate.annotations.CreationTimestamp; 
import org.hibernate.annotations.UpdateTimestamp; 

import java.time.LocalDateTime; 
import java.util.ArrayList; 
import java.util.List;     

@Entity 
@Table(name = "posts") 
@Data
@NoArgsConstructor 
@AllArgsConstructor 
public class Post {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false) 
    private String content; 

    @Column(name = "image_url", columnDefinition = "TEXT") 
    private String imageUrl;

    @Column(name = "video_url", columnDefinition = "TEXT") 
    private String videoUrl; 

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "user_id", nullable = false) 
    private User user; 

    @CreationTimestamp 
    @Column(name = "created_at", updatable = false) 
    private LocalDateTime createdAt;

    @UpdateTimestamp 
    @Column(name = "updated_at") 
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>(); 

    @ManyToMany
    @JoinTable(
            name = "post_likes",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> likedByUsers = new ArrayList<>(); 
}