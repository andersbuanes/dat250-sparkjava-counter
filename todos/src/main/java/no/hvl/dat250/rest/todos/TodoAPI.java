package no.hvl.dat250.rest.todos;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static spark.Spark.*;

/**
 * Rest-Endpoint.
 */
public class TodoAPI {

    private static long currentId = 1L;
    private static List<Todo> todos = new ArrayList<>();
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        if (args.length > 0) {
            port(Integer.parseInt(args[0]));
        } else {
            port(8080);
        }

        after((req, res) -> res.type("application/json"));

        get("/todos", (req, res) -> {
            List<String> todosJson = new ArrayList<>();
            todos.stream().forEach(todo -> todosJson.add(gson.toJson(todo)));
            return todosJson;
        });

        get("/todos/:id", (req, res) -> {
            long id = -1;

            try {
                id = Long.parseLong(req.params(":id"));
            } catch (NumberFormatException e) {
                return "The id \"" + req.params(":id") + "\" is not a number!";
            }

            Todo todo = getTodoById(id);

            if (todo == null) {
                return "Todo with the id \"" + id + "\" not found!";
            }

            return gson.toJson(todo);
        });

        post("/todos", (req, res) -> {
            Todo todo = gson.fromJson(req.body(), Todo.class);
            Todo result = new Todo(currentId++, todo.getSummary(), todo.getDescription());
            todos.add(result);
            return gson.toJson(result);
        });

        put("/todos/:id", (req, res) -> {
            long id = -1;
            try {
                id = Long.parseLong(req.params(":id"));
            } catch (NumberFormatException e) {
                return "The id \"" + req.params(":id") + "\" is not a number!";
            }

            Todo todo = getTodoById(id);

            if (todo == null) {
                return "Todo with the id \"" + id + "\" not found!";
            }

            Todo newTodo = gson.fromJson(req.body(), Todo.class);

            todos = todos.stream()
                    .map(t -> Objects.equals(t.getId(), newTodo.getId()) ? newTodo : t)
                    .collect(Collectors.toList());

            return gson.toJson(newTodo);
        });

        delete("todos/:id", (req, res) -> {
            long id = -1;
            try {
                id = Long.parseLong(req.params(":id"));
            } catch (NumberFormatException e) {
                return "The id \"" + req.params(":id") + "\" is not a number!";
            }
            Todo todo = getTodoById(id);

            if (todo == null) {
                return "Todo with the id \"" + id + "\" not found!";
            }

            Todo todoCopy = todo;
            todos.remove(todo);
            return gson.toJson(todoCopy);
        });
    }

    private static Todo getTodoById(long id) {
        return todos.stream()
                .filter(todo -> todo.getId() == id)
                .findAny()
                .orElse(null);
    }
}
