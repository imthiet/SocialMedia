package com.example.manageruser.Controller;

import com.example.manageruser.Model.Like;
import com.example.manageruser.Model.Notification;
import com.example.manageruser.Model.Post;
import com.example.manageruser.Model.User;
import com.example.manageruser.Service.LikeService;
import com.example.manageruser.Service.NotificationService;
import com.example.manageruser.Service.PostService;
import com.example.manageruser.Service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.Principal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static com.example.manageruser.Model.NotificationType.LIKE_COMMENT_SHARE;

@RestController
public class newFeedController {

    @Autowired
    private PostService postService;
    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    NotificationService notificationService;
    // Hiển thị trang newsfeed
    @GetMapping("/newsfeed")
    public String showNewsFeed(Model model, HttpServletResponse response,
                               Principal principal,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "7") int size) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        String username = principal.getName();
        User user = userService.findByUsername(username);

        int uID = user.getId();


        // Lấy danh sách bài post từ bạn bè của người dùng hiện tại
        List<Post> posts = postService.getPostsByFriendShip(uID, page, size);
        System.out.println("get post"+ posts);

        model.addAttribute("posts", posts);
        model.addAttribute("currentPage", page);
        return "newsfeed";
    }

    // Hiển thị ảnh từ bài post
    @GetMapping("/post/image")
    public ResponseEntity<byte[]> displayPostImage(@RequestParam("id") Long postId) throws SQLException, IOException {
        // Lấy post theo id
        Post post = postService.findPostById(postId);

        // Kiểm tra xem post có ảnh không
        if (post == null || post.getPng() == null) {
            System.out.println("khong có ảnh");
            return ResponseEntity.notFound().build();

        }
        System.out.println("có ảnh");
        // Chuyển đổi Blob thành byte[]
        byte[] imageBytes = post.getPng().getBytes(1, (int) post.getPng().length());

        // Trả về ảnh dưới định dạng JPEG (hoặc loại định dạng khác tuỳ vào kiểu ảnh của bạn)
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(imageBytes);

   
    }


    @PostMapping("/like")
    public ResponseEntity<String> likePost(@RequestParam("postId") Long postId, Principal principal) {
        String currentUsername = principal.getName();
        User user = userService.findByUsername(currentUsername);
        Post post = postService.findById(postId);

        if (user != null && post != null) {
            // Kiểm tra xem người dùng đã like bài đăng chưa
            if (!likeService.existsByUserAndPost(user, post)) {
                // Thực hiện like
                Like like = new Like();
                like.setUser(user);
                like.setPost(post);
                likeService.save(like);

                // Tạo thông báo
                Notification notification = new Notification();
                notification.setContentnoti(user.getUsername() + " đã thích bài đăng của bạn.");
                notification.setType(LIKE_COMMENT_SHARE);
                notification.setSender(user);
                notification.setReceiver(post.getUser());
                notification.setStatus("unread");
                notification.setTimestamp(LocalDateTime.now());
                notificationService.save(notification);

                return ResponseEntity.ok("Post liked");
            } else {
                return ResponseEntity.badRequest().body("Post already liked");
            }
        }
        return ResponseEntity.badRequest().body("User or post not found");
    }

}
