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


import de.azcore.azcoreRuntime.AZCoreRuntimeApp;
import de.azcore.azcoreRuntime.modules.pluginModule.AZPlugin;
import de.linzn.systemChain.runnables.TemperatureScheduler;

public class SystemChainPlugin extends AZPlugin {

    public static SystemChainPlugin runnablePlugin;


    public SystemChainPlugin() {
        runnablePlugin = this;
    }

    @Override
    public void onEnable() {

        AZCoreRuntimeApp.getInstance().getCallBackService().registerCallbackListener(new TemperatureScheduler(), this);
    }

    @Override
    public void onDisable() {
    }
}
