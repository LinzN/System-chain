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

package de.linzn.systemChain.runnables;


import de.azcore.azcoreRuntime.AZCoreRuntimeApp;
import de.azcore.azcoreRuntime.AppLogger;
import de.azcore.azcoreRuntime.modules.databaseModule.DataContainer;
import de.azcore.azcoreRuntime.modules.notificationModule.NotificationContainer;
import de.azcore.azcoreRuntime.modules.notificationModule.NotificationPriority;
import de.azcore.azcoreRuntime.taskManagment.AbstractCallback;
import de.azcore.azcoreRuntime.taskManagment.CallbackTime;
import de.azcore.azcoreRuntime.taskManagment.operations.OperationRegister;
import de.azcore.azcoreRuntime.taskManagment.operations.TaskOperation;
import org.json.JSONArray;
import org.json.JSONObject;


public class SystemUpdateScheduler extends AbstractCallback {


    @Override
    public void operation() {
        DataContainer dataContainer = AZCoreRuntimeApp.getInstance().getDatabaseModule().getData("shell_command_upgrade_linux");
        JSONArray jsonArray = dataContainer.getJSON().getJSONArray("host_names");
        String command = "apt-get update && apt-get -y -o DPkg::options::=--force-confdef -o DPkg::options::=--force-confold dist-upgrade && apt-get -y autoremove";
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject object = jsonArray.getJSONObject(i);
            String hostname = object.getString("host_name");
            int port = object.getInt("port");
            AppLogger.logger("Update " + hostname + ":" + port, true, false);
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
        AppLogger.logger("Finish update " + sshObject.getString("host") + ":" + sshObject.getInt("port") + " with exit " + exitCode, true, false);

        if (exitCode != 0) {
            String message = "Es ist ein Fehler (Code: " + exitCode + ") bei Upgrade von " + sshObject.getString("host") + ":" + sshObject.getInt("port") + " aufgetreten!";
            NotificationContainer notificationContainer = new NotificationContainer(message, NotificationPriority.HIGH);
            AZCoreRuntimeApp.getInstance().getNotificationModule().pushNotification(notificationContainer);
        }
    }

    @Override
    public CallbackTime getTime() {
        return new CallbackTime(0, 2, 0, true);
    }
}
