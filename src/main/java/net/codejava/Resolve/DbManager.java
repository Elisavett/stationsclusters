package net.codejava.Resolve;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Tuple;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DbManager {
    @Autowired
    private EntityManagerFactory em;

    public Map<Date, Double> map2002;
    public Map<Date, Double> map4402;

    public void countWeekTemperatures(int period) {
        map2002 = new TreeMap<>();
        map4402 = new TreeMap<>();
        //Время сейчас отнимает количество секунд в неделе
        long ut = (Instant.now().getEpochSecond() - 604800L);
        EntityManager entityManager = em.createEntityManager();
        List<Tuple> list = entityManager
                .createQuery(
                        "select " +
                                "   (t.time) as time, " +
                                "   t.temperature2002 as temperature2002, " +
                                "   t.temperature4402 as temperature4402 " +
                                "from " +
                                "   TemperatureFromBase as t " +
                                "where (t.time) >= :weekAgo and MOD(t.time, :period) = 0" +
                                "order by t.time desc", Tuple.class)
                .setParameter("weekAgo", ut)
                .setParameter("period", period)
                .getResultStream().collect(Collectors.toList());
        //Берем данные раз в час
        //list.removeIf(entry -> (long)entry.get(0)%3600!=0);
        for (Tuple item : list) {
           // if((long) item.get(0)%period==0) {
                map2002.put(new Date((long) item.get(0) * 1000), (Double) item.get(1));
                map4402.put(new Date((long) item.get(0) * 1000), (Double) item.get(2));
            //}
        }
    }
}
