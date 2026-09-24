/**
 * CANOPOLIS - Clínica & Hospital Veterinario
 * Integración de Frontend Institucional, Portal Clínico y Base de Datos MySQL (XAMPP)
 */

// Estado Global
const state = {
    currentUser: null,
    token: null,
    isBackendConnected: false,
    modalMode: 'login' // 'login' | 'register'
};

// =======================================================
// INICIALIZACIÓN
// =======================================================
document.addEventListener('DOMContentLoaded', () => {
    highlightCurrentPageNav();
    setupNavbarScroll();
    setupDateInputConstraint();
    setupStatsObserver();
    setupElementAnimations();
    setupTestimoniosDots();
    setupQuickAddUser();
    checkPersistedSession();
    checkBackendConnection();
    cargarServiciosEnSelect();
    checkAdminRedirect();
});

// =======================================================
// NAVBAR & NAVEGACIÓN
// =======================================================
const navbar = document.getElementById('navbar');
const backToTopBtn = document.getElementById('backToTop');

function setupNavbarScroll() {
    window.addEventListener('scroll', () => {
        if (window.scrollY > 40) {
            if (navbar) navbar.classList.add('scrolled');
        } else {
            if (navbar) navbar.classList.remove('scrolled');
        }
        updateActiveLink();
        toggleBackToTop();
    });
}

function toggleMenu() {
    const menu = document.getElementById('navMenu');
    const hamburger = document.getElementById('hamburgerBtn');
    
    if (menu) {
        menu.classList.toggle('open');
    }
    if (hamburger) {
        hamburger.classList.toggle('active');
    }
}

// Cerrar menú al hacer clic en un enlace
document.addEventListener('click', (e) => {
    if (e.target.matches('.nav-link')) {
        const menu = document.getElementById('navMenu');
        const hamburger = document.getElementById('hamburgerBtn');
        if (menu) menu.classList.remove('open');
        if (hamburger) hamburger.classList.remove('active');
    }
});

// Cerrar menú al hacer clic fuera del navbar
document.addEventListener('click', (e) => {
    const menu = document.getElementById('navMenu');
    const hamburger = document.getElementById('hamburgerBtn');
    const navbar = document.querySelector('.navbar');
    
    if (menu && hamburger && navbar && !navbar.contains(e.target)) {
        menu.classList.remove('open');
        hamburger.classList.remove('active');
    }
});

function highlightCurrentPageNav() {
    const fullPath = window.location.pathname;
    const pageName = fullPath.substring(fullPath.lastIndexOf('/') + 1) || 'index.html';
    
    document.querySelectorAll('.nav-link').forEach(link => {
        const href = link.getAttribute('href');
        if (!href) return;
        
        if (href === pageName || (pageName === 'index.html' && (href === 'index.html' || href === './'))) {
            link.classList.add('active');
        } else if (!href.startsWith('#')) {
            link.classList.remove('active');
        }
    });
}

function updateActiveLink() {
    const fullPath = window.location.pathname;
    const pageName = fullPath.substring(fullPath.lastIndexOf('/') + 1) || 'index.html';
    if (pageName !== 'index.html' && pageName !== '') {
        return; // Mantener resaltado de la página correspondiente
    }

    const sections = document.querySelectorAll('section[id]');
    const scrollY = window.scrollY + 120;
    sections.forEach(section => {
        const sectionTop = section.offsetTop;
        const sectionHeight = section.offsetHeight;
        const id = section.getAttribute('id');
        const link = document.querySelector(`.nav-link[href="#${id}"]`);
        if (link) {
            if (scrollY >= sectionTop && scrollY < sectionTop + sectionHeight) {
                document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
                link.classList.add('active');
            }
        }
    });
}

function scrollToSection(id) {
    const el = document.getElementById(id);
    if (el) {
        el.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
}

function toggleBackToTop() {
    if (!backToTopBtn) return;
    if (window.scrollY > 400) {
        backToTopBtn.classList.add('show');
    } else {
        backToTopBtn.classList.remove('show');
    }
}

document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', e => {
        const target = document.querySelector(anchor.getAttribute('href'));
        if (target) {
            e.preventDefault();
            target.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
    });
});

// =======================================================
// MODAL CONTROLS & AUTHENTICATION FLOW
// =======================================================
function openModal(id = 'loginModal') {
    if (!state.currentUser) {
        checkPersistedSession();
    }

    // Si intenta agendar cita sin estar registrado o haber iniciado sesión
    if (id === 'appointmentModal' && !state.currentUser) {
        showToast('⚠️ Regístrate primero', 'Debes crear una cuenta o iniciar sesión para agendar una cita.');
        switchModalTab('register');
        openModal('loginModal');
        return;
    }

    const modal = document.getElementById(id) || document.getElementById('loginModal');
    if (modal) {
        modal.classList.add('open');
        document.body.style.overflow = 'hidden';
        clearAuthFeedback();
        
        if ((id === 'loginModal' || !id) && !state.currentUser) {
            switchModalTab('register');
        }
        
        setTimeout(() => {
            const focusInput = state.modalMode === 'register' ? document.getElementById('regName') : document.getElementById('loginEmail');
            if (focusInput) focusInput.focus();
        }, 150);
    }
}

function closeModal(id = 'loginModal') {
    const modal = document.getElementById(id) || document.getElementById('loginModal');
    if (modal) {
        modal.classList.remove('open');
        document.body.style.overflow = '';
    }
}

function closeModalOutside(event, id) {
    if (event.target === event.currentTarget) {
        closeModal(id);
    }
}

document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
        document.querySelectorAll('.modal-overlay.open').forEach(modal => {
            closeModal(modal.id);
        });
    }
});

