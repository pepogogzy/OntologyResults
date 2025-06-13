package mk.ukim.finki.wp.service;


import mk.ukim.finki.wp.model.User;

public interface AuthService {

    User login(String username, String password);

}

