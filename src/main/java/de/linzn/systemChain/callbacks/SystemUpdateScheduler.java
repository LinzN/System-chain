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


import de.azcore.azcoreRuntime.AZCoreRuntimeApp;
import de.azcore.azcoreRuntime.AppLogger;
import de.azcore.azcoreRuntime.modules.notificationModule.NotificationContainer;
import de.azcore.azcoreRuntime.modules.notificationModule.NotificationPriority;
import de.azcore.azcoreRuntime.taskManagment.AbstractCallback;
import de.azcore.azcoreRuntime.taskManagment.CallbackTime;
import de.azcore.azcoreRuntime.taskManagment.operations.OperationRegister;
import de.azcore.azcoreRuntime.taskManagment.operations.TaskOperation;
import de.linzn.simplyConfiguration.FileConfiguration;
import de.linzn.simplyConfiguration.provider.YamlConfiguration;
import de.linzn.systemChain.SystemChainPlugin;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;


public class SystemUpdateScheduler extends AbstractCallback {
    private FileConfiguration fileConfiguration;

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
            AppLogger.logger("Update " + hostname + ":" + port, true);
            TaskOperation taskOperation = OperationRegister.getOperation("run_linux_shell");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("useOutput", false);
            JSONObject sshObject = new JSONObject();
            jsonObject.put("ssh", sshObject);
            sshObject.put("useSSH", true);
            sshObject.put("user", "root");
            sshObject.put("host", hostname);
            sshObject.put("port", port);
            JSONObject commandObject = new JSONObject();
            jsonObject.put("command", commandObject);
            commandObject.put("isScript", false);
            commandObject.put("script", command);
            addOperationData(taskOperation, jsonObject);
        }
    }

    @Override
    public void callback(Object object) {
        JSONObject jsonObject = (JSONObject) object;
        int exitCode = jsonObject.getInt("exitcode");
        JSONObject sshObject = jsonObject.getJSONObject("requestData").getJSONObject("ssh");
        AppLogger.logger("Finish update " + sshObject.getString("host") + ":" + sshObject.getInt("port") + " with exit " + exitCode, true);

        if (exitCode != 0) {
            String message = "Es ist ein Fehler (Code: " + exitCode + ") bei Upgrade von " + sshObject.getString("host") + ":" + sshObject.getInt("port") + " aufgetreten!";
            NotificationContainer notificationContainer = new NotificationContainer(message, NotificationPriority.HIGH);
            AZCoreRuntimeApp.getInstance().getNotificationModule().pushNotification(notificationContainer);
        }
    }

    @Override
    public CallbackTime getTime() {
        return new CallbackTime(0, 3, 30, true);
    }
}
