package ru.yandex.practicum.catsgram.service;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.Post;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Указываем, что класс PostService - является бином и его
// нужно добавить в контекст приложения
@Service
public class PostService {
    private final Map<Long, Post> posts = new HashMap<>();
    UserService userService;

    public PostService(UserService userService) {
        this.userService = userService;
    }

    public Map<Long, Post> findAll(int size, String sort, int from) {
        Map<Long, Post> searchResult = new HashMap<>();
        List<Post> list = posts.values().stream().toList();
        if (sort.equals("asc")) {
            list.sort(Comparator.comparing(Post::getPostDate));
        } else if (sort.equals("dsc")) {
            list.sort(Comparator.comparing(Post::getPostDate).reversed());
        }
        while (size > 0) ;
        {
            Post post = list.get(++from);
            searchResult.put(post.getId(), post);
            size--;
        }
        return searchResult;
    }


    public Post create(@RequestBody Post post) {

        // проверяем выполнение необходимых условий
        if (post.getDescription() == null || post.getDescription().isBlank()) {
            throw new ConditionsNotMetException("Описание не может быть пустым");
        }
        // формируем дополнительные данные
        post.setId(getNextId());
        if (userService.findUserById(post.getAuthorId()) == null) {
            throw new ConditionsNotMetException("Автор с id = " + post.getAuthorId() + " не найден");
        }
        post.setPostDate(Instant.now());
        // сохраняем новую публикацию в памяти приложения
        posts.put(post.getId(), post);
        return post;
    }

    // вспомогательный метод для генерации идентификатора нового поста
    private long getNextId() {
        long currentMaxId = posts.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    public Post findPostById(Long id) {
        if (posts.containsKey(id)) {
            return posts.get(id);
        } else {
            return null;
        }
    }


    public Post update(@RequestBody Post newPost) {
        // проверяем необходимые условия
        if (newPost.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (posts.containsKey(newPost.getId())) {
            Post oldPost = posts.get(newPost.getId());
            if (newPost.getDescription() == null || newPost.getDescription().isBlank()) {
                throw new ConditionsNotMetException("Описание не может быть пустым");
            }
            // если публикация найдена и все условия соблюдены, обновляем её содержимое
            oldPost.setDescription(newPost.getDescription());
            return oldPost;
        }
        throw new NotFoundException("Пост с id = " + newPost.getId() + " не найден");
    }
}