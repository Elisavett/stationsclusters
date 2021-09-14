import {anychartInit, initializeYandexMap} from './map1.js';
let form = document.getElementById("form1");
form.addEventListener('submit', e => {
    e.preventDefault();
    if(submitForm(form)){
        anychartInit();
        initializeYandexMap();
    }
});

let element2 = document.getElementById("analyseData");
element2.addEventListener('mousedown', e => dragElement(e, element2, "left"));


let element3 = document.getElementById("amplPhaseCalc");
element3.addEventListener('mousedown', e => dragElement(e, element3, "left"));

let element4 = document.getElementById("clusters");
element4.addEventListener('mousedown', e => dragElement(e, element4, "left"));

let element5 = document.getElementById("classes");
element5.addEventListener('mousedown', e => dragElement(e, element5, "left"));

let element6 = document.getElementById("showOnMap");
element6.addEventListener('mousedown', e => dragElement(e, element6, "left"));

let element7 = document.getElementById("getData");
element7.addEventListener('mousedown', e => dragElement(e, element7, "right"));

let currentDroppable = null;
let diagramFlour = document.getElementById("myDiagramDiv");
let paletteFlour = document.getElementById("myPaletteDiv");
let isOnDroppable = false;

function falseFunc() {
    return false;
}
$(function () {
    // функция с параметрами idModal1 (1 модальное окно)
    var twoModal = function (idModal1) {
        $(idModal1).modal('hide');
    };
    twoModal('#modal-1');
});
function setMap(map){
    $(map).css('display', 'inline');
    let hiddenmap = map==='.yandex'?'.anychart':'.yandex';
    $(hiddenmap).css('display', 'none');
}


function dragElement(e, ball, float) {
    let shiftX = e.clientX - ball.getBoundingClientRect().left;
    let shiftY = e.clientY - ball.getBoundingClientRect().top;

    moveAt(e.pageX - shiftX, e.pageY - shiftY);
    // переносит мяч на координаты (pageX, pageY),
    // дополнительно учитывая изначальный сдвиг относительно указателя мыши
    function moveAt(pageX, pageY) {
        ball.style.left = pageX - 10 + 'px';
        ball.style.top = pageY + 'px';
    }

    function onMouseMove(event) {
        ball.style.position = 'absolute';
        ball.style.zIndex = 1000;
        document.body.append(ball);

        ball.hidden = true;
        let elemBelow = document.elementFromPoint(event.clientX, event.clientY);
        ball.hidden = false;

        //if (!elemBelow) return;

        let droppableBelow = elemBelow?.closest('.droppable');
        if (currentDroppable !== droppableBelow) {
            if (currentDroppable) { // null если мы были не над droppable до этого события
                // (например, над пустым пространством)
                isOnDroppable = false;
            }
            currentDroppable = droppableBelow;
            if (currentDroppable) { // null если мы не над droppable сейчас, во время этого события
                // (например, только что покинули droppable)
                isOnDroppable = true;
            }
        }
        if(isOnDroppable) {
            let strelka = ball.firstElementChild;
            strelka.setAttribute("hidden", "hidden");
        }
        moveAt(event.pageX - shiftX, event.pageY);

    }
    document.addEventListener('mousemove', onMouseMove);

    // отпустить мяч, удалить ненужные обработчики
    ball.onmouseup = function() {
        if(isOnDroppable) enterDroppable();
        else leaveDroppable();
        document.removeEventListener('mousemove', onMouseMove);
        ball.onmouseup = null;
        ball.addEventListener('click', e => {
            console.log(isOnDroppable);
        });
    };
    function enterDroppable() {
        ball.style.position = 'static';
        ball.style.float = 'top';
        diagramFlour.append(ball);
        ball.contextmenu = 'menu';
        let strelka = ball.firstElementChild;
        strelka.removeAttribute("hidden");
        resolve(ball);
    }
    function leaveDroppable() {
        ball.style.position = 'static';
        ball.style.float = float;
        paletteFlour.append(ball);
        ball.contextmenu = '';
    }
    function resolve(element){
        let val = ajaxResolve(element);
        if(val) {
            anychartInit();
            initializeYandexMap();
        }

    }
}