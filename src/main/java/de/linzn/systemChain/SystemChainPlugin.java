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
import de.linzn.systemChain.callbacks.GitPushHTW;
import de.linzn.systemChain.callbacks.NetworkScheduler;
import de.linzn.systemChain.callbacks.SystemUpdateScheduler;
import de.linzn.systemChain.callbacks.TemperatureScheduler;

public class SystemChainPlugin extends AZPlugin {

    public static SystemChainPlugin systemChainPlugin;

    private TemperatureScheduler temperatureScheduler;
    private NetworkScheduler networkScheduler;
    private SystemUpdateScheduler systemUpdateScheduler;
    private GitPushHTW gitPushHTW;

    @Override
    public void onEnable() {
        systemChainPlugin = this;
        temperatureScheduler = new TemperatureScheduler();
        AZCoreRuntimeApp.getInstance().getCallBackService().registerCallbackListener(temperatureScheduler, this);
        networkScheduler = new NetworkScheduler();
        AZCoreRuntimeApp.getInstance().getCallBackService().registerCallbackListener(networkScheduler, this);
        systemUpdateScheduler = new SystemUpdateScheduler();
        AZCoreRuntimeApp.getInstance().getCallBackService().registerCallbackListener(systemUpdateScheduler, this);
        gitPushHTW = new GitPushHTW();
        AZCoreRuntimeApp.getInstance().getCallBackService().registerCallbackListener(gitPushHTW, this);
    }

    @Override
    public void onDisable() {
        AZCoreRuntimeApp.getInstance().getCallBackService().unregisterCallbackListeners(this);
    }
}
