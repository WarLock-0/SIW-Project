package it.uniroma3.siw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.repository.ItemRepository;
import it.uniroma3.siw.repository.OrderItemRepository;
import it.uniroma3.siw.repository.SaleRepository;
import it.uniroma3.siw.repository.UserRepository;
import it.uniroma3.siw.service.CredentialsService;
import it.uniroma3.siw.service.UserService;
import it.uniroma3.siw.validator.CredentialsValidator;
import it.uniroma3.siw.validator.UserValidator;
import jakarta.validation.Valid;

@Controller
public class AuthenticationController {

    @Autowired
    private CredentialsService credentialsService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private SaleRepository saleRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private UserValidator userValidator;
    @Autowired
    private CredentialsValidator credentialsValidator;
    @Autowired
    private UserService userService;

    @GetMapping("/error")
    public String errorPage() {
        return "notFound.html";
    }

    @GetMapping("/login")
    public String toLoginPage() {
        return "login.html";
    }

    @GetMapping("/admin/formNewUser")
    public String formNewstaff(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("credentials", new Credentials());
        return "admin/formNewUser.html";
    }

    @PostMapping(value = { "/admin/formNewUser" })
    public String registerUser(@Valid @ModelAttribute("user") User user,
            BindingResult userBindingResult, @Valid @ModelAttribute("credentials") Credentials credentials,
            BindingResult credentialsBindingResult, Model model) {
        this.userValidator.validate(user, userBindingResult);
        // this.credentialsValidator.validate(user.getCredentials(),credentialsBindingResult);
        // se user e credential hanno entrambi contenuti validi, memorizza User e le
        // Credentials nel DB
        if (!userBindingResult.hasErrors() && !credentialsBindingResult.hasErrors()) {
            credentialsService.saveCredentials(user.getCredentials());

            user.getCredentials().setUser(user);
            userService.saveUser(user);
            model.addAttribute("user", user);

            return "admin/user.html";
        }
        return "admin/fromNewUser.html";
    }

    @GetMapping(value = "/index")
    public String index(Model model) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
        if (credentials.getRole().equals(Credentials.DEFAULT_ROLE)) {
            return "staff/waiterMenu.html";
        } else {
            if (credentials.getRole().equals(Credentials.ADMIN_ROLE)) {
                // carica i dati necessari alla managerPage
                AdminController.loadManagerPageAttributes(model, orderItemRepository, itemRepository, saleRepository);
                return "admin/adminMenu.html";
            }
        }
        return "index.html";
    }

    @GetMapping(value = "/success")
    public String defaultAfterLogin(Model model) {

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Credentials credentials = credentialsService.getCredentials(userDetails.getUsername());
        if (credentials.getRole().equals(Credentials.ADMIN_ROLE)) {
            // carica i dati necessari alla managerPage
            AdminController.loadManagerPageAttributes(model, orderItemRepository, itemRepository, saleRepository);
            return "admin/adminMenu.html";
        } else if (credentials.getRole().equals(Credentials.DEFAULT_ROLE)) {
            return "staff/waiterMenu.html";
        }
        return "login.html";
    }

}