function switchModalTab(mode) {
    state.modalMode = mode;
    const loginTab = document.getElementById('modalTabLogin');
    const regTab = document.getElementById('modalTabRegister');
    const loginForm = document.getElementById('loginForm');
    const regForm = document.getElementById('registerForm');
    const modalTitle = document.getElementById('modalTitle');
    const modalSub = document.getElementById('modalSubtitle');
    const modalFooterPrompt = document.getElementById('modalFooterPrompt');
    
    clearAuthFeedback();
    
    if (mode === 'login') {
        if (loginTab) loginTab.classList.add('active');
        if (regTab) regTab.classList.remove('active');
        if (loginForm) loginForm.style.display = 'block';
        if (regForm) regForm.style.display = 'none';
        if (modalTitle) modalTitle.textContent = 'Iniciar Sesión';
        if (modalSub) modalSub.textContent = 'Accede al portal médico, historial clínico y gestión MySQL';
        if (modalFooterPrompt) {
            modalFooterPrompt.innerHTML = '¿No tienes cuenta? <a href="javascript:void(0)" onclick="switchModalTab(\'register\')">Regístrate gratis</a>';
        }
    } else {
        if (regTab) regTab.classList.add('active');
        if (loginTab) loginTab.classList.remove('active');
        if (loginForm) loginForm.style.display = 'none';
        if (regForm) regForm.style.display = 'block';
        if (modalTitle) modalTitle.textContent = 'Crear Cuenta';
        if (modalSub) modalSub.textContent = 'Regístrate como propietario o personal médico en MySQL';
        if (modalFooterPrompt) {
            modalFooterPrompt.innerHTML = '¿Ya tienes cuenta? <a href="javascript:void(0)" onclick="switchModalTab(\'login\')">Inicia sesión</a>';
        }
    }
}

function togglePasswordVisibility(inputId) {
    const input = document.getElementById(inputId);
    if (!input) return;
    input.type = input.type === 'password' ? 'text' : 'password';
}

function fillDemoCredentials() {
    const emailInput = document.getElementById('loginEmail');
    const pwdInput = document.getElementById('loginPassword');
    if (emailInput) emailInput.value = 'admin@canopolis.com';
    if (pwdInput) pwdInput.value = 'admin123';
    showAuthFeedback('Credenciales demo asignadas (Dr. Admin)', 'success');
}

function showAuthFeedback(msg, type = 'error') {
    const fb = document.getElementById('authFeedback');
    if (!fb) return;
    fb.textContent = msg;
    fb.className = `form-feedback ${type}`;
    fb.style.display = 'flex';
}

function clearAuthFeedback() {
    const fb = document.getElementById('authFeedback');
    if (!fb) return;
    fb.textContent = '';
    fb.style.display = 'none';
}

// VALIDACIÓN STRICT DE CORREO ELECTRÓNICO
function validarCorreoElectronico(email) {
    const rawEmail = String(email || '').trim();
    if (!rawEmail) {
        return { valido: false, mensaje: 'Por favor ingresa tu correo electrónico.' };
    }
    
    // 1. Debe contener obligatoriamente el carácter @
    if (!rawEmail.includes('@')) {
        return { valido: false, mensaje: 'El correo electrónico debe incluir obligatoriamente el carácter @.' };
    }
    
    const partes = rawEmail.split('@');
    if (partes.length !== 2) {
        return { valido: false, mensaje: 'El correo electrónico no puede contener múltiples caracteres @.' };
    }
    
    const usuario = partes[0].trim();
    const dominio = partes[1].trim();
    
    // 2. Debe tener nombre de usuario antes del @
    if (!usuario) {
        return { valido: false, mensaje: 'Falta el nombre de usuario antes del carácter @.' };
    }
    
    // 3. Regla obligatoria: si le falta alguna letra/extensión después del @, no se permite
    if (!dominio) {
        return { valido: false, mensaje: 'Al correo le faltan caracteres y la extensión de dominio después del @ (ejemplo: @gmail.com, @hotmail.com).' };
    }
    
    // 4. Debe contener el punto (.) en el dominio
    if (!dominio.includes('.')) {
        return { valido: false, mensaje: 'El dominio del correo (después del @) debe incluir obligatoriamente un punto (.) y su extensión (ejemplo: @gmail.com, @hotmail.com).' };
    }
    
    const partesDominio = dominio.split('.');
    const nombreDominio = partesDominio[0].trim();
    const extension = partesDominio[partesDominio.length - 1].trim();
    
    if (!nombreDominio || nombreDominio.length < 2) {
        return { valido: false, mensaje: 'Al correo le falta el nombre del proveedor después del @ (ejemplo: @gmail.com, @hotmail.com).' };
    }
    
    if (!extension || extension.length < 2) {
        return { valido: false, mensaje: 'Al correo le falta una extensión de dominio válida (ejemplo: .com, .net, .co, .org).' };
    }
    
    // Regex estándar para formato final
    const regexCompleto = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (!regexCompleto.test(rawEmail)) {
        return { valido: false, mensaje: 'El formato de correo es incorrecto. Ejemplo correcto: nombre@dominio.com' };
    }
    
    return { valido: true, mensaje: '' };
}

// INICIAR SESIÓN (LOGIN)
async function handleLogin(event) {
    if (event) event.preventDefault();
    const emailInput = document.getElementById('loginEmail');
    const pwdInput = document.getElementById('loginPassword');
    const submitBtn = document.getElementById('loginSubmitBtn');
    
    const email = emailInput ? emailInput.value.trim() : '';
    const password = pwdInput ? pwdInput.value : '';
    
    if (!email || !password) {
        showAuthFeedback('Ingresa tu correo y contraseña.', 'error');
        return;
    }
    
    // Validar correo estrictamente
    const checkEmail = validarCorreoElectronico(email);
    if (!checkEmail.valido) {
        showAuthFeedback('⚠️ ' + checkEmail.mensaje, 'error');
        return;
    }
    
    // Verificar si el usuario está inactivo en el registro local
    const usuariosRegistrados = JSON.parse(localStorage.getItem('canopolis_usuarios') || '[]');
    const usuarioLocal = usuariosRegistrados.find(u => u.email.toLowerCase() === email.toLowerCase());
    
    if (usuarioLocal && String(usuarioLocal.estado).toUpperCase() === 'INACTIVO') {
        showAuthFeedback('⛔ Tu cuenta de usuario se encuentra INACTIVA. Contacta al administrador del sistema.', 'error');
        return;
    }
    
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span>Verificando credenciales...</span>';
    }
    
    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        }).catch(() => null);
        
        if (response && response.ok) {
            const data = await response.json();
            if (data.usuario && String(data.usuario.estado).toUpperCase() === 'INACTIVO') {
                showAuthFeedback('⛔ Tu cuenta de usuario se encuentra INACTIVA.', 'error');
                resetSubmitButton(submitBtn, 'Ingresar al Sistema');
                return;
            }
            completeLogin(data.usuario, data.token || 'sess_' + Math.random().toString(36).substring(2));
            return;
        } else if (response && response.status === 401) {
            const err = await response.json().catch(() => ({}));
            showAuthFeedback(err.mensaje || 'Contraseña incorrecta. Inténtalo de nuevo.', 'error');
            resetSubmitButton(submitBtn, 'Ingresar al Sistema');
            return;
        } else if (response && response.status === 404) {
            const err = await response.json().catch(() => ({}));
            showAuthFeedback(err.mensaje || 'Usuario no encontrado. Si no tienes cuenta, despliega la pestaña Crear Cuenta.', 'error');
            resetSubmitButton(submitBtn, 'Ingresar al Sistema');
            return;
        }
        
        // Fallback de demostración local
        await new Promise(r => setTimeout(r, 400));
        let rolUser = 'CLIENTE';
        let nombreUser = usuarioLocal ? usuarioLocal.nombre : (email.includes('@') ? email.split('@')[0].toUpperCase() : 'CLIENTE');
        
        if (email.toLowerCase().includes('admin')) {
            rolUser = 'ADMINISTRADOR';
            nombreUser = 'Administrador Sistema';
        } else if (email.toLowerCase().includes('vet')) {
            rolUser = 'VETERINARIO';
            nombreUser = 'Dr. Médico Veterinario';
        } else if (usuarioLocal) {
            rolUser = usuarioLocal.rol || 'CLIENTE';
        }

        const fallbackUser = {
            id: usuarioLocal ? usuarioLocal.id : Date.now(),
            nombre: nombreUser,
            email: email,
            rol: rolUser,
            estado: 'ACTIVO'
        };
        const fallbackToken = 'sess_local_' + Math.random().toString(36).substring(2, 10);
        completeLogin(fallbackUser, fallbackToken);
        
    } catch (error) {
        console.error('Error login:', error);
        showAuthFeedback('Error de comunicación. Inténtalo de nuevo.', 'error');
        resetSubmitButton(submitBtn, 'Ingresar al Sistema');
    }
}

