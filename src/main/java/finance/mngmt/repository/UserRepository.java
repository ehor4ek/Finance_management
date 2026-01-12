package finance.mngmt.repository;

import finance.mngmt.model.User;
import java.util.*;

public class UserRepository {
    private Map<String, User> users;

    public UserRepository() {
        this.users = new HashMap<>();
    }

    public void addUser(User user) {
        users.put(user.getUsername(), user);
    }

    public User getUser(String username) {
        return users.get(username);
    }

    public boolean userExists(String username) {
        return users.containsKey(username);
    }

    public boolean authenticate(String username, String password) {
        User user = users.get(username);
        return user != null && user.checkPassword(password);
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public void removeUser(String username) {
        users.remove(username);
    }

    public int getUserCount() {
        return users.size();
    }
}
