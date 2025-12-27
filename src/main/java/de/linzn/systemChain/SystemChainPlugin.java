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



import de.linzn.stem.STEMApp;
import de.linzn.stem.modules.pluginModule.STEMPlugin;
import de.linzn.systemChain.callbacks.NetworkScheduler;
import de.linzn.systemChain.callbacks.SystemUpdateScheduler;
import de.linzn.systemChain.callbacks.TemperatureScheduler;


public class SystemChainPlugin extends STEMPlugin {

    public static SystemChainPlugin systemChainPlugin;

    private TemperatureScheduler temperatureScheduler;
    private NetworkScheduler networkScheduler;
    private SystemUpdateScheduler systemUpdateScheduler;

    @Override
    public void onEnable() {
        systemChainPlugin = this;
        if(this.getDefaultConfig().getBoolean("module.temperature", true)) {
            temperatureScheduler = new TemperatureScheduler();
            STEMApp.getInstance().getCallBackService().registerCallbackListener(temperatureScheduler, this);
        }
        if(this.getDefaultConfig().getBoolean("module.network", true)) {
            networkScheduler = new NetworkScheduler();
            STEMApp.getInstance().getCallBackService().registerCallbackListener(networkScheduler, this);
        }
        if(this.getDefaultConfig().getBoolean("module.update", true)) {
            systemUpdateScheduler = new SystemUpdateScheduler();
            STEMApp.getInstance().getCallBackService().registerCallbackListener(systemUpdateScheduler, this);
        }
        this.getDefaultConfig().save();
    }

    @Override
    public void onDisable() {
        STEMApp.getInstance().getCallBackService().unregisterCallbackListeners(this);
    }
}