// CREAR CUENTA (REGISTRO)
async function handleRegister(event) {
    if (event) event.preventDefault();
    const nameInput = document.getElementById('regName');
    const emailInput = document.getElementById('regEmail');
    const pwdInput = document.getElementById('regPassword');
    const submitBtn = document.getElementById('registerSubmitBtn');
    
    const nombre = nameInput ? nameInput.value.trim() : '';
    const email = emailInput ? emailInput.value.trim() : '';
    const password = pwdInput ? pwdInput.value : '';
    
    if (!nombre || !email || !password) {
        showAuthFeedback('Por favor completa todos los campos requeridos.', 'error');
        return;
    }
    
    // Validación estricta de correo electrónico
    const checkEmail = validarCorreoElectronico(email);
    if (!checkEmail.valido) {
        showAuthFeedback('⚠️ ' + checkEmail.mensaje, 'error');
        return;
    }
    
    // Guardar nuevo cliente en lista local para sincronización inmediata con Admin Panel
    let listaUsuarios = JSON.parse(localStorage.getItem('canopolis_usuarios') || 'null');
    if (!listaUsuarios) {
        listaUsuarios = [
            { id: 1, nombre: "Carlos Gómez", email: "carlos.gomez@gmail.com", rol: "CLIENTE", estado: "ACTIVO", fechaRegistro: "2026-01-15" },
            { id: 2, nombre: "María Rodríguez", email: "maria.rodriguez@hotmail.com", rol: "CLIENTE", estado: "ACTIVO", fechaRegistro: "2026-02-10" },
            { id: 3, nombre: "Ana Martínez", email: "ana.martinez@yahoo.com", rol: "CLIENTE", estado: "INACTIVO", fechaRegistro: "2026-03-01" },
            { id: 4, nombre: "Dr. Roberto Silva", email: "roberto.silva@canopolis.com", rol: "VETERINARIO", estado: "ACTIVO", fechaRegistro: "2025-11-20" },
            { id: 5, nombre: "Admin Canopolis", email: "admin@canopolis.com", rol: "ADMINISTRADOR", estado: "ACTIVO", fechaRegistro: "2025-10-01" }
        ];
    }
    
    const yaExiste = listaUsuarios.some(u => u.email.toLowerCase() === email.toLowerCase());
    if (yaExiste) {
        showAuthFeedback('⚠️ Este correo electrónico ya está registrado. Si ya tienes cuenta, ingresa por la opción Iniciar Sesión.', 'error');
        return;
    }

    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span>Registrando cuenta de cliente...</span>';
    }
    
    try {
        const response = await fetch('/api/auth/registro', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ nombre, email, password })
        }).catch(() => null);
        
        const nuevoClienteObj = {
            id: Date.now(),
            nombre: nombre,
            email: email,
            rol: 'CLIENTE',
            estado: 'ACTIVO',
            fechaRegistro: new Date().toISOString().split('T')[0]
        };

        listaUsuarios.push(nuevoClienteObj);
        localStorage.setItem('canopolis_usuarios', JSON.stringify(listaUsuarios));
        
        if (response && (response.status === 201 || response.ok)) {
            const data = await response.json().catch(() => ({}));
            completeLogin(data.usuario || nuevoClienteObj, data.token || 'sess_' + Math.random().toString(36).substring(2));
            showToast('¡Cuenta creada!', `Bienvenido a Canopolis, ${nombre}`);
            return;
        } else if (response && response.status === 409) {
            const err = await response.json().catch(() => ({}));
            showAuthFeedback(err.mensaje || 'El correo electrónico ya está registrado.', 'error');
            resetSubmitButton(submitBtn, 'Crear Cuenta y Entrar');
            return;
        }
        
        // Fallback local
        await new Promise(r => setTimeout(r, 400));
        completeLogin(nuevoClienteObj, 'sess_local_' + Math.random().toString(36).substring(2));
        showToast('¡Registro exitoso!', `Bienvenido a Canopolis, ${nombre}`);
        
    } catch (err) {
        console.error('Error registro:', err);
        showAuthFeedback('Error al registrar usuario.', 'error');
        resetSubmitButton(submitBtn, 'Crear Cuenta y Entrar');
    }
}

function resetSubmitButton(btn, text) {
    if (!btn) return;
    btn.disabled = false;
    btn.innerHTML = `<span>${text}</span>`;
}

