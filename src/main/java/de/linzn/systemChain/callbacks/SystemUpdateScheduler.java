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

import de.linzn.simplyConfiguration.FileConfiguration;
import de.linzn.simplyConfiguration.provider.YamlConfiguration;
import de.linzn.stem.modules.informationModule.InformationBlock;
import de.linzn.stem.modules.informationModule.InformationIntent;
import de.linzn.stem.taskManagment.AbstractCallback;
import de.linzn.stem.taskManagment.CallbackTime;
import de.linzn.stem.taskManagment.operations.OperationOutput;
import de.linzn.stem.taskManagment.operations.defaultOperations.ScriptOperation;
import de.linzn.systemChain.SystemChainPlugin;
import de.linzn.systemChain.events.SystemUpdateEvent;

import java.io.File;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

public class SystemUpdateScheduler extends AbstractCallback {

    private final FileConfiguration fileConfiguration;

    public SystemUpdateScheduler() {
        fileConfiguration = YamlConfiguration.loadConfiguration(new File(SystemChainPlugin.systemChainPlugin.getDataFolder(), "systemUpdateScheduler.yml"));
        fileConfiguration.save();
    }

    @Override
    public void operation() {
        if (!(fileConfiguration.get("systems") instanceof Map<?, ?> systems)) {
            SystemChainPlugin.systemChainPlugin.getPluginLogger().warning("No systems configured in systemUpdateScheduler.yml");
            return;
        }
        for (Object keyObject : systems.keySet()) {
            String key = String.valueOf(keyObject);
            String hostname = fileConfiguration.getString("systems." + key + ".hostname");
            int port = fileConfiguration.getInt("systems." + key + ".port");
            if (hostname == null || hostname.isBlank()) {
                SystemChainPlugin.systemChainPlugin.getPluginLogger().warning("System " + key + " has no hostname");
                continue;
            }
            SystemChainPlugin.systemChainPlugin.getPluginLogger().info("Update " + hostname + ":" + port);
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

        SystemChainPlugin plugin = SystemChainPlugin.systemChainPlugin;
        plugin.getPluginLogger().info("Finish update " + hostname + ":" + port + " with exit " + exitCode);
        plugin.getStemApi().events().fire(new SystemUpdateEvent(exitCode, hostname, port));

        if (exitCode != 0) {
            String error = "Server upgrade failed for server " + hostname + ":" + port + " with error code: " + exitCode;
            InformationBlock informationBlock = new InformationBlock("System-Upgrade", error, plugin, error);
            informationBlock.setExpireTime(Instant.now().plus(12, ChronoUnit.HOURS));
            informationBlock.addIntent(InformationIntent.NOTIFY_USER);
            informationBlock.addIntent(InformationIntent.SHOW_DISPLAY);
            informationBlock.setIcon("SERVER");
            plugin.getStemApi().information().queue(informationBlock);
        }
    }

    @Override
    public CallbackTime getTime() {
        return new CallbackTime(0, 2, 30, true);
    }
}
