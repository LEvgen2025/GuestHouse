document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll("tbody td:nth-child(2)").forEach(function (td) {
        let raw = td.textContent.replace(/\D/g, ''); // Очищаем от всего кроме цифр
        if (raw.length === 11) {
            let formatted = `+7(${raw.slice(1, 4)})-${raw.slice(4, 7)}-${raw.slice(7, 9)}-${raw.slice(9, 11)}`;
            td.textContent = formatted;
        }
    });
});

// Общие функции для работы с модальными окнами
function openModal(modalId) {
    document.getElementById(modalId).style.display = 'block';
}

function closeModal(modalId) {
    document.getElementById(modalId).style.display = 'none';
}

// Закрытие при клике вне модального окна
window.onclick = function (event) {
    if (event.target.className === 'modal') {
        const modals = document.getElementsByClassName('modal');
        for (let modal of modals) {
            modal.style.display = 'none';
        }
    }
}

// Функция открытия модального окна редактирования
function openEditModal(button) {
    document.getElementById('editClientId').value = button.getAttribute('data-id');
    document.getElementById('editName').value = button.getAttribute('data-name');
    document.getElementById('editPhone').value = button.getAttribute('data-phone');
    openModal('editModal');
}

// Обработчик формы редактирования
document.getElementById('editForm').addEventListener('submit', async function (e) {
    e.preventDefault();

    const clientId = document.getElementById('editClientId').value;
    const name = document.getElementById('editName').value;
    const phone = document.getElementById('editPhone').value;

    try {
        const response = await fetch(`/api/clients/${clientId}?name=${encodeURIComponent(name)}&phoneNumber=${encodeURIComponent(phone)}`, {
                    method: 'PUT'
        });

        if (response.ok) {
            const row = document.querySelector(`tr[data-id="${clientId}"]`);
            if (row) {
                row.cells[0].textContent = name;
                row.cells[1].textContent = phone;
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

// Обработчик формы добавления
document.getElementById('addForm').addEventListener('submit', async function (e) {
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
            },
            body: JSON.stringify({
                name: name,
                phoneNumber: phone
            })
        });

        if (response.ok) {
            const newClient = await response.json();
            addClientToTable(newClient);
            closeModal('addModal');
            document.getElementById('addForm').reset();
            alert('Клиент успешно добавлен!');
        } else {
            alert('Ошибка при добавлении клиента: ' + await response.text());
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Произошла ошибка при добавлении');
    }
});

// Функция для добавления клиента в таблицу
function addClientToTable(client) {
    const tbody = document.querySelector('table tbody');
    const newRow = document.createElement('tr');
    newRow.setAttribute('data-id', client.id);

    newRow.innerHTML = `
        <td>${client.name}</td>
        <td>${client.phoneNumber}</td>
        <td>
            <button class="edit-btn"
                    data-id="${client.id}"
                    data-name="${client.name}"
                    data-phone="${client.phoneNumber}"
                    onclick="openEditModal(this)">
                Редактировать
            </button>
            <button class="delete-btn"
                    data-id="${client.id}"
                    onclick="confirmDelete(this)">
                Удалить
            </button>
        </td>
    `;

    tbody.appendChild(newRow);
}

// Логика удаления клиента
let clientIdToDelete = null;

function confirmDelete(button) {
    clientIdToDelete = button.getAttribute('data-id');
    openModal('deleteModal');
}

document.getElementById('confirmDeleteBtn').addEventListener('click', async function () {
    if (!clientIdToDelete) return;

    try {
        const response = await fetch(`/api/clients/${clientIdToDelete}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            const row = document.querySelector(`tr[data-id="${clientIdToDelete}"]`);
            if (row) {
                row.remove();
            }
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