// LOGUEO COMPLETADO
function completeLogin(user, token) {
    state.currentUser = user;
    state.token = token;
    localStorage.setItem('canopolis_user', JSON.stringify(user));
    localStorage.setItem('canopolis_token', token);
    localStorage.setItem('canopolis_time', new Date().toLocaleTimeString());
    
    closeModal('loginModal');
    updateAuthUI();
    loadRegisteredUsers();
  
    showToast('¡Bienvenido!', `Sesión iniciada como ${user.nombre}.`);
    
    const loginSubmit = document.getElementById('loginSubmitBtn');
    const regSubmit = document.getElementById('registerSubmitBtn');
    resetSubmitButton(loginSubmit, 'Ingresar al Sistema');
    resetSubmitButton(regSubmit, 'Crear Cuenta y Entrar');
    
    const rol = String(user.rol || '').toUpperCase();
    if (rol === 'ADMIN' || rol === 'ADMINISTRADOR') {
        sessionStorage.removeItem('bypass_admin_redirect');
        setTimeout(() => {
            window.location.href = 'admin.html';
        }, 400);
    }
}

function updateAuthUI() {
    const user = state.currentUser;
    
    // Elementos Desktop
    const guestNav = document.getElementById('guestNavActions');
    const userBadge = document.getElementById('userProfileBadge');
    const panelVetNav = document.getElementById('panelVetNav');
    const btnMisCitas = document.getElementById('btnMisCitas');
    const btnHistorialClinico = document.getElementById('btnHistorialClinico');
    
    // Elementos Móviles
    const mobileGuestActions = document.getElementById('mobileGuestActions');
    const mobileUserBadge = document.getElementById('mobileUserBadge');
    const mobileAuthSeparator = document.getElementById('mobileAuthSeparator');
    
    if (user) {
        // === USUARIO LOGUEADO ===
        
        // Ocultar botones de invitado
        if (guestNav) guestNav.style.display = 'none';
        if (mobileGuestActions) mobileGuestActions.style.display = 'none';
        
        // Mostrar badges de usuario
        if (userBadge) userBadge.style.display = 'flex';
       if (mobileUserBadge) mobileUserBadge.style.display = 'flex';
        if (mobileAuthSeparator) mobileAuthSeparator.style.display = 'block';
        
        // Actualizar información del usuario - Desktop
        const userName = document.getElementById('userName');
        const userRole = document.getElementById('userRole');
        const userAvatar = document.getElementById('userAvatar');
        if (userName) userName.textContent = user.nombre || 'Usuario';
        if (userRole) userRole.textContent = user.rol || 'CLIENTE';
        if (userAvatar) userAvatar.textContent = (user.nombre || 'U').charAt(0).toUpperCase();
        
        // Actualizar información del usuario - Móvil
        const mobileUserName = document.getElementById('mobileUserName');
        const mobileUserRole = document.getElementById('mobileUserRole');
        const mobileUserAvatar = document.getElementById('mobileUserAvatar');
        if (mobileUserName) mobileUserName.textContent = user.nombre || 'Usuario';
        if (mobileUserRole) mobileUserRole.textContent = user.rol || 'CLIENTE';
        if (mobileUserAvatar) mobileUserAvatar.textContent = (user.nombre || 'U').charAt(0).toUpperCase();
        
        // Mostrar/ocultar según rol
        const rol = (user.rol || '').toUpperCase();
        
        if (rol === 'ADMIN' || rol === 'ADMINISTRADOR') {
            if (panelVetNav) {
                panelVetNav.style.display = 'block';
                const link = panelVetNav.querySelector('a');
                if (link) {
                    link.href = 'admin.html';
                    link.innerHTML = '⚙️ Panel Admin';
                }
            }
            if (btnMisCitas) btnMisCitas.style.display = 'none';
            if (btnHistorialClinico) btnHistorialClinico.style.display = 'none';
        } else if (rol === 'VETERINARIO' || rol.includes('VETERINARIO')) {
            if (panelVetNav) {
                panelVetNav.style.display = 'block';
                const link = panelVetNav.querySelector('a');
                if (link) {
                    link.href = 'veterinario.html';
                    link.innerHTML = '🩺 Panel Médico';
                }
            }
            if (btnMisCitas) btnMisCitas.style.display = 'none';
            if (btnHistorialClinico) btnHistorialClinico.style.display = 'none';
        } else {
            // Cliente: Ocultar Panel Médico, mostrar Mis Citas e Historial
            if (panelVetNav) panelVetNav.style.display = 'none';
            if (btnMisCitas) btnMisCitas.style.display = 'block';
            if (btnHistorialClinico) btnHistorialClinico.style.display = 'block';
        }
        
    } else {
        // === USUARIO NO LOGUEADO ===
        
        // Mostrar botones de invitado
        if (guestNav) guestNav.style.display = 'flex';
        if (mobileGuestActions) mobileGuestActions.style.display = 'block';
        
        // Ocultar badges de usuario
        if (userBadge) userBadge.style.display = 'none';
        if (mobileUserBadge) mobileUserBadge.style.display = 'none';
        if (mobileAuthSeparator) mobileAuthSeparator.style.display = 'none';
        
        // Ocultar todos los elementos condicionales de rol
        if (panelVetNav) panelVetNav.style.display = 'none';
        if (btnMisCitas) btnMisCitas.style.display = 'none';
        if (btnHistorialClinico) btnHistorialClinico.style.display = 'none';
    }
}

function accederPanelMedico(event) {
    if (event) event.preventDefault();
    const user = state.currentUser;
    
    if (!user) {
        showToast('⚠️ Acceso Restringido', 'Debes iniciar sesión para acceder al panel.', 'error');
        setTimeout(() => {
            openModal('loginModal');
        }, 1200);
        return;
    }
    
    const rol = String(user.rol || '').toUpperCase();
    if (rol === 'ADMIN' || rol === 'ADMINISTRADOR') {
        window.location.href = 'admin.html';
    } else if (rol === 'VETERINARIO' || rol.includes('VETERINARIO')) {
        window.location.href = 'veterinario.html';
    } else {
        showToast('🚫 Acceso Restringido', 'Tu cuenta es de cliente. El panel administrativo o médico requiere privilegios de personal.', 'error');
    }
}

// Exponer función global
window.accederPanelMedico = accederPanelMedico;
// Exponer función global
window.accederPanelMedico = accederPanelMedico;


function handleLogout() {
    state.currentUser = null;
    state.token = null;
    localStorage.removeItem('canopolis_user');
    localStorage.removeItem('canopolis_token');
    localStorage.removeItem('canopolis_time');
    
    updateAuthUI();
   
    showToast('Sesión cerrada', 'Has cerrado tu sesión de forma segura.');
}

