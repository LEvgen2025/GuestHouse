// Глобальные переменные
let userIdToDelete = null;

// Инициализация страницы
function initAdminPanel() {
    // Настройка обработчиков событий
    setupEventListeners();
}

// Настройка обработчиков событий
function setupEventListeners() {
    // Обработчик кнопки удаления
    document.getElementById('confirmDeleteBtn').addEventListener('click', deleteUser);

    // Обработчик клика вне модального окна
    window.addEventListener('click', handleOutsideClick);
}

// Функции для работы с модальными окнами
function openModal(modalId) {
    document.getElementById(modalId).style.display = 'block';
}

function closeModal(modalId) {
    document.getElementById(modalId).style.display = 'none';
}

function handleOutsideClick(event) {
    if (event.target.classList.contains('modal')) {
        document.querySelectorAll('.modal').forEach(modal => {
            modal.style.display = 'none';
        });
    }
}

// Подтверждение удаления
function confirmDelete(button) {
    userIdToDelete = button.getAttribute('data-id');
    openModal('deleteModal');
}

// Функция удаления пользователя
async function deleteUser() {
    if (!userIdToDelete) return;

    try {
        const response = await fetch(`/admin/user/delete/${userIdToDelete}`, {
            method: 'DELETE',
            headers: {
                'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
            }
        });

        if (response.ok) {
            // Удаляем строку из таблицы
            document.querySelector(`tr[data-id="${userIdToDelete}"]`)?.remove();
            alert('Пользователь удалён успешно!');
        } else if (response.status === 400) {
            alert('Нельзя удалить самого себя!');
        } else {
            const errorText = await response.text();
            alert('Ошибка при удалении: ' + (errorText || response.status));
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Ошибка сети: ' + error.message);
    } finally {
        closeModal('deleteModal');
        userIdToDelete = null;
    }
}

// Экспорт функций в глобальную область видимости
window.openModal = openModal;
window.closeModal = closeModal;
window.confirmDelete = confirmDelete;

// Инициализация после загрузки DOM
document.addEventListener('DOMContentLoaded', initAdminPanel);