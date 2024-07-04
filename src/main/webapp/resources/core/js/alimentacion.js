document.addEventListener('DOMContentLoaded', () => {
    const mealList = document.getElementById('meal');
    const mealDetailsContent = document.querySelector('.meal-details-content');
    const recipeCloseBtn = document.getElementById('recipe-close-btn');

    // Objetivos y sus respectivos IDs de platos
    const mealsByObjective = {
        gananciamuscular: ['52885', '52768', '52855', '52848', '52997', '53080', '52965', '52913', '52961', '53060', '52920', '53063', '52966', '52987', '53034', '53021', '52877', '53006', '53022', '53052', '52854', '52902', '52804', '52908', '52869', '52772', '52929', '52822', '53032', '52957'],
        perdidadepeso: ['52807', '52959', '53078', '53069', '52914', '52842', '52939', '52850', '52998', '53072', '52999', '52907', '52955', '52919', '53033', '53012', '52973', '53047', '53029', '53079', '52903', '53041', '52915', '52815', '52930', '52960', '53044', '52971', '53004', '53081', '52925', '53040', '52911', '53065', '52949', '53026', '52852', '52867', '52871'],
        definicion: ['53049', '52893', '52956', '52895', '53064', '53079', '53061', '52994', '52982', '52872', '52769', '53082', '53035', '52764', '53068', '52956', '52851', '52955', '53062', '52913', '53037', '52843', '52963', '53076', '52939', '52915', '53080', '53044', '53012', '52815', '53022', '52997', '53079', '52821', '53030', '53029', '52831', '52896', '52903', '52930', '53059', '53079', '52915', '52867', '52911', '53052']
    };

    // Función para normalizar el objetivo
    function normalizeObjective(objetivo) {
        return objetivo.toLowerCase().replace(/_/g, '');
    }

    const userObjective = normalizeObjective(document.getElementById('objetivoFitness').value);
    console.log("User Objective (normalized):", userObjective);  // Verificar el valor del objetivo normalizado
    console.log("Available Objectives:", Object.keys(mealsByObjective));  // Verificar las claves disponibles en mealsByObjective

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
                })
                .catch(error => console.error("Error fetching meal details:", error));
        });
    }

    // Obtener receta de la comida
    function getMealRecipe(e) {
        e.preventDefault();
        if (e.target.classList.contains('recipe-btn')) {
            let mealItem = e.target.parentElement.parentElement;
            fetch(`https://www.themealdb.com/api/json/v1/1/lookup.php?i=${mealItem.dataset.id}`)
                .then(response => response.json())
                .then(data => mealRecipeModal(data.meals))
                .catch(error => console.error("Error fetching recipe:", error));
        }
    }

    // Crear un modal
    function mealRecipeModal(meal) {
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

    // Mostrar los platos según el objetivo del usuario
    if (mealsByObjective[userObjective]) {
        console.log("Loading meals for objective:", userObjective);
        loadMealsByIds(mealsByObjective[userObjective]);
    } else {
        console.error("Objective not found:", userObjective);
        mealList.innerHTML = "No se encontraron platos para el objetivo seleccionado.";
    }
});