function checkPersistedSession() {
    const savedUser = localStorage.getItem('canopolis_user') || localStorage.getItem('nexus_user');
    let savedToken = localStorage.getItem('canopolis_token') || localStorage.getItem('nexus_token');
    
    if (savedUser) {
        try {
            state.currentUser = JSON.parse(savedUser);
            if (!savedToken) {
                savedToken = 'sess_active_' + Math.random().toString(36).substring(2, 10);
                localStorage.setItem('canopolis_token', savedToken);
            }
            state.token = savedToken;
            updateAuthUI();
        } catch (e) {
            console.error('Error al restaurar sesión:', e);
            localStorage.removeItem('canopolis_user');
            localStorage.removeItem('canopolis_token');
            state.currentUser = null;
            state.token = null;
        }
    }
}

function checkAdminRedirect() {
    if (state.currentUser) {
        const rol = String(state.currentUser.rol || '').toUpperCase();
        const fullPath = window.location.pathname;
        const pageName = fullPath.substring(fullPath.lastIndexOf('/') + 1) || 'index.html';
        const isPublicPage = pageName === 'index.html' || pageName === '' || pageName === 'index';
        const hasBypass = sessionStorage.getItem('bypass_admin_redirect') === 'true';

        if (isPublicPage && (rol === 'ADMIN' || rol === 'ADMINISTRADOR') && !hasBypass) {
            window.location.href = 'admin.html';
        }
    }
}

// =======================================================
// CONEXIÓN A MYSQL (XAMPP localhost:3306)
// =======================================================
async function checkBackendConnection() {
    const dbStatusDot = document.getElementById('dbStatusDot');
    const dbStatusText = document.getElementById('dbStatusText');
    
    if (dbStatusDot) dbStatusDot.className = 'status-dot';
    if (dbStatusText) dbStatusText.textContent = 'Verificando...';
    
    try {
        const res = await fetch('/api/conexion', { cache: 'no-cache' });
        if (res.ok) {
            const data = await res.json();
            state.isBackendConnected = true;
            if (dbStatusDot) dbStatusDot.className = 'status-dot online';
           if (dbStatusText) dbStatusText.textContent = 'Conectado';
            showToast('MySQL Activo', 'Conexión con la base de datos verificada (XAMPP).');
            loadRegisteredUsers();
            // Ocultar banner si el backend está online
            const banner = document.getElementById('backendBanner');
            if (banner) banner.style.display = 'none';
            return;
        }
    } catch (err) {
        // Backend offline
    }
    
    state.isBackendConnected = false;
    if (dbStatusDot) dbStatusDot.className = 'status-dot offline';
    if (dbStatusText) dbStatusText.textContent = 'Modo Demo';
    loadRegisteredUsers();
    // Mostrar banner si el backend está offline
    const banner = document.getElementById('backendBanner');
    if (banner) banner.style.display = 'block';
}

// CARGA DE USUARIOS Y PROPIETARIOS
async function loadRegisteredUsers() {
    const tbody = document.getElementById('usersTableBody');
    const metricUserCount = document.getElementById('metricUserCount');
    if (!tbody) return;
    
    try {
        const res = await fetch('/api/usuarios');
        if (res.ok) {
            const users = await res.json();
            renderUsersTable(users);
            if (metricUserCount) metricUserCount.textContent = users.length;
            return;
        }
    } catch (e) {}
    
    // Datos demo predeterminados si MySQL no está levantado
    const sampleClients = [
        { id: 1, nombre: 'Dr. Jesus Admin', email: 'admin@demo.com', rol: 'Administrador / Cirujano' },
        { id: 2, nombre: 'Ana García (Dueña de Max)', email: 'ana.garcia@correo.com', rol: 'Propietario' },
        { id: 3, nombre: 'Carlos Rivera (Dueño de Luna)', email: 'carlos.rivera@correo.com', rol: 'Propietario' },
        { id: 4, nombre: 'Sandra Morales (Dueña de Toby)', email: 'sandra.morales@correo.com', rol: 'Propietario' }
    ];
    renderUsersTable(sampleClients);
    if (metricUserCount) metricUserCount.textContent = sampleClients.length;
}

function renderUsersTable(users) {
    const tbody = document.getElementById('usersTableBody');
    if (!tbody) return;
    
    if (!users || users.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" style="text-align:center; padding: 20px; color: var(--clr-gray);">No hay registros aún en la base de datos MySQL.</td></tr>';
        return;
    }
    
    tbody.innerHTML = users.map(u => `<tr>
        <td><strong>#${u.id}</strong></td>
        <td>
            <div style="display:flex; align-items:center; gap:10px;">
                <div style="width:30px; height:30px; border-radius:50%; background:var(--clr-primary-light); color:var(--clr-primary); display:flex; align-items:center; justify-content:center; font-weight:800; font-size:0.8rem;">
                    ${u.nombre ? u.nombre.charAt(0).toUpperCase() : 'U'}
                </div>
                <span><strong>${escapeHtml(u.nombre)}</strong></span>
            </div>
        </td>
        <td><code>${escapeHtml(u.email)}</code></td>
        <td>
            <span style="font-size:0.75rem; padding:3px 10px; border-radius:999px; background:var(--clr-primary-light); color:var(--clr-primary-dark); font-weight:700;">
                ${u.rol || 'Registrado'}
            </span>
        </td>
    </tr>`).join('');
}

// FORMULARIO RÁPIDO PARA INSERTAR EN MYSQL
function setupQuickAddUser() {
    const form = document.getElementById('quickAddUserForm');
    if (!form) return;
    
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const nameInput = document.getElementById('newUserName');
        const emailInput = document.getElementById('newUserEmail');
        const nombre = nameInput ? nameInput.value.trim() : '';
        const email = emailInput ? emailInput.value.trim() : '';
        
        if (!nombre || !email) return;
        
        try {
            const res = await fetch('/api/usuarios', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ nombre, email })
            });
            if (res.ok) {
                showToast('Guardado en MySQL', `Propietario "${nombre}" registrado con éxito.`);
                nameInput.value = '';
                emailInput.value = '';
                loadRegisteredUsers();
                return;
            }
        } catch (err) {}
        
        showToast('Registrado', `Propietario "${nombre}" añadido.`);
        nameInput.value = '';
        emailInput.value = '';
        loadRegisteredUsers();
    });
}

