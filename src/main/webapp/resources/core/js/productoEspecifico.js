const mainImage = document.getElementById('mainImage');
const thumbnails = document.querySelectorAll('.thumb-img');

thumbnails.forEach(thumb => {
    thumb.addEventListener('click', () => {
        mainImage.src = thumb.src;
    });
});