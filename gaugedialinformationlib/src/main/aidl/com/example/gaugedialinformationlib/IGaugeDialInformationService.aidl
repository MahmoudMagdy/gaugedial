package com.example.gaugedialinformationlib;

import com.example.gaugedialinformationlib.IGaugeDialInformationListener;

interface IGaugeDialInformationService {
    boolean registerListener(IGaugeDialInformationListener listener);
    boolean unregisterListener(IGaugeDialInformationListener listener);
    boolean resume();
    boolean pause();
}