// =======================================================
// ⭐ FORMULARIO DE AGENDAMIENTO DE CITAS (CONECTADO AL BACKEND)
// =======================================================
async function submitForm(event) {
    if (event) event.preventDefault();
    
    // 1. Validar que el usuario esté logueado
    if (!state.currentUser) {
        checkPersistedSession();
    }
    
    if (!state.currentUser) {
        showToast('⚠️ Regístrate primero', 'Debes crear una cuenta o iniciar sesión para agendar una cita.');
        switchModalTab('register');
        openModal('loginModal');
        return;
    }
    
    // 2. Obtener valores del formulario
    const form = document.getElementById('appointmentForm');
    const formData = form ? new FormData(form) : null;
    const getValue = (name, fallbackId) => {
        let val = formData ? formData.get(name) : null;
        if (!val && fallbackId) {
            const el = document.getElementById(fallbackId);
            if (el) val = el.value;
        }
        return String(val || '').trim();
    };

    const petName = getValue('petName', 'petName');
    const species = document.getElementById('petSpecies')?.value || 'Perro';
    const serviceRaw = getValue('appointmentType', 'appointmentType');
    const schedule = getValue('appointmentSchedule', 'appointmentSchedule') || 'Jornada Mañana';
    const date = getValue('appointmentDate', 'preferredDate') || new Date().toISOString().split('T')[0];
    const time = getValue('appointmentTime', 'preferredTime') || '09:00';
    const reasonRaw = getValue('notes', 'appointmentNotes');
    
    const mapService = {
        'general': 'Consulta General',
        'vaccine': 'Vacunación',
        'checkup': 'Chequeo Preventivo',
        'surgery': 'Cirugía Especializada'
    };
    const service = mapService[serviceRaw] || serviceRaw || 'Consulta General';
    const reason = `[Mascota: ${species} | Horario: ${schedule}] ${reasonRaw}`.trim();
    const btn = document.getElementById('appointmentSubmitBtn');
    
    // 3. Validar campos obligatorios
    if (!petName || !serviceRaw) {
        showToast('⚠️ Campos incompletos', 'Completa el nombre de la mascota y el tipo de consulta.');
        return;
    }
    
    if (btn) {
        btn.disabled = true;
        btn.innerHTML = '<span>Registrando Cita Médica...</span>';
    }
    
    let citaExitosa = false;
    
    try {
        // Intentar guardar en backend Spring Boot (MySQL)
        const mascotaResponse = await fetch('/api/mascotas', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                nombre: petName,
                especie: species,
                raza: 'No especificada',
                sexo: 'MACHO',
                fechaNacimiento: '2020-01-01',
                peso: 5.0,
                propietario: { id: state.currentUser.id }
            })
        }).catch(() => null);
        
        let mascotaId = null;
        if (mascotaResponse && mascotaResponse.ok) {
            const mascotaData = await mascotaResponse.json();
            mascotaId = mascotaData.mascota ? mascotaData.mascota.id : mascotaData.id;
        }
        
        if (mascotaId) {
            const citaResponse = await fetch('/api/citas', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    mascota: { id: mascotaId },
                    servicio: service,
                    fecha: date,
                    hora: time,
                    motivo: reason || 'Consulta general',
                    estado: 'PENDIENTE'
                })
            }).catch(() => null);
            
            if (citaResponse && citaResponse.ok) {
                citaExitosa = true;
            }
        }
    } catch (err) {
        console.warn('Backend offline o no disponible, procesando cita localmente:', err);
    }
    
    // Fallback local garantizado si backend no está activo o falló
    if (!citaExitosa) {
        const localCitas = JSON.parse(localStorage.getItem('canopolis_citas') || '[]');
        const nuevaCita = {
            id: Date.now(),
            mascota: {
                id: Date.now() + 1,
                nombre: petName,
                especie: species,
                propietario: { id: state.currentUser.id, nombre: state.currentUser.nombre }
            },
            servicio: service,
            fecha: date,
            hora: time,
            motivo: reason || 'Consulta médica',
            estado: 'PENDIENTE',
            fechaRegistro: new Date().toISOString()
        };
        localCitas.push(nuevaCita);
        localStorage.setItem('canopolis_citas', JSON.stringify(localCitas));
        citaExitosa = true;
    }
    
    if (form) form.reset();
    const petSpeciesInput = document.getElementById('petSpecies');
    if (petSpeciesInput) petSpeciesInput.value = 'Perro';
    
    if (btn) {
        btn.disabled = false;
        btn.innerHTML = '📅 Confirmar Cita';
    }
    
    closeModal('appointmentModal');
    showToast('✅ ¡Cita Agendada!', `Tu cita para ${petName} ha sido registrada con éxito.`);
    
    // REDIRECCIÓN AL APARTADO DE LAS CITAS PARA VER SU HISTORIAL
    setTimeout(() => {
        abrirModalMisCitas();
    }, 450);
}

// =======================================================
// FORMULARIO DE CONTACTO (CONFIRMACIÓN VISUAL)
// =======================================================
function handleContactForm(event) {
    if (event) event.preventDefault();
    const form = event ? event.target : document.getElementById('contactForm');
    if (!form) return;

    const name = form.querySelector('[name="name"]')?.value?.trim();
    const email = form.querySelector('[name="email"]')?.value?.trim();
    const message = form.querySelector('[name="message"]')?.value?.trim();

    if (!name || !email || !message) {
        showToast('⚠️ Campos requeridos', 'Por favor completa tu nombre, correo y mensaje.');
        return;
    }

    showToast('✅ ¡Mensaje Enviado!', `Gracias ${name}, hemos recibido tu mensaje. Nos comunicaremos contigo prontamente.`);
    form.reset();
}

// =======================================================
// TOAST NOTIFICATIONS
// =======================================================
function showToast(title, message) {
    const toast = document.getElementById('toast');
    const titleEl = document.getElementById('toastTitle');
    const msgEl = document.getElementById('toastMessage');
    
    if (titleEl) titleEl.textContent = title;
    if (msgEl) msgEl.textContent = message;
    
    if (toast) {
        toast.classList.add('show');
        clearTimeout(toast._timeout);
        toast._timeout = setTimeout(() => {
            toast.classList.remove('show');
        }, 4500);
    }
}

// Exponer funciones globales para interacción en todas las páginas
window.showToast = showToast;
window.handleContactForm = handleContactForm;
window.submitForm = submitForm;

