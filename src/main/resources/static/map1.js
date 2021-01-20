//Цвета кругов
let colors = ["#34C924", "#990066", "#FF6E4A", "#B8B428",
    "#3C18FF", "#343030", "#066", "#990000",
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

//anychart MAP
anychart.onDocumentReady(function () {

    anychart.format.outputLocale('ru-ru');
    json = document.getElementById('json').value;
    let data = JSON.parse(json);

    //console.log(data);

    // рисует карту
    var map = anychart.map();
    map.geoData('anychart.maps.world')
    //.padding(0)
    ;

    map.background().fill({
        keys: ["#e0fffb",  "#d2f6fc", "#e0fcff" ,"#e9f2f3"],
        angle: 130,
    });

    map.unboundRegions()
        .enabled(true)
        //.fill('#f0f4ed')
        .fill({
            keys: ["#d7dbd5", "#d0d4d4", "#e5eadf"],
            angle: 130,
        })
        .stroke('#3D3C3C');


    map.credits()
        .enabled(true)
        .text('Data source: https://opendata.socrata.com')
        .logoSrc('https://opendata.socrata.com/stylesheets/images/common/favicon.ico');

    // Заголовок
    map.title()
        .enabled(true);
    // .padding([20, 0, 10, 0])
    //.text('Карта');
    var title = map.title();
    title.useHtml(true);
    title.text(
        "<br><a style=\"color:#000; font-size: 20px;\">" +
        "Группы"
    );

    // создает набор данных из данных образца
    var crashesDataSet = anychart.data.set(data).mapAs();

//---------------------------------------------Функции-------------------------------------------------
    // функция для создания маркера
    var createSeries = function (name, data, color, type, size) {
        var series = map.marker(data);
        series.name(name)
            .fill(color)
            .stroke(color)
            .type(type)
            .size(size)
            .labels(false)
            .selectionMode('none')
            .tooltip({title: false, separator: false});
        series.hovered()
            .stroke(color)
            .size(size*2);
        series.legendItem()
            .iconType(type)
            .iconSize(14)
            .iconFill(color)
            .iconStroke(color);
    };

    //Меняем маркеры по запросу
    window.marker_update = function (name, color, type, number, size) {
        var seridddes = map.getSeriesAt(number);
        seridddes.name(name)
            .stroke(color)
            .type(type)
            .size(size)
            .fill(color);
        seridddes.hovered()
            .size(size);
        seridddes.legendItem()
            .iconFill(color)
            .iconType(type);
    };
//---------------------------------------------------------------------------------


    // прохожусь столько раз сколько номеров групп
    let json_lenght = data[data.length - 1].number_group;
    let j = 1;
    let grouped = new Array();
    let notGruped = crashesDataSet.filter('isLessThenFive', filter_function(true));
    for (let i = 0; i < json_lenght; i++) {

        if(!crashesDataSet.get(crashesDataSet.find('number_group', i + 1), 'isLessThenFive'))
            grouped.push(crashesDataSet.filter('number_group', filter_function(i + 1)));
    }
    for (let i = 0; i < grouped.length; i++) {
        type = 'circle';
        size = '4';
        createSeries(i+1, grouped[i], colors[i], type, size);

    }
    createSeries(grouped.length+1, notGruped, '#000000', 'diagonal-cross', 2);

    //передаю максимальное значение номера группы
    document.getElementById('number').max = json_lenght;

    map.tooltip()
        .useHtml(true)
        // .padding([8, 13, 10, 13])
        .width(350)
        .fontSize(12)
        .fontColor('#e6e6e6')
        .format(function () {
            +this.getData('summary');
            //if (this.getData('summary') == 'null') summary = '';
            return '<span style="font-size: 15px">' +
                '<span style="color: #bfbfbf">№Станции: ' + '</span>' + this.getData('number_station') + '<br/>' +
                '<span style="color: #bfbfbf">№Группы: ' + '</span>' + (this.getData('isLessThenFive')===true ? "не группы" : this.getData('number_group')) + '</span>';
        });

    // Включает легенду
    map.legend(true);
    // масштабирование
    var zoomController = anychart.ui.zoom();
    zoomController.render(map);
    // масштабирование колесиком мыши
    // map.interactivity().zoomOnMouseWheel(true);
    // sets container id for the chart
    map.container('container');
    // Рисует
    map.draw();
});

//открываю настройки маркера
function openbox() {
    let display = document.getElementById('box').style.display;
    if (display === "none") {
        document.getElementById('box').style.display = 'block';
    } else {
        document.getElementById('box').style.display = "none";
    }
}

//меняю маркер
function update() {
    let index = document.getElementById('number').value;
    let name = document.getElementById('name').value;
    let color = document.getElementById('color').value;

    marker_update(name, color, 'circle', index - 1, '4');

    let element = $('#'+index);
    let nameElement = $('#'+index).parent().children(".div2");
    colors[index] = color;
    rectangleClusters[index].get(0).options.set('fillColor', color);
    rectangleClusters[index].get(0).options.set('strokeColor', color);
    element.css('background-color', color);

    circleClusters[index].each(function (el, i) {
        el.options.set('fillColor', color);
        el.options.set('strokeColor', color);
    });


    nameElement.html(name);
}

function filter_function(val1) {
    return function (fieldVal) {
        return val1 == fieldVal;
    };
}


//Yandex MAP

var map;
var circlesToShow = [];
var rectangleClusters = [];
var circleClusters = [];
var centerClusters = [];
var clusterCordsSumm = [];
var currentClusterNums = new Set();
var mode = 'rect';

function setType (type) {
    if(type==='yandex#map'){
        $('.div2').css('color', 'black');
    }
    else{
        $('.div2').css('color', 'white');
    }
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
        map.geoObjects.remove(centerClusters[i]);
    }
}
function setObjectType (type) {
    mapRemoveAll();
    mode = type;
    if(type==='rect') {
        for (let i=1; i<rectangleClusters.length; i++) {
           if(currentClusterNums.has(i)) map.geoObjects.add(rectangleClusters[i]);
        }
    }
    if(type==='circ') {

        for (let i=1; i<circleClusters.length; i++) {
            if(currentClusterNums.has(i)) map.geoObjects.add(circleClusters[i]);
        }
        if(circleClusters[0]!==undefined)
        {
            if(currentClusterNums.has(0)) map.geoObjects.add(circleClusters[0]);
        }
    }
    if(type==='both') {
        for (let i=1; i<circleClusters.length; i++) {
            if(currentClusterNums.has(i)) {
                map.geoObjects.add(rectangleClusters[i]);
                map.geoObjects.add(circleClusters[i]);
            }
        }
        if(circleClusters[0]!==undefined && currentClusterNums.has(0))
        {
            map.geoObjects.add(circleClusters[0]);
        }
    }
    for (let i=1; i<centerClusters.length; i++) {
        if(currentClusterNums.has(i)) map.geoObjects.add(centerClusters[i]);
    }
}

