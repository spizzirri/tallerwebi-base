function desaparecerBanners() {
    let banners = document.querySelectorAll('.banners-info');
    banners.forEach(banner => {
        setTimeout(() => {
            banner.style.display = 'none';
        }, 2500);
    });
}

document.addEventListener("DOMContentLoaded", function () {
    desaparecerBanners();
});