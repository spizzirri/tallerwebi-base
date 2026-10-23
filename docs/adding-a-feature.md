# Adding a Feature

Step-by-step guide for adding a new feature to the project. This guide follows the layered architecture.

## Example: Adding a "Products" feature

Let's say we want to add product listing and creation.

## Step 1: Create the Domain Entity

Create `src/main/java/com/valhalla/domain/product/Product.java`:

```java
package com.valhalla.domain.product;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;
  private Double price;

  public Product() {}

  public Product(String name, Double price) {
    this.name = name;
    this.price = price;
  }

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public Double getPrice() { return price; }
  public void setPrice(Double price) { this.price = price; }
}
```

## Step 2: Create the Repository

Create `src/main/java/com/valhalla/infrastructure/product/ProductRepository.java`:

```java
package com.valhalla.infrastructure.product;

import com.valhalla.domain.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
```

## Step 3: Create the Service Interface

Create `src/main/java/com/valhalla/domain/product/ProductService.java`:

```java
package com.valhalla.domain.product;

import java.util.List;

public interface ProductService {
  List<Product> findAll();
  Product save(String name, Double price);
}
```

## Step 4: Create the Service Implementation

Create `src/main/java/com/valhalla/domain/product/ProductServiceImpl.java`:

```java
package com.valhalla.domain.product;

import com.valhalla.infrastructure.product.ProductRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;

  @Autowired
  public ProductServiceImpl(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @Override
  public List<Product> findAll() {
    return productRepository.findAll();
  }

  @Override
  public Product save(String name, Double price) {
    return productRepository.save(new Product(name, price));
  }
}
```

## Step 5: Create the DTO

Create `src/main/java/com/valhalla/presentation/product/ProductRequest.java`:

```java
package com.valhalla.presentation.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ProductRequest {

  @NotBlank(message = "Name is required")
  private String name;

  @NotNull(message = "Price is required")
  @Positive(message = "Price must be positive")
  private Double price;

  public ProductRequest() {}

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public Double getPrice() { return price; }
  public void setPrice(Double price) { this.price = price; }
}
```

## Step 6: Create the Controller

Create `src/main/java/com/valhalla/presentation/product/ProductController.java`:

```java
package com.valhalla.presentation.product;

import com.valhalla.domain.product.ProductService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ProductController {

  private static final String VIEW_PRODUCTS = "pages/products/list";
  private static final String VIEW_NEW_PRODUCT = "pages/products/new";

  private final ProductService productService;

  @Autowired
  public ProductController(ProductService productService) {
    this.productService = productService;
  }

  @GetMapping("/products")
  public ModelAndView list() {
    Map<String, Object> model = new ModelMap();
    model.put("products", productService.findAll());
    return new ModelAndView(VIEW_PRODUCTS, model);
  }

  @GetMapping("/products/new")
  public ModelAndView showNew() {
    Map<String, Object> model = new ModelMap();
    model.put("productData", new ProductRequest());
    return new ModelAndView(VIEW_NEW_PRODUCT, model);
  }

  @PostMapping("/products")
  public ModelAndView create(
    @Valid @ModelAttribute("productData") ProductRequest productData,
    BindingResult bindingResult
  ) {
    if (bindingResult.hasErrors()) {
      Map<String, Object> model = new ModelMap();
      model.put("productData", productData);
      model.put("error", "Invalid product data");
      return new ModelAndView(VIEW_NEW_PRODUCT, model);
    }
    productService.save(productData.getName(), productData.getPrice());
    return new ModelAndView("redirect:/products");
  }
}
```

## Step 7: Create the Template

Create `src/main/webapp/WEB-INF/templates/pages/products/list.html`:

