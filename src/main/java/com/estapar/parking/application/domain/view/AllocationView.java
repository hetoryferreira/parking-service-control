package com.estapar.parking.application.domain.view;

import com.estapar.parking.application.domain.model.GarageSector;

public record AllocationView(
        GarageSector sector
) {}