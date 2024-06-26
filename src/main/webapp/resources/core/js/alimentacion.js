const searchBtn = document.getElementById('search-btn');
const mealList = document.getElementById('meal');
const mealDetailsContent = document.querySelector('.meal-details-content');
const recipeCloseBtn = document.getElementById('recipe-close-btn');

// event listeners
searchBtn.addEventListener('click', getMealList);
mealList.addEventListener('click', getMealRecipe);
recipeCloseBtn.addEventListener('click', () => {
    mealDetailsContent.parentElement.classList.remove('showRecipe');
});

// get meal list that matches with the ingredients
async function getMealList() {
    let searchInputTxt = document.getElementById('search-input').value.trim();
    try {
        console.log('Palabra clave original:', searchInputTxt);

        // Traducir la palabra clave de búsqueda al inglés
        const translatedSearchInput = await translateText(searchInputTxt, 'es', 'en');
        console.log('Palabra clave traducida:', translatedSearchInput);

        const response = await fetch(`https://www.themealdb.com/api/json/v1/1/filter.php?i=${translatedSearchInput}`);
        const data = await response.json();
        console.log('Respuesta de la API:', data);

        let html = "";
        if (data.meals) {
            for (const meal of data.meals) {
                const translatedMealName = await translateText(meal.strMeal, 'en', 'es');
                html += `
                    <div class = "meal-item" data-id = "${meal.idMeal}">
                        <div class = "meal-img">
                            <img src = "${meal.strMealThumb}" alt = "food">
                        </div>
                        <div class = "meal-name">
                            <h3>${translatedMealName}</h3>
                            <a href = "#" class = "recipe-btn">Obtener receta</a>
                        </div>
                    </div>
                `;
            }
            mealList.classList.remove('notFound');
        } else {
            html = "Lo siento, no encontramos ninguna comida!";
            mealList.classList.add('notFound');
        }

        mealList.innerHTML = html;
    } catch (error) {
        console.error('Error al obtener la lista de comidas:', error);
    }
}

// get recipe of the meal
async function getMealRecipe(e) {
    e.preventDefault();
    if (e.target.classList.contains('recipe-btn')) {
        let mealItem = e.target.parentElement.parentElement;
        try {
            const response = await fetch(`https://www.themealdb.com/api/json/v1/1/lookup.php?i=${mealItem.dataset.id}`);
            const data = await response.json();
            mealRecipeModal(data.meals);
        } catch (error) {
            console.error('Error al obtener la receta:', error);
        }
    }
}

// create a modal
async function mealRecipeModal(meal) {
    meal = meal[0];
    const translatedMealName = await translateText(meal.strMeal, 'en', 'es');
    const translatedCategory = await translateText(meal.strCategory, 'en', 'es');
    const translatedInstructions = await translateText(meal.strInstructions, 'en', 'es');
    let html = `
        <h2 class = "recipe-title">${translatedMealName}</h2>
        <p class = "recipe-category">${translatedCategory}</p>
        <div class = "recipe-instruct">
            <h3>Instrucciones:</h3>
            <p>${translatedInstructions}</p>
        </div>
        <div class = "recipe-meal-img">
            <img src = "${meal.strMealThumb}" alt = "">
        </div>
        <div class = "recipe-link">
            <a href = "${meal.strYoutube}" target = "_blank">Ver Video</a>
        </div>
    `;
    mealDetailsContent.innerHTML = html;
    mealDetailsContent.parentElement.classList.add('showRecipe');
}

// translate text using MyMemory API
const translateText = async (text, sourceLang, targetLang) => {
    const url = `https://api.mymemory.translated.net/get?q=${encodeURIComponent(text)}&langpair=${sourceLang}|${targetLang}`;
    try {
        const response = await fetch(url);
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Error en la respuesta: ${errorText}`);
        }
        const data = await response.json();
        console.log(`Texto traducido (${sourceLang} a ${targetLang}):`, data.responseData.translatedText);
        return data.responseData.translatedText;
    } catch (error) {
        console.error('Error en la traducción:', error);
        return text;
    }
};
