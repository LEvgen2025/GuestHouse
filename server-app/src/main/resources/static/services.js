// Глобальные переменные
let serviceIdToDelete = null;

// Форматирование цены (единообразное для всех страниц)
function formatPrice(price) {
    const num = parseFloat(price) || 0;
    return num.toLocaleString('ru-RU', {
        style: 'currency',
        currency: 'RUB',
        minimumFractionDigits: num % 1 === 0 ? 0 : 2
    });
}

// Инициализация страницы
function initServicesPage() {
    // Форматирование существующих цен при загрузке
    formatExistingPrices();

    // Настройка обработчиков событий
    setupEventListeners();
}

// Форматирование цен уже имеющихся в таблице
function formatExistingPrices() {
    document.querySelectorAll("tbody td:nth-child(2)").forEach(td => {
        const price = parseFloat(td.textContent);
        if (!isNaN(price)) {
            td.textContent = formatPrice(price);
        }
    });
}

// Настройка обработчиков событий
function setupEventListeners() {
    // Обработчик формы добавления
    document.getElementById('addForm').addEventListener('submit', handleAddService);

    // Обработчик формы редактирования
    document.getElementById('editForm').addEventListener('submit', handleEditService);

    // Обработчик кнопки удаления
    document.getElementById('confirmDeleteBtn').addEventListener('click', deleteService);

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

// Функция открытия модального окна редактирования
function openEditModal(button) {
    document.getElementById('editHouseId').value = button.getAttribute('data-id');
    document.getElementById('editName').value = button.getAttribute('data-name');
    document.getElementById('editPrice').value = button.getAttribute('data-price');
    openModal('editModal');
}

// Подтверждение удаления
function confirmDelete(button) {
    serviceIdToDelete = button.getAttribute('data-id');
    openModal('deleteModal');
}

// Обработчики форм
async function handleAddService(e) {
    e.preventDefault();

    const name = document.getElementById('addName').value;
    const price = parseFloat(document.getElementById('addPrice').value);

    if (!name || isNaN(price)) {
        alert('Заполните все поля корректно');
        return;
    }

    try {
        const response = await fetch('/api/services', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
            },
            body: JSON.stringify({
                name: name,
                price: price
            })
        });

        if (response.ok) {
            const newService = await response.json();
            addServiceToTable(newService);
            closeModal('addModal');
            document.getElementById('addForm').reset();
            alert('Услуга успешно добавлена!');
        } else {
            alert('Ошибка в введенных данных');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Произошла ошибка при добавлении');
    }
}

async function handleEditService(e) {
    e.preventDefault();

    const serviceId = document.getElementById('editHouseId').value;
    const name = document.getElementById('editName').value;
    const price = parseFloat(document.getElementById('editPrice').value);

    try {
        const response = await fetch(`/api/services/${serviceId}?name=${encodeURIComponent(name)}&price=${encodeURIComponent(price)}`, {
            method: 'PUT',
            headers: {
                'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
            }
        });

        if (response.ok) {
            const row = document.querySelector(`tr[data-id="${serviceId}"]`);
            if (row) {
                row.cells[0].textContent = name;
                row.cells[1].textContent = formatPrice(price);
                const editBtn = row.querySelector('.edit-btn');
                editBtn.setAttribute('data-name', name);
                editBtn.setAttribute('data-price', price);
            }
            closeModal('editModal');
            alert('Данные успешно обновлены!');
        } else {
            alert('Ошибка при обновлении данных');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Произошла ошибка при отправке данных');
    }
}

// Функция для добавления услуги в таблицу
function addServiceToTable(service) {
    const tbody = document.querySelector('table tbody');
    const newRow = document.createElement('tr');
    newRow.setAttribute('data-id', service.id);

    newRow.innerHTML = `
        <td>${service.name}</td>
        <td>${formatPrice(service.price)}</td>
        <td>
            <button class="edit-btn"
                    data-id="${service.id}"
                    data-name="${service.name}"
                    data-price="${service.price}"
                    onclick="openEditModal(this)">
                <i class="bi bi-pencil"></i>
            </button>
            <button class="delete-btn"
                    data-id="${service.id}"
                    onclick="confirmDelete(this)">
                <i class="bi bi-trash"></i>
            </button>
        </td>
    `;

    tbody.appendChild(newRow);
}

// Функция удаления услуги
async function deleteService() {
    if (!serviceIdToDelete) return;

    try {
        const response = await fetch(`/api/services/${serviceIdToDelete}`, {
            method: 'DELETE',
            headers: {
                'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
            }
        });

        if (response.ok) {
            document.querySelector(`tr[data-id="${serviceIdToDelete}"]`)?.remove();
            alert('Услуга успешно удалена!');
        } else {
            alert('Ошибка при удалении услуги');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Произошла ошибка при удалении');
    } finally {
        closeModal('deleteModal');
        serviceIdToDelete = null;
    }
}

// Экспорт функций в глобальную область видимости
window.openModal = openModal;
window.closeModal = closeModal;
window.openEditModal = openEditModal;
window.confirmDelete = confirmDelete;

// Инициализация после загрузки DOM
document.addEventListener('DOMContentLoaded', initServicesPage);