var map;
var circlesToShow = [];
var rectangleClusters = [];
var circleClusters = [];
function setType (type) {
    map.setType(type);
}
function mapRemoveAll (){
    if(circleClusters[0]!==undefined)
    {
        map.geoObjects.remove(circleClusters[0]);
    }
    for (let i=1; i<circleClusters.length; i++) {
        map.geoObjects.remove(circleClusters[i]);
        map.geoObjects.remove(rectangleClusters[i]);
    }
}
function setObjectType (type) {
    mapRemoveAll();
    if(type==='rect') {
        for (let i=1; i<rectangleClusters.length; i++) {
            map.geoObjects.add(rectangleClusters[i]);
        }
    }
    if(type==='circ') {

        for (let i=1; i<circleClusters.length; i++) {
            map.geoObjects.add(circleClusters[i]);
        }
        if(circleClusters[0]!==undefined)
        {
            map.geoObjects.add(circleClusters[0]);
        }
    }
    if(type==='both') {
        for (let i=1; i<circleClusters.length; i++) {
            map.geoObjects.add(rectangleClusters[i]);
            map.geoObjects.add(circleClusters[i]);
        }
        if(circleClusters[0]!==undefined)
        {
            map.geoObjects.add(circleClusters[0]);
        }
    }
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
            center: [58.846573, 80.930432],
            zoom: 2,
            controls: ['zoomControl']
        }, {
            //searchControlProvider: 'yandex#search',
            //restrictMapArea: true,
            //restrictMapArea: [[-40, -159], [80, 200]],
            minZoom: 2,
            maxZoom: 12,
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
        var rects = [];
        circlesToShow[0] = [];
        circleClusters[0] = new ymaps.GeoObjectCollection();
        for (let i = 0; i < coordinates.length; i++) {
            // Создаем круг.
            if (coordinates[i].isLessThenFive === false) {
                if (rects[coordinates[i].number_group] === undefined) {
                    rectangleClusters[coordinates[i].number_group] = new ymaps.GeoObjectCollection();
                    circleClusters[coordinates[i].number_group] = new ymaps.GeoObjectCollection();
                    circlesToShow[coordinates[i].number_group] = [];
                    rects[coordinates[i].number_group] = [];
                    rects[coordinates[i].number_group][0] = coordinates[i].lat;
                    rects[coordinates[i].number_group][1] = coordinates[i].long;
                    rects[coordinates[i].number_group][2] = coordinates[i].lat;
                    rects[coordinates[i].number_group][3] = coordinates[i].long;
                } else {
                    if (parseFloat(coordinates[i].lat) < parseFloat(rects[coordinates[i].number_group][0])) {
                        rects[coordinates[i].number_group][0] = coordinates[i].lat;
                    }
                    if (parseFloat(coordinates[i].long) < parseFloat(rects[coordinates[i].number_group][1])) {
                        rects[coordinates[i].number_group][1] = coordinates[i].long;
                    }
                    if (parseFloat(coordinates[i].lat) > parseFloat(rects[coordinates[i].number_group][2])) {
                        rects[coordinates[i].number_group][2] = coordinates[i].lat;
                    }
                    if (parseFloat(coordinates[i].long) > parseFloat(rects[coordinates[i].number_group][3])) {
                        rects[coordinates[i].number_group][3] = coordinates[i].long;
                    }

                }
                var myCircle = new ymaps.Circle([
                        // Координаты центра круга.
                        [coordinates[i].lat, coordinates[i].long],
                        // Радиус круга в метрах.
                        20500
                    ],
                    {
                        // Содержимое балуна. //
                        balloonContentBody:
                            " №Станции: " + parseInt(coordinates[i].number_station) + "<br \/>" +
                            " Координаты: " + coordinates[i].lat + "; " + coordinates[i].long + "<br \/>" +
                            " №группы: " + coordinates[i].number_group
                        // Содержимое хинта.
                        //hintContent: "Подвинь меня"
                    }, {
                        // Задаем опции круга.
                        // Цвет заливки.
                        // Последний байт (77) определяет прозрачность.
                        fillColor: color[(coordinates[i].number_group - 1) % color.length],
                        // Цвет обводки.
                        strokeColor: color[(coordinates[i].number_group - 1) % color.length],
                        // Ширина обводки в пикселях.
                        strokeWidth: 7,
                        geodesic: true
                    });

                // Добавляем круг на карту.
                circleClusters[coordinates[i].number_group].add(myCircle);
            } else {
                var myCircle = new ymaps.Placemark([coordinates[i].lat, coordinates[i].long],
                    {
                        // Содержимое балуна. //
                        balloonContentBody:
                            " №Станции: " + coordinates[i].number_station + "<br \/>" +
                            " Координаты: " + coordinates[i].lat + "; " + coordinates[i].long + "<br \/>" +
                            " №группы: без группы"
                        // Содержимое хинта.
                        //hintContent: "Подвинь меня"
                    }, {
                        iconLayout: 'default#image',

                        // Своё изображение иконки метки.
                        iconImageHref: '/img/cross.png',
                        // Размеры метки.
                        iconImageSize: [7, 7],
                        // Смещение левого верхнего угла иконки относительно
                        // её "ножки" (точки привязки).
                        iconImageOffset: [-1, -1],// Задаем опции круга.

                    });
                circleClusters[0].add(myCircle);
                // Добавляем круг на карту.
                //map.geoObjects.add(myCircle);
            }
        }

        for (let i = 1; i < rects.length; i++)
        {

            let myRectangle = new ymaps.Rectangle([
                // Setting the coordinates of the diagonal corners of the rectangle.
                [rects[i][0], rects[i][1]],
                [rects[i][2], rects[i][3]]
            ], {
                //Properties
                hintContent: '№группы: ' + i
            }, {
                /**
                 * Options.
                 *  The fill color and transparency.
                 */
                fillColor: color[(i - 1) % color.length],
                /**
                 * Additional fill transparency.
                 *  The resulting transparency will not be #33(0.2), but 0.1(0.2*0.5).
                 */
                fillOpacity: 0.2,
                // Stroke color.
                strokeColor: color[(i - 1) % color.length],
                // Stroke transparency.
                strokeOpacity: 0.8,
                // Line width.
                strokeWidth: 6,
                /**
                 * The radius of rounded corners.
                 *  This option is accepted only for a rectangle.
                 */
                borderRadius: 4
            });

            rectangleClusters[i].add(myRectangle);
        }
        setObjectType('rect');


    }
}