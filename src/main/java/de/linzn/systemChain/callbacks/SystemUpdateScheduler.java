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

import de.linzn.openJL.converter.TimeAdapter;
import de.linzn.simplyConfiguration.FileConfiguration;
import de.linzn.simplyConfiguration.provider.YamlConfiguration;
import de.linzn.systemChain.SystemChainPlugin;
import de.linzn.systemChain.events.SystemUpdateEvent;
import de.stem.stemSystem.STEMSystemApp;
import de.stem.stemSystem.modules.informationModule.InformationBlock;
import de.stem.stemSystem.modules.informationModule.InformationIntent;
import de.stem.stemSystem.taskManagment.AbstractCallback;
import de.stem.stemSystem.taskManagment.CallbackTime;
import de.stem.stemSystem.taskManagment.operations.OperationOutput;
import de.stem.stemSystem.taskManagment.operations.defaultOperations.ScriptOperation;

import java.io.File;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;


public class SystemUpdateScheduler extends AbstractCallback {
    private final FileConfiguration fileConfiguration;

    public SystemUpdateScheduler() {
        fileConfiguration = YamlConfiguration.loadConfiguration(new File(SystemChainPlugin.systemChainPlugin.getDataFolder(), "systemUpdateScheduler.yml"));
        fileConfiguration.save();
    }


    @Override
    public void operation() {
        HashMap<String, Object> systems = (HashMap<String, Object>) fileConfiguration.get("systems");

        for (String key : systems.keySet()) {

            String hostname = fileConfiguration.getString("systems." + key + ".hostname");
            int port = fileConfiguration.getInt("systems." + key + ".port");

            STEMSystemApp.LOGGER.INFO("Update " + hostname + ":" + port);
            ScriptOperation scriptOperation = new ScriptOperation("update_linux-system");

            scriptOperation.addParameter("remotehost", hostname);
            scriptOperation.addParameter("remoteport", String.valueOf(port));
            addOperationData(scriptOperation);
        }
    }

    @Override
    public void callback(OperationOutput operationOutput) {
        int exitCode = operationOutput.getExit();
        ScriptOperation abstractOperation = (ScriptOperation) operationOutput.getAbstractOperation();
        String hostname = abstractOperation.getParameterValue("remotehost");
        int port = Integer.parseInt(abstractOperation.getParameterValue("remoteport"));

        STEMSystemApp.LOGGER.INFO("Finish update " + hostname + ":" + port + " with exit " + exitCode);

        SystemUpdateEvent systemUpdateEvent = new SystemUpdateEvent(exitCode, hostname, port);
        STEMSystemApp.getInstance().getEventModule().getStemEventBus().fireEvent(systemUpdateEvent);

        if (exitCode != 0) {
            String error = "Server upgrade failed for server " + hostname + ":" + port + " with error code: " + exitCode;
            InformationBlock informationBlock = new InformationBlock("System-Upgrade", error, SystemChainPlugin.systemChainPlugin, error);
            informationBlock.setExpireTime(TimeAdapter.getTimeInstant().plus(12, ChronoUnit.HOURS));
            informationBlock.addIntent(InformationIntent.NOTIFY_USER);
            informationBlock.addIntent(InformationIntent.SHOW_DISPLAY);
            informationBlock.setIcon("SERVER");
            STEMSystemApp.getInstance().getInformationModule().queueInformationBlock(informationBlock);
        }
    }

    @Override
    public CallbackTime getTime() {
        return new CallbackTime(0, 2, 30, true);
    }
}