window.onload = function () {
    var json = document.getElementById('json').value;
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


        var rects = [];
        circlesToShow[0] = [];
        circleClusters[0] = new ymaps.GeoObjectCollection();
        let Xsumm = 0;
        let Ysumm = 0;
        for (let i = 0; i < coordinates.length; i++) {
            // Создаем круг.
            if (coordinates[i].isLessThenFive === false) {
                if (rects[coordinates[i].number_group] === undefined) {
                    rectangleClusters[coordinates[i].number_group] = new ymaps.GeoObjectCollection();
                    circleClusters[coordinates[i].number_group] = new ymaps.GeoObjectCollection();
                    centerClusters[coordinates[i].number_group] = new ymaps.GeoObjectCollection();
                    circlesToShow[coordinates[i].number_group] = [];
                    rects[coordinates[i].number_group] = [];
                    rects[coordinates[i].number_group][0] = coordinates[i].lat;
                    rects[coordinates[i].number_group][1] = coordinates[i].long;
                    rects[coordinates[i].number_group][2] = coordinates[i].lat;
                    rects[coordinates[i].number_group][3] = coordinates[i].long;
                    clusterCordsSumm[coordinates[i].number_group] = [coordinates[i].lat, coordinates[i].long];
                } else {
                    clusterCordsSumm[coordinates[i].number_group][0] += coordinates[i].lat;
                    clusterCordsSumm[coordinates[i].number_group][1] += coordinates[i].long;
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
                        fillColor: colors[(coordinates[i].number_group - 1) % colors.length],
                        // Цвет обводки.
                        strokeColor: colors[(coordinates[i].number_group - 1) % colors.length],
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
                        //iconImageOffset: [-1, -1],// Задаем опции круга.

                    });
                circleClusters[0].add(myCircle);
                //Добавляем круг на карту.
                //map.geoObjects.add(myCircle);
            }
        }
        currentClusterNums.add(0);
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
                 *  The fill colors and transparency.
                 */
                fillColor: colors[(i - 1) % colors.length],
                /**
                 * Additional fill transparency.
                 *  The resulting transparency will not be #33(0.2), but 0.1(0.2*0.5).
                 */
                fillOpacity: 0.2,
                // Stroke colors.
                strokeColor: colors[(i - 1) % colors.length],
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
            currentClusterNums.add(i);

            var trangleSize = 1.2;
            var centerX = Math.floor(clusterCordsSumm[i][0]/circleClusters[i].getLength()*100)/100;
            var centerY = Math.floor(clusterCordsSumm[i][1]/circleClusters[i].getLength()*100)/100;
            //Центры кластеров
            var centerTrangle = new ymaps.Polygon([[
                // Координаты вершин внешней границы многоугольника.
                [centerX + (trangleSize-0.1)/Math.log(centerX/22), centerY],
                [centerX - 0.5*trangleSize, centerY - 0.866*trangleSize],
                [centerX - 0.5*trangleSize, centerY + 0.866*trangleSize]
            ]],
                {
                    // Содержимое балуна. //
                    balloonContentBody:
                        " Центр кластера " + i + "<br \/>" +
                        " Координаты: " + centerX + "; " + centerY

                }, {
                    // Задаем опции круга.
                    // Цвет заливки.
                    // Последний байт (77) определяет прозрачность.
                    fillColor: "#ffffff",
                    // Цвет обводки.
                    fillOpacity: 1,
                    // Stroke colors.
                    strokeColor: colors[(i - 1) % colors.length],
                    // Stroke transparency.
                    strokeOpacity: 0.9,

                    strokeWidth: 2,
                });

            centerClusters[i].add(centerTrangle);

        }
        setObjectType('rect');

    }
    $('.slider__item>.div1').on('mouseenter', function(){
        let index = this.id;
        if(mode!=='circ' && index > 0) {

                rectangleClusters[index].get(0).options.set('fillOpacity', 0.6);
                rectangleClusters[index].get(0).options.set('strokeWidth', 9);
        }
        else{
            if(index == 0){
                circleClusters[index].each(function (el, i) {
                    el.options.set('iconImageSize', [9, 9]);
                });
            }
            else {
                circleClusters[index].each(function (el, i) {
                    el.options.set('strokeWidth', 12);
                });
            }
        }

    });
    $('.slider__item>.div1').on('mouseleave', function(){
        let index = this.id;
        if(mode!=='circ' && index > 0) {
            rectangleClusters[index].get(0).options.set('fillOpacity', 0.2);
            rectangleClusters[index].get(0).options.set('strokeWidth', 6);
        }
        else{
            if(index == 0){
                circleClusters[index].each(function (el, i) {
                    el.options.set('iconImageSize', [7, 7]);
                });
            }
            else {
                circleClusters[index].each(function (el, i) {
                    el.options.set('strokeWidth', 7);
                });
            }
        }
    });
    $('.slider__item>.div1').on('click', function(){
        let index = this.id;
        let element = $('#'+index);

            //Если включены
            if (element.css('background-color') !== 'rgb(255, 255, 255)') {
                //Если кластер учтен, убираем его из списка
                if(currentClusterNums.has(parseInt(index))) currentClusterNums.delete(parseInt(index));
                //Если в режиме прямоугольников и мы отключили не группу "без группы" удаляем прямоугольник с карты
                if (mode === 'rect' && index > 0) map.geoObjects.remove(rectangleClusters[index]);
                //Если в режиме точечном, удаляем кластер с карты
                else if (mode === 'circ') {
                    map.geoObjects.remove(circleClusters[index]);
                } else { //Если в совместном режиме удаляем и прямоугольник и кластер из точек
                    if (index > 0) map.geoObjects.remove(rectangleClusters[index]);
                    map.geoObjects.remove(circleClusters[index]);
                }
                //Если мы отключили группу "крестик" удаляем изображение креста из легенды
                if(index==0){
                    element.css('background', '');
                }
                if (index > 0) map.geoObjects.remove(centerClusters[index]);
                element.css('background-color', '#ffffff');
            } else { //Если включаем группу
                if(!currentClusterNums.has(parseInt(index))) currentClusterNums.add(parseInt(index));
                if (mode === 'rect') {
                    if(index > 0) {
                        map.geoObjects.add(rectangleClusters[index]);
                    }
                }
                else if (mode === 'circ') {
                    map.geoObjects.add(circleClusters[index]);
                } else {
                    if (index > 0) map.geoObjects.add(rectangleClusters[index]);
                    map.geoObjects.add(circleClusters[index]);
                }
                if(index==0){
                    element.css('background', 'url(/img/cross.png) no-repeat center /cover');
                }
                else
                {
                    map.geoObjects.add(centerClusters[index]);
                    element.css('background-color', colors[index - 1]);
                }
            }

    });
}