package spring.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import spring.dao.AdminDAO;
import spring.models.Student;
import spring.security.dao.UserDAO;
import spring.security.model.User;

@Controller
@RequestMapping("/")
public class DefaultController {

    private final UserDAO userDAO;
    private final AdminDAO adminDAO;

    @Autowired
    public DefaultController(UserDAO userDAO, AdminDAO adminDAO) {
        this.userDAO = userDAO;
        this.adminDAO = adminDAO;
    }

    @GetMapping()
    @PreAuthorize("hasAnyAuthority('users:read','users:write')")
    public String index(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        User user = userDAO.findByEmail(currentPrincipalName);
        if (userDAO.isStudent(user.getId())) {
            return "redirect:/students/" + user.getId();
        }
        else {
            model.addAttribute("admin",adminDAO.getAdminById(user.getId()));
            return "helloAdmin";
        }
    }

}