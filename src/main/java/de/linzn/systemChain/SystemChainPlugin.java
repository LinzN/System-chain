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
import de.linzn.systemChain.runnables.GitPushHTW;
import de.linzn.systemChain.runnables.NetworkScheduler;
import de.linzn.systemChain.runnables.SystemUpdateScheduler;
import de.linzn.systemChain.runnables.TemperatureScheduler;

public class SystemChainPlugin extends AZPlugin {

    public static SystemChainPlugin systemChainPlugin;


    public SystemChainPlugin() {
        systemChainPlugin = this;
    }

    @Override
    public void onEnable() {
        AZCoreRuntimeApp.getInstance().getCallBackService().registerCallbackListener(new TemperatureScheduler(), this);
        AZCoreRuntimeApp.getInstance().getCallBackService().registerCallbackListener(new NetworkScheduler(), this);
        AZCoreRuntimeApp.getInstance().getCallBackService().registerCallbackListener(new SystemUpdateScheduler(), this);
        AZCoreRuntimeApp.getInstance().getCallBackService().registerCallbackListener(new GitPushHTW(), this);
    }

    @Override
    public void onDisable() {
    }
}
