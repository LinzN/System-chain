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


import de.azcore.azcoreRuntime.AppLogger;
import de.azcore.azcoreRuntime.taskManagment.AbstractCallback;
import de.azcore.azcoreRuntime.taskManagment.CallbackTime;
import de.azcore.azcoreRuntime.taskManagment.operations.OperationRegister;
import de.azcore.azcoreRuntime.taskManagment.operations.OperationSettings;
import de.azcore.azcoreRuntime.taskManagment.operations.TaskOperation;
import de.azcore.azcoreRuntime.utils.Color;
import de.linzn.simplyConfiguration.FileConfiguration;
import de.linzn.simplyConfiguration.provider.YamlConfiguration;
import de.linzn.systemChain.SystemChainPlugin;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NetworkScheduler extends AbstractCallback {

    private static float lastPing = -1;
    private FileConfiguration fileConfiguration;

    public NetworkScheduler() {
        fileConfiguration = YamlConfiguration.loadConfiguration(new File(SystemChainPlugin.systemChainPlugin.getDataFolder(), "networkScheduler.yml"));
        fileConfiguration.get("hostname", "test");
        fileConfiguration.get("username", "test");
        fileConfiguration.get("port", 22);
        fileConfiguration.get("command", "ssh test");
        fileConfiguration.save();
    }

    public static float getLastPing() {
        return lastPing;
    }

    @Override
    public void operation() {
        String command = fileConfiguration.getString("command");
        String username = fileConfiguration.getString("username");
        String hostname = fileConfiguration.getString("hostname");
        int port = fileConfiguration.getInt("port");

        TaskOperation taskOperation = OperationRegister.getOperation("run_linux_shell");
        OperationSettings operationSettings = new OperationSettings();

        operationSettings.addSetting("ssh.use", true);
        operationSettings.addSetting("ssh.user", username);
        operationSettings.addSetting("ssh.host", hostname);
        operationSettings.addSetting("ssh.port", port);

        operationSettings.addSetting("command.script", command);

        operationSettings.addSetting("output.use", true);

        addOperationData(taskOperation, operationSettings);
    }

    @Override
    public void callback(Object object) {
        OperationSettings operationSettings = (OperationSettings) object;

        List<String> list = (List<String>) operationSettings.getSetting("output");

        if (operationSettings.getIntSetting("exit") != 0) {
            lastPing = -1;
        } else {
            String line = list.get(0).substring(21).replace("ms", "");
            String[] pingArray = line.split("/");

            lastPing = getFloat(pingArray[1]);
            AppLogger.debug(Color.GREEN + "Network state " + lastPing + " ms");
        }

    }

    @Override
    public CallbackTime getTime() {
        return new CallbackTime(5, 10, TimeUnit.SECONDS);
    }

    private float getFloat(String line) {
        return Float.valueOf(line.replaceAll("[^\\d.]", ""));
    }
}
