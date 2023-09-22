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

package de.linzn.systemChain.callbacks;

import de.linzn.gptFramework.GPTFrameworkPlugin;
import de.linzn.openJL.converter.TimeAdapter;
import de.linzn.simplyConfiguration.FileConfiguration;
import de.linzn.simplyConfiguration.provider.YamlConfiguration;
import de.linzn.systemChain.SystemChainPlugin;
import de.linzn.systemChain.events.SystemUpdateEvent;
import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.informationModule.InformationBlock;
import de.stem.stemSystem.taskManagment.AbstractCallback;
import de.stem.stemSystem.taskManagment.CallbackTime;
import de.stem.stemSystem.taskManagment.operations.OperationOutput;
import de.stem.stemSystem.taskManagment.operations.defaultOperations.ShellOperation;
import org.json.JSONObject;

import java.io.File;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;


public class SystemUpdateScheduler extends AbstractCallback {
    private final FileConfiguration fileConfiguration;

    public SystemUpdateScheduler() {
        fileConfiguration = YamlConfiguration.loadConfiguration(new File(SystemChainPlugin.systemChainPlugin.getDataFolder(), "systemUpdateScheduler.yml"));
        fileConfiguration.get("command", "test123");
        fileConfiguration.save();
    }


    @Override
    public void operation() {
        HashMap<String, Object> systems = (HashMap<String, Object>) fileConfiguration.get("systems");

        String command = fileConfiguration.getString("command");

        for (String key : systems.keySet()) {

            String hostname = fileConfiguration.getString("systems." + key + ".hostname");
            int port = fileConfiguration.getInt("systems." + key + ".port");
            STEMSystemApp.LOGGER.INFO("Update " + hostname + ":" + port);
            ShellOperation shellOperation = new ShellOperation();

            shellOperation.setUseSSH(true);
            shellOperation.setSshUser("root");
            shellOperation.setSshHost(hostname);
            shellOperation.setSshPort(port);

            shellOperation.setScriptCommand(command);
            shellOperation.setUseOutput(false);

            addOperationData(shellOperation);
        }
    }

    @Override
    public void callback(OperationOutput operationOutput) {
        int exitCode = operationOutput.getExit();
        ShellOperation abstractOperation = (ShellOperation) operationOutput.getAbstractOperation();
        STEMSystemApp.LOGGER.INFO("Finish update " + abstractOperation.getSshHost() + ":" + abstractOperation.getSshPort() + " with exit " + exitCode);

        SystemUpdateEvent systemUpdateEvent = new SystemUpdateEvent(exitCode, abstractOperation.getSshHost(), abstractOperation.getSshPort());
        STEMSystemApp.getInstance().getEventModule().getStemEventBus().fireEvent(systemUpdateEvent);

        if (exitCode != 0) {
            String error = "Server upgrade failed for " + abstractOperation.getSshHost() + ":" + abstractOperation.getSshPort() + " with errorcode " + exitCode;
            JSONObject response = GPTFrameworkPlugin.gptFrameworkPlugin.getGptManager().createAIEventCompletion().requestEventResponse(error);
            InformationBlock informationBlock = new InformationBlock("System-Upgrade", response.getString("output"), SystemChainPlugin.systemChainPlugin);
            informationBlock.setExpireTime(TimeAdapter.getTimeInstant().plus(12, ChronoUnit.HOURS));
            informationBlock.setIcon("SERVER");
            STEMSystemApp.getInstance().getInformationModule().queueInformationBlock(informationBlock);
        }
    }

    @Override
    public CallbackTime getTime() {
        return new CallbackTime(0, 2, 30, true);
    }
}
