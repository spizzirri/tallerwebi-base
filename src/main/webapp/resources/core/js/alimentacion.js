document.addEventListener('DOMContentLoaded', () => {
    const mealList = document.getElementById('meal');
    const mealDetailsContent = document.querySelector('.meal-details-content');
    const recipeCloseBtn = document.getElementById('recipe-close-btn');

    const mealIds = ['52885', '53049', '52893']; // Lista de IDs de los platos que quieres mostrar

    mealList.addEventListener('click', getMealRecipe);
    recipeCloseBtn.addEventListener('click', () => {
        mealDetailsContent.parentElement.classList.remove('showRecipe');
    });

    // Obtener los detalles de los platos específicos
    function loadMealsByIds(ids) {
        let html = '';
        ids.forEach(id => {
            fetch(`https://www.themealdb.com/api/json/v1/1/lookup.php?i=${id}`)
                .then(response => response.json())
                .then(data => {
                    const meal = data.meals[0];
                    html += `
                        <div class="meal-item" data-id="${meal.idMeal}">
                            <div class="meal-img">
                                <img src="${meal.strMealThumb}" alt="food">
                            </div>
                            <div class="meal-name">
                                <h3>${meal.strMeal}</h3>
                                <a href="#" class="recipe-btn">Get Recipe</a>
                            </div>
                        </div>
                    `;
                    mealList.innerHTML = html;
                });
        });
    }

    // Obtener receta de la comida
    function getMealRecipe(e) {
        e.preventDefault();
        if (e.target.classList.contains('recipe-btn')) {
            let mealItem = e.target.parentElement.parentElement;
            fetch(`https://www.themealdb.com/api/json/v1/1/lookup.php?i=${mealItem.dataset.id}`)
                .then(response => response.json())
                .then(data => mealRecipeModal(data.meals));
        }
    }

    // Crear un modal
    function mealRecipeModal(meal) {
        console.log(meal);
        meal = meal[0];
        let html = `
            <h2 class="recipe-title">${meal.strMeal}</h2>
            <p class="recipe-category">${meal.strCategory}</p>
            <div class="recipe-instruct">
                <h3>Instructions:</h3>
                <p>${meal.strInstructions}</p>
            </div>
            <div class="recipe-meal-img">
                <img src="${meal.strMealThumb}" alt="">
            </div>
            <div class="recipe-link">
                <a href="${meal.strYoutube}" target="_blank">Watch Video</a>
            </div>
        `;
        mealDetailsContent.innerHTML = html;
        mealDetailsContent.parentElement.classList.add('showRecipe');
    }

    loadMealsByIds(mealIds);
});