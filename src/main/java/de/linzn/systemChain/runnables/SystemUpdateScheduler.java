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


import de.linzn.evy.EvyApp;
import de.linzn.evy.api.OperationRegister;
import de.linzn.evy.internal.containers.*;
import de.linzn.evy.module.notification.NotificationContainer;
import de.linzn.evy.module.notification.NotificationPriority;
import de.linzn.evy.plugin.runnables.data.Runner;
import org.json.JSONArray;
import org.json.JSONObject;


public class SystemUpdateScheduler extends Runner {


    @Override
    public void schedule() {
        DataContainer dataContainer = EvyApp.getInstance().getDatabaseModule().getData("shell_command_upgrade_linux");
        JSONArray jsonArray = dataContainer.getJSON().getJSONArray("host_names");
        String command = "apt-get update && apt-get -y -o DPkg::options::=--force-confdef -o DPkg::options::=--force-confold dist-upgrade && apt-get -y autoremove";
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject object = jsonArray.getJSONObject(i);
            String hostname = object.getString("host_name");
            int port = object.getInt("port");
            EvyApp.logger("Update " + hostname + ":" + port, true, false);
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
            TaskContainer taskContainer = new TaskContainer(taskOperation, jsonObject);
            addOperation(taskContainer);
        }
    }

    @Override
    public void loopback(JSONObject jsonObject) {
        int exitCode = jsonObject.getInt("exitcode");
        JSONObject sshObject = jsonObject.getJSONObject("requestData").getJSONObject("ssh");
        EvyApp.logger("Finish update " + sshObject.getString("host") + ":" + sshObject.getInt("port") + " with exit " + exitCode, true, false);

        if (exitCode != 0) {
            String message = "Es ist ein Fehler (Code: " + exitCode + ") bei Upgrade von " + sshObject.getString("host") + ":" + sshObject.getInt("port") + " aufgetreten!";
            NotificationContainer notificationContainer = new NotificationContainer(message, NotificationPriority.HIGH);
            EvyApp.getInstance().getNotificationModule().pushNotification(notificationContainer);
        }
    }

    @Override
    public TimeData runnableTimer() {
        return new TimedTimeData(0, 2, 0);
    }

}
