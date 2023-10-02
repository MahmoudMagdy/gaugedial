package com.example.gaugedialinformationlib;

import com.example.gaugedialinformationlib.SpeedUpdate;

oneway interface IGaugeDialInformationListener {
    void onSpeedUpdated(in SpeedUpdate update);
}