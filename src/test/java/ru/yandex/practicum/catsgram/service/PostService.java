    package ru.yandex.practicum.catsgram.service;

    import org.springframework.stereotype.Service;
    import org.springframework.web.bind.annotation.RequestBody;
    import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
    import ru.yandex.practicum.catsgram.exception.NotFoundException;
    import ru.yandex.practicum.catsgram.model.Post;

    import java.time.Instant;
    import java.util.Collection;
    import java.util.HashMap;
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

        public Collection<Post> findAll() {
            return posts.values();
        }


        public Post create(@RequestBody Post post) {

            // проверяем выполнение необходимых условий
            if (post.getDescription() == null || post.getDescription().isBlank()) {
                throw new ConditionsNotMetException("Описание не может быть пустым");
            }
            // формируем дополнительные данные
            post.setId(getNextId());
            userService.findUserById(post.getAuthorId()).orElseThrow(() ->
                    new ConditionsNotMetException("Автор с id = " + post.getAuthorId() + " не найден"));

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