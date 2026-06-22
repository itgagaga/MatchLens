package com.zzx.matchlens.observer;

import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.entity.MatchEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MatchEventPublisher {

    private final List<MatchObserver> observers = new ArrayList<>();

    public MatchEventPublisher(List<MatchObserver> observers) {
        this.observers.addAll(observers);
    }

    public void subscribe(MatchObserver observer) {
        observers.add(observer);
    }

    public void unsubscribe(MatchObserver observer) {
        observers.remove(observer);
    }

    public void publish(Match match, MatchEvent event) {
        for (MatchObserver observer : observers) {
            observer.onEventAdded(match, event);
        }
    }
}
