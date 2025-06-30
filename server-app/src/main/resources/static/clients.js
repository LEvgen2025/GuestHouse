// Глобальные переменные
let clientIdToDelete = null;

// Основные функции
function loadClients() {
    try {
        fetch('/api/clients/show')
            .then(response => {
                if (response.ok) {
                    return response.json();
                }
                throw new Error('Ошибка загрузки клиентов');
            })
            .then(clients => renderClientsTable(clients))
            .catch(error => {
                console.error('Error:', error);
            });
    } catch (error) {
        console.error('Error:', error);
    }
}

function renderClientsTable(clients) {
    const tbody = document.querySelector('table tbody');
    tbody.innerHTML = ''; // Очищаем таблицу

    clients.forEach(client => {
        const row = document.createElement('tr');
        row.setAttribute('data-id', client.id);

        row.innerHTML = `
            <td>${client.name}</td>
            <td>${formatPhoneNumber(client.phoneNumber)}</td>
            <td>
                <button class="edit-btn"
                        data-id="${client.id}"
                        data-name="${client.name}"
                        data-phone="${client.phoneNumber}"
                        onclick="openEditModal(this)">
                    <i class="bi bi-pencil"></i>
                </button>
                <button class="delete-btn"
                        data-id="${client.id}"
                        onclick="confirmDelete(this)">
                    <i class="bi bi-trash"></i>
                </button>
            </td>
        `;

        tbody.appendChild(row);
    });
}

function formatPhoneNumber(phone) {
    const hasPlus = phone.startsWith('+');
    let raw = phone.replace(/\D/g, '');

    if (raw.length === 11) {
        return `+7(${raw.slice(1, 4)})-${raw.slice(4, 7)}-${raw.slice(7, 9)}-${raw.slice(9, 11)}`;
    } else if (hasPlus && raw.length === 11) {
        return `+7(${raw.slice(1, 4)})-${raw.slice(4, 7)}-${raw.slice(7, 9)}-${raw.slice(9, 11)}`;
    }
    return phone;
}

// Функции для работы с модальными окнами
function openModal(modalId) {
    document.getElementById(modalId).style.display = 'block';
}

function closeModal(modalId) {
    document.getElementById(modalId).style.display = 'none';
}

function handleOutsideClick(event) {
    if (event.target.className === 'modal') {
        document.querySelectorAll('.modal').forEach(modal => {
            modal.style.display = 'none';
        });
    }
}

function openEditModal(button) {
    document.getElementById('editClientId').value = button.getAttribute('data-id');
    document.getElementById('editName').value = button.getAttribute('data-name');
    document.getElementById('editPhone').value = button.getAttribute('data-phone');
    openModal('editModal');
}

function confirmDelete(button) {
    clientIdToDelete = button.getAttribute('data-id');
    openModal('deleteModal');
}

// Функции для работы с таблицей
function addClientToTable(client) {
    const tbody = document.querySelector('table tbody');
    const newRow = document.createElement('tr');
    newRow.setAttribute('data-id', client.id);

    newRow.innerHTML = `
        <td>${client.name}</td>
        <td>${formatPhoneNumber(client.phoneNumber)}</td>
        <td>
            <button class="edit-btn"
                    data-id="${client.id}"
                    data-name="${client.name}"
                    data-phone="${client.phoneNumber}"
                    onclick="openEditModal(this)">
                <i class="bi bi-pencil"></i>
            </button>
            <button class="delete-btn"
                    data-id="${client.id}"
                    onclick="confirmDelete(this)">
                <i class="bi bi-trash"></i>
            </button>
        </td>
    `;

    tbody.appendChild(newRow);
}

// Обработчики событий
function setupEventListeners() {
    // Обработчик формы добавления
    document.getElementById('addForm').addEventListener('submit', async function(e) {
        e.preventDefault();

        const name = document.getElementById('addName').value;
        const phone = document.getElementById('addPhone').value;

        if (!name || !phone) {
            alert('Заполните все поля');
            return;
        }

        try {
            const response = await fetch('/api/clients', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
                },
                body: JSON.stringify({ name, phoneNumber: phone })
            });

            if (response.ok) {
                const newClient = await response.json();
                addClientToTable(newClient);
                closeModal('addModal');
                document.getElementById('addForm').reset();
                alert('Клиент успешно добавлен!');
            } else {
                alert('Ошибка в введенных данных');
            }
        } catch (error) {
            console.error('Error:', error);
            alert('Произошла ошибка при добавлении');
        }
    });

    // Обработчик формы редактирования
    document.getElementById('editForm').addEventListener('submit', async function(e) {
        e.preventDefault();

        const clientId = document.getElementById('editClientId').value;
        const name = document.getElementById('editName').value;
        const phone = document.getElementById('editPhone').value;

        try {
            const response = await fetch(`/api/clients/${clientId}?name=${encodeURIComponent(name)}&phoneNumber=${encodeURIComponent(phone)}`, {
                method: 'PUT',
                headers: {
                    'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
                },
            });

            if (response.ok) {
                const row = document.querySelector(`tr[data-id="${clientId}"]`);
                if (row) {
                    row.cells[0].textContent = name;
                    row.cells[1].textContent = formatPhoneNumber(phone);
                    const editBtn = row.querySelector('.edit-btn');
                    editBtn.setAttribute('data-name', name);
                    editBtn.setAttribute('data-phone', phone);
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
    });

    // Обработчик удаления
    document.getElementById('confirmDeleteBtn').addEventListener('click', async function() {
        if (!clientIdToDelete) return;

        try {
            const response = await fetch(`/api/clients/${clientIdToDelete}`, {
                method: 'DELETE',
                headers: {
                    'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
                },
            });

            if (response.ok) {
                document.querySelector(`tr[data-id="${clientIdToDelete}"]`)?.remove();
                alert('Клиент успешно удален!');
            } else {
                alert('Ошибка при удалении клиента');
            }
        } catch (error) {
            console.error('Error:', error);
            alert('Произошла ошибка при удалении');
        } finally {
            closeModal('deleteModal');
            clientIdToDelete = null;
        }
    });

    // Обработчик клика вне модального окна
    window.addEventListener('click', handleOutsideClick);
}

// Инициализация
function initClientsPage() {
    loadClients();
    setupEventListeners();
    
    // Форматируем номера телефонов при загрузке
    document.querySelectorAll("tbody td:nth-child(2)").forEach(td => {
        td.textContent = formatPhoneNumber(td.textContent);
    });
}

// Делаем функции доступными глобально
window.openModal = openModal;
window.closeModal = closeModal;
window.openEditModal = openEditModal;
window.confirmDelete = confirmDelete;

// Запуск после загрузки DOM
document.addEventListener("DOMContentLoaded", initClientsPage);