// =======================================================
// INTERSECCIÓN & ANIMACIONES
// =======================================================
function setupStatsObserver() {
    function animateCounter(el, target, duration = 1800) {
        const start = 0;
        const startTime = performance.now();
        const isDecimal = target.toString().includes('.');
        
        function update(currentTime) {
            const elapsed = currentTime - startTime;
            const progress = Math.min(elapsed / duration, 1);
            const eased = 1 - Math.pow(1 - progress, 3);
            const current = start + (target - start) * eased;
            
            el.textContent = isDecimal
                ? current.toFixed(1) + '%'
                : '+' + Math.floor(current).toLocaleString('es-CO');
            
            if (progress < 1) requestAnimationFrame(update);
        }
        requestAnimationFrame(update);
    }
    
    const statsObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const nums = entry.target.querySelectorAll('.stat-num');
                const targets = [12000, 24, 99.8];
                nums.forEach((num, i) => {
                    if (i === 1) { num.textContent = '24/7'; return; }
                    animateCounter(num, targets[i]);
                });
                statsObserver.unobserve(entry.target);
            }
        });
    }, { threshold: 0.5 });
    
    const statsBlock = document.querySelector('.hero-stats');
    if (statsBlock) statsObserver.observe(statsBlock);
}

function setupElementAnimations() {
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -30px 0px'
    };
    
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('visible');
                observer.unobserve(entry.target);
            }
        });
    }, observerOptions);
    
    const animatedEls = document.querySelectorAll(
        '.service-card, .team-card, .testimonio-card, .why-feature, .gallery-item, .contact-item, .stat'
    );
    
    animatedEls.forEach((el, i) => {
        el.style.opacity = '0';
        el.style.transform = 'translateY(24px)';
        el.style.transition = `opacity 0.55s ease ${i * 0.05}s, transform 0.55s ease ${i * 0.05}s`;
        observer.observe(el);
    });
    
    const styleSheet = document.createElement('style');
    styleSheet.textContent = `.visible { opacity: 1 !important; transform: translateY(0) !important; }`;
    document.head.appendChild(styleSheet);
}

function setupTestimoniosDots() {
    const dots = document.querySelectorAll('.nav-dot');
    dots.forEach((dot, i) => {
        dot.addEventListener('click', () => {
            dots.forEach(d => d.classList.remove('active'));
            dot.classList.add('active');
        });
    });
}

function setupDateInputConstraint() {
    const dateInput = document.getElementById('preferredDate')
        || document.getElementById('appointmentDate');
    const timeInput = document.getElementById('preferredTime')
        || document.getElementById('appointmentTime');
    
    if (dateInput) {
        // Establecer fecha mínima (mañana)
        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        dateInput.min = tomorrow.toISOString().split('T')[0];
        
        // Establecer fecha máxima (6 meses adelante)
        const maxDate = new Date();
        maxDate.setMonth(maxDate.getMonth() + 6);
        dateInput.max = maxDate.toISOString().split('T')[0];
        
        // Validar día seleccionado
        dateInput.addEventListener('change', (e) => {
            const selectedDate = new Date(e.target.value + 'T00:00:00');
            const dayOfWeek = selectedDate.getDay(); // 0 = Domingo
            
            if (dayOfWeek === 0) {
                showToast('️ Horario especial', 'Los domingos atendemos de 8:00 am a 6:00 pm.');
                if (timeInput) {
                    timeInput.min = '08:00';
                    timeInput.max = '18:00';
                }
            } else {
                // Lunes a Sábado: 7am - 8pm
                if (timeInput) {
                    timeInput.min = '07:00';
                    timeInput.max = '20:00';
                }
            }
        });
    }
    
    if (timeInput) {
        // Horario por defecto (Lunes a Sábado)
        timeInput.min = '07:00';
        timeInput.max = '20:00';
    }
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// =======================================================
// FASE 2: MÓDULO DE CITAS Y CATÁLOGO DINÁMICO
// =======================================================

/**
 * Carga dinámicamente los servicios activos desde la API al select del formulario
 */
async function cargarServiciosEnSelect() {
    const serviceSelect = document.getElementById('serviceSelect');
    if (!serviceSelect) return;

    try {
        const res = await fetch('/api/servicios');
        if (res.ok) {
            const servicios = await res.json();
            if (Array.isArray(servicios) && servicios.length > 0) {
                serviceSelect.innerHTML = '<option value="">Seleccionar...</option>';
                servicios.forEach(s => {
                    const option = document.createElement('option');
                    option.value = s.nombre;
                    option.textContent = `${s.icono || '🩺'} ${s.nombre} - $${Number(s.precio).toLocaleString('es-CO')}`;
                    serviceSelect.appendChild(option);
                });
            }
        }
    } catch (e) {
        console.warn('No se pudo cargar el catálogo dinámico de servicios, usando opciones predeterminadas.');
    }
}

/**
 * Abre el modal de "Mis Citas" y consulta las citas del cliente en la BD
 */
async function abrirModalMisCitas() {
    if (!state.currentUser) {
        checkPersistedSession();
    }

    if (!state.currentUser) {
        showToast('Inicia sesión', 'Debes iniciar sesión para consultar tus citas.');
        switchModalTab('register');
        openModal('loginModal');
        return;
    }

    openModal('misCitasModal');
    const container = document.getElementById('misCitasContenido');
    if (!container) return;

    container.innerHTML = '<p style="text-align:center; color: var(--clr-gray); padding: 20px;">Cargando tus citas médicas...</p>';

    let citas = [];

    try {
        const res = await fetch(`/api/citas/cliente/${state.currentUser.id}`).catch(() => null);
        if (res && res.ok) {
            citas = await res.json();
        }
    } catch (err) {
        console.warn('Backend no disponible para listar citas:', err);
    }

    const localCitas = JSON.parse(localStorage.getItem('canopolis_citas') || '[]');
    const userLocalCitas = localCitas.filter(c => 
        c.mascota && c.mascota.propietario && (
            Number(c.mascota.propietario.id) === Number(state.currentUser.id) ||
            (state.currentUser.email && c.mascota.propietario.email && String(c.mascota.propietario.email).toLowerCase() === String(state.currentUser.email).toLowerCase())
        )
    );

    const backendIds = new Set(citas.map(c => Number(c.id)));
    userLocalCitas.forEach(lc => {
        if (!backendIds.has(Number(lc.id))) {
            citas.push(lc);
        }
    });

    renderizarMisCitas(citas);
}

/**
 * Renderiza la lista de citas del cliente con opción de cancelación
 */
function renderizarMisCitas(citas) {
    const container = document.getElementById('misCitasContenido');
    if (!container) return;

    if (!citas || citas.length === 0) {
        container.innerHTML = `
            <div style="text-align: center; padding: 30px 10px;">
                <p style="font-size: 1.1rem; color: var(--clr-gray); margin-bottom: 12px;">No tienes citas programadas actualmente.</p>
                <a href="#contacto" onclick="closeModal('misCitasModal')" class="btn-primary" style="padding: 8px 18px;">Agendar mi primera cita</a>
            </div>
        `;
        return;
    }

    let html = '<div style="display: flex; flex-direction: column; gap: 14px;">';

    citas.forEach(c => {
        let badgeBg = '#fef3c7';
        let badgeColor = '#b45309';

        if (c.estado === 'CONFIRMADA') {
            badgeBg = '#d1fae5';
            badgeColor = '#065f46';
        } else if (c.estado === 'CANCELADA') {
            badgeBg = '#fee2e2';
            badgeColor = '#991b1b';
        } else if (c.estado === 'REPROGRAMADA') {
            badgeBg = '#e0e7ff';
            badgeColor = '#3730a3';
        }

        const petName = c.mascota ? c.mascota.nombre : 'Mascota';
        const petSpec = c.mascota ? c.mascota.especie : '';

        html += `
            <div style="background: #fff; border: 1px solid rgba(0,0,0,0.08); border-radius: 10px; padding: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px;">
                <div>
                    <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 6px;">
                        <strong style="font-size: 1.05rem; color: var(--clr-dark);">${escapeHtml(c.servicio)}</strong>
                        <span style="background: ${badgeBg}; color: ${badgeColor}; font-weight: 700; font-size: 0.72rem; padding: 3px 8px; border-radius: 12px; text-transform: uppercase;">
                            ${escapeHtml(c.estado)}
                        </span>
                    </div>
                    <p style="margin: 0; font-size: 0.9rem; color: var(--clr-gray);">
                        🐾 Paciente: <strong>${escapeHtml(petName)}</strong> (${escapeHtml(petSpec)})
                    </p>
                    <p style="margin: 3px 0 0; font-size: 0.88rem; color: var(--clr-gray);">
                        📅 Fecha: <strong>${c.fecha}</strong> | ⏰ Hora: <strong>${c.hora}</strong>
                    </p>
                    ${c.motivo ? `<p style="margin: 4px 0 0; font-size: 0.82rem; color: #64748b; font-style: italic;">Motivo: "${escapeHtml(c.motivo)}"</p>` : ''}
                </div>
                <div style="display: flex; gap: 8px; align-items: center;">
                    ${c.mascota && c.mascota.id ? `
                        <a href="historia-clinica.html?mascotaId=${c.mascota.id}" class="btn-secondary" style="padding: 6px 12px; font-size: 0.82rem; color: var(--clr-primary); border-color: var(--clr-primary);">
                            📋 Historia Clínica
                        </a>
                    ` : ''}
                    ${c.estado !== 'CANCELADA' ? `
                        <button onclick="cancelarCitaCliente(${c.id})" class="btn-secondary" style="padding: 6px 14px; font-size: 0.82rem; color: #dc2626; border-color: #fca5a5;">
                            Cancelar Cita
                        </button>
                    ` : `
                        <span style="font-size: 0.8rem; color: #94a3b8;">Horario liberado</span>
                    `}
                </div>
            </div>
        `;
    });

    html += '</div>';
    container.innerHTML = html;
}

/**
 * Cancela la cita médica del cliente liberando el horario en BD
 */
async function cancelarCitaCliente(citaId) {
    if (!confirm('¿Estás seguro de que deseas cancelar esta cita? El horario quedará liberado.')) {
        return;
    }

    try {
        const res = await fetch(`/api/citas/${citaId}/cancelar`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' }
        });

        const data = await res.json();
        if (res.ok) {
            showToast('✅ Cita cancelada', 'Tu cita ha sido cancelada y el horario ha sido liberado.');
            abrirModalMisCitas(); // Refrescar listado
        } else {
            showToast('❌ Error', data.mensaje || 'No se pudo cancelar la cita.');
        }
    } catch (e) {
        showToast('❌ Error de red', 'No se pudo conectar con el servidor.');
    }
}

