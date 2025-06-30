// Глобальные переменные
let bookedDates = {};
let selectedHouseId = null;
let startPicker, endPicker;

// Функции для работы с модальными окнами
function openModal(modalId) {
    document.getElementById(modalId).style.display = 'block';
    loadRentalFormData();

    // При открытии окна добавления — обновляем disable
    if (modalId === 'addRentalModal') {
        selectedHouseId = null; // сбрасываем выбранный дом
        updateDisabledDates();  // снова блокируем все даты до выбора дома
    }
}

function closeModal(modalId) {
    document.getElementById(modalId).style.display = 'none';
    if (modalId === 'addRentalModal') {
        document.getElementById('addRentalForm').reset();
    }
}

// Закрытие при клике вне модального окна
function setupModalCloseHandlers() {
    window.onclick = function(event) {
        if (event.target.className === 'modal') {
            const modals = document.getElementsByClassName('modal');
            for (let modal of modals) {
                modal.style.display = 'none';
            }
        }
    }
}

// Функция для загрузки клиентов и домов в выпадающие списки
async function loadRentalFormData() {
    try {
        const clientSelect = document.getElementById('addClient');
        const houseSelect = document.getElementById('addHouse');

        // Очищаем списки
        clientSelect.innerHTML = '<option value="">Выберите клиента</option>';
        houseSelect.innerHTML = '<option value="">Выберите дом</option>';

        // Параллельная загрузка клиентов и домов
        const [clientsResponse, housesResponse] = await Promise.all([
            fetch('/api/clients/show'),
            fetch('/api/houses/show')
        ]);

        const clients = await clientsResponse.json();
        const houses = await housesResponse.json();

        // Заполняем список клиентов
        clients.forEach(client => {
            const option = document.createElement('option');
            option.value = client.id;
            option.textContent = client.name;
            clientSelect.appendChild(option);
        });

        // Заполняем список домов
        houses.forEach(house => {
            const option = document.createElement('option');
            option.value = house.id;
            option.textContent = house.name;
            houseSelect.appendChild(option);
        });
    } catch (error) {
        console.error('Ошибка при загрузке данных:', error);
        alert('Не удалось загрузить данные');
    }
}

// Форматирование даты
function formatDateLocal(date) {
    if (!date) return '';
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

// Обновление заблокированных дат
function updateDisabledDates() {
    if (!selectedHouseId) {
        // Если дом не выбран — блокируем все даты
        startPicker.set('disable', [{ from: "1900-01-01", to: "9999-12-31" }]);
        endPicker.set('disable', [{ from: "1900-01-01", to: "9999-12-31" }]);
        return;
    }

    if (!bookedDates[selectedHouseId] || bookedDates[selectedHouseId].length === 0) {
        // Если для выбранного дома нет забронированных периодов — разблокируем все даты
        startPicker.set('disable', []);
        endPicker.set('disable', []);
        return;
    }

    // Блокируем только занятые периоды для выбранного дома
    const periods = bookedDates[selectedHouseId];
    const disabledRanges = periods.map(p => {
        return {
            from: p.startDate,
            to: p.endDate
        };
    });

    startPicker.set('disable', disabledRanges);
    endPicker.set('disable', disabledRanges);
}

// Инициализация при загрузке страницы
function initRentalPage() {
    // Инициализация календарей
    startPicker = flatpickr("#addStartDate", {
        dateFormat: "Y-m-d",
        locale: "ru",
        minDate: "today",
        disable: [{ from: "1900-01-01", to: "9999-12-31" }], // Изначально блокируем все
        onChange: function(selectedDates, dateStr) {
            endPicker.set('minDate', dateStr);
        }
    });

    endPicker = flatpickr("#addEndDate", {
        dateFormat: "Y-m-d",
        locale: "ru",
        minDate: "today",
        disable: [{ from: "1900-01-01", to: "9999-12-31" }] // Изначально блокируем все
    });

    document.getElementById('addHouse').addEventListener('change', function (e) {
        selectedHouseId = e.target.value;
        updateDisabledDates();
    });

    // Обработчик формы добавления клиента
    document.getElementById('addForm').addEventListener('submit', async function(e) {
        e.preventDefault();

        const name = document.getElementById('addName').value;
        let phone = document.getElementById('addPhone').value;

        if (!name || !phone) {
            alert('Заполните все поля');
            return;
        }

        try {
            // Сохраняем номер как есть (с +, если он есть)
            const response = await fetch('/api/clients', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
                },
                body: JSON.stringify({
                    name: name,
                    phoneNumber: phone // Сохраняем номер в исходном виде
                })
            });

            if (response.ok) {
                const newClient = await response.json();
                closeModal('addClientModal');
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

    // Обработчик формы добавления аренды
    document.getElementById('addRentalForm').addEventListener('submit', async function(e) {
        e.preventDefault();

        const clientId = document.getElementById('addClient').value;
        const houseId = document.getElementById('addHouse').value;
        const startDate = formatDateLocal(startPicker.selectedDates[0]);
        const endDate = formatDateLocal(endPicker.selectedDates[0]);

        if (!clientId || !houseId || !startDate || !endDate) {
            alert('Заполните все поля');
            return;
        }

        const requestData = {
            client: { id: parseInt(clientId) },
            house: { id: parseInt(houseId) },
            startDate: startDate,
            endDate: endDate
        };

        try {
            const response = await fetch('/api/rentals', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
                },
                body: JSON.stringify(requestData)
            });

            if (response.ok) {
                closeModal('addRentalModal');
                document.getElementById('addRentalForm').reset();
                alert('Аренда успешно добавлена!');
            } else {
                alert('Ошибка в введенных данных');
            }
        } catch (error) {
            console.error('Error:', error);
            alert('Произошла ошибка при добавлении аренды');
        }
    });

    setupModalCloseHandlers();
}

// Инициализация после загрузки DOM
document.addEventListener('DOMContentLoaded', function() {
    // Получаем данные из Thymeleaf
    bookedDates = window.bookedPeriods || {};
    initRentalPage();
});