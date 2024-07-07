  const menuBtn = document.getElementById('menu-btn');
    const closeMenuBtn = document.getElementById('close-menu');
    const sideMenu = document.getElementById('side-menu');

    menuBtn.addEventListener('click', () => {
        sideMenu.classList.toggle('-translate-x-full');
    });

    closeMenuBtn.addEventListener('click', () => {
        sideMenu.classList.add('-translate-x-full');
    });