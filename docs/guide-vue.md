# Guide: Creating a View with Thymeleaf + Tailwind + Vue

Step-by-step guide for creating an interactive view using the full stack.

> How that page's JavaScript is written — API style, where server data enters, DOM vs
> markup, errors — is the [JavaScript contract](guide-js.md). Read it alongside this guide.

## Base Structure

Every view follows this structure:

```html
<!DOCTYPE HTML>
<html lang="en" xmlns:th="http://www.thymeleaf.org">

<head th:replace="~{layouts/base :: head('Title')}"></head>

<body th:replace="~{layouts/base :: layout(~{::main})}">

  <main class="flex-1 flex flex-col items-center justify-center gap-4 p-4">
    <!-- Your content here -->
  </main>

  <script>
    const { createApp, ref } = Vue;

    createApp({
      setup() {
        const message = ref('');
        return { message };
      }
    }).mount('#my-app');
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
    <div id="contact-app" class="w-full max-w-md rounded-lg bg-white p-8 shadow-md"
         th:attr="data-error=${error},
                  data-name=${contactData.name},
                  data-email=${contactData.email},
                  data-message=${contactData.message}">

      <form ref="form" @submit.prevent="submit" action="#" th:action="@{/contact}" method="POST">
        <h3 class="mb-2 text-2xl font-semibold text-gray-900">Contact</h3>
        <hr class="mb-6 border-gray-200">

        <!-- Name field -->
        <div class="mb-4">
          <label for="name" class="mb-1 block text-sm font-medium text-gray-700">Name</label>
          <input v-model="name" id="name" name="name" type="text"
            class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none" />
        </div>

        <!-- Email field -->
        <div class="mb-4">
          <label for="email" class="mb-1 block text-sm font-medium text-gray-700">Email</label>
          <input v-model="email" id="email" name="email" type="email"
            class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none" />
        </div>

        <!-- Message field -->
        <div class="mb-4">
          <label for="message" class="mb-1 block text-sm font-medium text-gray-700">Message</label>
          <textarea v-model="message" id="message" name="message" rows="4"
            class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none"></textarea>
        </div>

        <!-- Submit button -->
        <button type="submit" :disabled="loading"
          class="w-full rounded-md bg-blue-600 py-2 font-medium text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50">
          <span v-if="loading">Sending...</span>
          <span v-else>Send</span>
        </button>

        <!-- General error, seeded from data-error on the root element -->
        <p v-if="error" class="mt-4 rounded-md bg-red-100 px-4 py-3 text-sm text-red-700">{{ error }}</p>
      </form>

    </div>
  </main>

  <script>
    const { ref } = Vue;
    const root = document.getElementById('contact-app');

    Vue.createApp({
      setup() {
        const data = root.dataset;
        const name = ref(data.name || '');
        const email = ref(data.email || '');
        const message = ref(data.message || '');
        const error = ref(data.error || '');
        const loading = ref(false);
        const submit = () => {
          if (loading.value) return;
          loading.value = true;
          root.querySelector('form').submit();
        };
        return { name, email, message, error, loading, submit };
      }
    }).mount('#contact-app');
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
    // Process the form
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

### Loading state

```html
<button type="submit" :disabled="loading">
  <span v-if="loading">Saving...</span>
  <span v-else>Save</span>
</button>
```

### Server errors and values to Vue

Server data reaches the app through `data-*` on its root, never as an inline expression
inside `<script>`: `[[${...}]]` there is HTML-escaped text, so `&` arrives as `&amp;` and a
newline ends the string literal. See [guide-js.md](guide-js.md) R3.

```html
<div id="contact-app" th:attr="data-error=${error},data-name=${contactData.name}"></div>
<script>
  const data = document.getElementById('contact-app').dataset;

  Vue.createApp({
    setup() {
      const error = ref(data.error || '');
      const name = ref(data.name || '');
      return { error, name };
    }
  }).mount('#contact-app');
</script>
```

### Native submit with loading

```javascript
const submit = () => {
  if (loading.value) return;   // guards Enter as well as a second click
  loading.value = true;
  form.submit();               // Native browser POST
};
```

### Confirmation before action

```html
<button @click="confirmDelete" class="text-red-600 hover:underline">Delete</button>

<!-- Confirmation modal -->
<div v-if="showConfirm" class="fixed inset-0 flex items-center justify-center bg-black/50">
  <div class="rounded-lg bg-white p-6 shadow-md">
    <p>Are you sure?</p>
    <div class="mt-4 flex gap-2">
      <button @click="showConfirm = false" class="rounded bg-gray-200 px-4 py-2">Cancel</button>
      <button @click="doDelete" class="rounded bg-red-600 px-4 py-2 text-white">Delete</button>
    </div>
  </div>
</div>
```

### Reactive lists

```html
<ul>
  <li v-for="item in items" :key="item.id" class="py-2">
    {{ item.name }}
    <button @click="removeItem(item.id)" class="ml-2 text-red-600">X</button>
  </li>
</ul>
```

## Rules

1. **Each page mounts its own Vue app** , do not share between pages; the root is
   `#<view>-app` (`#contact-app`, `#explore-app`), never a generic `#app`
2. **Thymeleaf renders initial data** , Vue takes over after
3. **Always `th:action` + `method="POST"`** , the form still posts when the script fails to
   load (see the visibility-shield caveat in [guide-js.md](guide-js.md))
4. **`v-model` on all inputs** , reactive binding
5. **Native submit with an in-flight guard** , `form.submit()` behind a `loading` flag that
   the handler checks, so Enter and a second click cannot post twice
6. **Server data enters through `data-*` on the root** , read it with `dataset`, never as
   `[[${variable}]]` inside `<script>`
7. **`:disabled="loading"`** , user feedback
8. **`v-if="error"`** , inline error display; never `alert()`

How that JavaScript is written — Composition API, request helper, DOM over markup — is the
[JavaScript contract](guide-js.md). This guide covers the view structure only.
