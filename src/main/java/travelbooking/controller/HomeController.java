package travelbooking.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    /**
     * Serves the dedicated login/registration page.
     */
    @GetMapping("/login")
    public String loginPage() {
        return "forward:/login.html";
    }

    /**
     * Forwards / and /index to the static homepage.
     * Client-side auth guard in app.js handles the redirect to /login if not authenticated.
     */
    @GetMapping({"/", "/index"})
    public String index() {
        return "forward:/index.html";
    }
}
