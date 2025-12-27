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

package de.linzn.systemChain.callbacks;

import de.linzn.openJL.converter.TimeAdapter;
import de.linzn.simplyConfiguration.FileConfiguration;
import de.linzn.simplyConfiguration.provider.YamlConfiguration;
import de.linzn.stem.STEMApp;
import de.linzn.stem.modules.informationModule.InformationBlock;
import de.linzn.stem.modules.informationModule.InformationIntent;
import de.linzn.stem.taskManagment.AbstractCallback;
import de.linzn.stem.taskManagment.CallbackTime;
import de.linzn.stem.taskManagment.operations.OperationOutput;
import de.linzn.stem.taskManagment.operations.defaultOperations.ScriptOperation;
import de.linzn.systemChain.SystemChainPlugin;
import de.linzn.systemChain.events.SystemUpdateEvent;


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

            STEMApp.LOGGER.INFO("Update " + hostname + ":" + port);
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

        STEMApp.LOGGER.INFO("Finish update " + hostname + ":" + port + " with exit " + exitCode);

        SystemUpdateEvent systemUpdateEvent = new SystemUpdateEvent(exitCode, hostname, port);
        STEMApp.getInstance().getEventModule().getStemEventBus().fireEvent(systemUpdateEvent);

        if (exitCode != 0) {
            String error = "Server upgrade failed for server " + hostname + ":" + port + " with error code: " + exitCode;
            InformationBlock informationBlock = new InformationBlock("System-Upgrade", error, SystemChainPlugin.systemChainPlugin, error);
            informationBlock.setExpireTime(TimeAdapter.getTimeInstant().plus(12, ChronoUnit.HOURS));
            informationBlock.addIntent(InformationIntent.NOTIFY_USER);
            informationBlock.addIntent(InformationIntent.SHOW_DISPLAY);
            informationBlock.setIcon("SERVER");
            STEMApp.getInstance().getInformationModule().queueInformationBlock(informationBlock);
        }
    }

    @Override
    public CallbackTime getTime() {
        return new CallbackTime(0, 2, 30, true);
    }
}
