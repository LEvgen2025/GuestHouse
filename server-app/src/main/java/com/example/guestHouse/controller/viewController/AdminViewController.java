package com.example.guestHouse.controller.viewController;

import com.example.guestHouse.repository.User;
import com.example.guestHouse.repository.enums.Role;
import com.example.guestHouse.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@Controller
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminViewController {

    private final UserService userService;

    public AdminViewController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/admin")
    public String admin(Model model, Principal principal) {
        model.addAttribute("currentUsername",principal.getName());
        model.addAttribute("users", userService.findAll());
        return "admin";
    }

    @GetMapping("/admin/user/edit/{id}")
    public String userEdit(@PathVariable("id") Long id, Model model) {
        User user = userService.findUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + id));
        model.addAttribute("user", user);
        model.addAttribute("roles", Role.values());
        return "user-edit";
    }

    @PostMapping("/admin/user/edit")
    public String userEdit(@RequestParam("userId") Long userId,
                           @RequestParam("roles") String roleName) {
        User user = userService.findUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + userId));

        // Преобразуем строку в Enum
        Role newRole = Role.valueOf(roleName);

        userService.changeUserRole(user, newRole);  // Обновляем роль
        return "redirect:/admin";
    }

    @DeleteMapping("/admin/user/delete/{id}")
    @ResponseBody
    public ResponseEntity<Void> userDelete(@PathVariable Long id, Principal principal) {
        String currentUsername = principal.getName();
        String usernameToDelete = userService.findUsernameById(id);

        if (currentUsername.equals(usernameToDelete)) {
            return ResponseEntity.badRequest().build();
        }

        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
}
