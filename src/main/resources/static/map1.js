var map;
function setType (type) {
    // Меняем тип карты на "Гибрид".
    map.setType(type);
}
window.onload = function (){


    json = document.getElementById('json').value;
    let coordinates = JSON.parse(json);
    //преобразую в число с плавающей точкой
    ymaps.ready(init);
    function init() {
        // Создание экземпляра карты и его привязка к контейнеру с
        // заданным id ("map").
        map = new ymaps.Map('map', {
            // При инициализации карты обязательно нужно указать
            // её центр и коэффициент масштабирования.
            center: [58.846573, 60.930432],
            zoom: 2,
            controls: ['zoomControl']
        }, {
            //searchControlProvider: 'yandex#search',
            //restrictMapArea: true,
            minZoom: 1,
            maxZoom: 7,
        });

        //массив
        //[0] - T
        //[1], [2] - координаты
        //[3] - №станции
        //[4] - Tc
        //[5] - №группы

        //Цвета кругов
        let color = ["#34C924", "#990066", "#FF6E4A", "#B8B428",
            "#3C18FF", "#000000", "#066", "#990000",
            "#622", "#4b6273", "#252850", "#346e24",
            "#480607", "#ACB78E", "#d3a7ff", "#1CA9C9",
            "#FFA000", "#FF0000", "#2c4815", "#FF5349",
            "#FEFE22", "#025669", "#00FF00", "#534B4F",
            "#7F180D", "#00A86B", "#999950", "#BAACC7",
            "#31372B", "#003366", "#FF9218", "#FF496C",
            "#F5DEB3", "#F3DA0B", "#B7410E", "#B76E79",
            "#99FF99", "#92000A", "#846A20", "#BBBBBB",
            "#966A57", "#84C3BE", "#382C1E", "#B85D43",
            "#413D51", "#CADABA", "#317F43", "#8A2BE2",
            "#282828", "#6699CC", "#FF6E4A", "#7BA05B",
            "#714B23", "#CF3476", "#3B83BD", "#D8A903",
            "#FFDAB9", "#472A3F", "#915F6D", "#000080",
            "#CC6C5C", "#313830", "#310062", "#9B2F1F",
            "#C37629", "#03C03C", "#5B1E31", "#564042",
            "#371F1C", "#ffb900", "#82898F", "#ffffff",
            "#BA55D3", "#00035a", "#82898F", "#671c20",
            "#A12312", "#5DA130", "#45CEA2", "#FF7518",
            "#B57281", "#8A3324", "#48D1CC", "#5E490F",
            "#7D512D", "#EE9374", "#D79D41", "#30626B",
            "#D35339", "#8C4566", "#423C63", "#EA8DF7",
            "#F75394", "#123524", "#BEF574", "#806B2A",
            "#4D7198", "#6fffb8", "#4E1609", "#FFA474",
            "#008CF0", "#78A2B7", "#FFF8DC", "#FFCC00",
            "#2e3b4b", "#EBC2AF", "#5a4528", "#7FFF00",
            "#D2691E", "#CDB891", "#45322E", "#40826D",
            "#FF845C", "#93AA00", "#F13A13", "#00836E",
            "#08E8DE", "#FFB300", "#007CAD", "#CD00CD",
            "#99c5cc", "#f0ff83", "#1a5478", "#9a5aff",
        ];

        for (let i = 0; i < coordinates.length; i++) {

            // Создаем круг.
            if (coordinates[i][4] == 'false') {
                var myCircle = new ymaps.Circle([
                        // Координаты центра круга.
                        [coordinates[i][0], coordinates[i][1]],
                        // Радиус круга в метрах.
                        20500
                    ],
                    {
                        // Содержимое балуна. //
                        balloonContentBody:
                            " №Станции: " + coordinates[i][2] + "<br \/>" +
                            " Координаты: " + coordinates[i][0] + "; " + coordinates[i][1] + "<br \/>" +
                            " №группы: " + coordinates[i][3]
                        // Содержимое хинта.
                        //hintContent: "Подвинь меня"
                    }, {
                        // Задаем опции круга.
                        // Цвет заливки.
                        // Последний байт (77) определяет прозрачность.
                        fillColor: color[(coordinates[i][3] - 1)%color.length],
                        // Цвет обводки.
                        strokeColor: color[(coordinates[i][3] - 1)%color.length],
                        // Ширина обводки в пикселях.
                        strokeWidth: 7,
                        geodesic: true
                    });

                // Добавляем круг на карту.
                map.geoObjects.add(myCircle);
            }
            else{
                var myCircle = new ymaps.Placemark([coordinates[i][0], coordinates[i][1]],
                    {
                        // Содержимое балуна. //
                        balloonContentBody:
                            " №Станции: " + coordinates[i][2] + "<br \/>" +
                            " Координаты: " + coordinates[i][0] + "; " + coordinates[i][1] + "<br \/>" +
                            " №группы: без группы"
                        // Содержимое хинта.
                        //hintContent: "Подвинь меня"
                    }, {
                        iconLayout: 'default#image',

                        // Своё изображение иконки метки.
                        iconImageHref: '/img/cross.png',
                        // Размеры метки.
                        iconImageSize: [6, 6],
                        // Смещение левого верхнего угла иконки относительно
                        // её "ножки" (точки привязки).
                        iconImageOffset: [-16, -16],// Задаем опции круга.

                    });

                // Добавляем круг на карту.
                map.geoObjects.add(myCircle);
            }
        }
        }
}