window.abrirModalMisCitas = abrirModalMisCitas;
window.cancelarCitaCliente = cancelarCitaCliente;
window.cargarServiciosEnSelect = cargarServiciosEnSelect;



// =======================================================
// FUNCIONES PARA MINI MENÚ MASCOTAS Y CATÁLOGO VACUNAS
// =======================================================
function selectPetType(btn, speciesValue) {
    document.querySelectorAll('.pet-type-btn').forEach(b => b.classList.remove('active'));
    if (btn) btn.classList.add('active');
    const hiddenInput = document.getElementById('petSpecies');
    if (hiddenInput) hiddenInput.value = speciesValue;
}

function openAppointmentModal(serviceVal) {
    if (!state.currentUser) {
        showToast('⚠️ Regístrate primero', 'Debes crear una cuenta o iniciar sesión para agendar una cita.');
        switchModalTab('register');
        openModal('loginModal');
        return;
    }
    openModal('appointmentModal');
    if (serviceVal) {
        const select = document.getElementById('appointmentType');
        if (select) select.value = serviceVal;
    }
}

function selectVaccineAndBook(vaccineName) {
    if (!state.currentUser) {
        closeModal('vaccineCatalogModal');
        showToast('⚠️ Regístrate primero', 'Debes crear una cuenta o iniciar sesión para solicitar una vacuna.');
        switchModalTab('register');
        openModal('loginModal');
        return;
    }
    closeModal('vaccineCatalogModal');
    openAppointmentModal('vaccine');
    const notes = document.getElementById('appointmentNotes');
    if (notes) notes.value = `Solicitud de vacuna: ${vaccineName}`;
}

// =======================================================
// FUNCIONES GLOBALES EXPUESTAS
// =======================================================
window.showToast = showToast;
window.handleContactForm = handleContactForm;
window.submitForm = submitForm;
window.abrirModalMisCitas = abrirModalMisCitas;
window.cancelarCitaCliente = cancelarCitaCliente;
window.cargarServiciosEnSelect = cargarServiciosEnSelect;
window.accederPanelMedico = accederPanelMedico;
window.selectPetType = selectPetType;
window.openAppointmentModal = openAppointmentModal;
window.selectVaccineAndBook = selectVaccineAndBook;
