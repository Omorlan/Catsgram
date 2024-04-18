package ru.yandex.practicum.catsgram.service;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.DuplicatedDataException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.User;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {
    private final Map<Long, User> users = new HashMap<>();


    public User findUserById(Long id) {
        if (users.containsKey(id)) {
            return users.get(id);
        } else {
            return null;
        }
    }

    public Collection<User> findAll() {
        return users.values();
    }

    // вспомогательный метод для генерации идентификатора нового поста
    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    public User create(@RequestBody User user) {
        // проверяем выполнение необходимых условий
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            throw new ConditionsNotMetException("Имейл должен быть указан");
        }
        if (users.values().contains(user.getEmail())) {
            throw new DuplicatedDataException("Этот имейл уже используется");
        }
        // формируем дополнительные данные
        user.setId(getNextId());
        user.setRegistrationDate(Instant.now());
        // сохраняем нового пользователя в памяти приложения
        users.put(user.getId(), user);
        return user;
    }

    public User update(@RequestBody User newUser) {
        if (newUser.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (users.values().contains(newUser.getEmail())) {
            throw new DuplicatedDataException("Этот имейл уже используется");
        }
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            if (!(newUser.getUsername() == null ||
                    newUser.getPassword() == null ||
                    newUser.getEmail() == null)) {
                oldUser.setPassword(newUser.getPassword());
                oldUser.setEmail(newUser.getEmail());
                oldUser.setUsername(newUser.getUsername());
            }

            return oldUser;
        }
        throw new NotFoundException("Пост с id = " + newUser.getId() + " не найден");
    }
}
