(function() {
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
//anychart.onDocumentReady(anychartInit);

    function anychartInit() {

        anychart.format.outputLocale('ru-ru');
        let json = document.getElementById('json').value;
        let data = JSON.parse(json);

        //console.log(data);

        // рисует карту
        var map = anychart.map();
        map.geoData('anychart.maps.world')
        //.padding(0)
        ;

        map.background().fill({
            keys: ["#e0fffb", "#d2f6fc", "#e0fcff", "#e9f2f3"],
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
                .size(size * 2);
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
        let grouped = [];
        let notGruped = crashesDataSet.filter('isLessThenMinGroupMembers', filter_function(true));
        for (let i = 0; i < json_lenght; i++) {

            if (!crashesDataSet.get(crashesDataSet.find('number_group', i + 1), 'isLessThenMinGroupMembers'))
                grouped.push(crashesDataSet.filter('number_group', filter_function(i + 1)));
        }
        for (let i = 0; i < grouped.length; i++) {
            let type = 'circle';
            let size = '4';
            createSeries(i + 1, grouped[i], colors[i], type, size);

        }
        createSeries(grouped.length + 1, notGruped, '#000000', 'diagonal-cross', 2);

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
                    '<span style="color: #bfbfbf">№Группы: ' + '</span>' + (this.getData('isLessThenMinGroupMembers') === true ? "не группы" : this.getData('number_group')) + '</span>';
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
    }

//открываю настройки маркера
    function openbox() {
        let display = document.getElementById('box').style.display;
        if (display === "none") {
            document.getElementById('box').style.display = 'block';
        } else {
            document.getElementById('box').style.display = "none";
        }
    }

    function outlineStation(station) {
        let index = stationNums[station][0];
        let num = stationNums[station][1];
        if (index == 0) {
            circleClusters[index].get(num - 1).options.set('iconImageSize', [13, 13]);

        } else {
            circleClusters[index].get(num - 1).options.set('strokeWidth', 18);
        }
        window.setTimeout(function () {
            backStation(index, num);
        }, 3000);
    }

    function backStation(index, num) {
        if (index == 0) {
            circleClusters[index].get(num - 1).options.set('iconImageSize', [7, 7]);

        } else {
            circleClusters[index].get(num - 1).options.set('strokeWidth', 7);
        }
    }

//меняю маркер
    function update() {
        let index = document.getElementById('number').value;
        let name = document.getElementById('name').value;
        let color = document.getElementById('color').value;

        marker_update(name, color, 'circle', index - 1, '4');

        let element = $('#' + index);
        let nameElement = element.parent().children(".div2");
        colors[index] = color;
        rectangleClusters[index].get(0).options.set('fillColor', color);
        rectangleClusters[index].get(0).options.set('strokeColor', color);
        element.css('background-color', color);

        circleClusters[index].each(function (el) {
            el.options.set('fillColor', color);
            el.options.set('strokeColor', color);
        });


        nameElement.html(name);
    }

    function filter_function(val1) {
        return function (fieldVal) {
            return val1 === fieldVal;
        };
    }


//Yandex MAP
//window.onload = initializeYandexMap;
//if(ymaps) initializeYandexMap();
    anychartInit();
    initializeYandexMap();

    function initializeYandexMap() {
        var json = document.getElementById('json').value;
        let coordinates = JSON.parse(json);


        //преобразую в число с плавающей точкой
        ymaps.ready(init);

        function init() {

            var rects = [];
            circlesToShow[0] = [];
            circleClusters[0] = new ymaps.GeoObjectCollection();
            stationNums[0] = [];
            let max_lat = -90;
            let max_long = -180;
            let min_lat = 90;
            let min_long = 180;
            for (let i = 0; i < coordinates.length; i++) {
                if (coordinates[i].lat > max_lat) max_lat = coordinates[i].lat;
                else if (coordinates[i].lat < min_lat) min_lat = coordinates[i].lat;
                if (coordinates[i].long > max_long) max_long = coordinates[i].long;
                else if (coordinates[i].long < min_long) min_long = coordinates[i].long;


                var stringParam = "";
                for (const [key, value] of Object.entries(coordinates[i].additionalParams)) {
                    stringParam += key + ": " + value + "<br \/>";
                }

                if (coordinates[i].isLessThenMinGroupMembers === false) {
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
                        clusterCordsSumm[coordinates[i].number_group] = [coordinates[i].lat, coordinates[i].lat, coordinates[i].long, coordinates[i].long];
                    } else {
                        if (coordinates[i].lat > clusterCordsSumm[coordinates[i].number_group][0]) clusterCordsSumm[coordinates[i].number_group][0] = coordinates[i].lat;
                        else if (coordinates[i].lat < clusterCordsSumm[coordinates[i].number_group][1]) clusterCordsSumm[coordinates[i].number_group][1] = coordinates[i].lat;
                        if (coordinates[i].long > clusterCordsSumm[coordinates[i].number_group][2]) clusterCordsSumm[coordinates[i].number_group][2] = coordinates[i].long;
                        else if (coordinates[i].long < clusterCordsSumm[coordinates[i].number_group][3]) clusterCordsSumm[coordinates[i].number_group][3] = coordinates[i].long;

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
                            15500
                        ],
                        {
                            // Содержимое балуна. //
                            balloonContentBody:
                                " №Станции: " + parseInt(coordinates[i].number_station) + "<br \/>" +
                                " Координаты: ш: " + coordinates[i].lat + "; д: " + coordinates[i].long + "<br \/>" +
                                stringParam +
                                " №группы: " + coordinates[i].number_group + "<br \/>" +
                                "<a target='_blank' href='/dataAnalysisForStation?station=" + parseInt(coordinates[i].number_station) + "'>Анализ данных</a>"
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
                    stationNums[coordinates[i].number_station] = [coordinates[i].number_group, circleClusters[coordinates[i].number_group].getLength()];
                } else {
                    var myCircle = new ymaps.Placemark([coordinates[i].lat, coordinates[i].long],
                        {
                            // Содержимое балуна. //
                            balloonContentBody:
                                " №Станции: " + coordinates[i].number_station + "<br \/>" +
                                " Координаты: ш: " + coordinates[i].lat + "; д: " + coordinates[i].long + "<br \/>" +
                                stringParam +
                                " №группы: без группы" + "<br \/>" +
                                "<a target='_blank' href='/dataAnalysisForStation?station=" + parseInt(coordinates[i].number_station) + "'>Анализ данных</a>"
                            // Содержимое хинта.
                            //hintContent: "Подвинь меня"
                        }, {
                            iconLayout: 'default#image',

                            // Своё изображение иконки метки.
                            iconImageHref: '/img/cross.png',
                            // Размеры метки.
                            iconImageSize: [8, 8],
                            iconColor: colors[(coordinates[i].number_group - 1) % colors.length],
                            // Смещение левого верхнего угла иконки относительно
                            // её "ножки" (точки привязки).
                            iconImageOffset: [-2, -2],// Задаем опции круга.
                            geodesic: true

                        });

                    circleClusters[0].add(myCircle);
                    stationNums[coordinates[i].number_station] = [0, circleClusters[0].getLength()];
                    //Добавляем круг на карту.
                    //map.geoObjects.add(myCircle);
                }
            }


            // Создание экземпляра карты и его привязка к контейнеру с
            // заданным id ("map").
            let delta = 2;
            var zoomControl = new ymaps.control.ZoomControl({
                options: {
                    position: {
                        right: 20,
                        left: 'auto',
                        top: 108,
                        bottom: 'auto'
                    }
                }
            });
            map = new ymaps.Map('map', {
                // При инициализации карты обязательно нужно указать
                // её центр и коэффициент масштабирования.
                bounds: ([[min_lat - delta, min_long - delta], [max_lat + delta, max_long + delta]]),
                center: [(min_lat + max_lat) / 2, (min_long + max_long) / 2],
                zoom: 1,
                controls: []
            }, {
                //searchControlProvider: 'yandex#search',
                //restrictMapArea: true,
                //restrictMapArea: [[min_lat - delta, min_long - delta], [max_lat + delta, max_long+ delta]],
                minZoom: 1,
                maxZoom: 20,
                maxAnimationZoomDifference: 1,
                avoidFractionalZoom: false
            });
            map.controls.add(zoomControl);

            //нннннннннннннннннннннннннннннннннннннннн обрезка карты

            /*ymaps.borders.load('RU', {
                lang: 'ru',
                quality: 2
            }).then(function (result) {

                // Создадим многоугольник, который будет скрывать весь мир, кроме заданной страны.
                var background = new ymaps.Polygon([
                    [
                        [85, -100],
                        [85, 0],
                        [85, 100],
                        [85, 180],
                        [85, -110],
                        [-85, -110],
                        [-85, 180],
                        [-85, 100],
                        [-85, 0],
                        [-85, -100],
                        [85, -100]
                    ]
                ], {}, {
                    fillColor: '#ffffff',
                    strokeWidth: 0,
                    // Для того чтобы полигон отобразился на весь мир, нам нужно поменять
                    // алгоритм пересчета координат геометрии в пиксельные координаты.
                    //coordRendering: 'straightPath'
                });

                // Найдём страну по её iso коду.
                var region = result.features.filter(function (feature) { return feature.properties.iso3166 == 'RU-KYA'; })[0];

                // Добавим координаты этой страны в полигон, который накрывает весь мир.
                // В полигоне образуется полость, через которую будет видно заданную страну.
                var masks = region.geometry.coordinates;
                masks.forEach(function(mask){
                    background.geometry.insert(1, mask);
                });

                // Добавим многоугольник на карту.
                map.geoObjects.add(background);
            });*/

            //нннннннннннннннннннннннннннннннннннннннн

            //массив
            //[0] - T
            //[1], [2] - координаты
            //[3] - №станции
            //[4] - Tc
            //[5] - №группы


            currentClusterNums.add(0);
            for (let i = 1; i < rects.length; i++) {
                let myRectangle = new ymaps.Rectangle([
                    // Setting the coordinates of the diagonal corners of the rectangle.
                    [rects[i][0], rects[i][1]],
                    [rects[i][2], rects[i][3]]
                ], {
                    //Properties
                    //hintContent: '№группы: ' + i,
                    balloonContentBody:
                        '№группы: ' + i + "<br \/>" +
                        " Координаты левого нижнего угла: ш: " + rects[i][0] + ", д: " + rects[i][1] + "<br \/>" +
                        " Координаты правого верхнего угла: ш: " + rects[i][2] + ", д: " + rects[i][3] + "<br \/>" +
                        "<a target='_blank' href='/showClusterAnalysis?clusterNum=" + i + "'>Анализ группы</a>"
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
                var centerX = Math.floor((clusterCordsSumm[i][0] + clusterCordsSumm[i][1]) / 2 * 100) / 100;
                var centerY = Math.floor((clusterCordsSumm[i][2] + clusterCordsSumm[i][3]) / 2 * 100) / 100;

                /*var clasterCenter= new ymaps.Placemark(
                        // Координаты центра круга.
                        [centerX, centerY],
                        // Радиус круга в метрах.,
                    {
                        // Содержимое балуна. //
                        balloonContentBody:
                            " Центр кластера: " + "<br \/>" +
                            " Координаты: " + centerX + "; " + centerY + "<br \/>"
                        // Содержимое хинта.
                        //hintContent: "Подвинь меня"
                    }, {
                        preset: "islands#circleIcon",
                        iconColor:  colors[(i - 1) % colors.length],

                    });*/
                var clasterCenter1 = new ymaps.Polyline([[rects[i][0], rects[i][1]],
                        [rects[i][2], rects[i][3]]],
                    {
                        // Содержимое балуна. //
                        balloonContentBody:
                            " Центр кластера: " + "<br \/>" +
                            " Координаты: ш: " + centerX + "; д: " + centerY + "<br \/>"
                        // Содержимое хинта.
                        //hintContent: "Подвинь меня"
                    },
                    {
                        strokeColor: colors[(i - 1) % colors.length],
                        strokeWidth: 4,
                        opacity: 0.5
                    });
                var clasterCenter2 = new ymaps.Polyline([[rects[i][0], rects[i][3]],
                        [rects[i][2], rects[i][1]]],
                    {
                        // Содержимое балуна. //
                        balloonContentBody:
                            " Центр кластера: " + "<br \/>" +
                            " Координаты: ш: " + centerX + "; д: " + centerY + "<br \/>"
                        // Содержимое хинта.
                        //hintContent: "Подвинь меня"
                    },
                    {
                        strokeColor: colors[(i - 1) % colors.length],
                        strokeWidth: 4,
                        opacity: 0.5
                    });
                centerClusters[i].add(clasterCenter1);
                centerClusters[i].add(clasterCenter2);

            }
            setObjectType('both');

        }

        $('.slider__item>.div1').on('mouseenter', function () {
            let index = this.id;
            if (mode !== 'circ' && index > 0) {

                rectangleClusters[index].get(0).options.set('fillOpacity', 0.6);
                rectangleClusters[index].get(0).options.set('strokeWidth', 9);
            } else {
                if (index == 0) {
                    circleClusters[index].each(function (el, i) {
                        el.options.set('iconImageSize', [9, 9]);
                    });
                } else {
                    circleClusters[index].each(function (el, i) {
                        el.options.set('strokeWidth', 12);
                    });
                }
            }

        });
        $('.slider__item>.div1').on('mouseleave', function () {
            let index = this.id;
            if (mode !== 'circ' && index > 0) {
                rectangleClusters[index].get(0).options.set('fillOpacity', 0.2);
                rectangleClusters[index].get(0).options.set('strokeWidth', 6);
            } else {
                if (index == 0) {
                    circleClusters[index].each(function (el, i) {
                        el.options.set('iconImageSize', [7, 7]);
                    });
                } else {
                    circleClusters[index].each(function (el, i) {
                        el.options.set('strokeWidth', 7);
                    });
                }
            }
        });
        $('.slider__item>.div1').on('click', function () {
            let index = this.id;
            let element = $('#' + index);

            //Если включены
            if (element.css('background-color') !== 'rgb(255, 255, 255)') {
                //Если кластер учтен, убираем его из списка
                if (currentClusterNums.has(parseInt(index))) currentClusterNums.delete(parseInt(index));
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
                if (index == 0) {
                    element.css('background', '');
                }
                if (index > 0) map.geoObjects.remove(centerClusters[index]);
                element.css('background-color', '#ffffff');
            } else { //Если включаем группу
                if (!currentClusterNums.has(parseInt(index))) currentClusterNums.add(parseInt(index));
                if (mode === 'rect') {
                    if (index > 0) {
                        map.geoObjects.add(rectangleClusters[index]);
                    }
                } else if (mode === 'circ') {
                    //if(circleClusters[index].getLength() > 0)
                    map.geoObjects.add(circleClusters[index]);
                } else {
                    if (index > 0) map.geoObjects.add(rectangleClusters[index]);
                    //if(circleClusters[index].getLength() > 0)
                    map.geoObjects.add(circleClusters[index]);
                }
                if (index == 0) {
                    element.css('background', 'url(/img/cross.png) no-repeat center /cover');
                } else {
                    if (isChecked) map.geoObjects.add(centerClusters[index]);
                    element.css('background-color', colors[index - 1]);
                }
            }

        });

        $('#centersShow').on('click', function () {
            if (isChecked) {
                for (let i = 1; i < circleClusters.length; i++) {
                    map.geoObjects.remove(centerClusters[i]);
                }
                isChecked = false;
            } else {
                for (let i = 1; i < circleClusters.length; i++) {
                    if (currentClusterNums.has(i)) map.geoObjects.add(centerClusters[i]);

                }
                isChecked = true;
            }

        });
    }
})()
function setType(type) {
    if (type === 'yandex#map') {
        $('.div2').css('color', 'black');
    } else {
        $('.div2').css('color', 'white');
    }
    map.setType(type);
}

function mapRemoveAll() {
    if (circleClusters[0] !== undefined) {
        map.geoObjects.remove(circleClusters[0]);
    }
    for (let i = 1; i < circleClusters.length; i++) {
        map.geoObjects.remove(circleClusters[i]);
        map.geoObjects.remove(rectangleClusters[i]);
        map.geoObjects.remove(centerClusters[i]);
    }
}

function setObjectType(type) {
    mapRemoveAll();
    mode = type;
    if (type === 'rect') {
        for (let i = 1; i < rectangleClusters.length; i++) {
            if (currentClusterNums.has(i)) map.geoObjects.add(rectangleClusters[i]);
        }
    }
    if (type === 'circ') {

        for (let i = 1; i < circleClusters.length; i++) {
            if (currentClusterNums.has(i)) map.geoObjects.add(circleClusters[i]);
        }
        if (circleClusters[0] !== undefined) {
            if (currentClusterNums.has(0)) map.geoObjects.add(circleClusters[0]);
        }
    }
    if (type === 'both') {
        for (let i = 1; i < circleClusters.length; i++) {
            if (currentClusterNums.has(i)) {
                map.geoObjects.add(rectangleClusters[i]);
            }
        }
        for (let i = 1; i < circleClusters.length; i++) {
            if (currentClusterNums.has(i)) {
                map.geoObjects.add(circleClusters[i]);
            }
        }
        if (circleClusters[0] !== undefined && currentClusterNums.has(0)) {
            map.geoObjects.add(circleClusters[0]);
        }
    }
    for (let i = 1; i < centerClusters.length; i++) {
        if (currentClusterNums.has(i) && isChecked) map.geoObjects.add(centerClusters[i]);
    }
}