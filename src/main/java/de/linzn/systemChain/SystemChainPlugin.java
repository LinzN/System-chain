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