```html
<!DOCTYPE HTML>
<html lang="en" xmlns:th="http://www.thymeleaf.org">

<head th:replace="~{layouts/base :: head('Products')}"></head>

<body th:replace="~{layouts/base :: layout(~{::main})}">

  <main class="flex-1 p-4">
    <div class="mx-auto max-w-4xl">
      <div class="mb-6 flex items-center justify-between">
        <h1 class="text-2xl font-semibold text-gray-900">Products</h1>
        <a th:href="@{/products/new}"
          class="rounded-md bg-blue-600 px-4 py-2 text-white hover:bg-blue-700">
          New Product
        </a>
      </div>

      <div class="rounded-lg bg-white shadow-md">
        <table class="w-full">
          <thead class="border-b bg-gray-50">
            <tr>
              <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">Name</th>
              <th class="px-4 py-3 text-left text-sm font-medium text-gray-700">Price</th>
            </tr>
          </thead>
          <tbody>
            <tr th:each="product : ${products}" class="border-b">
              <td class="px-4 py-3 text-sm text-gray-900" th:text="${product.name}">Name</td>
              <td class="px-4 py-3 text-sm text-gray-900" th:text="${product.price}">Price</td>
            </tr>
            <tr th:if="${#lists.isEmpty(products)}">
              <td colspan="2" class="px-4 py-8 text-center text-sm text-gray-500">No products</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </main>

</body>
</html>
```

Create `src/main/webapp/WEB-INF/templates/pages/products/new.html`:

```html
<!DOCTYPE HTML>
<html lang="en" xmlns:th="http://www.thymeleaf.org">

<head th:replace="~{layouts/base :: head('New Product')}"></head>

<body th:replace="~{layouts/base :: layout(~{::main})}">

  <main class="flex-1 flex items-center justify-center p-4">
    <div id="product-app" class="w-full max-w-md rounded-lg bg-white p-8 shadow-md"
      th:attr="data-error=${error}">

      <form ref="form" @submit.prevent="submit" action="#" th:action="@{/products}" method="POST">
        <h3 class="mb-2 text-2xl font-semibold text-gray-900">New Product</h3>
        <hr class="mb-6 border-gray-200">

        <div class="mb-4">
          <label for="name" class="mb-1 block text-sm font-medium text-gray-700">Name</label>
          <input v-model="name" id="name" name="name" type="text"
            class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none" />
        </div>

        <div class="mb-4">
          <label for="price" class="mb-1 block text-sm font-medium text-gray-700">Price</label>
          <input v-model="price" id="price" name="price" type="number" step="0.01"
            class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none" />
        </div>

        <button type="submit" :disabled="loading"
          class="w-full rounded-md bg-blue-600 py-2 font-medium text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50">
          <span v-if="loading">Creating...</span>
          <span v-else>Create</span>
        </button>

        <p v-if="error" class="mt-4 rounded-md bg-red-100 px-4 py-3 text-sm text-red-700">{{ error }}</p>
      </form>

    </div>
  </main>

  <script>
    const { createApp, ref } = Vue;

    createApp({
      setup() {
        const data = document.getElementById('product-app').dataset;
        const name = ref('');
        const price = ref('');
        const error = ref(data.error || '');
        const loading = ref(false);
        const form = ref(null);
        const submit = () => {
          if (loading.value) return;
          loading.value = true;
          form.value.submit();
        };
        return { name, price, error, loading, form, submit };
      }
    }).mount('#product-app');
  </script>

</body>
</html>
```

## Step 8: Run and Test

```shell
mvn clean jetty:run
```

Navigate to [http://localhost:8080/products](http://localhost:8080/products).

## Summary

| Step | Layer | Package | File |
| :--- | :--- | :--- | :--- |
| 1 | domain | `com.valhalla.domain.product` | `Product.java` |
| 2 | infrastructure | `com.valhalla.infrastructure.product` | `ProductRepository.java` |
| 3 | domain | `com.valhalla.domain.product` | `ProductService.java` (interface) |
| 4 | domain | `com.valhalla.domain.product` | `ProductServiceImpl.java` |
| 5 | presentation | `com.valhalla.presentation.product` | `ProductRequest.java` |
| 6 | presentation | `com.valhalla.presentation.product` | `ProductController.java` |
| 7 | templates | | `pages/products/list.html`, `pages/products/new.html` |
