/*
 * Copyright (c) 2025 MirraNET, Niklas Linz. All rights reserved.
 *
 * This file is part of the MirraNET project and is licensed under the
 * GNU Lesser General Public License v3.0 (LGPLv3).
 *
 * You may use, distribute and modify this code under the terms
 * of the LGPLv3 license. You should have received a copy of the
 * license along with this file. If not, see <https://www.gnu.org/licenses/lgpl-3.0.html>
 * or contact: niklas.linz@mirranet.de
 */


package de.linzn.systemChain;

import de.linzn.stem.api.scheduler.TaskScheduler;
import de.linzn.stem.modules.pluginModule.STEMPlugin;
import de.linzn.systemChain.callbacks.NetworkScheduler;
import de.linzn.systemChain.callbacks.SystemUpdateScheduler;
import de.linzn.systemChain.callbacks.TemperatureScheduler;

public class SystemChainPlugin extends STEMPlugin {

    public static SystemChainPlugin systemChainPlugin;

    @Override
    public void onEnable() {
        systemChainPlugin = this;
        TaskScheduler scheduler = this.getStemApi().scheduler();
        if (this.getDefaultConfig().getBoolean("module.temperature", true)) {
            scheduler.registerCallback(new TemperatureScheduler());
        }
        if (this.getDefaultConfig().getBoolean("module.network", true)) {
            scheduler.registerCallback(new NetworkScheduler());
        }
        if (this.getDefaultConfig().getBoolean("module.update", true)) {
            scheduler.registerCallback(new SystemUpdateScheduler());
        }
        this.getDefaultConfig().save();
    }

    @Override
    public void onDisable() {
        /* Callbacks registered through the STEM API are removed automatically */
    }
}
