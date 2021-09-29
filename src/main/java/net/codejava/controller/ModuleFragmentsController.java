package net.codejava.controller;

import net.codejava.Resolve.Model.ResolveForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/*
    Контроллер для получение фрагметов формы модульного рассчета
 */

@Controller
public class ModuleFragmentsController {
    //Получение формы прикрепления файлов для модкльного рассчета
    @GetMapping("/resolveModules")
    public String resolve(Model model) {
        //Установка исходных (стандартных) параметров рассчета
        ResolveForm.addAllToModel(model);
        //Установка параметров, которые должны быть вычисленны в проыессе рассчета, в null
        ResolveForm.countableCharacter = null;
        ResolveForm.arrayTypical = null;
        ResolveForm.arrayGroup = null;
        ResolveForm.isPhasesCounted = true;
        //Обозначаем, что рассчет является модульным
        model.addAttribute("isModules", "true");
        return "resolve/resolve";
    }
    //Получение фрагмета формы для указание параметров рассета частотного анализа
    @GetMapping("/getFrequencyFragment")
    public String getFrequencyFragment() {
        return "fragments/frequencyAnalysisFragment";
    }

    //Получение фрагмета формы для указание параметров рассета фазы / амплитуды
    @GetMapping("/getPhaseFragment")
    public String getPhaseFragment() {
        return "fragments/phaseFragment";
    }

    //Получение фрагмета формы для указание параметров рассета кластеризации
    @GetMapping("/getClusterFragment")
    public String getClusterFragment(Model model) {
        //Если фаза не рассчитана - на форме будет предупреждение
        if (ResolveForm.arrayPhase == null){
            model.addAttribute("phaseWarning", "Фазы/амплитуды не расчитаны. Данные из исходного файла будут использоваться, как данные фаз/амплитуд");
        }
        return "fragments/clusterFragment";
    }

    //Получение фрагмета формы для указание параметров рассета классификация
    @GetMapping("/getClassesFragment")
    public String getClassesFragment(Model model) {
        //Если типовые температуры не рассчитаны - на форме будет предупреждение
        if (ResolveForm.arrayTypical == null)
            model.addAttribute("warning", "Перед данным расчетом необходимо выполнить кластеризацию");
        return "fragments/classesFragment";
    }

    //Получение фрагмета формы для указание параметров рассета кластеризации
    @GetMapping("/getShowGrFragment")
    public String getShowGrFragment(Model model) {
        //Если группы не рассчитаны - на форме будет предупреждение
        if (ResolveForm.arrayGroup == null)
            model.addAttribute("warning", "Нет данных для отображения. Необходимо выполнить расчеты последовательно");
        return "fragments/showGrFragment";
    }
}
