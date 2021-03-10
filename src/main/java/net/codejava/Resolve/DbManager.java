package net.codejava.Resolve;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DbManager {
    @Autowired
    private EntityManagerFactory em;

    public Map<Date, Double> getWeekTemperatures(int period) {
        //Время сейчас отнимает количество секунд в неделе
        long ut = (Instant.now().getEpochSecond() - 604800L);
        EntityManager entityManager = em.createEntityManager();
        List<Tuple> list = entityManager
                .createQuery(
                        "select " +
                                "   (t.time) as time, " +
                                "   t.temperature as temperature " +
                                "from " +
                                "   TemperatureFromBase as t " +
                                "where (t.time) >= :weekAgo and MOD(t.time, :period) = 0" +
                                "order by t.time desc", Tuple.class)
                .setParameter("weekAgo", ut)
                .setParameter("period", period)
                .getResultStream().collect(Collectors.toList());
        //Берем данные раз в час
        //list.removeIf(entry -> (long)entry.get(0)%3600!=0);
        Map<Date, Double> map = new TreeMap<>();
        for (Tuple item : list) {
           // if((long) item.get(0)%period==0) {
                map.put(new Date((long) item.get(0) * 1000), (Double) item.get(1));
            //}
        }
        return map;
    }
}
