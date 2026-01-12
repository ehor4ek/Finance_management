package finance.mngmt.service;

import finance.mngmt.model.User;
import finance.mngmt.repository.UserRepository;
import finance.mngmt.exception.AuthorizationException;
import finance.mngmt.exception.ValidationException;

public class UserService {
    private final UserRepository userRepository;
    private User currentUser;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void register(String username, String password, String confirmPassword) {
        validateRegistration(username, password, confirmPassword);

        if (userRepository.userExists(username)) {
            throw new AuthorizationException("Пользователь с таким именем уже существует");
        }

        User user = new User(username, password);
        userRepository.addUser(user);
        System.out.println("Пользователь " + username + " успешно зарегистрирован");
    }

    public void login(String username, String password) {
        if (!userRepository.authenticate(username, password)) {
            throw new AuthorizationException("Неверное имя пользователя или пароль");
        }

        currentUser = userRepository.getUser(username);
        System.out.println("Добро пожаловать, " + username + "!");
    }

    public void logout() {
        if (currentUser != null) {
            System.out.println("До свидания, " + currentUser.getUsername() + "!");
            currentUser = null;
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }

    private void validateRegistration(String username, String password, String confirmPassword) {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Имя пользователя не может быть пустым");
        }

        if (username.length() < 3) {
            throw new ValidationException("Имя пользователя должно содержать минимум 3 символа");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("Пароль не может быть пустым");
        }

        if (password.length() < 4) {
            throw new ValidationException("Пароль должен содержать минимум 4 символа");
        }

        if (!password.equals(confirmPassword)) {
            throw new ValidationException("Пароли не совпадают");
        }
    }

    public void changePassword(String oldPassword, String newPassword, String confirmPassword) {
        if (!currentUser.checkPassword(oldPassword)) {
            throw new AuthorizationException("Неверный текущий пароль");
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new ValidationException("Новый пароль не может быть пустым");
        }

        if (newPassword.length() < 4) {
            throw new ValidationException("Новый пароль должен содержать минимум 4 символа");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new ValidationException("Пароли не совпадают");
        }

        currentUser.setPassword(newPassword);
        System.out.println("Пароль успешно изменен");
    }
}
