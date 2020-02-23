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


import de.azcore.azcoreRuntime.taskManagment.AbstractCallback;
import de.azcore.azcoreRuntime.taskManagment.CallbackTime;
import de.azcore.azcoreRuntime.taskManagment.operations.OperationRegister;
import de.azcore.azcoreRuntime.taskManagment.operations.TaskOperation;
import de.linzn.simplyConfiguration.FileConfiguration;
import de.linzn.simplyConfiguration.provider.YamlConfiguration;
import de.linzn.systemChain.SystemChainPlugin;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class NetworkScheduler extends AbstractCallback {

    private FileConfiguration fileConfiguration;
    private static float lastPing = -1;

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
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("useOutput", true);
        JSONObject sshObject = new JSONObject();
        jsonObject.put("ssh", sshObject);
        sshObject.put("useSSH", true);
        sshObject.put("user", username);
        sshObject.put("host", hostname);
        sshObject.put("port", port);
        JSONObject commandObject = new JSONObject();
        jsonObject.put("command", commandObject);
        commandObject.put("isScript", false);
        commandObject.put("script", command);
        addOperationData(taskOperation, jsonObject);
    }

    @Override
    public void callback(Object object) {
        JSONObject jsonObject = (JSONObject) object;
        JSONArray jsonArray = jsonObject.getJSONArray("output");
        if (jsonObject.getInt("exitcode") != 0) {
            lastPing = -1;
        } else {
            String line = jsonArray.getString(0).substring(21).replace("ms", "");
            String[] pingArray = line.split("/");

            lastPing = getFloat(pingArray[1]);
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
