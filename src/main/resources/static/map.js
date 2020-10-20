anychart.onDocumentReady(function () {
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

            //color = rainbow(grouped.length, i+1);
            if(grouped.length%2===0)
                color = rainbow(grouped.length+2, i%2===0?i+1:grouped.length-i+1);
            else
                color = rainbow(grouped.length+2, i%2===0?i+2:grouped.length-i+1);
            type = 'circle';
            size = '4';
            createSeries(i+1, grouped[i], color, type, size);

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
//генерация цветов
function rainbow(numOfSteps, step) {
    // This function generates vibrant, "evenly spaced" colours (i.e. no clustering). This is ideal for creating easily distinguishable vibrant markers in Google Maps and other apps.
    // Adam Cole, 2011-Sept-14
    // HSV to RBG adapted from: http://mjijackson.com/2008/02/rgb-to-hsl-and-rgb-to-hsv-color-model-conversion-algorithms-in-javascript
    var r, g, b;
    var h = step / numOfSteps;
    var i = ~~(h * 6);
    var f = h * 6 - i;
    var q = 1 - f;
    switch(i % 6){
        case 0: r = 1; g = f; b = 0; break;
        case 1: r = q; g = 1; b = 0; break;
        case 2: r = 0; g = 1; b = f; break;
        case 3: r = 0; g = q; b = 1; break;
        case 4: r = f; g = 0; b = 1; break;
        case 5: r = 1; g = 0; b = q; break;
    }
    var c = "#" + ("00" + (~ ~(r * 255)).toString(16)).slice(-2) + ("00" + (~ ~(g * 255)).toString(16)).slice(-2) + ("00" + (~ ~(b * 255)).toString(16)).slice(-2);
    return (c);
}

//меняю маркер
function update() {
    let size = document.getElementById('size').value;
    let number = document.getElementById('number').value;
    let name = document.getElementById('name').value;
    let color = document.getElementById('color').value;

    let type = document.getElementsByName('type');
    for (var i = 0; i < type.length; i++) {
        if (type[i].checked) {
            marker_update(name, color, type[i].value, number - 1, size);
        }
    }
}

function filter_function(val1) {
    return function (fieldVal) {
        return val1 == fieldVal;
    };
}