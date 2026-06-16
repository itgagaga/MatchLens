package com.zzx.matchlens.observer;

import com.zzx.matchlens.entity.Match;
import com.zzx.matchlens.entity.MatchEvent;

public interface MatchObserver {

    void onEventAdded(Match match, MatchEvent event);
}
