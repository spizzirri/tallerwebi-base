# Guide: Creating a View with Thymeleaf + CSS + Vanilla JS

Step-by-step guide for creating a view without frameworks, using only HTML + CSS + JavaScript.

> The JavaScript rules still apply here: this guide is the no-framework path, not a
> different contract. See [guide-js.md](guide-js.md) for the API style, where server data
> enters, and DOM vs markup. Two snippets below predate it -- `var` and
> `innerHTML = await res.text()`. The contract's answer is `const`/`let` (R5) and a node
> built with `el()` (R7); what those snippets show is the no-framework shape, not the rule.

## Base Structure

```html
<!DOCTYPE HTML>
<html lang="en" xmlns:th="http://www.thymeleaf.org">

<head th:replace="~{layouts/base :: head('Title')}"></head>

<body th:replace="~{layouts/base :: layout(~{::main})}">

  <main class="flex-1 flex items-center justify-center p-4">
    <!-- Your content here -->
  </main>

  <script>
    // Your JavaScript here
  </script>

</body>
</html>
```

## Complete Example: Contact Form

### 1. Create the template

Create `src/main/webapp/WEB-INF/templates/pages/contact.html`:

```html
<!DOCTYPE HTML>
<html lang="en" xmlns:th="http://www.thymeleaf.org">

<head th:replace="~{layouts/base :: head('Contact')}"></head>

<body th:replace="~{layouts/base :: layout(~{::main})}">

  <main class="flex-1 flex items-center justify-center p-4">
    <div id="contact-form-area" class="w-full max-w-md rounded-lg bg-white p-8 shadow-md">

      <form id="contact-form" action="#"
        th:action="@{/contact}"
        method="POST"
        th:object="${contactData}">

        <h3 class="mb-2 text-2xl font-semibold text-gray-900">Contact</h3>
        <hr class="mb-6 border-gray-200">

        <!-- Name field -->
        <div class="mb-4">
          <label for="name" class="mb-1 block text-sm font-medium text-gray-700">Name</label>
          <input th:field="*{name}" id="name" type="text"
            class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none" />
          <p th:if="${#fields.hasErrors('name')}" th:errors="*{name}"
            class="mt-1 text-sm text-red-600"></p>
        </div>

        <!-- Email field -->
        <div class="mb-4">
          <label for="email" class="mb-1 block text-sm font-medium text-gray-700">Email</label>
          <input th:field="*{email}" id="email" type="email"
            class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none" />
          <p th:if="${#fields.hasErrors('email')}" th:errors="*{email}"
            class="mt-1 text-sm text-red-600"></p>
        </div>

        <!-- Message field -->
        <div class="mb-4">
          <label for="message" class="mb-1 block text-sm font-medium text-gray-700">Message</label>
          <textarea th:field="*{message}" id="message" rows="4"
            class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none"></textarea>
          <p th:if="${#fields.hasErrors('message')}" th:errors="*{message}"
            class="mt-1 text-sm text-red-600"></p>
        </div>

        <!-- Submit button -->
        <button id="btn-submit" type="submit"
          class="w-full rounded-md bg-blue-600 py-2 font-medium text-white transition hover:bg-blue-700">
          Send
        </button>

        <!-- General error -->
        <p th:if="${error}" class="mt-4 rounded-md bg-red-100 px-4 py-3 text-sm text-red-700"
          th:text="${error}"></p>
      </form>

    </div>
  </main>

  <script>
    document.getElementById('contact-form').addEventListener('submit', async function(e) {
      e.preventDefault();
      var form = e.target;
      var res = await fetch(form.action, {
        method: 'POST',
        body: new FormData(form)
      });
      if (res.redirected) {
        window.location.href = res.url;
      } else {
        document.getElementById('contact-form-area').innerHTML = await res.text();
      }
    });
  </script>

</body>
</html>
```

### 2. Create the controller

```java
@Controller
public class ContactController {

  @GetMapping("/contact")
  public String showForm(Model model) {
    model.addAttribute("contactData", new ContactData());
    return "pages/contact";
  }

  @PostMapping("/contact")
  public String submit(@Valid @ModelAttribute ContactData data, BindingResult result) {
    if (result.hasErrors()) {
      return "pages/contact";
    }
    return "redirect:/contact?success";
  }
}
```

### 3. Create the DTO

```java
public class ContactData {

  @NotBlank(message = "Name is required")
  private String name;

  @NotBlank(message = "Email is required")
  @Email(message = "Email is not valid")
  private String email;

  @NotBlank(message = "Message is required")
  private String message;

  // getters and setters
}
```

## Common Patterns

### Form without JS (traditional)

```html
<!-- Form does traditional submit (page reload) -->
<form th:action="@{/contact}" method="POST" th:object="${contactData}">
  <input th:field="*{name}" type="text" />
  <p th:if="${#fields.hasErrors('name')}" th:errors="*{name}"></p>
  <button type="submit">Send</button>
</form>
```

### Form with JS (fetch, no reload)

```javascript
document.getElementById('my-form').addEventListener('submit', async function(e) {
  e.preventDefault();
  var form = e.target;
  var res = await fetch(form.action, {
    method: 'POST',
    body: new FormData(form)
  });
  if (res.redirected) {
    window.location.href = res.url;
  } else {
    document.getElementById('form-area').innerHTML = await res.text();
  }
});
```

### Errors with Thymeleaf

```html
<!-- Field error -->
<p th:if="${#fields.hasErrors('email')}" th:errors="*{email}" class="text-sm text-red-600"></p>

<!-- General error -->
<p th:if="${error}" th:text="${error}" class="rounded-md bg-red-100 px-4 py-3 text-sm text-red-700"></p>
```

### Conditionals with Thymeleaf

```html
<!-- Show something only if user exists -->
<div th:if="${user != null}">
  <span th:text="${user.email}">email</span>
</div>

<!-- Show something only if NO user -->
<div th:if="${user == null}">
  <a th:href="@{/admin/login}">Sign in</a>
</div>
```

### Loop with Thymeleaf

```html
<ul>
  <li th:each="item : ${items}" class="py-2">
    <span th:text="${item.name}">Name</span>
  </li>
</ul>
```

## When to use Vanilla JS vs Vue

| Scenario | Vanilla JS | Vue |
| :--- | :--- | :--- |
| Simple form (1-2 fields) | Better | Overkill |
| Form with loading state | Manual | Reactive |
| Inline errors | textContent | v-if |
| Confirmation modal | Manual DOM | Reactive v-if |
| Reactive list | Painful | v-for |
| Timer / clock | setInterval | ref() |
| No frameworks allowed | Only option | Do not use |

## Rules

1. **Always `th:action` + `method="POST"`** , the form posts without JS (the layout's
   visibility shield still needs `<noscript>` before that is true end to end; see
   [guide-js.md](guide-js.md))
2. **`id` on the form and container** , for JS and Thymeleaf
3. **`th:field` for binding** , Thymeleaf handles values
4. **`th:if` for errors** , only shown if they exist
5. **`async/await` in fetch** , clean code
6. **`res.redirected`** , follow server redirects
7. **`textContent` on error** , replace only the text of the form area; `innerHTML` with a server value is an XSS (R7 in [guide-js.md](guide-js.md))
