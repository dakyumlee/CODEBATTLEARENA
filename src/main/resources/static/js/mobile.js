function toggleMobileLogin() {
    const authContainer = document.querySelector('.auth-container');
    authContainer.classList.toggle('mobile-show');
}

document.addEventListener('DOMContentLoaded', function() {
    if (window.innerWidth <= 768) {
        const authContainer = document.querySelector('.auth-container');
        if (authContainer && !document.querySelector('.mobile-login-toggle')) {
            const toggleButton = document.createElement('button');
            toggleButton.className = 'mobile-login-toggle';
            toggleButton.textContent = '로그인';
            toggleButton.onclick = toggleMobileLogin;
            document.body.appendChild(toggleButton);
        }
    }
});

window.addEventListener('resize', function() {
    const toggleButton = document.querySelector('.mobile-login-toggle');
    if (window.innerWidth <= 768) {
        if (!toggleButton) {
            const button = document.createElement('button');
            button.className = 'mobile-login-toggle';
            button.textContent = '로그인';
            button.onclick = toggleMobileLogin;
            document.body.appendChild(button);
        }
    } else {
        if (toggleButton) {
            toggleButton.remove();
        }
        const authContainer = document.querySelector('.auth-container');
        if (authContainer) {
            authContainer.classList.remove('mobile-show');
        }
    }
});
