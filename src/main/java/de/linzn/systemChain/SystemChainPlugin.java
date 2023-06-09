/*
 * Copyright (C) 2020. Niklas Linz - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the LGPLv3 license, which unfortunately won't be
 * written for another century.
 *
 * You should have received a copy of the LGPLv3 license with
 * this file. If not, please write to: niklas.linz@enigmar.de
 *
 */

package de.linzn.systemChain;



import de.linzn.systemChain.callbacks.NetworkScheduler;
import de.linzn.systemChain.callbacks.SystemUpdateScheduler;
import de.linzn.systemChain.callbacks.TemperatureScheduler;
import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.pluginModule.STEMPlugin;

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
            STEMSystemApp.getInstance().getCallBackService().registerCallbackListener(temperatureScheduler, this);
        }
        if(this.getDefaultConfig().getBoolean("module.network", true)) {
            networkScheduler = new NetworkScheduler();
            STEMSystemApp.getInstance().getCallBackService().registerCallbackListener(networkScheduler, this);
        }
        if(this.getDefaultConfig().getBoolean("module.update", true)) {
            systemUpdateScheduler = new SystemUpdateScheduler();
            STEMSystemApp.getInstance().getCallBackService().registerCallbackListener(systemUpdateScheduler, this);
        }
        this.getDefaultConfig().save();
    }

    @Override
    public void onDisable() {
        STEMSystemApp.getInstance().getCallBackService().unregisterCallbackListeners(this);
    }
}
